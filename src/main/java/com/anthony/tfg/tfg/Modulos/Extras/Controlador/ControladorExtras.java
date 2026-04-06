package com.anthony.tfg.tfg.Modulos.Extras.Controlador;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaHorasExtraDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudHorasExtraDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.AccionHorasExtraDTO;
import com.anthony.tfg.tfg.Modulos.Extras.Servicio.ServicioExtras;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/horas-extra")
@Validated
public class ControladorExtras {

    private final ServicioExtras servicio;

    public ControladorExtras(ServicioExtras servicio) {
        this.servicio = servicio;
    }

    /** 
     * @param id
     * @return ResponseEntity<RespuestaHorasExtraDTO>
     */
    @GetMapping("/{id}")
    public ResponseEntity<RespuestaHorasExtraDTO> obtenerPorId(@PathVariable Long id) {
        RespuestaHorasExtraDTO respuesta = servicio.obtenerPorId(id);
        if(respuesta == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(respuesta);
    }

    /** 
     * @return ResponseEntity<List<RespuestaHorasExtraDTO>>
     */
    @GetMapping
    public ResponseEntity<List<RespuestaHorasExtraDTO>> obtenerTodos() {
        List<RespuestaHorasExtraDTO> lista = servicio.obtenerTodos();
        return ResponseEntity.ok(lista);
    }

    /** 
     * @param solicitud
     * @return ResponseEntity<RespuestaHorasExtraDTO>
     */
    @PostMapping
    public ResponseEntity<RespuestaHorasExtraDTO> crear(@Valid @RequestBody SolicitudHorasExtraDTO solicitud) {
        RespuestaHorasExtraDTO respuesta = servicio.guardar(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /**
     * Endpoint para que un empleado autenticado solicite horas extra.
     * Toma el empleado del usuario autenticado y aplica las validaciones del servicio.
     */
    @PostMapping("/solicitar")
    public ResponseEntity<RespuestaHorasExtraDTO> solicitar(@Valid @RequestBody SolicitudHorasExtraDTO solicitud, Authentication auth) {
        RespuestaHorasExtraDTO respuesta = servicio.guardar(solicitud, auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /** 
     * @param id
     * @param auth
     * @return ResponseEntity<RespuestaHorasExtraDTO>
     */
    @PutMapping("/{id}/aprobar-jefe")
    public ResponseEntity<RespuestaHorasExtraDTO> aprobarPorJefe(@PathVariable Long id, Authentication auth) {
        RespuestaHorasExtraDTO respuesta = servicio.aprobarPorJefe(id, auth);
        return ResponseEntity.ok(respuesta);
    }

    /** 
     * @param id
     * @param accion
     * @param auth
     * @return ResponseEntity<RespuestaHorasExtraDTO>
     */
    @PutMapping("/{id}/rechazar-jefe")
    public ResponseEntity<RespuestaHorasExtraDTO> rechazarPorJefe(@PathVariable Long id, 
            @Valid @RequestBody AccionHorasExtraDTO accion, Authentication auth) {
        RespuestaHorasExtraDTO respuesta = servicio.rechazarPorJefe(id, accion.comentarios, auth);
        return ResponseEntity.ok(respuesta);
    }

    /** 
     * @param id
     * @param auth
     * @return ResponseEntity<RespuestaHorasExtraDTO>
     */
    @PutMapping("/{id}/aprobar-rh")
    public ResponseEntity<RespuestaHorasExtraDTO> aprobarPorRH(@PathVariable Long id, Authentication auth) {
        RespuestaHorasExtraDTO respuesta = servicio.aprobarPorRH(id, auth);
        return ResponseEntity.ok(respuesta);
    }

    /** 
     * @param id
     * @param accion
     * @param auth
     * @return ResponseEntity<RespuestaHorasExtraDTO>
     */
    @PutMapping("/{id}/rechazar-rh")
    public ResponseEntity<RespuestaHorasExtraDTO> rechazarPorRH(@PathVariable Long id, 
            @Valid @RequestBody AccionHorasExtraDTO accion, Authentication auth) {
        RespuestaHorasExtraDTO respuesta = servicio.rechazarPorRH(id, accion.comentarios, auth);
        return ResponseEntity.ok(respuesta);
    }

    /** 
     * @param actualizar(
     * @return ResponseEntity<RespuestaHorasExtraDTO>
     */
    @PutMapping("/{id}")
    public ResponseEntity<RespuestaHorasExtraDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudHorasExtraDTO solicitud) {
        RespuestaHorasExtraDTO respuesta = servicio.actualizar(id, solicitud);
        if(respuesta == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(respuesta);
    }

    /** 
     * @param id
     * @return ResponseEntity<Void>
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
