package com.anthony.tfg.tfg.Modulos.Auxiliares.Servicio;

import java.util.List;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaConfiguracionRentaDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudConfiguracionRentaDTO;
import com.anthony.tfg.tfg.Entidades.ConfiguracionRenta;
import com.anthony.tfg.tfg.Exceptions.ResourceNotFoundException;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasConfiguracionRentas;
import com.anthony.tfg.tfg.Modulos.Interfaces.ServicioInterface;
import com.anthony.tfg.tfg.Modulos.Mantenimientos.MantenimientosConfiguracionRenta;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ServicioConfiguracionRenta implements ServicioInterface<RespuestaConfiguracionRentaDTO, 
                                                                    SolicitudConfiguracionRentaDTO, 
                                                                    ConfiguracionRenta>{

    private final ConsultasConfiguracionRentas consulta;
    private final MantenimientosConfiguracionRenta mantenimiento;

    /**
     * Inicializa el servicio con sus dependencias principales.
     * @param consulta parametro de entrada de la operacion.
     * @param mantenimiento parametro de entrada de la operacion.
     */
    public ServicioConfiguracionRenta(ConsultasConfiguracionRentas consulta, MantenimientosConfiguracionRenta mantenimiento) {
        this.consulta = consulta;
        this.mantenimiento = mantenimiento;
    }

    /**
     * Obtiene un registro por su identificador.
     * @param id parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public RespuestaConfiguracionRentaDTO obtenerPorId(Long id) {
        ConfiguracionRenta configuracionRenta = consulta.obtenerPorId(id);
        if(configuracionRenta == null){
            log.warn("No se ha encontrado la configuración de renta con ID: " + id);
            throw new ResourceNotFoundException("ConfiguracionRenta", "id", id);
        }
        log.info("Se ha encontrado la configuración de renta con ID: " + id);
        return deEntidadDtoARespuesta(configuracionRenta);
    }

    /**
     * Obtiene todos los registros disponibles.
     * @return resultado de la operacion.
     */
    public List<RespuestaConfiguracionRentaDTO> obtenerTodos() {
        List<ConfiguracionRenta> entidades = consulta.obtenerTodos();
        log.info("Se han obtenido todas las configuraciones de renta. La cantidad de registros es: " + entidades.size());
        return deListaEntidadADto(entidades);
    }

    /**
     * Guarda un nuevo registro.
     * @param entidad parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public RespuestaConfiguracionRentaDTO guardar(SolicitudConfiguracionRentaDTO entidad) {
        ConfiguracionRenta nuevaConfiguracionRenta = deSolicitudDtoAEntidad(entidad);
        ConfiguracionRenta configuracionRentaGuardada = mantenimiento.crear(nuevaConfiguracionRenta);
        log.info("Se ha guardado una nueva configuración de renta con ID: " + configuracionRentaGuardada.getId());
        return deEntidadDtoARespuesta(configuracionRentaGuardada);
    }

    /**
     * Actualiza un registro existente.
     * @param id parametro de entrada de la operacion.
     * @param entidad parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public RespuestaConfiguracionRentaDTO actualizar(Long id, SolicitudConfiguracionRentaDTO entidad) {
        ConfiguracionRenta configuracionRentaExistente = consulta.obtenerPorId(id);
        if(configuracionRentaExistente == null){
            log.warn("No se ha encontrado la configuración de renta con ID: " + id + " para actualizar");
            throw new ResourceNotFoundException("ConfiguracionRenta", "id", id);
        }
        configuracionRentaExistente.setMontoMaximo(entidad.montoMaximo);
        configuracionRentaExistente.setMontoMinimo(entidad.montoMinimo);
        configuracionRentaExistente.setPorcentaje(entidad.porcentaje);
        ConfiguracionRenta configuracionRentaActualizada = mantenimiento.actualizar(configuracionRentaExistente);
        log.info("Se ha actualizado la configuración de renta con ID: " + id);
        return deEntidadDtoARespuesta(configuracionRentaActualizada);
    }

    /**
     * Elimina un registro por su identificador.
     * @param id parametro de entrada de la operacion.
     */
    public void eliminar(Long id) {
        mantenimiento.eliminar(id);
        log.info("Se ha eliminado la configuración de renta con ID: " + id);
    }

    /**
     * Convierte un DTO de solicitud a entidad.
     * @param solicitud parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public ConfiguracionRenta deSolicitudDtoAEntidad(SolicitudConfiguracionRentaDTO solicitud) {
        if(solicitud == null){
            log.warn("El DTO de solicitud es nulo, no se puede convertir a entidad ConfiguracionRenta.");
            return null;
        }
        ConfiguracionRenta configuracionRenta = ConfiguracionRenta.builder()
                    .id(solicitud.id)
                    .montoMaximo(solicitud.montoMaximo)
                    .montoMinimo(solicitud.montoMinimo)
                    .porcentaje(solicitud.porcentaje)
                    .build();
        log.info("Se ha convertido el DTO de solicitud a entidad ConfiguracionRenta: {}", configuracionRenta);
        return configuracionRenta;
    }

    /**
     * Convierte una entidad a DTO de respuesta.
     * @param entidad parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public RespuestaConfiguracionRentaDTO deEntidadDtoARespuesta(ConfiguracionRenta entidad) {
        if(entidad == null){
            log.warn("La entidad ConfiguracionRenta es nula, no se puede convertir a DTO de respuesta.");
            return null;
        }
        RespuestaConfiguracionRentaDTO respuesta = new RespuestaConfiguracionRentaDTO();
        respuesta.id = entidad.getId();
        respuesta.montoMaximo = entidad.getMontoMaximo();
        respuesta.montoMinimo = entidad.getMontoMinimo();
        respuesta.porcentaje = entidad.getPorcentaje();
        log.info("Se ha convertido la entidad ConfiguracionRenta a DTO de respuesta: {}", respuesta);
        return respuesta;
    }

    /**
     * Convierte una lista de entidades a DTOs de respuesta.
     * @param entidades parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public List<RespuestaConfiguracionRentaDTO> deListaEntidadADto(List<ConfiguracionRenta> entidades) {
        return entidades.stream()
                .map(this::deEntidadDtoARespuesta)
                .toList();
    }

}
