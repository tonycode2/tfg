package com.anthony.tfg.tfg.Modulos.Incapacidad.Controlador;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaIncapacidadesDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.AccionIncapacidadDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudExtensionIncapacidadDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudIncapacidadesDTO;
import com.anthony.tfg.tfg.Modulos.Incapacidad.Servicio.ServicioIncapacidad;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/incapacidades")
@Validated
public class ControladorIncapacidad {

    private final ServicioIncapacidad servicio;

    public ControladorIncapacidad(ServicioIncapacidad servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/{id}")
    public ResponseEntity<RespuestaIncapacidadesDTO> obtenerPorId(@PathVariable Long id) {
        RespuestaIncapacidadesDTO respuesta = servicio.obtenerPorId(id);
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<List<RespuestaIncapacidadesDTO>> obtenerTodos() {
        List<RespuestaIncapacidadesDTO> lista = servicio.obtenerTodos();
        return ResponseEntity.ok(lista);
    }

    @PostMapping
    public ResponseEntity<RespuestaIncapacidadesDTO> crear(
            @Valid @RequestBody SolicitudIncapacidadesDTO solicitud,
            Authentication authentication) {
        RespuestaIncapacidadesDTO respuesta = servicio.guardar(solicitud, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RespuestaIncapacidadesDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudIncapacidadesDTO solicitud) {
        RespuestaIncapacidadesDTO respuesta = servicio.actualizar(id, solicitud);
        return ResponseEntity.ok(respuesta);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            Authentication authentication) {
        servicio.eliminar(id, authentication);
        return ResponseEntity.noContent().build();
    }

    // ==================== ENDPOINTS PARA EMPLEADOS ====================

    /**
     * Obtiene las solicitudes de incapacidad del empleado autenticado
     */
    @GetMapping("/mis-solicitudes")
    public ResponseEntity<List<RespuestaIncapacidadesDTO>> obtenerMisSolicitudes(Authentication authentication) {
        List<RespuestaIncapacidadesDTO> solicitudes = servicio.obtenerMisSolicitudes(authentication);
        return ResponseEntity.ok(solicitudes);
    }

    // ==================== ENDPOINTS PARA JEFES ====================

    /**
     * Obtiene las solicitudes de incapacidad pendientes del departamento del jefe autenticado
     */
    @GetMapping("/pendientes-departamento")
    @PreAuthorize("hasAnyRole('JEFE', 'HR', 'ADMIN')")
    public ResponseEntity<List<RespuestaIncapacidadesDTO>> obtenerSolicitudesPendientesDepartamento(
            Authentication authentication) {
        List<RespuestaIncapacidadesDTO> solicitudes = servicio.obtenerSolicitudesPendientesDepartamento(authentication);
        return ResponseEntity.ok(solicitudes);
    }

    /**
     * Obtiene los empleados actualmente incapacitados del departamento del jefe autenticado
     */
    @GetMapping("/empleados-incapacitados-departamento")
    @PreAuthorize("hasAnyRole('JEFE', 'HR', 'ADMIN')")
    public ResponseEntity<List<RespuestaIncapacidadesDTO>> obtenerEmpleadosIncapacitadosDepartamento(
            Authentication authentication) {
        List<RespuestaIncapacidadesDTO> incapacitados = servicio.obtenerEmpleadosIncapacitadosDepartamento(authentication);
        return ResponseEntity.ok(incapacitados);
    }

    /**
     * Solicita una extensión de incapacidad
     */
    @PostMapping("/{id}/solicitar-extension")
    @PreAuthorize("hasAnyRole('JEFE', 'HR', 'ADMIN')")
    public ResponseEntity<RespuestaIncapacidadesDTO> solicitarExtension(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudExtensionIncapacidadDTO solicitudExtension,
            Authentication authentication) {
        RespuestaIncapacidadesDTO respuesta = servicio.solicitarExtension(id, solicitudExtension, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /**
     * Aprueba una solicitud de incapacidad como jefe
     */
    @PostMapping("/{id}/aprobar-jefe")
    @PreAuthorize("hasAnyRole('JEFE', 'HR', 'ADMIN')")
    public ResponseEntity<RespuestaIncapacidadesDTO> aprobarPorJefe(
            @PathVariable Long id,
            @Valid @RequestBody AccionIncapacidadDTO accion,
            Authentication authentication) {
        RespuestaIncapacidadesDTO respuesta = servicio.aprobarPorJefe(id, accion.comentarios, authentication);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Rechaza una solicitud de incapacidad como jefe
     */
    @PostMapping("/{id}/rechazar-jefe")
    @PreAuthorize("hasAnyRole('JEFE', 'HR', 'ADMIN')")
    public ResponseEntity<RespuestaIncapacidadesDTO> rechazarPorJefe(
            @PathVariable Long id,
            @Valid @RequestBody AccionIncapacidadDTO accion,
            Authentication authentication) {
        RespuestaIncapacidadesDTO respuesta = servicio.rechazarPorJefe(id, accion.comentarios, authentication);
        return ResponseEntity.ok(respuesta);
    }

    // ==================== ENDPOINTS PARA RH ====================

    /**
     * Obtiene las solicitudes de incapacidad que necesitan aprobación de RH
     */
    @GetMapping("/pendientes-rh")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<List<RespuestaIncapacidadesDTO>> obtenerSolicitudesParaRH() {
        List<RespuestaIncapacidadesDTO> solicitudes = servicio.obtenerSolicitudesParaRH();
        return ResponseEntity.ok(solicitudes);
    }

    /**
     * Obtiene todas las solicitudes de incapacidad (para auditoría)
     */
    @GetMapping("/todas")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<List<RespuestaIncapacidadesDTO>> obtenerTodasLasSolicitudes() {
        List<RespuestaIncapacidadesDTO> solicitudes = servicio.obtenerTodasLasSolicitudes();
        return ResponseEntity.ok(solicitudes);
    }

    /**
     * Obtiene las incapacidades activas (aprobadas y en curso)
     */
    @GetMapping("/activas")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<List<RespuestaIncapacidadesDTO>> obtenerIncapacidadesActivas() {
        List<RespuestaIncapacidadesDTO> solicitudes = servicio.obtenerIncapacidadesActivas();
        return ResponseEntity.ok(solicitudes);
    }

    /**
     * Aprueba una solicitud de incapacidad como RH (aprobación final)
     */
    @PostMapping("/{id}/aprobar-rh")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<RespuestaIncapacidadesDTO> aprobarPorRH(
            @PathVariable Long id,
            @Valid @RequestBody AccionIncapacidadDTO accion,
            Authentication authentication) {
        RespuestaIncapacidadesDTO respuesta = servicio.aprobarPorRH(id, accion.comentarios, authentication);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Rechaza una solicitud de incapacidad como RH
     */
    @PostMapping("/{id}/rechazar-rh")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<RespuestaIncapacidadesDTO> rechazarPorRH(
            @PathVariable Long id,
            @Valid @RequestBody AccionIncapacidadDTO accion,
            Authentication authentication) {
        RespuestaIncapacidadesDTO respuesta = servicio.rechazarPorRH(id, accion.comentarios, authentication);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Cancela una solicitud de incapacidad aprobada (solo RH puede cancelar)
     */
    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<RespuestaIncapacidadesDTO> cancelarSolicitud(
            @PathVariable Long id,
            Authentication authentication) {
        RespuestaIncapacidadesDTO respuesta = servicio.cancelarSolicitud(id, authentication);
        return ResponseEntity.ok(respuesta);
    }
}
