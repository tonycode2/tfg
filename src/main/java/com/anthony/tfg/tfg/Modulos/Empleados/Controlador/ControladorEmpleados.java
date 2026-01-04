package com.anthony.tfg.tfg.Modulos.Empleados.Controlador;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

    @GetMapping("/{id}")
    public ResponseEntity<RespuestaEmpleadosDTO> obtenerPorId(@PathVariable Long id) {
        RespuestaEmpleadosDTO respuesta = servicio.obtenerPorId(id);
        if(respuesta == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping
    public ResponseEntity<Page<RespuestaEmpleadosDTO>> obtenerTodos(Pageable pageable) {
        var todos = servicio.obtenerTodos();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), todos.size());
        Page<RespuestaEmpleadosDTO> page = new PageImpl<>(
            todos.subList(start, end), 
            pageable, 
            todos.size()
        );
        return ResponseEntity.ok(page);
    }

    @PostMapping
    public ResponseEntity<RespuestaEmpleadosDTO> crear(@Valid @RequestBody SolicitudEmpleadosDTO solicitud) {
        RespuestaEmpleadosDTO respuesta = servicio.guardar(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/generar-usuario")
    public ResponseEntity<RespuestaCredencialesDTO> generarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudGenerarUsuarioDTO solicitud) {
        solicitud.idEmpleado = id;
        RespuestaCredencialesDTO respuesta = servicioGeneracionUsuario.generarUsuarioParaEmpleado(
            solicitud.idEmpleado, 
            solicitud.role
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }
}
