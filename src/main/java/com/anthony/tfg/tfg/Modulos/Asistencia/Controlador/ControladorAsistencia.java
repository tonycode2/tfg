package com.anthony.tfg.tfg.Modulos.Asistencia.Controlador;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.anthony.tfg.tfg.DTOs.Respuesta.EstadoAsistenciaDTO;
import com.anthony.tfg.tfg.DTOs.Respuesta.ResumenDepartamentoDTO;
import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaAsistenciaDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudAsistenciaDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudRegistroAsistenciaDTO;
import com.anthony.tfg.tfg.Modulos.Asistencia.Servicio.ServicioAsistencia;
import com.anthony.tfg.tfg.Modulos.Asistencia.Servicio.ServicioRegistroAsistencia;

import jakarta.validation.Valid;

/**
 * Controller for attendance management
 * Provides endpoints for clock-in/clock-out, status checking, and department summaries
 */
@RestController
@RequestMapping("/api/asistencias")
@Validated
public class ControladorAsistencia {

    private final ServicioAsistencia servicio;
    private final ServicioRegistroAsistencia servicioRegistro;

    public ControladorAsistencia(ServicioAsistencia servicio, ServicioRegistroAsistencia servicioRegistro) {
        this.servicio = servicio;
        this.servicioRegistro = servicioRegistro;
    }

    // ==================== CLOCK-IN/CLOCK-OUT ENDPOINTS ====================

    /**
     * Register clock-in (ENTRADA) for the authenticated user
     * POST /api/asistencias/entrada
     * 
     * @param solicitud Optional body with custom fechaHora (for testing)
     * @return The created attendance record
     */
    @PostMapping("/entrada")
    public ResponseEntity<RespuestaAsistenciaDTO> registrarEntrada(
            @RequestBody(required = false) SolicitudRegistroAsistenciaDTO solicitud) {
        LocalDateTime fechaHora = solicitud != null ? solicitud.fechaHora : null;
        RespuestaAsistenciaDTO respuesta = servicioRegistro.registrarEntrada(fechaHora);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /**
     * Register clock-out (SALIDA) for the authenticated user
     * POST /api/asistencias/salida
     * 
     * @param solicitud Optional body with custom fechaHora (for testing)
     * @return The created attendance record
     */
    @PostMapping("/salida")
    public ResponseEntity<RespuestaAsistenciaDTO> registrarSalida(
            @RequestBody(required = false) SolicitudRegistroAsistenciaDTO solicitud) {
        LocalDateTime fechaHora = solicitud != null ? solicitud.fechaHora : null;
        RespuestaAsistenciaDTO respuesta = servicioRegistro.registrarSalida(fechaHora);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    // ==================== STATUS ENDPOINTS ====================

    /**
     * Get current attendance status for the authenticated user
     * GET /api/asistencias/mi-estado
     * 
     * @return Current status including last event, timestamps, and observations
     */
    @GetMapping("/mi-estado")
    public ResponseEntity<EstadoAsistenciaDTO> obtenerMiEstado() {
        EstadoAsistenciaDTO estado = servicioRegistro.obtenerMiEstado();
        return ResponseEntity.ok(estado);
    }

    /**
     * Get list of department IDs that the current user can access
     * GET /api/asistencias/departamentos-accesibles
     * 
     * @return List of department IDs
     */
    @GetMapping("/departamentos-accesibles")
    public ResponseEntity<List<Long>> obtenerDepartamentosAccesibles() {
        List<Long> departamentos = servicioRegistro.obtenerDepartamentosAccesibles();
        return ResponseEntity.ok(departamentos);
    }

    /**
     * Get attendance summary for a department
     * GET /api/asistencias/departamento/{idDepartamento}
     * 
     * Access: HR, ADMIN (all departments), JEFE (only managed departments)
     * 
     * @param idDepartamento Department ID
     * @return Department summary with all employees' status
     */
    @GetMapping("/departamento/{idDepartamento}")
    public ResponseEntity<ResumenDepartamentoDTO> obtenerResumenDepartamento(
            @PathVariable Long idDepartamento) {
        ResumenDepartamentoDTO resumen = servicioRegistro.obtenerResumenDepartamento(idDepartamento);
        return ResponseEntity.ok(resumen);
    }

    // ==================== HISTORY/QUERY ENDPOINTS ====================

    /**
     * Get attendance history with optional filters
     * GET /api/asistencias/historial
     * 
     * @param idEmpleado Optional employee ID (null for own records)
     * @param fechaInicio Optional start date
     * @param fechaFin Optional end date
     * @return List of attendance records
     */
    @GetMapping("/historial")
    public ResponseEntity<List<RespuestaAsistenciaDTO>> obtenerHistorial(
            @RequestParam(required = false) Long idEmpleado,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime fechaFin) {
        List<RespuestaAsistenciaDTO> historial = servicioRegistro.obtenerHistorial(idEmpleado, fechaInicio, fechaFin);
        return ResponseEntity.ok(historial);
    }

    // ==================== LEGACY CRUD ENDPOINTS ====================
    // These are kept for backwards compatibility with MantenimientosView

    /**
     * Get attendance record by ID
     * GET /api/asistencias/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<RespuestaAsistenciaDTO> obtenerPorId(@PathVariable Long id) {
        RespuestaAsistenciaDTO respuesta = servicio.obtenerPorId(id);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Get all attendance records
     * GET /api/asistencias
     */
    @GetMapping
    public ResponseEntity<List<RespuestaAsistenciaDTO>> obtenerTodos() {
        List<RespuestaAsistenciaDTO> lista = servicio.obtenerTodos();
        return ResponseEntity.ok(lista);
    }

    /**
     * Create attendance record (legacy - for admin use)
     * POST /api/asistencias
     */
    @PostMapping
    public ResponseEntity<RespuestaAsistenciaDTO> crear(@Valid @RequestBody SolicitudAsistenciaDTO solicitud) {
        RespuestaAsistenciaDTO respuesta = servicio.guardar(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /**
     * Update attendance record (legacy - for admin use)
     * PUT /api/asistencias/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<RespuestaAsistenciaDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudAsistenciaDTO solicitud) {
        RespuestaAsistenciaDTO respuesta = servicio.actualizar(id, solicitud);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Delete attendance record (legacy - for admin use)
     * DELETE /api/asistencias/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
