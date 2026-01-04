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

@Service
@Slf4j
public class ServicioDireccion implements ServicioInterface<RespuestaDireccionDTO, 
                                                            SolicitudDireccionDTO, 
                                                            Direccion>{

    private final ConsultasDirecciones consulta;
    private final MantenimientosDirecciones mantenimiento;

    public ServicioDireccion(ConsultasDirecciones consulta, MantenimientosDirecciones mantenimiento) {
        this.consulta = consulta;
        this.mantenimiento = mantenimiento;
    }

    public RespuestaDireccionDTO obtenerPorId(Long id) {
        Direccion direccion = consulta.obtenerPorId(id);
        if(direccion == null){
            log.warn("No se ha encontrado la dirección con ID: " + id);
            throw new ResourceNotFoundException("Direccion", "id", id);
        }
        log.info("Se ha encontrado la dirección con ID: " + id);
        return deEntidadDtoARespuesta(direccion);
    }

    public List<RespuestaDireccionDTO> obtenerTodos() {
        List<Direccion> entidades = consulta.obtenerTodos();
        log.info("Se han obtenido todas las direcciones. La cantidad de registros es: " + entidades.size());
        return deListaEntidadADto(entidades);
    }

    public RespuestaDireccionDTO guardar(SolicitudDireccionDTO entidad) {
        Direccion nuevaDireccion = deSolicitudDtoAEntidad(entidad);
        Direccion direccionGuardada = mantenimiento.crear(nuevaDireccion);
        log.info("Se ha guardado una nueva dirección con ID: " + direccionGuardada.getId());
        return deEntidadDtoARespuesta(direccionGuardada);
    }

    public RespuestaDireccionDTO actualizar(Long id, SolicitudDireccionDTO entidad) {
        Direccion direccionExistente = consulta.obtenerPorId(id);
        if(direccionExistente == null){
            log.warn("No se ha encontrado la dirección con ID: " + id + " para actualizar");
            throw new ResourceNotFoundException("Direccion", "id", id);
        }
        direccionExistente.setProvincia(entidad.provincia);
        direccionExistente.setCanton(entidad.canton);
        direccionExistente.setDistrito(entidad.distrito);
        direccionExistente.setIndicaciones(entidad.indicaciones);
        Direccion direccionActualizada = mantenimiento.actualizar(direccionExistente);
        log.info("Se ha actualizado la dirección con ID: " + id);
        return deEntidadDtoARespuesta(direccionActualizada);
    }

    public void eliminar(Long id) {
        mantenimiento.eliminar(id);
        log.info("Se ha eliminado la dirección con ID: " + id);
    }

    public Direccion deSolicitudDtoAEntidad(SolicitudDireccionDTO solicitud) {
        if(solicitud == null){
            log.warn("El DTO de solicitud es nulo, no se puede convertir a entidad Direccion.");
            return null;
        }
        Direccion direccion = Direccion.builder()
                    .id(solicitud.id)
                    .provincia(solicitud.provincia)
                    .canton(solicitud.canton)
                    .distrito(solicitud.distrito)
                    .indicaciones(solicitud.indicaciones)
                    .build();
        log.info("Se ha convertido el DTO de solicitud a entidad Direccion: {}", direccion);
        return direccion;
    }

    public RespuestaDireccionDTO deEntidadDtoARespuesta(Direccion entidad) {
        if(entidad == null){
            log.warn("La entidad Direccion es nula, no se puede convertir a DTO de respuesta.");
            return null;
        }
        RespuestaDireccionDTO respuesta = new RespuestaDireccionDTO();
        respuesta.id = entidad.getId();
        respuesta.provincia = entidad.getProvincia();
        respuesta.canton = entidad.getCanton();
        respuesta.distrito = entidad.getDistrito();
        respuesta.indicaciones = entidad.getIndicaciones();
        log.info("Se ha convertido la entidad Direccion a DTO de respuesta: {}", respuesta);
        return respuesta;
    }

    public List<RespuestaDireccionDTO> deListaEntidadADto(List<Direccion> entidades) {
        return entidades.stream()
                .map(this::deEntidadDtoARespuesta)
                .toList();
    }

}
