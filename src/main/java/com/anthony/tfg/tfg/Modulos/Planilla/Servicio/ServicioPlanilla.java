package com.anthony.tfg.tfg.Modulos.Planilla.Servicio;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaPlanillaEmpleadoDTO;
import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaPlanillaEncabezadoDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudPlanillaEncabezadoDTO;
import com.anthony.tfg.tfg.Entidades.PlanillaDetalle;
import com.anthony.tfg.tfg.Entidades.PlanillaEncabezado;
import com.anthony.tfg.tfg.Entidades.Enums.EstadoPlanilla;
import com.anthony.tfg.tfg.Exceptions.ResourceNotFoundException;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasPlanillaEncabezado;
import com.anthony.tfg.tfg.Modulos.Interfaces.ServicioInterface;
import com.anthony.tfg.tfg.Modulos.Mantenimientos.MantenimientosPlanillaEncabezados;
import com.anthony.tfg.tfg.Repositorios.PlanillaDetalleRepositorio;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ServicioPlanilla implements ServicioInterface<RespuestaPlanillaEncabezadoDTO, 
                                                        SolicitudPlanillaEncabezadoDTO, 
                                                        PlanillaEncabezado>{

    private final ConsultasPlanillaEncabezado consulta;
    private final MantenimientosPlanillaEncabezados mantenimiento;
    private final PlanillaDetalleRepositorio planillaDetalleRepo;

    public ServicioPlanilla(ConsultasPlanillaEncabezado consulta, 
                           MantenimientosPlanillaEncabezados mantenimiento,
                           PlanillaDetalleRepositorio planillaDetalleRepo) {
        this.consulta = consulta;
        this.mantenimiento = mantenimiento;
        this.planillaDetalleRepo = planillaDetalleRepo;
    }

    /**
     * Obtiene las planillas de un empleado específico
     * @param empleadoId ID del empleado
     * @return Lista de planillas del empleado con sus detalles
     */
    public List<RespuestaPlanillaEmpleadoDTO> obtenerPlanillasPorEmpleado(Long empleadoId) {
        log.info("Obteniendo planillas para el empleado con ID: {}", empleadoId);
        List<PlanillaDetalle> detalles = planillaDetalleRepo.findByEmpleadoId(empleadoId);
        
        List<RespuestaPlanillaEmpleadoDTO> planillas = detalles.stream()
            .map(this::deDetalleADtoEmpleado)
            .collect(Collectors.toList());
        
        log.info("Se encontraron {} planillas para el empleado con ID: {}", planillas.size(), empleadoId);
        return planillas;
    }

    /**
     * Convierte PlanillaDetalle a RespuestaPlanillaEmpleadoDTO
     */
    private RespuestaPlanillaEmpleadoDTO deDetalleADtoEmpleado(PlanillaDetalle detalle) {
        RespuestaPlanillaEmpleadoDTO dto = new RespuestaPlanillaEmpleadoDTO();
        
        // Datos del encabezado
        PlanillaEncabezado encabezado = detalle.getPlanillaEncabezado();
        dto.idEncabezado = encabezado.getId();
        dto.fechaInicioPeriodo = encabezado.getFechaInicioPeriodo();
        dto.fechaFinPeriodo = encabezado.getFechaFinPeriodo();
        dto.fechaPago = encabezado.getFechaPago();
        dto.estadoPlanilla = encabezado.getEstadoPlanilla() != null ? 
                            encabezado.getEstadoPlanilla().name() : null;
        
        // Datos del detalle
        dto.idDetalle = detalle.getId();
        dto.salarioBasePeriodo = detalle.getSalarioBasePeriodo() != null ? detalle.getSalarioBasePeriodo() : 0.0;
        dto.cantidadDiasFeriados = detalle.getCantidadDiasFeriados() != null ? detalle.getCantidadDiasFeriados() : 0;
        dto.montoHorasExtra = detalle.getMontoHorasExtra() != null ? detalle.getMontoHorasExtra() : 0.0;
        dto.montoIncapacidad = detalle.getMontoIncapacidad() != null ? detalle.getMontoIncapacidad() : 0.0;
        dto.deduccionCcssIvm = detalle.getDeduccionCcssIvm() != null ? detalle.getDeduccionCcssIvm() : 0.0;
        dto.deduccionCcssSem = detalle.getDeduccionCcssSem() != null ? detalle.getDeduccionCcssSem() : 0.0;
        dto.impuestoDeRenta = detalle.getImpuestoDeRenta() != null ? detalle.getImpuestoDeRenta() : 0.0;
        dto.otrasDeducciones = detalle.getOtrasDeducciones() != null ? detalle.getOtrasDeducciones() : 0.0;
        
        // Calcular totales
        dto.totalDevengado = dto.salarioBasePeriodo + dto.montoHorasExtra + dto.montoIncapacidad;
        dto.totalDeducciones = dto.deduccionCcssIvm + dto.deduccionCcssSem + 
                              dto.impuestoDeRenta + dto.otrasDeducciones;
        dto.salarioNeto = dto.totalDevengado - dto.totalDeducciones;
        
        return dto;
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

    public List<RespuestaPlanillaEncabezadoDTO> obtenerTodos() {
        List<PlanillaEncabezado> entidades = consulta.obtenerTodos();
        log.info("Se han obtenido todas las planillas. La cantidad de registros es: " + entidades.size());
        return deListaEntidadADto(entidades);
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
        planillaExistente.setFechaInicioPeriodo(entidad.getFechaInicioPeriodo());
        planillaExistente.setFechaFinPeriodo(entidad.getFechaFinPeriodo());
        planillaExistente.setFechaPago(entidad.getFechaPago());
        planillaExistente.setTotalPlanillaBruto(entidad.getTotalPlanillaBruto());
        planillaExistente.setTotalPlanillaNeto(entidad.getTotalPlanillaNeto());
        
        EstadoPlanilla estadoPlanilla = obtenerEstadoPlanilla(entidad.getEstadoPlanilla());
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
                    .id(solicitud.getId())
                    .fechaInicioPeriodo(solicitud.getFechaInicioPeriodo())
                    .fechaFinPeriodo(solicitud.getFechaFinPeriodo())
                    .fechaPago(solicitud.getFechaPago())
                    .totalPlanillaBruto(solicitud.getTotalPlanillaBruto())
                    .totalPlanillaNeto(solicitud.getTotalPlanillaNeto())
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
