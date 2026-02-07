package com.anthony.tfg.tfg.Modulos.Liquidacion.Controlador;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaCalculoLiquidacionDTO;
import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaLiquidacionesDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudCalculoLiquidacionDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudLiquidacionesDTO;
import com.anthony.tfg.tfg.Modulos.Liquidacion.Servicio.ServicioLiquidacion;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/liquidaciones")
@Validated
public class ControladorLiquidacion {

    private final ServicioLiquidacion servicio;

    public ControladorLiquidacion(ServicioLiquidacion servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/calcular")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<RespuestaCalculoLiquidacionDTO> calcularLiquidacion(
            @Valid @RequestBody SolicitudCalculoLiquidacionDTO solicitud) {
        RespuestaCalculoLiquidacionDTO respuesta = servicio.calcularYGuardar(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RespuestaLiquidacionesDTO> obtenerPorId(@PathVariable Long id) {
        RespuestaLiquidacionesDTO respuesta = servicio.obtenerPorId(id);
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping
    public ResponseEntity<List<RespuestaLiquidacionesDTO>> obtenerTodos() {
        List<RespuestaLiquidacionesDTO> lista = servicio.obtenerTodos();
        return ResponseEntity.ok(lista);
    }

    @PostMapping
    public ResponseEntity<RespuestaLiquidacionesDTO> crear(
            @Valid @RequestBody SolicitudLiquidacionesDTO solicitud) {
        RespuestaLiquidacionesDTO respuesta = servicio.guardar(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RespuestaLiquidacionesDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudLiquidacionesDTO solicitud) {
        RespuestaLiquidacionesDTO respuesta = servicio.actualizar(id, solicitud);
        return ResponseEntity.ok(respuesta);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
