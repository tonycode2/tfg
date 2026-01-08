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

    public ServicioAsistencia(ConsultasAsistencias consulta, MantenimientosAsistencia mantenimiento, ConsultasEmpleados consultasEmpleados) {
        this.consulta = consulta;
        this.mantenimiento = mantenimiento;
        this.consultasEmpleados = consultasEmpleados;
    }

    public RespuestaAsistenciaDTO obtenerPorId(Long id) {
        Asistencia asistencia = consulta.obtenerPorId(id);
        if(asistencia == null){
            log.warn("No se ha encontrado la asistencia con ID: " + id);
            throw new ResourceNotFoundException("Asistencia", "id", id);
        }
        log.info("Se ha encontrado la asistencia con ID: " + id);
        return deEntidadDtoARespuesta(asistencia);
    }

    public List<RespuestaAsistenciaDTO> obtenerTodos() {
        List<Asistencia> entidades = consulta.obtenerTodos();
        log.info("Se han obtenido todas las asistencias. La cantidad de registros es: " + entidades.size());
        return deListaEntidadADto(entidades);
    }

    public RespuestaAsistenciaDTO guardar(SolicitudAsistenciaDTO entidad) {
        Asistencia nuevaAsistencia = deSolicitudDtoAEntidad(entidad);
        Asistencia asistenciaGuardada = mantenimiento.crear(nuevaAsistencia);
        log.info("Se ha guardado una nueva asistencia con ID: " + asistenciaGuardada.getId());
        return deEntidadDtoARespuesta(asistenciaGuardada);
    }

    public RespuestaAsistenciaDTO actualizar(Long id, SolicitudAsistenciaDTO entidad) {
        Asistencia asistenciaExistente = consulta.obtenerPorId(id);
        if(asistenciaExistente == null){
            log.warn("No se ha encontrado la asistencia con ID: " + id + " para actualizar");
            throw new ResourceNotFoundException("Asistencia", "id", id);
        }
        asistenciaExistente.setTipoEvento(entidad.tipoEvento);
        asistenciaExistente.setFechaHora(entidad.fechaHora);
        asistenciaExistente.setObservaciones(entidad.observaciones);
        
        Empleados empleado = consultasEmpleados.obtenerPorId(entidad.idEmpleado);
        if(empleado != null){
            asistenciaExistente.setEmpleado(empleado);
        }
        
        Asistencia asistenciaActualizada = mantenimiento.actualizar(asistenciaExistente);
        log.info("Se ha actualizado la asistencia con ID: " + id);
        return deEntidadDtoARespuesta(asistenciaActualizada);
    }

    public void eliminar(Long id) {
        mantenimiento.eliminar(id);
        log.info("Se ha eliminado la asistencia con ID: " + id);
    }

    public Asistencia deSolicitudDtoAEntidad(SolicitudAsistenciaDTO solicitud) {
        if(solicitud == null){
            log.warn("El DTO de solicitud es nulo, no se puede convertir a entidad Asistencia.");
            return null;
        }
        
        Empleados empleado = consultasEmpleados.obtenerPorId(solicitud.idEmpleado);
        if(empleado == null){
            log.warn("No se ha encontrado el empleado con ID: " + solicitud.idEmpleado);
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

    public List<RespuestaAsistenciaDTO> deListaEntidadADto(List<Asistencia> entidades) {
        return entidades.stream()
                .map(this::deEntidadDtoARespuesta)
                .toList();
    }

}
