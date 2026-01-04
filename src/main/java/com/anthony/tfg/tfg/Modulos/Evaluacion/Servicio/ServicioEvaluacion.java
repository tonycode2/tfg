package com.anthony.tfg.tfg.Modulos.Evaluacion.Servicio;

import java.util.List;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaEvaluacionDeDesempenoDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudEvaluacionDeDesempenoDTO;
import com.anthony.tfg.tfg.Entidades.Empleados;
import com.anthony.tfg.tfg.Entidades.EvaluacionDeDesempeno;
import com.anthony.tfg.tfg.Exceptions.ResourceNotFoundException;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasEmpleados;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasEvaluacionDeDesempeno;
import com.anthony.tfg.tfg.Modulos.Interfaces.ServicioInterface;
import com.anthony.tfg.tfg.Modulos.Mantenimientos.MantenimientosEvaluacionDeDesempeno;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ServicioEvaluacion implements ServicioInterface<RespuestaEvaluacionDeDesempenoDTO, 
                                                            SolicitudEvaluacionDeDesempenoDTO, 
                                                            EvaluacionDeDesempeno>{

    private final ConsultasEvaluacionDeDesempeno consulta;
    private final MantenimientosEvaluacionDeDesempeno mantenimiento;
    private final ConsultasEmpleados consultasEmpleados;

    public ServicioEvaluacion(ConsultasEvaluacionDeDesempeno consulta, MantenimientosEvaluacionDeDesempeno mantenimiento, ConsultasEmpleados consultasEmpleados) {
        this.consulta = consulta;
        this.mantenimiento = mantenimiento;
        this.consultasEmpleados = consultasEmpleados;
    }

    public RespuestaEvaluacionDeDesempenoDTO obtenerPorId(Long id) {
        EvaluacionDeDesempeno evaluacion = consulta.obtenerPorId(id);
        if(evaluacion == null){
            log.warn("No se ha encontrado la evaluación de desempeño con ID: " + id);
            throw new ResourceNotFoundException("EvaluacionDeDesempeno", "id", id);
        }
        log.info("Se ha encontrado la evaluación de desempeño con ID: " + id);
        return deEntidadDtoARespuesta(evaluacion);
    }

    public List<RespuestaEvaluacionDeDesempenoDTO> obtenerTodos() {
        List<EvaluacionDeDesempeno> entidades = consulta.obtenerTodos();
        log.info("Se han obtenido todas las evaluaciones de desempeño. La cantidad de registros es: " + entidades.size());
        return deListaEntidadADto(entidades);
    }

    public RespuestaEvaluacionDeDesempenoDTO guardar(SolicitudEvaluacionDeDesempenoDTO entidad) {
        EvaluacionDeDesempeno nuevaEvaluacion = deSolicitudDtoAEntidad(entidad);
        EvaluacionDeDesempeno evaluacionGuardada = mantenimiento.crear(nuevaEvaluacion);
        log.info("Se ha guardado una nueva evaluación de desempeño con ID: " + evaluacionGuardada.getId());
        return deEntidadDtoARespuesta(evaluacionGuardada);
    }

    public RespuestaEvaluacionDeDesempenoDTO actualizar(Long id, SolicitudEvaluacionDeDesempenoDTO entidad) {
        EvaluacionDeDesempeno evaluacionExistente = consulta.obtenerPorId(id);
        if(evaluacionExistente == null){
            log.warn("No se ha encontrado la evaluación de desempeño con ID: " + id + " para actualizar");
            return null;
        }
        evaluacionExistente.setFechaEvaluacion(entidad.fechaEvaluacion);
        evaluacionExistente.setPeriodoEvaluado(entidad.periodoEvaluado);
        evaluacionExistente.setPuntuacionFinal(entidad.puntuacionFinal);
        evaluacionExistente.setObservaciones(entidad.observaciones);
        evaluacionExistente.setPlanDeMejora(entidad.planDeMejora);
        
        Empleados empleado = consultasEmpleados.obtenerPorId(entidad.idEmpleado);
        if(empleado != null){
            evaluacionExistente.setEmpleado(empleado);
        }
        
        EvaluacionDeDesempeno evaluacionActualizada = mantenimiento.actualizar(evaluacionExistente);
        log.info("Se ha actualizado la evaluación de desempeño con ID: " + id);
        return deEntidadDtoARespuesta(evaluacionActualizada);
    }

    public void eliminar(Long id) {
        mantenimiento.eliminar(id);
        log.info("Se ha eliminado la evaluación de desempeño con ID: " + id);
    }

    public EvaluacionDeDesempeno deSolicitudDtoAEntidad(SolicitudEvaluacionDeDesempenoDTO solicitud) {
        if(solicitud == null){
            log.warn("El DTO de solicitud es nulo, no se puede convertir a entidad EvaluacionDeDesempeno.");
            return null;
        }
        
        Empleados empleado = consultasEmpleados.obtenerPorId(solicitud.idEmpleado);
        if(empleado == null){
            log.warn("No se ha encontrado el empleado con ID: " + solicitud.idEmpleado);
            return null;
        }
        
        EvaluacionDeDesempeno evaluacion = EvaluacionDeDesempeno.builder()
                    .id(solicitud.id)
                    .fechaEvaluacion(solicitud.fechaEvaluacion)
                    .periodoEvaluado(solicitud.periodoEvaluado)
                    .puntuacionFinal(solicitud.puntuacionFinal)
                    .observaciones(solicitud.observaciones)
                    .planDeMejora(solicitud.planDeMejora)
                    .empleado(empleado)
                    .build();
        log.info("Se ha convertido el DTO de solicitud a entidad EvaluacionDeDesempeno: {}", evaluacion);
        return evaluacion;
    }

    public RespuestaEvaluacionDeDesempenoDTO deEntidadDtoARespuesta(EvaluacionDeDesempeno entidad) {
        if(entidad == null){
            log.warn("La entidad EvaluacionDeDesempeno es nula, no se puede convertir a DTO de respuesta.");
            return null;
        }
        RespuestaEvaluacionDeDesempenoDTO respuesta = new RespuestaEvaluacionDeDesempenoDTO();
        respuesta.id = entidad.getId();
        respuesta.fechaEvaluacion = entidad.getFechaEvaluacion();
        respuesta.periodoEvaluado = entidad.getPeriodoEvaluado();
        respuesta.puntuacionFinal = entidad.getPuntuacionFinal();
        respuesta.observaciones = entidad.getObservaciones();
        respuesta.planDeMejora = entidad.getPlanDeMejora();
        
        if(entidad.getEmpleado() != null){
            respuesta.nombreEmpleado = entidad.getEmpleado().getNombre();
            respuesta.primerApellidoEmpleado = entidad.getEmpleado().getPrimerApellido();
            respuesta.segundoApellidoEmpleado = entidad.getEmpleado().getSegundoApellido();
        }
        
        log.info("Se ha convertido la entidad EvaluacionDeDesempeno a DTO de respuesta: {}", respuesta);
        return respuesta;
    }

    public List<RespuestaEvaluacionDeDesempenoDTO> deListaEntidadADto(List<EvaluacionDeDesempeno> entidades) {
        return entidades.stream()
                .map(this::deEntidadDtoARespuesta)
                .toList();
    }

}
