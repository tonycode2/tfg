package com.anthony.tfg.tfg.Modulos.Aguinaldo.Controlador;

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

import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaAguinaldosDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudAguinaldosDTO;
import com.anthony.tfg.tfg.Modulos.Aguinaldo.Servicio.ServicioAguinaldo;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/aguinaldos")
@Validated
public class ControladorAguinaldo {

    private final ServicioAguinaldo servicio;

    public ControladorAguinaldo(ServicioAguinaldo servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/{id}")
    public ResponseEntity<RespuestaAguinaldosDTO> obtenerPorId(@PathVariable Long id) {
        RespuestaAguinaldosDTO respuesta = servicio.obtenerPorId(id);
        if(respuesta == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping
    public ResponseEntity<List<RespuestaAguinaldosDTO>> obtenerTodos() {
        List<RespuestaAguinaldosDTO> lista = servicio.obtenerTodos();
        return ResponseEntity.ok(lista);
    }

    @PostMapping
    public ResponseEntity<RespuestaAguinaldosDTO> crear(@Valid @RequestBody SolicitudAguinaldosDTO solicitud) {
        RespuestaAguinaldosDTO respuesta = servicio.guardar(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RespuestaAguinaldosDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudAguinaldosDTO solicitud) {
        RespuestaAguinaldosDTO respuesta = servicio.actualizar(id, solicitud);
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
