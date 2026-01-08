package com.anthony.tfg.tfg.Modulos.Incapacidad.Servicio;

import java.util.List;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaIncapacidadesDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudIncapacidadesDTO;
import com.anthony.tfg.tfg.Entidades.Empleados;
import com.anthony.tfg.tfg.Entidades.Incapacidades;
import com.anthony.tfg.tfg.Entidades.Enums.EstadoSolicitud;
import com.anthony.tfg.tfg.Entidades.Enums.TipoEntidadEmisora;
import com.anthony.tfg.tfg.Entidades.Enums.TipoIncapacidad;
import com.anthony.tfg.tfg.Exceptions.BadRequestException;
import com.anthony.tfg.tfg.Exceptions.ResourceNotFoundException;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasEmpleados;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasIncapacidades;
import com.anthony.tfg.tfg.Modulos.Interfaces.ServicioInterface;
import com.anthony.tfg.tfg.Modulos.Mantenimientos.MantenimientosIncapacidades;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ServicioIncapacidad implements ServicioInterface<RespuestaIncapacidadesDTO, 
                                                              SolicitudIncapacidadesDTO, 
                                                              Incapacidades> {

    private final ConsultasIncapacidades consulta;
    private final MantenimientosIncapacidades mantenimiento;
    private final ConsultasEmpleados consultasEmpleados;

    public ServicioIncapacidad(ConsultasIncapacidades consulta, 
                               MantenimientosIncapacidades mantenimiento, 
                               ConsultasEmpleados consultasEmpleados) {
        this.consulta = consulta;
        this.mantenimiento = mantenimiento;
        this.consultasEmpleados = consultasEmpleados;
    }

    public RespuestaIncapacidadesDTO obtenerPorId(Long id) {
        Incapacidades incapacidad = consulta.obtenerPorId(id);
        if (incapacidad == null) {
            log.warn("No se ha encontrado la incapacidad con ID: " + id);
            throw new ResourceNotFoundException("Incapacidades", "id", id);
        }
        log.info("Se ha encontrado la incapacidad con ID: " + id);
        return deEntidadDtoARespuesta(incapacidad);
    }

    public List<RespuestaIncapacidadesDTO> obtenerTodos() {
        List<Incapacidades> entidades = consulta.obtenerTodos();
        log.info("Se han obtenido todas las incapacidades. La cantidad de registros es: " + entidades.size());
        return deListaEntidadADto(entidades);
    }

    public RespuestaIncapacidadesDTO guardar(SolicitudIncapacidadesDTO entidad) {
        Incapacidades nuevaIncapacidad = deSolicitudDtoAEntidad(entidad);
        if (nuevaIncapacidad == null) {
            throw new BadRequestException("No se pudo procesar la solicitud de incapacidad");
        }
        Incapacidades incapacidadGuardada = mantenimiento.crear(nuevaIncapacidad);
        log.info("Se ha guardado una nueva incapacidad con ID: " + incapacidadGuardada.getId());
        return deEntidadDtoARespuesta(incapacidadGuardada);
    }

    public RespuestaIncapacidadesDTO actualizar(Long id, SolicitudIncapacidadesDTO entidad) {
        Incapacidades incapacidadExistente = consulta.obtenerPorId(id);
        if (incapacidadExistente == null) {
            log.warn("No se ha encontrado la incapacidad con ID: " + id + " para actualizar");
            throw new ResourceNotFoundException("Incapacidades", "id", id);
        }
        
        incapacidadExistente.setFechaInicio(entidad.fechaInicio);
        incapacidadExistente.setFechaFin(entidad.fechaFin);
        incapacidadExistente.setDiasTotales(entidad.diasTotales);
        incapacidadExistente.setPorcentajePago(entidad.porcentajePago);
        incapacidadExistente.setNumeroDocumento(entidad.numeroDocumento);
        incapacidadExistente.setObservaciones(entidad.observaciones);
        incapacidadExistente.setUrlDocumentoAdjunto(entidad.urlDocumentoAdjunto);
        
        TipoIncapacidad tipoIncapacidad = obtenerTipoIncapacidad(entidad.tipoIncapacidad);
        if (tipoIncapacidad != null) {
            incapacidadExistente.setTipoIncapacidad(tipoIncapacidad);
        }
        
        EstadoSolicitud estadoSolicitud = obtenerEstadoSolicitud(entidad.estadoSolicitud);
        if (estadoSolicitud != null) {
            incapacidadExistente.setEstadoSolicitud(estadoSolicitud);
        }
        
        TipoEntidadEmisora entidadEmisora = obtenerTipoEntidadEmisora(entidad.entidadEmisora);
        if (entidadEmisora != null) {
            incapacidadExistente.setEntidadEmisora(entidadEmisora);
        }
        
        Empleados empleado = consultasEmpleados.obtenerPorId(entidad.idEmpleado);
        if (empleado != null) {
            incapacidadExistente.setEmpleado(empleado);
        }
        
        Incapacidades incapacidadActualizada = mantenimiento.actualizar(incapacidadExistente);
        log.info("Se ha actualizado la incapacidad con ID: " + id);
        return deEntidadDtoARespuesta(incapacidadActualizada);
    }

    public void eliminar(Long id) {
        Incapacidades incapacidad = consulta.obtenerPorId(id);
        if (incapacidad == null) {
            throw new ResourceNotFoundException("Incapacidades", "id", id);
        }
        mantenimiento.eliminar(id);
        log.info("Se ha eliminado la incapacidad con ID: " + id);
    }

    public Incapacidades deSolicitudDtoAEntidad(SolicitudIncapacidadesDTO solicitud) {
        if (solicitud == null) {
            log.warn("El DTO de solicitud es nulo, no se puede convertir a entidad Incapacidades.");
            return null;
        }
        
        Empleados empleado = consultasEmpleados.obtenerPorId(solicitud.idEmpleado);
        if (empleado == null) {
            log.warn("No se ha encontrado el empleado con ID: " + solicitud.idEmpleado);
            throw new ResourceNotFoundException("Empleados", "id", solicitud.idEmpleado);
        }
        
        TipoIncapacidad tipoIncapacidad = obtenerTipoIncapacidad(solicitud.tipoIncapacidad);
        if (tipoIncapacidad == null) {
            log.warn("No se ha encontrado el tipo de incapacidad: " + solicitud.tipoIncapacidad);
            throw new BadRequestException("Tipo de incapacidad inválido: " + solicitud.tipoIncapacidad);
        }
        
        EstadoSolicitud estadoSolicitud = obtenerEstadoSolicitud(solicitud.estadoSolicitud);
        if (estadoSolicitud == null) {
            log.warn("No se ha encontrado el estado de solicitud: " + solicitud.estadoSolicitud);
            throw new BadRequestException("Estado de solicitud inválido: " + solicitud.estadoSolicitud);
        }
        
        TipoEntidadEmisora entidadEmisora = obtenerTipoEntidadEmisora(solicitud.entidadEmisora);
        if (entidadEmisora == null) {
            log.warn("No se ha encontrado la entidad emisora: " + solicitud.entidadEmisora);
            throw new BadRequestException("Entidad emisora inválida: " + solicitud.entidadEmisora);
        }
        
        Incapacidades incapacidad = Incapacidades.builder()
                .id(solicitud.id)
                .fechaInicio(solicitud.fechaInicio)
                .fechaFin(solicitud.fechaFin)
                .diasTotales(solicitud.diasTotales)
                .tipoIncapacidad(tipoIncapacidad)
                .estadoSolicitud(estadoSolicitud)
                .porcentajePago(solicitud.porcentajePago)
                .entidadEmisora(entidadEmisora)
                .numeroDocumento(solicitud.numeroDocumento)
                .observaciones(solicitud.observaciones)
                .urlDocumentoAdjunto(solicitud.urlDocumentoAdjunto)
                .empleado(empleado)
                .build();
        
        log.info("Se ha convertido el DTO de solicitud a entidad Incapacidades: {}", incapacidad);
        return incapacidad;
    }

    public RespuestaIncapacidadesDTO deEntidadDtoARespuesta(Incapacidades entidad) {
        if (entidad == null) {
            log.warn("La entidad Incapacidades es nula, no se puede convertir a DTO de respuesta.");
            return null;
        }
        
        RespuestaIncapacidadesDTO respuesta = new RespuestaIncapacidadesDTO();
        respuesta.id = entidad.getId();
        respuesta.fechaInicio = entidad.getFechaInicio();
        respuesta.fechaFin = entidad.getFechaFin();
        respuesta.diasTotales = entidad.getDiasTotales();
        respuesta.porcentajePago = entidad.getPorcentajePago();
        respuesta.numeroDocumento = entidad.getNumeroDocumento();
        respuesta.observaciones = entidad.getObservaciones();
        respuesta.urlDocumentoAdjunto = entidad.getUrlDocumentoAdjunto();
        
        if (entidad.getTipoIncapacidad() != null) {
            respuesta.tipoIncapacidad = entidad.getTipoIncapacidad().name();
        }
        
        if (entidad.getEstadoSolicitud() != null) {
            respuesta.estadoSolicitud = entidad.getEstadoSolicitud().name();
        }
        
        if (entidad.getEntidadEmisora() != null) {
            respuesta.entidadEmisora = entidad.getEntidadEmisora().name();
        }
        
        if (entidad.getEmpleado() != null) {
            respuesta.nombreEmpleado = entidad.getEmpleado().getNombre();
            respuesta.primerApellidoEmpleado = entidad.getEmpleado().getPrimerApellido();
            respuesta.segundoApellidoEmpleado = entidad.getEmpleado().getSegundoApellido();
        }
        
        log.info("Se ha convertido la entidad Incapacidades a DTO de respuesta: {}", respuesta);
        return respuesta;
    }

    public List<RespuestaIncapacidadesDTO> deListaEntidadADto(List<Incapacidades> entidades) {
        return entidades.stream()
                .map(this::deEntidadDtoARespuesta)
                .toList();
    }
    
    private TipoIncapacidad obtenerTipoIncapacidad(String tipo) {
        try {
            return TipoIncapacidad.valueOf(tipo.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }
    
    private EstadoSolicitud obtenerEstadoSolicitud(String estado) {
        try {
            return EstadoSolicitud.valueOf(estado.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }
    
    private TipoEntidadEmisora obtenerTipoEntidadEmisora(String entidad) {
        try {
            return TipoEntidadEmisora.valueOf(entidad.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }
}
