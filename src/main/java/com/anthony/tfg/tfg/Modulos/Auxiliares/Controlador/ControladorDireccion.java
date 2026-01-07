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

import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaDireccionDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudDireccionDTO;
import com.anthony.tfg.tfg.Modulos.Auxiliares.Servicio.ServicioDireccion;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/direcciones")
@Validated
public class ControladorDireccion {

    private final ServicioDireccion servicio;

    public ControladorDireccion(ServicioDireccion servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/{id}")
    public ResponseEntity<RespuestaDireccionDTO> obtenerPorId(@PathVariable Long id) {
        RespuestaDireccionDTO respuesta = servicio.obtenerPorId(id);
        if(respuesta == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping
    public ResponseEntity<List<RespuestaDireccionDTO>> obtenerTodos() {
        List<RespuestaDireccionDTO> lista = servicio.obtenerTodos();
        return ResponseEntity.ok(lista);
    }

    @PostMapping
    public ResponseEntity<RespuestaDireccionDTO> crear(@Valid @RequestBody SolicitudDireccionDTO solicitud) {
        RespuestaDireccionDTO respuesta = servicio.guardar(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RespuestaDireccionDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudDireccionDTO solicitud) {
        RespuestaDireccionDTO respuesta = servicio.actualizar(id, solicitud);
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
