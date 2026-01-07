package com.anthony.tfg.tfg.Modulos.Aguinaldo.Servicio;

import java.util.List;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaAguinaldosDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudAguinaldosDTO;
import com.anthony.tfg.tfg.Entidades.Aguinaldos;
import com.anthony.tfg.tfg.Entidades.Empleados;
import com.anthony.tfg.tfg.Exceptions.ResourceNotFoundException;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasAguinaldos;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasEmpleados;
import com.anthony.tfg.tfg.Modulos.Interfaces.ServicioInterface;
import com.anthony.tfg.tfg.Modulos.Mantenimientos.MantenimientosAguinaldo;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ServicioAguinaldo implements ServicioInterface<RespuestaAguinaldosDTO, 
                                                            SolicitudAguinaldosDTO, 
                                                            Aguinaldos>{

    private final ConsultasAguinaldos consulta;
    private final MantenimientosAguinaldo mantenimiento;
    private final ConsultasEmpleados consultasEmpleados;

    public ServicioAguinaldo(ConsultasAguinaldos consulta, MantenimientosAguinaldo mantenimiento, ConsultasEmpleados consultasEmpleados) {
        this.consulta = consulta;
        this.mantenimiento = mantenimiento;
        this.consultasEmpleados = consultasEmpleados;
    }

    public RespuestaAguinaldosDTO obtenerPorId(Long id) {
        Aguinaldos aguinaldo = consulta.obtenerPorId(id);
        if(aguinaldo == null){
            log.warn("No se ha encontrado el aguinaldo con ID: " + id);
            throw new ResourceNotFoundException("Aguinaldos", "id", id);
        }
        log.info("Se ha encontrado el aguinaldo con ID: " + id);
        return deEntidadDtoARespuesta(aguinaldo);
    }

    public List<RespuestaAguinaldosDTO> obtenerTodos() {
        List<Aguinaldos> entidades = consulta.obtenerTodos();
        log.info("Se han obtenido todos los aguinaldos. La cantidad de registros es: " + entidades.size());
        return deListaEntidadADto(entidades);
    }

    public RespuestaAguinaldosDTO guardar(SolicitudAguinaldosDTO entidad) {
        Aguinaldos nuevoAguinaldo = deSolicitudDtoAEntidad(entidad);
        Aguinaldos aguinaldoGuardado = mantenimiento.crear(nuevoAguinaldo);
        log.info("Se ha guardado un nuevo aguinaldo con ID: " + aguinaldoGuardado.getId());
        return deEntidadDtoARespuesta(aguinaldoGuardado);
    }

    public RespuestaAguinaldosDTO actualizar(Long id, SolicitudAguinaldosDTO entidad) {
        Aguinaldos aguinaldoExistente = consulta.obtenerPorId(id);
        if(aguinaldoExistente == null){
            log.warn("No se ha encontrado el aguinaldo con ID: " + id + " para actualizar");
            throw new ResourceNotFoundException("Aguinaldos", "id", id);
        }
        aguinaldoExistente.setAnio(entidad.anio);
        aguinaldoExistente.setFechaInicioPeriodo(entidad.fechaInicioPeriodo);
        aguinaldoExistente.setFechaFinPeriodo(entidad.fechaFinPeriodo);
        aguinaldoExistente.setTotalSalariosDevengados(entidad.totalSalariosDevengados);
        aguinaldoExistente.setMontoAguinaldo(entidad.montoAguinaldo);
        aguinaldoExistente.setFechaCalculo(entidad.fechaCalculo);
        aguinaldoExistente.setFechaPago(entidad.fechaPago);
        
        Empleados empleado = consultasEmpleados.obtenerPorId(entidad.idEmpleado);
        if(empleado != null){
            aguinaldoExistente.setEmpleado(empleado);
        }
        
        Aguinaldos aguinaldoActualizado = mantenimiento.actualizar(aguinaldoExistente);
        log.info("Se ha actualizado el aguinaldo con ID: " + id);
        return deEntidadDtoARespuesta(aguinaldoActualizado);
    }

    public void eliminar(Long id) {
        mantenimiento.eliminar(id);
        log.info("Se ha eliminado el aguinaldo con ID: " + id);
    }

    public Aguinaldos deSolicitudDtoAEntidad(SolicitudAguinaldosDTO solicitud) {
        if(solicitud == null){
            log.warn("El DTO de solicitud es nulo, no se puede convertir a entidad Aguinaldos.");
            return null;
        }
        
        Empleados empleado = consultasEmpleados.obtenerPorId(solicitud.idEmpleado);
        if(empleado == null){
            log.warn("No se ha encontrado el empleado con ID: " + solicitud.idEmpleado);
            return null;
        }
        
        Aguinaldos aguinaldo = Aguinaldos.builder()
                    .id(solicitud.id)
                    .anio(solicitud.anio)
                    .fechaInicioPeriodo(solicitud.fechaInicioPeriodo)
                    .fechaFinPeriodo(solicitud.fechaFinPeriodo)
                    .totalSalariosDevengados(solicitud.totalSalariosDevengados)
                    .montoAguinaldo(solicitud.montoAguinaldo)
                    .fechaCalculo(solicitud.fechaCalculo)
                    .fechaPago(solicitud.fechaPago)
                    .empleado(empleado)
                    .build();
        log.info("Se ha convertido el DTO de solicitud a entidad Aguinaldos: {}", aguinaldo);
        return aguinaldo;
    }

    public RespuestaAguinaldosDTO deEntidadDtoARespuesta(Aguinaldos entidad) {
        if(entidad == null){
            log.warn("La entidad Aguinaldos es nula, no se puede convertir a DTO de respuesta.");
            return null;
        }
        RespuestaAguinaldosDTO respuesta = new RespuestaAguinaldosDTO();
        respuesta.id = entidad.getId();
        respuesta.anio = entidad.getAnio();
        respuesta.fechaInicioPeriodo = entidad.getFechaInicioPeriodo();
        respuesta.fechaFinPeriodo = entidad.getFechaFinPeriodo();
        respuesta.totalSalariosDevengados = entidad.getTotalSalariosDevengados();
        respuesta.montoAguinaldo = entidad.getMontoAguinaldo();
        respuesta.fechaCalculo = entidad.getFechaCalculo();
        respuesta.fechaPago = entidad.getFechaPago();
        
        if(entidad.getEmpleado() != null){
            respuesta.nombreEmpleado = entidad.getEmpleado().getNombre();
            respuesta.primerApellidoEmpleado = entidad.getEmpleado().getPrimerApellido();
            respuesta.segundoApellidoEmpleado = entidad.getEmpleado().getSegundoApellido();
        }
        
        log.info("Se ha convertido la entidad Aguinaldos a DTO de respuesta: {}", respuesta);
        return respuesta;
    }

    public List<RespuestaAguinaldosDTO> deListaEntidadADto(List<Aguinaldos> entidades) {
        return entidades.stream()
                .map(this::deEntidadDtoARespuesta)
                .toList();
    }

}
