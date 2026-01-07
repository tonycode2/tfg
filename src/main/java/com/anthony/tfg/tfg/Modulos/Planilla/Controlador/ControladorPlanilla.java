package com.anthony.tfg.tfg.Modulos.Planilla.Controlador;

import java.util.List;

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

import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaPlanillaEncabezadoDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudPlanillaEncabezadoDTO;
import com.anthony.tfg.tfg.Modulos.Planilla.Servicio.ServicioPlanilla;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/planillas")
@Validated
public class ControladorPlanilla {

    private final ServicioPlanilla servicio;

    public ControladorPlanilla(ServicioPlanilla servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/{id}")
    public ResponseEntity<RespuestaPlanillaEncabezadoDTO> obtenerPorId(@PathVariable Long id) {
        RespuestaPlanillaEncabezadoDTO respuesta = servicio.obtenerPorId(id);
        if(respuesta == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping
    public ResponseEntity<List<RespuestaPlanillaEncabezadoDTO>> obtenerTodos() {
        List<RespuestaPlanillaEncabezadoDTO> lista = servicio.obtenerTodos();
        return ResponseEntity.ok(lista);
    }

    @PostMapping
    public ResponseEntity<RespuestaPlanillaEncabezadoDTO> crear(@Valid @RequestBody SolicitudPlanillaEncabezadoDTO solicitud) {
        RespuestaPlanillaEncabezadoDTO respuesta = servicio.guardar(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RespuestaPlanillaEncabezadoDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudPlanillaEncabezadoDTO solicitud) {
        RespuestaPlanillaEncabezadoDTO respuesta = servicio.actualizar(id, solicitud);
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
