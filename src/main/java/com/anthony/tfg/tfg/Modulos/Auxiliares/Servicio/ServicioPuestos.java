package com.anthony.tfg.tfg.Modulos.Auxiliares.Servicio;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaPuestosDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudPuestosDTO;
import com.anthony.tfg.tfg.Entidades.Departamento;
import com.anthony.tfg.tfg.Entidades.Puestos;
import com.anthony.tfg.tfg.Exceptions.ResourceNotFoundException;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasDepartamentos;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasPuestos;
import com.anthony.tfg.tfg.Modulos.Interfaces.ServicioInterface;
import com.anthony.tfg.tfg.Modulos.Mantenimientos.MantenimientosPuestos;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ServicioPuestos implements ServicioInterface<RespuestaPuestosDTO, 
                                                        SolicitudPuestosDTO, 
                                                        Puestos>{

    private final ConsultasPuestos consulta;
    private final MantenimientosPuestos mantenimiento;
    private final ConsultasDepartamentos consultasDepartamentos;

    public ServicioPuestos(ConsultasPuestos consulta, MantenimientosPuestos mantenimiento, ConsultasDepartamentos consultasDepartamentos) {
        this.consulta = consulta;
        this.mantenimiento = mantenimiento;
        this.consultasDepartamentos = consultasDepartamentos;
    }

    public RespuestaPuestosDTO obtenerPorId(Long id) {
        Puestos puesto = consulta.obtenerPorId(id);
        if(puesto == null){
            log.warn("No se ha encontrado el puesto con ID: " + id);
            throw new ResourceNotFoundException("Puestos", "id", id);
        }
        log.info("Se ha encontrado el puesto con ID: " + id);
        return deEntidadDtoARespuesta(puesto);
    }

    public Page<RespuestaPuestosDTO> obtenerTodos(Pageable pageable) {
        Page<Puestos> entidades = consulta.obtenerTodos(pageable);
        log.info("Se han obtenido todos los puestos. La cantidad de registros es: " + entidades.getTotalElements());
        return entidades.map(this::deEntidadDtoARespuesta);
    }

    public RespuestaPuestosDTO guardar(SolicitudPuestosDTO entidad) {
        Puestos nuevoPuesto = deSolicitudDtoAEntidad(entidad);
        Puestos puestoGuardado = mantenimiento.crear(nuevoPuesto);
        log.info("Se ha guardado un nuevo puesto con ID: " + puestoGuardado.getId());
        return deEntidadDtoARespuesta(puestoGuardado);
    }

    public RespuestaPuestosDTO actualizar(Long id, SolicitudPuestosDTO entidad) {
        Puestos puestoExistente = consulta.obtenerPorId(id);
        if(puestoExistente == null){
            log.warn("No se ha encontrado el puesto con ID: " + id + " para actualizar");
            throw new ResourceNotFoundException("Puestos", "id", id);
        }
        puestoExistente.setNombre(entidad.nombre);
        puestoExistente.setSalarioMinimo(entidad.salarioMinimo);
        puestoExistente.setHoraEntrada(entidad.horaEntrada);
        puestoExistente.setHoraSalida(entidad.horaSalida);
        
        Departamento departamento = consultasDepartamentos.obtenerPorId(entidad.idDepartamento);
        if(departamento == null){
            throw new ResourceNotFoundException("Departamento", "id", entidad.idDepartamento);
        }
        puestoExistente.setDepartamento(departamento);
        
        Puestos puestoActualizado = mantenimiento.actualizar(puestoExistente);
        log.info("Se ha actualizado el puesto con ID: " + id);
        return deEntidadDtoARespuesta(puestoActualizado);
    }

    public void eliminar(Long id) {
        mantenimiento.eliminar(id);
        log.info("Se ha eliminado el puesto con ID: " + id);
    }

    public Puestos deSolicitudDtoAEntidad(SolicitudPuestosDTO solicitud) {
        if(solicitud == null){
            log.warn("El DTO de solicitud es nulo, no se puede convertir a entidad Puestos.");
            return null;
        }
        
        Departamento departamento = consultasDepartamentos.obtenerPorId(solicitud.idDepartamento);
        if(departamento == null){
            log.warn("No se ha encontrado el departamento con ID: " + solicitud.idDepartamento);
            return null;
        }
        
        Puestos puesto = Puestos.builder()
                    .id(solicitud.id)
                    .nombre(solicitud.nombre)
                    .salarioMinimo(solicitud.salarioMinimo)
                    .horaEntrada(solicitud.horaEntrada)
                    .horaSalida(solicitud.horaSalida)
                    .departamento(departamento)
                    .build();
        log.info("Se ha convertido el DTO de solicitud a entidad Puestos: {}", puesto);
        return puesto;
    }

    public RespuestaPuestosDTO deEntidadDtoARespuesta(Puestos entidad) {
        if(entidad == null){
            log.warn("La entidad Puestos es nula, no se puede convertir a DTO de respuesta.");
            return null;
        }
        RespuestaPuestosDTO respuesta = new RespuestaPuestosDTO();
        respuesta.id = entidad.getId();
        respuesta.nombre = entidad.getNombre();
        respuesta.salarioMinimo = entidad.getSalarioMinimo();
        respuesta.horaEntrada = entidad.getHoraEntrada();
        respuesta.horaSalida = entidad.getHoraSalida();
        
        if(entidad.getDepartamento() != null){
            respuesta.nombreDepartamento = entidad.getDepartamento().getNombre();
        }
        
        log.info("Se ha convertido la entidad Puestos a DTO de respuesta: {}", respuesta);
        return respuesta;
    }

    public List<RespuestaPuestosDTO> deListaEntidadADto(List<Puestos> entidades) {
        return entidades.stream()
                .map(this::deEntidadDtoARespuesta)
                .toList();
    }

}
