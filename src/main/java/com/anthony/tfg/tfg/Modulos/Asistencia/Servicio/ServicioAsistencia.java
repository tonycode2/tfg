package com.anthony.tfg.tfg.Modulos.Asistencia.Servicio;

import java.util.List;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaAsistenciaDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudAsistenciaDTO;
import com.anthony.tfg.tfg.Entidades.Asistencia;
import com.anthony.tfg.tfg.Entidades.Empleados;
import com.anthony.tfg.tfg.Exceptions.ResourceNotFoundException;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasAsistencias;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasEmpleados;
import com.anthony.tfg.tfg.Modulos.Interfaces.ServicioInterface;
import com.anthony.tfg.tfg.Modulos.Mantenimientos.MantenimientosAsistencia;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ServicioAsistencia implements ServicioInterface<RespuestaAsistenciaDTO, 
                                                            SolicitudAsistenciaDTO, 
                                                            Asistencia>{

    private final ConsultasAsistencias consulta;
    private final MantenimientosAsistencia mantenimiento;
    private final ConsultasEmpleados consultasEmpleados;

    /**
     * Inicializa el servicio con sus dependencias principales.
     * @param consulta parametro de entrada de la operacion.
     * @param mantenimiento parametro de entrada de la operacion.
     * @param consultasEmpleados parametro de entrada de la operacion.
     */
    public ServicioAsistencia(ConsultasAsistencias consulta, MantenimientosAsistencia mantenimiento, ConsultasEmpleados consultasEmpleados) {
        this.consulta = consulta;
        this.mantenimiento = mantenimiento;
        this.consultasEmpleados = consultasEmpleados;
    }

    /**
     * Obtiene un registro por su identificador.
     * @param id parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public RespuestaAsistenciaDTO obtenerPorId(Long id) {
        Asistencia asistencia = consulta.obtenerPorId(id);
        if(asistencia == null){
            log.warn("No se ha encontrado la asistencia con ID: " + id);
            throw new ResourceNotFoundException("Asistencia", "id", id);
        }
        log.info("Se ha encontrado la asistencia con ID: " + id);
        return deEntidadDtoARespuesta(asistencia);
    }

    /**
     * Obtiene todos los registros disponibles.
     * @return resultado de la operacion.
     */
    public List<RespuestaAsistenciaDTO> obtenerTodos() {
        List<Asistencia> entidades = consulta.obtenerTodos();
        log.info("Se han obtenido todas las asistencias. La cantidad de registros es: " + entidades.size());
        return deListaEntidadADto(entidades);
    }

    /**
     * Guarda un nuevo registro.
     * @param entidad parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public RespuestaAsistenciaDTO guardar(SolicitudAsistenciaDTO entidad) {
        Asistencia nuevaAsistencia = deSolicitudDtoAEntidad(entidad);
        Asistencia asistenciaGuardada = mantenimiento.crear(nuevaAsistencia);
        log.info("Se ha guardado una nueva asistencia con ID: " + asistenciaGuardada.getId());
        return deEntidadDtoARespuesta(asistenciaGuardada);
    }

    /**
     * Actualiza un registro existente.
     * @param id parametro de entrada de la operacion.
     * @param entidad parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public RespuestaAsistenciaDTO actualizar(Long id, SolicitudAsistenciaDTO entidad) {
        Asistencia asistenciaExistente = consulta.obtenerPorId(id);
        if(asistenciaExistente == null){
            log.warn("No se ha encontrado la asistencia con ID: " + id + " para actualizar");
            throw new ResourceNotFoundException("Asistencia", "id", id);
        }
        asistenciaExistente.setTipoEvento(entidad.getTipoEvento());
        asistenciaExistente.setFechaHora(entidad.getFechaHora());
        asistenciaExistente.setObservaciones(entidad.getObservaciones());
        
        Empleados empleado = consultasEmpleados.obtenerPorId(entidad.getIdEmpleado());
        if(empleado != null){
            asistenciaExistente.setEmpleado(empleado);
        }
        
        Asistencia asistenciaActualizada = mantenimiento.actualizar(asistenciaExistente);
        log.info("Se ha actualizado la asistencia con ID: " + id);
        return deEntidadDtoARespuesta(asistenciaActualizada);
    }

    /**
     * Elimina un registro por su identificador.
     * @param id parametro de entrada de la operacion.
     */
    public void eliminar(Long id) {
        mantenimiento.eliminar(id);
        log.info("Se ha eliminado la asistencia con ID: " + id);
    }

    /**
     * Convierte un DTO de solicitud a entidad.
     * @param solicitud parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public Asistencia deSolicitudDtoAEntidad(SolicitudAsistenciaDTO solicitud) {
        if(solicitud == null){
            log.warn("El DTO de solicitud es nulo, no se puede convertir a entidad Asistencia.");
            return null;
        }
        
        Empleados empleado = consultasEmpleados.obtenerPorId(solicitud.getIdEmpleado());
        if(empleado == null){
            log.warn("No se ha encontrado el empleado con ID: " + solicitud.getIdEmpleado());
            return null;
        }
        
        Asistencia asistencia = Asistencia.builder()
                    .id(solicitud.id)
                    .tipoEvento(solicitud.tipoEvento)
                    .fechaHora(solicitud.fechaHora)
                    .observaciones(solicitud.observaciones)
                    .empleado(empleado)
                    .build();
        log.info("Se ha convertido el DTO de solicitud a entidad Asistencia: {}", asistencia);
        return asistencia;
    }

    /**
     * Convierte una entidad a DTO de respuesta.
     * @param entidad parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public RespuestaAsistenciaDTO deEntidadDtoARespuesta(Asistencia entidad) {
        if(entidad == null){
            log.warn("La entidad Asistencia es nula, no se puede convertir a DTO de respuesta.");
            return null;
        }
        RespuestaAsistenciaDTO respuesta = new RespuestaAsistenciaDTO();
        respuesta.id = entidad.getId();
        respuesta.tipoEvento = entidad.getTipoEvento();
        respuesta.fechaHora = entidad.getFechaHora();
        respuesta.observaciones = entidad.getObservaciones();
        
        if(entidad.getEmpleado() != null){
            respuesta.nombreEmpleado = entidad.getEmpleado().getNombre();
            respuesta.primerApellidoEmpleado = entidad.getEmpleado().getPrimerApellido();
            respuesta.segundoApellidoEmpleado = entidad.getEmpleado().getSegundoApellido();
        }
        
        log.info("Se ha convertido la entidad Asistencia a DTO de respuesta: {}", respuesta);
        return respuesta;
    }

    /**
     * Convierte una lista de entidades a DTOs de respuesta.
     * @param entidades parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public List<RespuestaAsistenciaDTO> deListaEntidadADto(List<Asistencia> entidades) {
        return entidades.stream()
                .map(this::deEntidadDtoARespuesta)
                .toList();
    }

}
