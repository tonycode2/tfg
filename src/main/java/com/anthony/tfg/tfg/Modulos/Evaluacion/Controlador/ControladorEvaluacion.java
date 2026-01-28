package com.anthony.tfg.tfg.Modulos.Evaluacion.Controlador;

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

import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaEvaluacionDeDesempenoDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudEvaluacionDeDesempenoDTO;
import com.anthony.tfg.tfg.DTOs.Respuesta.ResumenEvaluacionesDepartamentoDTO;
import com.anthony.tfg.tfg.DTOs.Respuesta.EmpleadoEvaluacionResumenDTO;
import com.anthony.tfg.tfg.Modulos.Evaluacion.Servicio.ServicioEvaluacion;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/evaluaciones")
@Validated
public class ControladorEvaluacion {

    private final ServicioEvaluacion servicio;

    public ControladorEvaluacion(ServicioEvaluacion servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/{id}")
    public ResponseEntity<RespuestaEvaluacionDeDesempenoDTO> obtenerPorId(@PathVariable Long id) {
        RespuestaEvaluacionDeDesempenoDTO respuesta = servicio.obtenerPorId(id);
        if(respuesta == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping
    public ResponseEntity<List<RespuestaEvaluacionDeDesempenoDTO>> obtenerTodos() {
        List<RespuestaEvaluacionDeDesempenoDTO> lista = servicio.obtenerTodos();
        return ResponseEntity.ok(lista);
    }

    @PostMapping
    public ResponseEntity<RespuestaEvaluacionDeDesempenoDTO> crear(@Valid @RequestBody SolicitudEvaluacionDeDesempenoDTO solicitud) {
        RespuestaEvaluacionDeDesempenoDTO respuesta = servicio.guardar(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RespuestaEvaluacionDeDesempenoDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudEvaluacionDeDesempenoDTO solicitud) {
        RespuestaEvaluacionDeDesempenoDTO respuesta = servicio.actualizar(id, solicitud);
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

    @GetMapping("/resumen-departamento/{idDepartamento}")
    public ResponseEntity<ResumenEvaluacionesDepartamentoDTO> obtenerResumenDepartamento(@PathVariable Long idDepartamento) {
        ResumenEvaluacionesDepartamentoDTO resumen = servicio.obtenerResumenDepartamento(idDepartamento);
        return ResponseEntity.ok(resumen);
    }

    @GetMapping("/empleados-mis-departamentos")
    public ResponseEntity<java.util.List<EmpleadoEvaluacionResumenDTO>> obtenerEmpleadosMisDepartamentos() {
        java.util.List<EmpleadoEvaluacionResumenDTO> lista = servicio.obtenerEmpleadosMisDepartamentos();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/por-empleado/{idEmpleado}")
    public ResponseEntity<java.util.List<RespuestaEvaluacionDeDesempenoDTO>> obtenerPorEmpleado(@PathVariable Long idEmpleado) {
        java.util.List<RespuestaEvaluacionDeDesempenoDTO> lista = servicio.obtenerPorEmpleado(idEmpleado);
        return ResponseEntity.ok(lista);
    }
}
