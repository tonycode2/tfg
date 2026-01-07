package com.anthony.tfg.tfg.Modulos.Evaluacion.Controlador;

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

import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaEvaluacionDeDesempenoDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudEvaluacionDeDesempenoDTO;
import com.anthony.tfg.tfg.Modulos.Evaluacion.Servicio.ServicioEvaluacion;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/evaluaciones")
@Validated
public class ControladorEvaluacion {

    private final ServicioEvaluacion servicio;

    public ControladorEvaluacion(ServicioEvaluacion servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/{id}")
    public ResponseEntity<RespuestaEvaluacionDeDesempenoDTO> obtenerPorId(@PathVariable Long id) {
        RespuestaEvaluacionDeDesempenoDTO respuesta = servicio.obtenerPorId(id);
        if(respuesta == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping
    public ResponseEntity<Page<RespuestaEvaluacionDeDesempenoDTO>> obtenerTodos(Pageable pageable) {
        Page<RespuestaEvaluacionDeDesempenoDTO> page = servicio.obtenerTodos(pageable);
        return ResponseEntity.ok(page);
    }

    @PostMapping
    public ResponseEntity<RespuestaEvaluacionDeDesempenoDTO> crear(@Valid @RequestBody SolicitudEvaluacionDeDesempenoDTO solicitud) {
        RespuestaEvaluacionDeDesempenoDTO respuesta = servicio.guardar(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RespuestaEvaluacionDeDesempenoDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudEvaluacionDeDesempenoDTO solicitud) {
        RespuestaEvaluacionDeDesempenoDTO respuesta = servicio.actualizar(id, solicitud);
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
