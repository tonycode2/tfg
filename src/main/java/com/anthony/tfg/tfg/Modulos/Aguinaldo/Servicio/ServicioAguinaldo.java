package com.anthony.tfg.tfg.Modulos.Aguinaldo.Servicio;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaCalculoAguinaldoDTO;

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
import com.anthony.tfg.tfg.Repositorios.AguinaldosRepositorio;
import com.anthony.tfg.tfg.Repositorios.EmpleadosRepositorio;
import com.anthony.tfg.tfg.Repositorios.PlanillaDetalleRepositorio;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ServicioAguinaldo implements ServicioInterface<RespuestaAguinaldosDTO, 
                                                            SolicitudAguinaldosDTO, 
                                                            Aguinaldos>{

    private final ConsultasAguinaldos consulta;
    private final MantenimientosAguinaldo mantenimiento;
    private final ConsultasEmpleados consultasEmpleados;
    private final EmpleadosRepositorio empleadosRepositorio;
    private final PlanillaDetalleRepositorio planillaDetalleRepositorio;
    private final AguinaldosRepositorio aguinaldosRepositorio;

    public ServicioAguinaldo(ConsultasAguinaldos consulta,
                             MantenimientosAguinaldo mantenimiento,
                             ConsultasEmpleados consultasEmpleados,
                             EmpleadosRepositorio empleadosRepositorio,
                             PlanillaDetalleRepositorio planillaDetalleRepositorio,
                             AguinaldosRepositorio aguinaldosRepositorio) {
        this.consulta = consulta;
        this.mantenimiento = mantenimiento;
        this.consultasEmpleados = consultasEmpleados;
        this.empleadosRepositorio = empleadosRepositorio;
        this.planillaDetalleRepositorio = planillaDetalleRepositorio;
        this.aguinaldosRepositorio = aguinaldosRepositorio;
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
        aguinaldoExistente.setAnio(entidad.getAnio());
        aguinaldoExistente.setFechaInicioPeriodo(entidad.getFechaInicioPeriodo());
        aguinaldoExistente.setFechaFinPeriodo(entidad.getFechaFinPeriodo());
        aguinaldoExistente.setTotalSalariosDevengados(entidad.getTotalSalariosDevengados());
        aguinaldoExistente.setMontoAguinaldo(entidad.getMontoAguinaldo());
        aguinaldoExistente.setFechaCalculo(entidad.getFechaCalculo());
        aguinaldoExistente.setFechaPago(entidad.getFechaPago());
        
        Empleados empleado = consultasEmpleados.obtenerPorId(entidad.getIdEmpleado());
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

        @Transactional
        public List<RespuestaCalculoAguinaldoDTO> calcularAguinaldos() {
        LocalDate fechaCalculo = LocalDate.now();
        int anio = fechaCalculo.getYear();
        LocalDate fechaInicio = LocalDate.of(anio - 1, 12, 1);
        LocalDate fechaFin = LocalDate.of(anio, 11, 30);

        List<Empleados> empleadosActivos = empleadosRepositorio.findByEstaActivoTrue();
        if (empleadosActivos.isEmpty()) {
            log.info("No hay empleados activos para calcular aguinaldo");
            return List.of();
        }

        Date fechaInicioSql = Date.valueOf(fechaInicio);
        Date fechaFinSql = Date.valueOf(fechaFin);
        Date fechaCalculoSql = Date.valueOf(fechaCalculo);

        log.info("Calculando aguinaldo para {} empleados en el periodo {} - {}",
            empleadosActivos.size(), fechaInicio, fechaFin);

        return empleadosActivos.stream()
            .map(empleado -> {
                Double totalDevengado = planillaDetalleRepositorio
                    .sumDevengadoByEmpleadoAndFechaPagoBetween(empleado.getId(), fechaInicio, fechaFin);
                double totalSalarios = totalDevengado != null ? totalDevengado : 0.0;
                double montoAguinaldo = totalSalarios / 12.0;

                Aguinaldos aguinaldo = aguinaldosRepositorio
                    .findByEmpleadoIdAndAnio(empleado.getId(), anio)
                    .orElseGet(() -> Aguinaldos.builder().empleado(empleado).build());

                aguinaldo.setAnio(anio);
                aguinaldo.setFechaInicioPeriodo(fechaInicioSql);
                aguinaldo.setFechaFinPeriodo(fechaFinSql);
                aguinaldo.setTotalSalariosDevengados(totalSalarios);
                aguinaldo.setMontoAguinaldo(montoAguinaldo);
                aguinaldo.setFechaCalculo(fechaCalculoSql);

                Aguinaldos guardado = mantenimiento.actualizar(aguinaldo);

                return new RespuestaCalculoAguinaldoDTO(
                    guardado.getId(),
                    empleado.getId(),
                    empleado.getNombre(),
                    empleado.getPrimerApellido(),
                    empleado.getSegundoApellido(),
                    guardado.getAnio(),
                    guardado.getFechaInicioPeriodo(),
                    guardado.getFechaFinPeriodo(),
                    guardado.getTotalSalariosDevengados(),
                    guardado.getMontoAguinaldo(),
                    guardado.getFechaCalculo(),
                    guardado.getFechaPago());
            })
            .toList();
        }

    public Aguinaldos deSolicitudDtoAEntidad(SolicitudAguinaldosDTO solicitud) {
        if(solicitud == null){
            log.warn("El DTO de solicitud es nulo, no se puede convertir a entidad Aguinaldos.");
            return null;
        }
        
        Empleados empleado = consultasEmpleados.obtenerPorId(solicitud.getIdEmpleado());
        if(empleado == null){
            log.warn("No se ha encontrado el empleado con ID: " + solicitud.getIdEmpleado());
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
