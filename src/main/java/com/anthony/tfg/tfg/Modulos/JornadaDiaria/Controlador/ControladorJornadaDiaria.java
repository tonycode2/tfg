package com.anthony.tfg.tfg.Modulos.JornadaDiaria.Controlador;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaJornadaDiariaDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudJornadaDiariaDTO;
import com.anthony.tfg.tfg.Modulos.JornadaDiaria.Servicio.ServicioJornadaDiaria;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/jornada-diaria")
@Validated
public class ControladorJornadaDiaria {

    private final ServicioJornadaDiaria servicio;

    public ControladorJornadaDiaria(ServicioJornadaDiaria servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/{id}")
    public ResponseEntity<RespuestaJornadaDiariaDTO> obtenerPorId(@PathVariable Long id) {
        RespuestaJornadaDiariaDTO respuesta = servicio.obtenerPorId(id);
        if (respuesta == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping
    public ResponseEntity<List<RespuestaJornadaDiariaDTO>> obtenerTodos() {
        List<RespuestaJornadaDiariaDTO> lista = servicio.obtenerTodos();
        return ResponseEntity.ok(lista);
    }

    @PostMapping
    public ResponseEntity<RespuestaJornadaDiariaDTO> crear(@Valid @RequestBody SolicitudJornadaDiariaDTO solicitud) {
        RespuestaJornadaDiariaDTO respuesta = servicio.guardar(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RespuestaJornadaDiariaDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudJornadaDiariaDTO solicitud) {
        RespuestaJornadaDiariaDTO respuesta = servicio.actualizar(id, solicitud);
        if (respuesta == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(respuesta);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/preview")
    public ResponseEntity<RespuestaJornadaDiariaDTO> previsualizarJornada(
            @RequestParam(required = false) String fechaHoraSalida) {
        
        LocalDateTime fechaHoraSalidaParsed = null;
        if (fechaHoraSalida != null && !fechaHoraSalida.isEmpty()) {
            try {
                // Formato: "yyyy-MM-dd HH:mm:ss" enviado por el frontend
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                fechaHoraSalidaParsed = LocalDateTime.parse(fechaHoraSalida, formatter);
            } catch (Exception e) {
                // Si falla el parsing, usar null y el servicio usará la hora actual
            }
        }
        
        RespuestaJornadaDiariaDTO respuesta = servicio.previsualizarJornadaDiaria(fechaHoraSalidaParsed);
        if (respuesta == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(respuesta);
    }
}
