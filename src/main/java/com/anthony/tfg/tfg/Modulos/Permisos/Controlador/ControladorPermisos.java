package com.anthony.tfg.tfg.Modulos.Permisos.Controlador;

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

import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaPermisosDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.AccionPermisoDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudPermisosDTO;
import com.anthony.tfg.tfg.Modulos.Permisos.Servicio.ServicioPermisos;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/permisos")
@Validated
public class ControladorPermisos {

    private final ServicioPermisos servicio;

    public ControladorPermisos(ServicioPermisos servicio) {
        this.servicio = servicio;
    }

    /** 
     * @param id
     * @return ResponseEntity<RespuestaPermisosDTO>
     */
    @GetMapping("/{id}")
    public ResponseEntity<RespuestaPermisosDTO> obtenerPorId(@PathVariable Long id) {
        RespuestaPermisosDTO respuesta = servicio.obtenerPorId(id);
        return ResponseEntity.ok(respuesta);
    }

    /** 
     * @return ResponseEntity<List<RespuestaPermisosDTO>>
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<List<RespuestaPermisosDTO>> obtenerTodos() {
        List<RespuestaPermisosDTO> lista = servicio.obtenerTodos();
        return ResponseEntity.ok(lista);
    }

    /** 
     * @param authentication
     * @return ResponseEntity<RespuestaPermisosDTO>
     */
    @PostMapping
    public ResponseEntity<RespuestaPermisosDTO> crear(
            @Valid @RequestBody SolicitudPermisosDTO solicitud,
            Authentication authentication) {
        RespuestaPermisosDTO respuesta = servicio.guardar(solicitud, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /** 
     * @param actualizar(
     * @return ResponseEntity<RespuestaPermisosDTO>
     */
    @PutMapping("/{id}")
    public ResponseEntity<RespuestaPermisosDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudPermisosDTO solicitud) {
        RespuestaPermisosDTO respuesta = servicio.actualizar(id, solicitud);
        return ResponseEntity.ok(respuesta);
    }

    /** 
     * @param authentication
     * @return ResponseEntity<Void>
     */
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
     * Obtiene las solicitudes del empleado autenticado
     */
    @GetMapping("/mis-solicitudes")
    public ResponseEntity<List<RespuestaPermisosDTO>> obtenerMisSolicitudes(Authentication authentication) {
        List<RespuestaPermisosDTO> solicitudes = servicio.obtenerMisSolicitudes(authentication);
        return ResponseEntity.ok(solicitudes);
    }

    // ==================== ENDPOINTS PARA JEFES ====================

    /**
     * Obtiene las solicitudes pendientes del departamento del jefe autenticado
     */
    @GetMapping("/pendientes-departamento")
    @PreAuthorize("hasAnyRole('JEFE', 'HR', 'ADMIN')")
    public ResponseEntity<List<RespuestaPermisosDTO>> obtenerSolicitudesPendientesDepartamento(
            Authentication authentication) {
        List<RespuestaPermisosDTO> solicitudes = servicio.obtenerSolicitudesPendientesDepartamento(authentication);
        return ResponseEntity.ok(solicitudes);
    }

    /**
     * Aprueba una solicitud como jefe
     */
    @PostMapping("/{id}/aprobar-jefe")
    @PreAuthorize("hasAnyRole('JEFE', 'HR', 'ADMIN')")
    public ResponseEntity<RespuestaPermisosDTO> aprobarPorJefe(
            @PathVariable Long id,
            @Valid @RequestBody AccionPermisoDTO accion,
            Authentication authentication) {
        RespuestaPermisosDTO respuesta = servicio.aprobarPorJefe(id, accion.comentarios, authentication);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Rechaza una solicitud como jefe
     */
    @PostMapping("/{id}/rechazar-jefe")
    @PreAuthorize("hasAnyRole('JEFE', 'HR', 'ADMIN')")
    public ResponseEntity<RespuestaPermisosDTO> rechazarPorJefe(
            @PathVariable Long id,
            @Valid @RequestBody AccionPermisoDTO accion,
            Authentication authentication) {
        RespuestaPermisosDTO respuesta = servicio.rechazarPorJefe(id, accion.comentarios, authentication);
        return ResponseEntity.ok(respuesta);
    }

    // ==================== ENDPOINTS PARA RH ====================

    /**
     * Obtiene las solicitudes que necesitan aprobación de RH
     */
    @GetMapping("/pendientes-rh")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<List<RespuestaPermisosDTO>> obtenerSolicitudesParaRH() {
        List<RespuestaPermisosDTO> solicitudes = servicio.obtenerSolicitudesParaRH();
        return ResponseEntity.ok(solicitudes);
    }

    /**
     * Aprueba una solicitud como RH (aprobación final)
     */
    @PostMapping("/{id}/aprobar-rh")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<RespuestaPermisosDTO> aprobarPorRH(
            @PathVariable Long id,
            @Valid @RequestBody AccionPermisoDTO accion,
            Authentication authentication) {
        RespuestaPermisosDTO respuesta = servicio.aprobarPorRH(id, accion.comentarios, authentication);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Rechaza una solicitud como RH
     */
    @PostMapping("/{id}/rechazar-rh")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<RespuestaPermisosDTO> rechazarPorRH(
            @PathVariable Long id,
            @Valid @RequestBody AccionPermisoDTO accion,
            Authentication authentication) {
        RespuestaPermisosDTO respuesta = servicio.rechazarPorRH(id, accion.comentarios, authentication);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Cancela una solicitud (solo RH puede cancelar solicitudes aprobadas)
     */
    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<RespuestaPermisosDTO> cancelarSolicitud(
            @PathVariable Long id,
            Authentication authentication) {
        RespuestaPermisosDTO respuesta = servicio.cancelarSolicitud(id, authentication);
        return ResponseEntity.ok(respuesta);
    }
}
