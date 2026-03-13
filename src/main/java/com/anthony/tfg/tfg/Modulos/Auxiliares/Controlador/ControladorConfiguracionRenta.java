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

import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaConfiguracionRentaDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudConfiguracionRentaDTO;
import com.anthony.tfg.tfg.Modulos.Auxiliares.Servicio.ServicioConfiguracionRenta;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/configuracion-renta")
@Validated
public class ControladorConfiguracionRenta {

    private final ServicioConfiguracionRenta servicio;

    public ControladorConfiguracionRenta(ServicioConfiguracionRenta servicio) {
        this.servicio = servicio;
    }

    /** 
     * @param id
     * @return ResponseEntity<RespuestaConfiguracionRentaDTO>
     */
    @GetMapping("/{id}")
    public ResponseEntity<RespuestaConfiguracionRentaDTO> obtenerPorId(@PathVariable Long id) {
        RespuestaConfiguracionRentaDTO respuesta = servicio.obtenerPorId(id);
        if(respuesta == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(respuesta);
    }

    /** 
     * @return ResponseEntity<List<RespuestaConfiguracionRentaDTO>>
     */
    @GetMapping
    public ResponseEntity<List<RespuestaConfiguracionRentaDTO>> obtenerTodos() {
        List<RespuestaConfiguracionRentaDTO> lista = servicio.obtenerTodos();
        return ResponseEntity.ok(lista);
    }

    /** 
     * @param solicitud
     * @return ResponseEntity<RespuestaConfiguracionRentaDTO>
     */
    @PostMapping
    public ResponseEntity<RespuestaConfiguracionRentaDTO> crear(@Valid @RequestBody SolicitudConfiguracionRentaDTO solicitud) {
        RespuestaConfiguracionRentaDTO respuesta = servicio.guardar(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /** 
     * @param actualizar(
     * @return ResponseEntity<RespuestaConfiguracionRentaDTO>
     */
    @PutMapping("/{id}")
    public ResponseEntity<RespuestaConfiguracionRentaDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudConfiguracionRentaDTO solicitud) {
        RespuestaConfiguracionRentaDTO respuesta = servicio.actualizar(id, solicitud);
        if(respuesta == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(respuesta);
    }

    /** 
     * @param id
     * @return ResponseEntity<Void>
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
