package com.anthony.tfg.tfg.Modulos.Extras.Servicio;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaHorasExtraDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudHorasExtraDTO;
import com.anthony.tfg.tfg.Entidades.Empleados;
import com.anthony.tfg.tfg.Entidades.HorasExtra;
import com.anthony.tfg.tfg.Entidades.Enums.EstadoSolicitud;
import com.anthony.tfg.tfg.Entidades.Enums.TipoTarifa;
import com.anthony.tfg.tfg.Exceptions.ResourceNotFoundException;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasEmpleados;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasHorasExtras;
import com.anthony.tfg.tfg.Modulos.Interfaces.ServicioInterface;
import com.anthony.tfg.tfg.Modulos.Mantenimientos.MantenimientosHorasExtras;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ServicioExtras implements ServicioInterface<RespuestaHorasExtraDTO, 
                                                        SolicitudHorasExtraDTO, 
                                                        HorasExtra>{

    private final ConsultasHorasExtras consulta;
    private final MantenimientosHorasExtras mantenimiento;
    private final ConsultasEmpleados consultasEmpleados;

    public ServicioExtras(ConsultasHorasExtras consulta, MantenimientosHorasExtras mantenimiento, ConsultasEmpleados consultasEmpleados) {
        this.consulta = consulta;
        this.mantenimiento = mantenimiento;
        this.consultasEmpleados = consultasEmpleados;
    }

    public RespuestaHorasExtraDTO obtenerPorId(Long id) {
        HorasExtra horaExtra = consulta.obtenerPorId(id);
        if(horaExtra == null){
            log.warn("No se ha encontrado la hora extra con ID: " + id);
            throw new ResourceNotFoundException("HorasExtra", "id", id);
        }
        log.info("Se ha encontrado la hora extra con ID: " + id);
        return deEntidadDtoARespuesta(horaExtra);
    }

    public Page<RespuestaHorasExtraDTO> obtenerTodos(Pageable pageable) {
        Page<HorasExtra> entidades = consulta.obtenerTodos(pageable);
        log.info("Se han obtenido todas las horas extra. La cantidad de registros es: " + entidades.getTotalElements());
        return entidades.map(this::deEntidadDtoARespuesta);
    }

    public RespuestaHorasExtraDTO guardar(SolicitudHorasExtraDTO entidad) {
        HorasExtra nuevaHoraExtra = deSolicitudDtoAEntidad(entidad);
        HorasExtra horaExtraGuardada = mantenimiento.crear(nuevaHoraExtra);
        log.info("Se ha guardado una nueva hora extra con ID: " + horaExtraGuardada.getId());
        return deEntidadDtoARespuesta(horaExtraGuardada);
    }

    public RespuestaHorasExtraDTO actualizar(Long id, SolicitudHorasExtraDTO entidad) {
        HorasExtra horaExtraExistente = consulta.obtenerPorId(id);
        if(horaExtraExistente == null){
            log.warn("No se ha encontrado la hora extra con ID: " + id + " para actualizar");
            return null;
        }
        horaExtraExistente.setFechaSolicitud(entidad.fechaSolicitud);
        horaExtraExistente.setCantidadDeHoras(entidad.cantidadDeHoras);
        horaExtraExistente.setMotivo(entidad.motivo);
        horaExtraExistente.setAprobado(entidad.aprobado);
        horaExtraExistente.setProcesado(entidad.procesado);
        
        EstadoSolicitud estadoSolicitud = obtenerEstadoSolicitud(entidad.estadoSolicitud);
        if(estadoSolicitud != null){
            horaExtraExistente.setEstadoSolicitud(estadoSolicitud);
        }
        
        TipoTarifa tipoTarifa = obtenerTipoTarifa(entidad.tipoTarifa);
        if(tipoTarifa != null){
            horaExtraExistente.setTipoTarifa(tipoTarifa);
        }
        
        Empleados empleado = consultasEmpleados.obtenerPorId(entidad.idEmpleado);
        if(empleado != null){
            horaExtraExistente.setEmpleado(empleado);
        }
        
        HorasExtra horaExtraActualizada = mantenimiento.actualizar(horaExtraExistente);
        log.info("Se ha actualizado la hora extra con ID: " + id);
        return deEntidadDtoARespuesta(horaExtraActualizada);
    }

    public void eliminar(Long id) {
        mantenimiento.eliminar(id);
        log.info("Se ha eliminado la hora extra con ID: " + id);
    }

    public HorasExtra deSolicitudDtoAEntidad(SolicitudHorasExtraDTO solicitud) {
        if(solicitud == null){
            log.warn("El DTO de solicitud es nulo, no se puede convertir a entidad HorasExtra.");
            return null;
        }
        
        Empleados empleado = consultasEmpleados.obtenerPorId(solicitud.idEmpleado);
        if(empleado == null){
            log.warn("No se ha encontrado el empleado con ID: " + solicitud.idEmpleado);
            return null;
        }
        
        EstadoSolicitud estadoSolicitud = obtenerEstadoSolicitud(solicitud.estadoSolicitud);
        if(estadoSolicitud == null){
            log.warn("No se ha encontrado el estado de solicitud: " + solicitud.estadoSolicitud);
            return null;
        }
        
        TipoTarifa tipoTarifa = obtenerTipoTarifa(solicitud.tipoTarifa);
        if(tipoTarifa == null){
            log.warn("No se ha encontrado el tipo de tarifa: " + solicitud.tipoTarifa);
            return null;
        }
        
        HorasExtra horaExtra = HorasExtra.builder()
                    .id(solicitud.id)
                    .fechaSolicitud(solicitud.fechaSolicitud)
                    .cantidadDeHoras(solicitud.cantidadDeHoras)
                    .motivo(solicitud.motivo)
                    .aprobado(solicitud.aprobado)
                    .procesado(solicitud.procesado)
                    .estadoSolicitud(estadoSolicitud)
                    .tipoTarifa(tipoTarifa)
                    .empleado(empleado)
                    .build();
        log.info("Se ha convertido el DTO de solicitud a entidad HorasExtra: {}", horaExtra);
        return horaExtra;
    }

    public RespuestaHorasExtraDTO deEntidadDtoARespuesta(HorasExtra entidad) {
        if(entidad == null){
            log.warn("La entidad HorasExtra es nula, no se puede convertir a DTO de respuesta.");
            return null;
        }
        RespuestaHorasExtraDTO respuesta = new RespuestaHorasExtraDTO();
        respuesta.id = entidad.getId();
        respuesta.fechaSolicitud = entidad.getFechaSolicitud();
        respuesta.cantidadDeHoras = entidad.getCantidadDeHoras();
        respuesta.motivo = entidad.getMotivo();
        respuesta.aprobado = entidad.getAprobado();
        respuesta.procesado = entidad.getProcesado();
        
        if(entidad.getEstadoSolicitud() != null){
            respuesta.estadoSolicitud = entidad.getEstadoSolicitud().name();
        }
        
        if(entidad.getTipoTarifa() != null){
            respuesta.tipoTarifa = entidad.getTipoTarifa().name();
        }
        
        if(entidad.getEmpleado() != null){
            respuesta.nombreEmpleado = entidad.getEmpleado().getNombre();
            respuesta.primerApellidoEmpleado = entidad.getEmpleado().getPrimerApellido();
            respuesta.segundoApellidoEmpleado = entidad.getEmpleado().getSegundoApellido();
        }
        
        log.info("Se ha convertido la entidad HorasExtra a DTO de respuesta: {}", respuesta);
        return respuesta;
    }

    public List<RespuestaHorasExtraDTO> deListaEntidadADto(List<HorasExtra> entidades) {
        return entidades.stream()
                .map(this::deEntidadDtoARespuesta)
                .toList();
    }
    
    private EstadoSolicitud obtenerEstadoSolicitud(String estado) {
        try {
            return EstadoSolicitud.valueOf(estado.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }
    
    private TipoTarifa obtenerTipoTarifa(String tipo) {
        try {
            return TipoTarifa.valueOf(tipo.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

}
