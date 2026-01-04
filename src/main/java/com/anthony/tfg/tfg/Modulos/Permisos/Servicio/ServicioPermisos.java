package com.anthony.tfg.tfg.Modulos.Permisos.Servicio;

import java.util.List;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaPermisosDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudPermisosDTO;
import com.anthony.tfg.tfg.Entidades.Empleados;
import com.anthony.tfg.tfg.Entidades.Permisos;
import com.anthony.tfg.tfg.Entidades.Enums.EstadoSolicitud;
import com.anthony.tfg.tfg.Entidades.Enums.TipoPermiso;
import com.anthony.tfg.tfg.Exceptions.ResourceNotFoundException;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasEmpleados;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasPermisos;
import com.anthony.tfg.tfg.Modulos.Interfaces.ServicioInterface;
import com.anthony.tfg.tfg.Modulos.Mantenimientos.MantenimientosPermisos;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ServicioPermisos implements ServicioInterface<RespuestaPermisosDTO, 
                                                        SolicitudPermisosDTO, 
                                                        Permisos>{

    private final ConsultasPermisos consulta;
    private final MantenimientosPermisos mantenimiento;
    private final ConsultasEmpleados consultasEmpleados;

    public ServicioPermisos(ConsultasPermisos consulta, MantenimientosPermisos mantenimiento, ConsultasEmpleados consultasEmpleados) {
        this.consulta = consulta;
        this.mantenimiento = mantenimiento;
        this.consultasEmpleados = consultasEmpleados;
    }

    public RespuestaPermisosDTO obtenerPorId(Long id) {
        Permisos permiso = consulta.obtenerPorId(id);
        if(permiso == null){
            log.warn("No se ha encontrado el permiso con ID: " + id);
            throw new ResourceNotFoundException("Permisos", "id", id);
        }
        log.info("Se ha encontrado el permiso con ID: " + id);
        return deEntidadDtoARespuesta(permiso);
    }

    public List<RespuestaPermisosDTO> obtenerTodos() {
        List<Permisos> entidades = consulta.obtenerTodos();
        log.info("Se han obtenido todos los permisos. La cantidad de registros es: " + entidades.size());
        return deListaEntidadADto(entidades);
    }

    public RespuestaPermisosDTO guardar(SolicitudPermisosDTO entidad) {
        Permisos nuevoPermiso = deSolicitudDtoAEntidad(entidad);
        Permisos permisoGuardado = mantenimiento.crear(nuevoPermiso);
        log.info("Se ha guardado un nuevo permiso con ID: " + permisoGuardado.getId());
        return deEntidadDtoARespuesta(permisoGuardado);
    }

    public RespuestaPermisosDTO actualizar(Long id, SolicitudPermisosDTO entidad) {
        Permisos permisoExistente = consulta.obtenerPorId(id);
        if(permisoExistente == null){
            log.warn("No se ha encontrado el permiso con ID: " + id + " para actualizar");
            return null;
        }
        permisoExistente.setFechaInicio(entidad.fechaInicio);
        permisoExistente.setFechaFin(entidad.fechaFin);
        permisoExistente.setDiasTotales(entidad.diasTotales);
        permisoExistente.setMotivo(entidad.motivo);
        permisoExistente.setUrlDocumentoAdjunto(entidad.urlDocumentoAdjunto);
        
        EstadoSolicitud estadoSolicitud = obtenerEstadoSolicitud(entidad.estadoSolicitud);
        if(estadoSolicitud != null){
            permisoExistente.setEstadoSolicitud(estadoSolicitud);
        }
        
        TipoPermiso tipoPermiso = obtenerTipoPermiso(entidad.tipoPermiso);
        if(tipoPermiso != null){
            permisoExistente.setTipoPermiso(tipoPermiso);
        }
        
        Empleados empleado = consultasEmpleados.obtenerPorId(entidad.idEmpleado);
        if(empleado != null){
            permisoExistente.setEmpleado(empleado);
        }
        
        Permisos permisoActualizado = mantenimiento.actualizar(permisoExistente);
        log.info("Se ha actualizado el permiso con ID: " + id);
        return deEntidadDtoARespuesta(permisoActualizado);
    }

    public void eliminar(Long id) {
        mantenimiento.eliminar(id);
        log.info("Se ha eliminado el permiso con ID: " + id);
    }

    public Permisos deSolicitudDtoAEntidad(SolicitudPermisosDTO solicitud) {
        if(solicitud == null){
            log.warn("El DTO de solicitud es nulo, no se puede convertir a entidad Permisos.");
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
        
        TipoPermiso tipoPermiso = obtenerTipoPermiso(solicitud.tipoPermiso);
        if(tipoPermiso == null){
            log.warn("No se ha encontrado el tipo de permiso: " + solicitud.tipoPermiso);
            return null;
        }
        
        Permisos permiso = Permisos.builder()
                    .id(solicitud.id)
                    .fechaInicio(solicitud.fechaInicio)
                    .fechaFin(solicitud.fechaFin)
                    .diasTotales(solicitud.diasTotales)
                    .motivo(solicitud.motivo)
                    .urlDocumentoAdjunto(solicitud.urlDocumentoAdjunto)
                    .estadoSolicitud(estadoSolicitud)
                    .tipoPermiso(tipoPermiso)
                    .empleado(empleado)
                    .build();
        log.info("Se ha convertido el DTO de solicitud a entidad Permisos: {}", permiso);
        return permiso;
    }

    public RespuestaPermisosDTO deEntidadDtoARespuesta(Permisos entidad) {
        if(entidad == null){
            log.warn("La entidad Permisos es nula, no se puede convertir a DTO de respuesta.");
            return null;
        }
        RespuestaPermisosDTO respuesta = new RespuestaPermisosDTO();
        respuesta.fechaInicio = entidad.getFechaInicio();
        respuesta.fechaFin = entidad.getFechaFin();
        respuesta.diasTotales = entidad.getDiasTotales();
        respuesta.motivo = entidad.getMotivo();
        respuesta.urlDocumentoAdjunto = entidad.getUrlDocumentoAdjunto();
        
        if(entidad.getEstadoSolicitud() != null){
            respuesta.estadoSolicitud = entidad.getEstadoSolicitud().name();
        }
        
        if(entidad.getTipoPermiso() != null){
            respuesta.tipoPermiso = entidad.getTipoPermiso().name();
        }
        
        if(entidad.getEmpleado() != null){
            respuesta.nombreEmpleado = entidad.getEmpleado().getNombre();
            respuesta.primerApellidoEmpleado = entidad.getEmpleado().getPrimerApellido();
            respuesta.segundApellidoEmpleado = entidad.getEmpleado().getSegundoApellido();
        }
        
        log.info("Se ha convertido la entidad Permisos a DTO de respuesta: {}", respuesta);
        return respuesta;
    }

    public List<RespuestaPermisosDTO> deListaEntidadADto(List<Permisos> entidades) {
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
    
    private TipoPermiso obtenerTipoPermiso(String tipo) {
        try {
            return TipoPermiso.valueOf(tipo.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

}
