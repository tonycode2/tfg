package com.anthony.tfg.tfg.Modulos.Auxiliares.Servicio;

import java.util.List;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaDepartamentoDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudDepartamentoDTO;
import com.anthony.tfg.tfg.Entidades.Departamento;
import com.anthony.tfg.tfg.Exceptions.ResourceNotFoundException;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasDepartamentos;
import com.anthony.tfg.tfg.Modulos.Interfaces.ServicioInterface;
import com.anthony.tfg.tfg.Modulos.Mantenimientos.MantenimientosDepartamentos;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ServicioDepartamento implements ServicioInterface<RespuestaDepartamentoDTO, 
                                                                SolicitudDepartamentoDTO, 
                                                                Departamento>{

    private final ConsultasDepartamentos consulta;
    private final MantenimientosDepartamentos mantenimiento;

    public ServicioDepartamento(ConsultasDepartamentos consulta, MantenimientosDepartamentos mantenimiento) {
        this.consulta = consulta;
        this.mantenimiento = mantenimiento;
    }

    public RespuestaDepartamentoDTO obtenerPorId(Long id) {
        Departamento departamento = consulta.obtenerPorId(id);
        if(departamento == null){
            log.warn("No se ha encontrado el departamento con ID: " + id);
            throw new ResourceNotFoundException("Departamento", "id", id);
        }
        log.info("Se ha encontrado el departamento con ID: " + id);
        return deEntidadDtoARespuesta(departamento);
    }

    public List<RespuestaDepartamentoDTO> obtenerTodos() {
        List<Departamento> entidades = consulta.obtenerTodos();
        log.info("Se han obtenido todos los departamentos. La cantidad de registros es: " + entidades.size());
        return deListaEntidadADto(entidades);
    }

    public RespuestaDepartamentoDTO guardar(SolicitudDepartamentoDTO entidad) {
        Departamento nuevoDepartamento = deSolicitudDtoAEntidad(entidad);
        Departamento departamentoGuardado = mantenimiento.crear(nuevoDepartamento);
        log.info("Se ha guardado un nuevo departamento con ID: " + departamentoGuardado.getId());
        return deEntidadDtoARespuesta(departamentoGuardado);
    }

    public RespuestaDepartamentoDTO actualizar(Long id, SolicitudDepartamentoDTO entidad) {
        Departamento departamentoExistente = consulta.obtenerPorId(id);
        if(departamentoExistente == null){
            log.warn("No se ha encontrado el departamento con ID: " + id + " para actualizar");
            throw new ResourceNotFoundException("Departamento", "id", id);
        }
        departamentoExistente.setNombre(entidad.nombre);
        Departamento departamentoActualizado = mantenimiento.actualizar(departamentoExistente);
        log.info("Se ha actualizado el departamento con ID: " + id);
        return deEntidadDtoARespuesta(departamentoActualizado);
    }

    public void eliminar(Long id) {
        mantenimiento.eliminar(id);
        log.info("Se ha eliminado el departamento con ID: " + id);
    }

    public Departamento deSolicitudDtoAEntidad(SolicitudDepartamentoDTO solicitud) {
        if (solicitud == null) {
            log.warn("El DTO de solicitud es nulo, no se puede convertir a entidad Departamento.");
            return null;
        }
        Departamento departamento = Departamento.builder().
                                        id(solicitud.id)
                                        .nombre(solicitud.nombre)
                                        .build();
        log.info("Se ha convertido el DTO de solicitud a entidad Departamento: {}", departamento);
        return departamento;
    }

    public RespuestaDepartamentoDTO deEntidadDtoARespuesta(Departamento entidad) {
        if (entidad == null) {
            log.warn("La entidad Departamento es nula, no se puede convertir a DTO de respuesta.");
            return null;
        }
        RespuestaDepartamentoDTO respuesta = new RespuestaDepartamentoDTO();
        respuesta.id = entidad.getId();
        respuesta.nombre = entidad.getNombre();
        log.info("Se ha convertido la entidad Departamento a DTO de respuesta: {}", respuesta);
        return respuesta;
    }

    public List<RespuestaDepartamentoDTO> deListaEntidadADto(List<Departamento> entidades) {
        return entidades.stream()
                        .map(this::deEntidadDtoARespuesta)
                        .toList();
    }

}
