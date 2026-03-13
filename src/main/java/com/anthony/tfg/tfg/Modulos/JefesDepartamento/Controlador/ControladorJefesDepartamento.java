package com.anthony.tfg.tfg.Modulos.JefesDepartamento.Controlador;

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

import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaJefesDepartamentoDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudJefesDepartamentoDTO;
import com.anthony.tfg.tfg.Modulos.JefesDepartamento.Servicio.ServicioJefesDepartamento;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/jefes-departamento")
@Validated
public class ControladorJefesDepartamento {

    private final ServicioJefesDepartamento servicio;

    public ControladorJefesDepartamento(ServicioJefesDepartamento servicio) {
        this.servicio = servicio;
    }

    /** 
     * @param id
     * @return ResponseEntity<RespuestaJefesDepartamentoDTO>
     */
    @GetMapping("/{id}")
    public ResponseEntity<RespuestaJefesDepartamentoDTO> obtenerPorId(@PathVariable Long id) {
        RespuestaJefesDepartamentoDTO respuesta = servicio.obtenerPorId(id);
        if(respuesta == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(respuesta);
    }

    /** 
     * @return ResponseEntity<List<RespuestaJefesDepartamentoDTO>>
     */
    @GetMapping
    public ResponseEntity<List<RespuestaJefesDepartamentoDTO>> obtenerTodos() {
        List<RespuestaJefesDepartamentoDTO> lista = servicio.obtenerTodos();
        return ResponseEntity.ok(lista);
    }

    /** 
     * @param solicitud
     * @return ResponseEntity<RespuestaJefesDepartamentoDTO>
     */
    @PostMapping
    public ResponseEntity<RespuestaJefesDepartamentoDTO> crear(@Valid @RequestBody SolicitudJefesDepartamentoDTO solicitud) {
        RespuestaJefesDepartamentoDTO respuesta = servicio.guardar(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /** 
     * @param id
     * @param solicitud
     * @return ResponseEntity<RespuestaJefesDepartamentoDTO>
     */
    @PutMapping("/{id}")
    public ResponseEntity<RespuestaJefesDepartamentoDTO> actualizar(@PathVariable Long id, @Valid @RequestBody SolicitudJefesDepartamentoDTO solicitud) {
        RespuestaJefesDepartamentoDTO respuesta = servicio.actualizar(id, solicitud);
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
