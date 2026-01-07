package com.anthony.tfg.tfg.Modulos.Auxiliares.Controlador;

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

import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaDepartamentoDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudDepartamentoDTO;
import com.anthony.tfg.tfg.Modulos.Auxiliares.Servicio.ServicioDepartamento;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/departamentos")
@Validated
public class ControladorDepartamento {

    private final ServicioDepartamento servicio;

    public ControladorDepartamento(ServicioDepartamento servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/{id}")
    public ResponseEntity<RespuestaDepartamentoDTO> obtenerPorId(@PathVariable Long id) {
        RespuestaDepartamentoDTO respuesta = servicio.obtenerPorId(id);
        if(respuesta == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping
    public ResponseEntity<List<RespuestaDepartamentoDTO>> obtenerTodos() {
        List<RespuestaDepartamentoDTO> lista = servicio.obtenerTodos();
        return ResponseEntity.ok(lista);
    }

    @PostMapping
    public ResponseEntity<RespuestaDepartamentoDTO> crear(@Valid @RequestBody SolicitudDepartamentoDTO solicitud) {
        RespuestaDepartamentoDTO respuesta = servicio.guardar(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RespuestaDepartamentoDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudDepartamentoDTO solicitud) {
        RespuestaDepartamentoDTO respuesta = servicio.actualizar(id, solicitud);
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
