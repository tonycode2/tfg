package com.anthony.tfg.tfg.Modulos.Permisos.Controlador;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.RestController;

import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaPermisosDTO;
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

    @GetMapping("/{id}")
    public ResponseEntity<RespuestaPermisosDTO> obtenerPorId(@PathVariable Long id) {
        RespuestaPermisosDTO respuesta = servicio.obtenerPorId(id);
        if(respuesta == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping
    public ResponseEntity<Page<RespuestaPermisosDTO>> obtenerTodos(Pageable pageable) {
        Page<RespuestaPermisosDTO> page = servicio.obtenerTodos(pageable);
        return ResponseEntity.ok(page);
    }

    @PostMapping
    public ResponseEntity<RespuestaPermisosDTO> crear(@Valid @RequestBody SolicitudPermisosDTO solicitud) {
        RespuestaPermisosDTO respuesta = servicio.guardar(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RespuestaPermisosDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudPermisosDTO solicitud) {
        RespuestaPermisosDTO respuesta = servicio.actualizar(id, solicitud);
        if(respuesta == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(respuesta);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
