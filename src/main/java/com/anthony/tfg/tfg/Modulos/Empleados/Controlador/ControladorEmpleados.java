package com.anthony.tfg.tfg.Modulos.Empleados.Controlador;

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

import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaEmpleadosDTO;
import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaCredencialesDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudEmpleadosDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudGenerarUsuarioDTO;
import com.anthony.tfg.tfg.Modulos.Empleados.Servicio.ServicioEmpleados;
import com.anthony.tfg.tfg.Modulos.Empleados.Servicio.ServicioGeneracionUsuario;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/empleados")
@Validated
public class ControladorEmpleados {

    private final ServicioEmpleados servicio;
    private final ServicioGeneracionUsuario servicioGeneracionUsuario;

    public ControladorEmpleados(ServicioEmpleados servicio, ServicioGeneracionUsuario servicioGeneracionUsuario) {
        this.servicio = servicio;
        this.servicioGeneracionUsuario = servicioGeneracionUsuario;
    }

    /** 
     * @param id
     * @return ResponseEntity<RespuestaEmpleadosDTO>
     */
    @GetMapping("/{id}")
    public ResponseEntity<RespuestaEmpleadosDTO> obtenerPorId(@PathVariable Long id) {
        RespuestaEmpleadosDTO respuesta = servicio.obtenerPorId(id);
        if(respuesta == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(respuesta);
    }

    /** 
     * @return ResponseEntity<List<RespuestaEmpleadosDTO>>
     */
    @GetMapping
    public ResponseEntity<List<RespuestaEmpleadosDTO>> obtenerTodos() {
        List<RespuestaEmpleadosDTO> lista = servicio.obtenerTodos();
        return ResponseEntity.ok(lista);
    }

    /** 
     * @param solicitud
     * @return ResponseEntity<RespuestaEmpleadosDTO>
     */
    @PostMapping
    public ResponseEntity<RespuestaEmpleadosDTO> crear(@Valid @RequestBody SolicitudEmpleadosDTO solicitud) {
        RespuestaEmpleadosDTO respuesta = servicio.guardar(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /** 
     * @param actualizar(
     * @return ResponseEntity<RespuestaEmpleadosDTO>
     */
    @PutMapping("/{id}")
    public ResponseEntity<RespuestaEmpleadosDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudEmpleadosDTO solicitud) {
        RespuestaEmpleadosDTO respuesta = servicio.actualizar(id, solicitud);
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
    
    /** 
     * @param generarUsuario(
     * @return ResponseEntity<RespuestaCredencialesDTO>
     */
    @PostMapping("/{id}/generar-usuario")
    public ResponseEntity<RespuestaCredencialesDTO> generarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudGenerarUsuarioDTO solicitud) {
        solicitud.setIdEmpleado(id);
        RespuestaCredencialesDTO respuesta = servicioGeneracionUsuario.generarUsuarioParaEmpleado(
            solicitud.getIdEmpleado(), 
            solicitud.getRole()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }
}
