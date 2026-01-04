package com.anthony.tfg.tfg.Modulos.Liquidacion.Servicio;

import java.util.List;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaLiquidacionesDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudLiquidacionesDTO;
import com.anthony.tfg.tfg.Entidades.Empleados;
import com.anthony.tfg.tfg.Entidades.Liquidaciones;
import com.anthony.tfg.tfg.Entidades.Enums.MotivoSalida;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasEmpleados;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasLiquidaciones;
import com.anthony.tfg.tfg.Modulos.Interfaces.ServicioInterface;
import com.anthony.tfg.tfg.Modulos.Mantenimientos.MantenimientosLiquidaciones;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ServicioLiquidacion implements ServicioInterface<RespuestaLiquidacionesDTO, 
                                                            SolicitudLiquidacionesDTO, 
                                                            Liquidaciones>{

    private final ConsultasLiquidaciones consulta;
    private final MantenimientosLiquidaciones mantenimiento;
    private final ConsultasEmpleados consultasEmpleados;

    public ServicioLiquidacion(ConsultasLiquidaciones consulta, MantenimientosLiquidaciones mantenimiento, ConsultasEmpleados consultasEmpleados) {
        this.consulta = consulta;
        this.mantenimiento = mantenimiento;
        this.consultasEmpleados = consultasEmpleados;
    }

    public RespuestaLiquidacionesDTO obtenerPorId(Long id) {
        RespuestaLiquidacionesDTO respuesta = deEntidadDtoARespuesta(consulta.obtenerPorId(id));
        if(respuesta != null){
            log.info("Se ha encontrado la liquidación con ID: " + id);
        } else {
            log.warn("No se ha encontrado la liquidación con ID: " + id);
        }
        return respuesta;
    }

    public List<RespuestaLiquidacionesDTO> obtenerTodos() {
        List<Liquidaciones> entidades = consulta.obtenerTodos();
        log.info("Se han obtenido todas las liquidaciones. La cantidad de registros es: " + entidades.size());
        return deListaEntidadADto(entidades);
    }

    public RespuestaLiquidacionesDTO guardar(SolicitudLiquidacionesDTO entidad) {
        Liquidaciones nuevaLiquidacion = deSolicitudDtoAEntidad(entidad);
        Liquidaciones liquidacionGuardada = mantenimiento.crear(nuevaLiquidacion);
        log.info("Se ha guardado una nueva liquidación con ID: " + liquidacionGuardada.getId());
        return deEntidadDtoARespuesta(liquidacionGuardada);
    }

    public RespuestaLiquidacionesDTO actualizar(Long id, SolicitudLiquidacionesDTO entidad) {
        Liquidaciones liquidacionExistente = consulta.obtenerPorId(id);
        if(liquidacionExistente == null){
            log.warn("No se ha encontrado la liquidación con ID: " + id + " para actualizar");
            return null;
        }
        liquidacionExistente.setFechaSalida(entidad.fechaSalida);
        liquidacionExistente.setMontoPreaviso(entidad.montoPreaviso);
        liquidacionExistente.setMontoCesantia(entidad.montoCesantia);
        liquidacionExistente.setMontoVacacionesPendientes(entidad.montoVacacionesPendientes);
        liquidacionExistente.setMontoAguinaldoPendiente(entidad.montoAguinaldoPendiente);
        liquidacionExistente.setTotalLiquidacion(entidad.totalLiquidacion);
        
        MotivoSalida motivoSalida = obtenerMotivoSalida(entidad.motivoSalida);
        if(motivoSalida != null){
            liquidacionExistente.setMotivoSalida(motivoSalida);
        }
        
        Empleados empleado = consultasEmpleados.obtenerPorId(entidad.idEmpleado);
        if(empleado != null){
            liquidacionExistente.setEmpleado(empleado);
        }
        
        Liquidaciones liquidacionActualizada = mantenimiento.actualizar(liquidacionExistente);
        log.info("Se ha actualizado la liquidación con ID: " + id);
        return deEntidadDtoARespuesta(liquidacionActualizada);
    }

    public void eliminar(Long id) {
        mantenimiento.eliminar(id);
        log.info("Se ha eliminado la liquidación con ID: " + id);
    }

    public Liquidaciones deSolicitudDtoAEntidad(SolicitudLiquidacionesDTO solicitud) {
        if(solicitud == null){
            log.warn("El DTO de solicitud es nulo, no se puede convertir a entidad Liquidaciones.");
            return null;
        }
        
        Empleados empleado = consultasEmpleados.obtenerPorId(solicitud.idEmpleado);
        if(empleado == null){
            log.warn("No se ha encontrado el empleado con ID: " + solicitud.idEmpleado);
            return null;
        }
        
        MotivoSalida motivoSalida = obtenerMotivoSalida(solicitud.motivoSalida);
        if(motivoSalida == null){
            log.warn("No se ha encontrado el motivo de salida: " + solicitud.motivoSalida);
            return null;
        }
        
        Liquidaciones liquidacion = Liquidaciones.builder()
                    .id(solicitud.id)
                    .fechaSalida(solicitud.fechaSalida)
                    .montoPreaviso(solicitud.montoPreaviso)
                    .montoCesantia(solicitud.montoCesantia)
                    .montoVacacionesPendientes(solicitud.montoVacacionesPendientes)
                    .montoAguinaldoPendiente(solicitud.montoAguinaldoPendiente)
                    .totalLiquidacion(solicitud.totalLiquidacion)
                    .motivoSalida(motivoSalida)
                    .empleado(empleado)
                    .build();
        log.info("Se ha convertido el DTO de solicitud a entidad Liquidaciones: {}", liquidacion);
        return liquidacion;
    }

    public RespuestaLiquidacionesDTO deEntidadDtoARespuesta(Liquidaciones entidad) {
        if(entidad == null){
            log.warn("La entidad Liquidaciones es nula, no se puede convertir a DTO de respuesta.");
            return null;
        }
        RespuestaLiquidacionesDTO respuesta = new RespuestaLiquidacionesDTO();
        respuesta.fechaSalida = entidad.getFechaSalida();
        respuesta.montoPreaviso = entidad.getMontoPreaviso();
        respuesta.montoCesantia = entidad.getMontoCesantia();
        respuesta.montoVacacionesPendientes = entidad.getMontoVacacionesPendientes();
        respuesta.montoAguinaldoPendiente = entidad.getMontoAguinaldoPendiente();
        respuesta.totalLiquidacion = entidad.getTotalLiquidacion();
        
        if(entidad.getMotivoSalida() != null){
            respuesta.motivoSalida = entidad.getMotivoSalida().name();
        }
        
        if(entidad.getEmpleado() != null){
            respuesta.nombreEmpleado = entidad.getEmpleado().getNombre();
            respuesta.primerApellidoEmpleado = entidad.getEmpleado().getPrimerApellido();
            respuesta.segundoApellidoEmpleado = entidad.getEmpleado().getSegundoApellido();
        }
        
        log.info("Se ha convertido la entidad Liquidaciones a DTO de respuesta: {}", respuesta);
        return respuesta;
    }

    public List<RespuestaLiquidacionesDTO> deListaEntidadADto(List<Liquidaciones> entidades) {
        return entidades.stream()
                .map(this::deEntidadDtoARespuesta)
                .toList();
    }
    
    private MotivoSalida obtenerMotivoSalida(String motivo) {
        try {
            return MotivoSalida.valueOf(motivo.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

}
