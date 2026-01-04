package com.anthony.tfg.tfg.Modulos.Asistencia.Controlador;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaAsistenciaDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudAsistenciaDTO;
import com.anthony.tfg.tfg.Modulos.Asistencia.Servicio.ServicioAsistencia;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/asistencias")
@Validated
public class ControladorAsistencia {

    private final ServicioAsistencia servicio;

    public ControladorAsistencia(ServicioAsistencia servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/{id}")
    public ResponseEntity<RespuestaAsistenciaDTO> obtenerPorId(@PathVariable Long id) {
        RespuestaAsistenciaDTO respuesta = servicio.obtenerPorId(id);
        if(respuesta == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping
    public ResponseEntity<Page<RespuestaAsistenciaDTO>> obtenerTodos(Pageable pageable) {
        var todos = servicio.obtenerTodos();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), todos.size());
        Page<RespuestaAsistenciaDTO> page = new PageImpl<>(
            todos.subList(start, end), 
            pageable, 
            todos.size()
        );
        return ResponseEntity.ok(page);
    }

    @PostMapping
    public ResponseEntity<RespuestaAsistenciaDTO> crear(@Valid @RequestBody SolicitudAsistenciaDTO solicitud) {
        RespuestaAsistenciaDTO respuesta = servicio.guardar(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RespuestaAsistenciaDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudAsistenciaDTO solicitud) {
        RespuestaAsistenciaDTO respuesta = servicio.actualizar(id, solicitud);
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
