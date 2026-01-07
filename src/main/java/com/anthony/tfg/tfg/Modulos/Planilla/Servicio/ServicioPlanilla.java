package com.anthony.tfg.tfg.Modulos.Planilla.Servicio;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaPlanillaEncabezadoDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudPlanillaEncabezadoDTO;
import com.anthony.tfg.tfg.Entidades.PlanillaEncabezado;
import com.anthony.tfg.tfg.Entidades.Enums.EstadoPlanilla;
import com.anthony.tfg.tfg.Exceptions.ResourceNotFoundException;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasPlanillaEncabezado;
import com.anthony.tfg.tfg.Modulos.Interfaces.ServicioInterface;
import com.anthony.tfg.tfg.Modulos.Mantenimientos.MantenimientosPlanillaEncabezados;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ServicioPlanilla implements ServicioInterface<RespuestaPlanillaEncabezadoDTO, 
                                                        SolicitudPlanillaEncabezadoDTO, 
                                                        PlanillaEncabezado>{

    private final ConsultasPlanillaEncabezado consulta;
    private final MantenimientosPlanillaEncabezados mantenimiento;

    public ServicioPlanilla(ConsultasPlanillaEncabezado consulta, MantenimientosPlanillaEncabezados mantenimiento) {
        this.consulta = consulta;
        this.mantenimiento = mantenimiento;
    }

    public RespuestaPlanillaEncabezadoDTO obtenerPorId(Long id) {
        PlanillaEncabezado planilla = consulta.obtenerPorId(id);
        if(planilla == null){
            log.warn("No se ha encontrado la planilla con ID: " + id);
            throw new ResourceNotFoundException("PlanillaEncabezado", "id", id);
        }
        log.info("Se ha encontrado la planilla con ID: " + id);
        return deEntidadDtoARespuesta(planilla);
    }

    public Page<RespuestaPlanillaEncabezadoDTO> obtenerTodos(Pageable pageable) {
        Page<PlanillaEncabezado> entidades = consulta.obtenerTodos(pageable);
        log.info("Se han obtenido todas las planillas. La cantidad de registros es: " + entidades.getTotalElements());
        return entidades.map(this::deEntidadDtoARespuesta);
    }

    public RespuestaPlanillaEncabezadoDTO guardar(SolicitudPlanillaEncabezadoDTO entidad) {
        PlanillaEncabezado nuevaPlanilla = deSolicitudDtoAEntidad(entidad);
        PlanillaEncabezado planillaGuardada = mantenimiento.crear(nuevaPlanilla);
        log.info("Se ha guardado una nueva planilla con ID: " + planillaGuardada.getId());
        return deEntidadDtoARespuesta(planillaGuardada);
    }

    public RespuestaPlanillaEncabezadoDTO actualizar(Long id, SolicitudPlanillaEncabezadoDTO entidad) {
        PlanillaEncabezado planillaExistente = consulta.obtenerPorId(id);
        if(planillaExistente == null){
            log.warn("No se ha encontrado la planilla con ID: " + id + " para actualizar");
            return null;
        }
        planillaExistente.setFechaInicioPeriodo(entidad.fechaInicioPeriodo);
        planillaExistente.setFechaFinPeriodo(entidad.fechaFinPeriodo);
        planillaExistente.setFechaPago(entidad.fechaPago);
        planillaExistente.setTotalPlanillaBruto(entidad.totalPlanillaBruto);
        planillaExistente.setTotalPlanillaNeto(entidad.totalPlanillaNeto);
        
        EstadoPlanilla estadoPlanilla = obtenerEstadoPlanilla(entidad.estadoPlanilla);
        if(estadoPlanilla != null){
            planillaExistente.setEstadoPlanilla(estadoPlanilla);
        }
        
        PlanillaEncabezado planillaActualizada = mantenimiento.actualizar(planillaExistente);
        log.info("Se ha actualizado la planilla con ID: " + id);
        return deEntidadDtoARespuesta(planillaActualizada);
    }

    public void eliminar(Long id) {
        mantenimiento.eliminar(id);
        log.info("Se ha eliminado la planilla con ID: " + id);
    }

    public PlanillaEncabezado deSolicitudDtoAEntidad(SolicitudPlanillaEncabezadoDTO solicitud) {
        if(solicitud == null){
            log.warn("El DTO de solicitud es nulo, no se puede convertir a entidad PlanillaEncabezado.");
            return null;
        }
        
        EstadoPlanilla estadoPlanilla = obtenerEstadoPlanilla(solicitud.estadoPlanilla);
        if(estadoPlanilla == null){
            log.warn("No se ha encontrado el estado de planilla: " + solicitud.estadoPlanilla);
            return null;
        }
        
        PlanillaEncabezado planilla = PlanillaEncabezado.builder()
                    .id(solicitud.id)
                    .fechaInicioPeriodo(solicitud.fechaInicioPeriodo)
                    .fechaFinPeriodo(solicitud.fechaFinPeriodo)
                    .fechaPago(solicitud.fechaPago)
                    .totalPlanillaBruto(solicitud.totalPlanillaBruto)
                    .totalPlanillaNeto(solicitud.totalPlanillaNeto)
                    .estadoPlanilla(estadoPlanilla)
                    .build();
        log.info("Se ha convertido el DTO de solicitud a entidad PlanillaEncabezado: {}", planilla);
        return planilla;
    }

    public RespuestaPlanillaEncabezadoDTO deEntidadDtoARespuesta(PlanillaEncabezado entidad) {
        if(entidad == null){
            log.warn("La entidad PlanillaEncabezado es nula, no se puede convertir a DTO de respuesta.");
            return null;
        }
        RespuestaPlanillaEncabezadoDTO respuesta = new RespuestaPlanillaEncabezadoDTO();
        respuesta.id = entidad.getId();
        respuesta.fechaInicioPeriodo = entidad.getFechaInicioPeriodo();
        respuesta.fechaFinPeriodo = entidad.getFechaFinPeriodo();
        respuesta.fechaPago = entidad.getFechaPago();
        respuesta.totalPlanillaBruto = entidad.getTotalPlanillaBruto();
        respuesta.totalPlanillaNeto = entidad.getTotalPlanillaNeto();
        
        if(entidad.getEstadoPlanilla() != null){
            respuesta.estadoPlanilla = entidad.getEstadoPlanilla().name();
        }
        
        log.info("Se ha convertido la entidad PlanillaEncabezado a DTO de respuesta: {}", respuesta);
        return respuesta;
    }

    public List<RespuestaPlanillaEncabezadoDTO> deListaEntidadADto(List<PlanillaEncabezado> entidades) {
        return entidades.stream()
                .map(this::deEntidadDtoARespuesta)
                .toList();
    }
    
    private EstadoPlanilla obtenerEstadoPlanilla(String estado) {
        try {
            return EstadoPlanilla.valueOf(estado.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

}
