package com.anthony.tfg.tfg.Modulos.Auxiliares.Servicio;

import java.util.List;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaDireccionDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudDireccionDTO;
import com.anthony.tfg.tfg.Entidades.Direccion;
import com.anthony.tfg.tfg.Exceptions.ResourceNotFoundException;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasDirecciones;
import com.anthony.tfg.tfg.Modulos.Interfaces.ServicioInterface;
import com.anthony.tfg.tfg.Modulos.Mantenimientos.MantenimientosDirecciones;

import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para gestionar operaciones CRUD sobre direcciones.
 * Utiliza inyección de dependencias por constructor y logging parametrizado.
 */
@Service
@Slf4j
public class ServicioDireccion implements ServicioInterface<RespuestaDireccionDTO, 
                                                            SolicitudDireccionDTO, 
                                                            Direccion>{

    private final ConsultasDirecciones consulta;
    private final MantenimientosDirecciones mantenimiento;

    /**
     * Inicializa el servicio con sus dependencias principales.
     * @param consulta parametro de entrada de la operacion.
     * @param mantenimiento parametro de entrada de la operacion.
     */
    public ServicioDireccion(ConsultasDirecciones consulta, MantenimientosDirecciones mantenimiento) {
        this.consulta = consulta;
        this.mantenimiento = mantenimiento;
    }

    /**
     * Obtiene un registro por su identificador.
     * @param id parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public RespuestaDireccionDTO obtenerPorId(Long id) {
        var direccion = consulta.obtenerPorId(id);
        if(direccion == null){
            log.warn("No se encontró dirección con ID: {}", id);
            throw new ResourceNotFoundException("Direccion", "id", id);
        }
        log.debug("Dirección encontrada con ID: {}", id);
        return deEntidadDtoARespuesta(direccion);
    }

    /**
     * Obtiene todos los registros disponibles.
     * @return resultado de la operacion.
     */
    public List<RespuestaDireccionDTO> obtenerTodos() {
        var entidades = consulta.obtenerTodos();
        log.info("Se obtuvieron {} direcciones", entidades.size());
        return deListaEntidadADto(entidades);
    }

    /**
     * Guarda un nuevo registro.
     * @param entidad parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public RespuestaDireccionDTO guardar(SolicitudDireccionDTO entidad) {
        var nuevaDireccion = deSolicitudDtoAEntidad(entidad);
        var direccionGuardada = mantenimiento.crear(nuevaDireccion);
        log.info("Dirección guardada con ID: {}", direccionGuardada.getId());
        return deEntidadDtoARespuesta(direccionGuardada);
    }

    /**
     * Actualiza un registro existente.
     * @param id parametro de entrada de la operacion.
     * @param entidad parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public RespuestaDireccionDTO actualizar(Long id, SolicitudDireccionDTO entidad) {
        var direccionExistente = consulta.obtenerPorId(id);
        if(direccionExistente == null){
            log.warn("No se encontró dirección con ID: {} para actualizar", id);
            throw new ResourceNotFoundException("Direccion", "id", id);
        }
        direccionExistente.setProvincia(entidad.provincia());
        direccionExistente.setCanton(entidad.canton());
        direccionExistente.setDistrito(entidad.distrito());
        direccionExistente.setIndicaciones(entidad.indicaciones());
        var direccionActualizada = mantenimiento.actualizar(direccionExistente);
        log.info("Dirección actualizada con ID: {}", id);
        return deEntidadDtoARespuesta(direccionActualizada);
    }

    /**
     * Elimina un registro por su identificador.
     * @param id parametro de entrada de la operacion.
     */
    public void eliminar(Long id) {
        mantenimiento.eliminar(id);
        log.info("Dirección eliminada con ID: {}", id);
    }

    /**
     * Convierte un DTO de solicitud a entidad.
     * @param solicitud parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public Direccion deSolicitudDtoAEntidad(SolicitudDireccionDTO solicitud) {
        if(solicitud == null){
            log.warn("DTO de solicitud nulo, no se puede convertir a entidad Direccion");
            throw new IllegalArgumentException("La solicitud no puede ser nula");
        }
        return Direccion.builder()
                    .id(solicitud.id())
                    .provincia(solicitud.provincia())
                    .canton(solicitud.canton())
                    .distrito(solicitud.distrito())
                    .indicaciones(solicitud.indicaciones())
                    .build();
    }

    /**
     * Convierte una entidad a DTO de respuesta.
     * @param entidad parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public RespuestaDireccionDTO deEntidadDtoARespuesta(Direccion entidad) {
        if(entidad == null){
            log.warn("Entidad Direccion nula, no se puede convertir a DTO de respuesta");
            throw new IllegalArgumentException("La entidad no puede ser nula");
        }
        return new RespuestaDireccionDTO(
            entidad.getId(),
            entidad.getProvincia(),
            entidad.getCanton(),
            entidad.getDistrito(),
            entidad.getIndicaciones()
        );
    }

    /**
     * Convierte una lista de entidades a DTOs de respuesta.
     * @param entidades parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public List<RespuestaDireccionDTO> deListaEntidadADto(List<Direccion> entidades) {
        return entidades.stream()
                .map(this::deEntidadDtoARespuesta)
                .toList();
    }

}
