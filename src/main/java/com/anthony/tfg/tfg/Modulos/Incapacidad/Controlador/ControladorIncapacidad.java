package com.anthony.tfg.tfg.Modulos.Incapacidad.Controlador;

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

import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaIncapacidadesDTO;
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
    public ResponseEntity<List<RespuestaIncapacidadesDTO>> obtenerTodos() {
        List<RespuestaIncapacidadesDTO> lista = servicio.obtenerTodos();
        return ResponseEntity.ok(lista);
    }

    @PostMapping
    public ResponseEntity<RespuestaIncapacidadesDTO> crear(@Valid @RequestBody SolicitudIncapacidadesDTO solicitud) {
        RespuestaIncapacidadesDTO respuesta = servicio.guardar(solicitud);
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
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
