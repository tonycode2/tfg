package com.anthony.tfg.tfg.Modulos.JefesDepartamento.Servicio;

import java.util.List;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaJefesDepartamentoDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudJefesDepartamentoDTO;
import com.anthony.tfg.tfg.Entidades.Departamento;
import com.anthony.tfg.tfg.Entidades.Empleados;
import com.anthony.tfg.tfg.Entidades.JefesDepartamento;
import com.anthony.tfg.tfg.Exceptions.ResourceNotFoundException;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasDepartamentos;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasEmpleados;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasJefesDepartamento;
import com.anthony.tfg.tfg.Modulos.Interfaces.ServicioInterface;
import com.anthony.tfg.tfg.Modulos.Mantenimientos.MantenimientosJefesDepartamento;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ServicioJefesDepartamento implements ServicioInterface<RespuestaJefesDepartamentoDTO, 
                                                                      SolicitudJefesDepartamentoDTO, 
                                                                      JefesDepartamento>{

    private final ConsultasJefesDepartamento consulta;
    private final MantenimientosJefesDepartamento mantenimiento;
    private final ConsultasDepartamentos consultasDepartamentos;
    private final ConsultasEmpleados consultasEmpleados;

    public ServicioJefesDepartamento(ConsultasJefesDepartamento consulta, 
                                     MantenimientosJefesDepartamento mantenimiento,
                                     ConsultasDepartamentos consultasDepartamentos,
                                     ConsultasEmpleados consultasEmpleados) {
        this.consulta = consulta;
        this.mantenimiento = mantenimiento;
        this.consultasDepartamentos = consultasDepartamentos;
        this.consultasEmpleados = consultasEmpleados;
    }

    /**
     * Obtiene un registro por su identificador.
     * @param id parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public RespuestaJefesDepartamentoDTO obtenerPorId(Long id) {
        JefesDepartamento jefe = consulta.obtenerPorId(id);
        if(jefe == null){
            log.warn("No se ha encontrado el jefe de departamento con ID: " + id);
            throw new ResourceNotFoundException("JefesDepartamento", "id", id);
        }
        log.info("Se ha encontrado el jefe de departamento con ID: " + id);
        return deEntidadDtoARespuesta(jefe);
    }

    /**
     * Obtiene todos los registros disponibles.
     * @return resultado de la operacion.
     */
    public List<RespuestaJefesDepartamentoDTO> obtenerTodos() {
        List<JefesDepartamento> entidades = consulta.obtenerTodos();
        log.info("Se han obtenido todos los jefes de departamento. La cantidad de registros es: " + entidades.size());
        return deListaEntidadADto(entidades);
    }

    /**
     * Guarda un nuevo registro.
     * @param entidad parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public RespuestaJefesDepartamentoDTO guardar(SolicitudJefesDepartamentoDTO entidad) {
        JefesDepartamento nuevoJefe = deSolicitudDtoAEntidad(entidad);
        JefesDepartamento jefeGuardado = mantenimiento.crear(nuevoJefe);
        log.info("Se ha guardado un nuevo jefe de departamento con ID: " + jefeGuardado.getId());
        return deEntidadDtoARespuesta(jefeGuardado);
    }

    /**
     * Actualiza un registro existente.
     * @param id parametro de entrada de la operacion.
     * @param entidad parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public RespuestaJefesDepartamentoDTO actualizar(Long id, SolicitudJefesDepartamentoDTO entidad) {
        JefesDepartamento jefeExistente = consulta.obtenerPorId(id);
        if(jefeExistente == null){
            log.warn("No se ha encontrado el jefe de departamento con ID: " + id + " para actualizar");
            throw new ResourceNotFoundException("JefesDepartamento", "id", id);
        }
        
        Departamento departamento = consultasDepartamentos.obtenerPorId(entidad.getIdDepartamento());
        if(departamento != null){
            jefeExistente.setDepartamento(departamento);
        }
        
        Empleados empleado = consultasEmpleados.obtenerPorId(entidad.getIdEmpleado());
        if(empleado != null){
            jefeExistente.setEmpleado(empleado);
        }
        
        jefeExistente.setFechaInicio(entidad.getFechaInicio());
        jefeExistente.setFechaFin(entidad.getFechaFin());
        jefeExistente.setEstaActivo(entidad.getEstaActivo());
        
        JefesDepartamento jefeActualizado = mantenimiento.actualizar(jefeExistente);
        log.info("Se ha actualizado el jefe de departamento con ID: " + id);
        return deEntidadDtoARespuesta(jefeActualizado);
    }

    /**
     * Elimina un registro por su identificador.
     * @param id parametro de entrada de la operacion.
     */
    public void eliminar(Long id) {
        mantenimiento.eliminar(id);
        log.info("Se ha eliminado el jefe de departamento con ID: " + id);
    }

    /**
     * Convierte un DTO de solicitud a entidad.
     * @param solicitud parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public JefesDepartamento deSolicitudDtoAEntidad(SolicitudJefesDepartamentoDTO solicitud) {
        if(solicitud == null){
            log.warn("El DTO de solicitud es nulo, no se puede convertir a entidad JefesDepartamento.");
            return null;
        }
        
        Departamento departamento = consultasDepartamentos.obtenerPorId(solicitud.getIdDepartamento());
        if(departamento == null){
            log.warn("No se ha encontrado el departamento con ID: " + solicitud.getIdDepartamento());
            return null;
        }
        
        Empleados empleado = consultasEmpleados.obtenerPorId(solicitud.getIdEmpleado());
        if(empleado == null){
            log.warn("No se ha encontrado el empleado con ID: " + solicitud.getIdEmpleado());
            return null;
        }
        
        JefesDepartamento jefe = JefesDepartamento.builder()
                    .id(solicitud.getId())
                    .departamento(departamento)
                    .empleado(empleado)
                    .fechaInicio(solicitud.getFechaInicio())
                    .fechaFin(solicitud.getFechaFin())
                    .estaActivo(solicitud.getEstaActivo())
                    .build();
        log.info("Se ha convertido el DTO de solicitud a entidad JefesDepartamento: {}", jefe);
        return jefe;
    }

    /**
     * Convierte una entidad a DTO de respuesta.
     * @param entidad parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public RespuestaJefesDepartamentoDTO deEntidadDtoARespuesta(JefesDepartamento entidad) {
        if(entidad == null){
            log.warn("La entidad JefesDepartamento es nula, no se puede convertir a DTO de respuesta.");
            return null;
        }
        RespuestaJefesDepartamentoDTO respuesta = new RespuestaJefesDepartamentoDTO();
        respuesta.id = entidad.getId();
        respuesta.fechaInicio = entidad.getFechaInicio();
        respuesta.fechaFin = entidad.getFechaFin();
        respuesta.estaActivo = entidad.getEstaActivo();
        
        if(entidad.getDepartamento() != null){
            respuesta.idDepartamento = entidad.getDepartamento().getId();
            respuesta.nombreDepartamento = entidad.getDepartamento().getNombre();
        }
        
        if(entidad.getEmpleado() != null){
            respuesta.idEmpleado = entidad.getEmpleado().getId();
            respuesta.nombreEmpleado = entidad.getEmpleado().getNombre();
            respuesta.primerApellidoEmpleado = entidad.getEmpleado().getPrimerApellido();
            respuesta.segundoApellidoEmpleado = entidad.getEmpleado().getSegundoApellido();
        }
        
        log.info("Se ha convertido la entidad JefesDepartamento a DTO de respuesta: {}", respuesta);
        return respuesta;
    }

    /**
     * Convierte una lista de entidades a DTOs de respuesta.
     * @param entidades parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public List<RespuestaJefesDepartamentoDTO> deListaEntidadADto(List<JefesDepartamento> entidades) {
        return entidades.stream()
                .map(this::deEntidadDtoARespuesta)
                .toList();
    }
}
