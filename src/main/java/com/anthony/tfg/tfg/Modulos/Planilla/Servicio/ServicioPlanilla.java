package com.anthony.tfg.tfg.Modulos.Planilla.Servicio;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaPlanillaDetalleDTO;
import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaPlanillaEmpleadoDTO;
import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaPlanillaEncabezadoDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudGenerarPlanillaDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudPlanillaEncabezadoDTO;
import com.anthony.tfg.tfg.Entidades.ConfiguracionRenta;
import com.anthony.tfg.tfg.Entidades.DiasFeriados;
import com.anthony.tfg.tfg.Entidades.Empleados;
import com.anthony.tfg.tfg.Entidades.JornadaDiaria;
import com.anthony.tfg.tfg.Entidades.PlanillaDetalle;
import com.anthony.tfg.tfg.Entidades.PlanillaEncabezado;
import com.anthony.tfg.tfg.Entidades.Enums.EstadoPlanilla;
import com.anthony.tfg.tfg.Entidades.Enums.TipoQuincena;
import com.anthony.tfg.tfg.Entidades.Enums.TipoEntidadEmisora;
import com.anthony.tfg.tfg.Entidades.Enums.TipoPermiso;
import com.anthony.tfg.tfg.Exceptions.BadRequestException;
import com.anthony.tfg.tfg.Exceptions.ResourceNotFoundException;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasConfiguracionRentas;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasPlanillaEncabezado;
import com.anthony.tfg.tfg.Modulos.Interfaces.ServicioInterface;
import com.anthony.tfg.tfg.Modulos.Mantenimientos.MantenimientosPlanillaEncabezados;
import com.anthony.tfg.tfg.Repositorios.DiasFeriadosRepositorio;
import com.anthony.tfg.tfg.Repositorios.EmpleadosRepositorio;
import com.anthony.tfg.tfg.Repositorios.JornadaDiariaRepositorio;
import com.anthony.tfg.tfg.Repositorios.PlanillaDetalleRepositorio;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ServicioPlanilla implements ServicioInterface<RespuestaPlanillaEncabezadoDTO, 
                                                        SolicitudPlanillaEncabezadoDTO, 
                                                        PlanillaEncabezado>{

    private final ConsultasPlanillaEncabezado consulta;
    private final MantenimientosPlanillaEncabezados mantenimiento;
    private final PlanillaDetalleRepositorio planillaDetalleRepo;
    private final EmpleadosRepositorio empleadosRepositorio;
    private final JornadaDiariaRepositorio jornadaDiariaRepositorio;
    private final DiasFeriadosRepositorio diasFeriadosRepositorio;
    private final ConsultasConfiguracionRentas consultasConfiguracionRentas;

    public ServicioPlanilla(ConsultasPlanillaEncabezado consulta, 
                           MantenimientosPlanillaEncabezados mantenimiento,
                           PlanillaDetalleRepositorio planillaDetalleRepo,
                           EmpleadosRepositorio empleadosRepositorio,
                           JornadaDiariaRepositorio jornadaDiariaRepositorio,
                           DiasFeriadosRepositorio diasFeriadosRepositorio,
                           ConsultasConfiguracionRentas consultasConfiguracionRentas) {
        this.consulta = consulta;
        this.mantenimiento = mantenimiento;
        this.planillaDetalleRepo = planillaDetalleRepo;
        this.empleadosRepositorio = empleadosRepositorio;
        this.jornadaDiariaRepositorio = jornadaDiariaRepositorio;
        this.diasFeriadosRepositorio = diasFeriadosRepositorio;
        this.consultasConfiguracionRentas = consultasConfiguracionRentas;
    }

    /**
     * Obtiene las planillas de un empleado específico
     * @param empleadoId ID del empleado
     * @return Lista de planillas del empleado con sus detalles
     */
    public List<RespuestaPlanillaEmpleadoDTO> obtenerPlanillasPorEmpleado(Long empleadoId) {
        log.info("Obteniendo planillas para el empleado con ID: {}", empleadoId);
        List<PlanillaDetalle> detalles = planillaDetalleRepo.findByEmpleadoId(empleadoId);
        
        List<RespuestaPlanillaEmpleadoDTO> planillas = detalles.stream()
            .map(this::deDetalleADtoEmpleado)
            .collect(Collectors.toList());
        
        log.info("Se encontraron {} planillas para el empleado con ID: {}", planillas.size(), empleadoId);
        return planillas;
    }

    /**
     * Obtiene los detalles de planilla para un encabezado específico.
     * @param planillaId ID de la planilla encabezado
     * @return Lista de detalles con datos del empleado
     */
    public List<RespuestaPlanillaDetalleDTO> obtenerDetallesPorPlanilla(Long planillaId) {
        PlanillaEncabezado planilla = consulta.obtenerPorId(planillaId);
        if (planilla == null) {
            log.warn("No se ha encontrado la planilla con ID: {} para obtener detalles", planillaId);
            throw new ResourceNotFoundException("PlanillaEncabezado", "id", planillaId);
        }

        List<PlanillaDetalle> detalles = planillaDetalleRepo.findByPlanillaEncabezadoId(planillaId);
        List<RespuestaPlanillaDetalleDTO> respuesta = detalles.stream()
            .map(this::deDetalleADto)
            .toList();

        log.info("Se encontraron {} detalles para la planilla con ID: {}", respuesta.size(), planillaId);
        return respuesta;
    }

    /**
     * Convierte PlanillaDetalle a RespuestaPlanillaEmpleadoDTO
     */
    private RespuestaPlanillaEmpleadoDTO deDetalleADtoEmpleado(PlanillaDetalle detalle) {
        RespuestaPlanillaEmpleadoDTO dto = new RespuestaPlanillaEmpleadoDTO();
        
        // Datos del encabezado
        PlanillaEncabezado encabezado = detalle.getPlanillaEncabezado();
        dto.idEncabezado = encabezado.getId();
        dto.fechaInicioPeriodo = encabezado.getFechaInicioPeriodo();
        dto.fechaFinPeriodo = encabezado.getFechaFinPeriodo();
        dto.fechaPago = encabezado.getFechaPago();
        dto.tipoQuincena = encabezado.getTipoQuincena() != null
                    ? encabezado.getTipoQuincena().name()
                    : null;
        dto.estadoPlanilla = encabezado.getEstadoPlanilla() != null ? 
                            encabezado.getEstadoPlanilla().name() : null;
        
        // Datos del detalle
        dto.idDetalle = detalle.getId();
        dto.salarioBasePeriodo = detalle.getSalarioBasePeriodo() != null ? detalle.getSalarioBasePeriodo() : 0.0;
        dto.cantidadDiasFeriados = detalle.getCantidadDiasFeriados() != null ? detalle.getCantidadDiasFeriados() : 0;
        dto.montoHorasExtra = detalle.getMontoHorasExtra() != null ? detalle.getMontoHorasExtra() : 0.0;
        dto.montoIncapacidad = detalle.getMontoIncapacidad() != null ? detalle.getMontoIncapacidad() : 0.0;
        dto.deduccionCcssIvm = detalle.getDeduccionCcssIvm() != null ? detalle.getDeduccionCcssIvm() : 0.0;
        dto.deduccionCcssSem = detalle.getDeduccionCcssSem() != null ? detalle.getDeduccionCcssSem() : 0.0;
        dto.impuestoDeRenta = detalle.getImpuestoDeRenta() != null ? detalle.getImpuestoDeRenta() : 0.0;
        dto.otrasDeducciones = detalle.getOtrasDeducciones() != null ? detalle.getOtrasDeducciones() : 0.0;
        
        // Calcular totales
        dto.totalDevengado = dto.salarioBasePeriodo + dto.montoHorasExtra + dto.montoIncapacidad;
        dto.totalDeducciones = dto.deduccionCcssIvm + dto.deduccionCcssSem + 
                              dto.impuestoDeRenta + dto.otrasDeducciones;
        dto.salarioNeto = dto.totalDevengado - dto.totalDeducciones;
        
        return dto;
    }

    /**
     * Convierte PlanillaDetalle a RespuestaPlanillaDetalleDTO
     */
    private RespuestaPlanillaDetalleDTO deDetalleADto(PlanillaDetalle detalle) {
        RespuestaPlanillaDetalleDTO dto = new RespuestaPlanillaDetalleDTO();
        dto.id = detalle.getId();
        dto.salarioBasePeriodo = detalle.getSalarioBasePeriodo() != null ? detalle.getSalarioBasePeriodo() : 0.0;
        dto.cantidadDiasFeriados = detalle.getCantidadDiasFeriados() != null ? detalle.getCantidadDiasFeriados() : 0;
        dto.montoHorasExtra = detalle.getMontoHorasExtra() != null ? detalle.getMontoHorasExtra() : 0.0;
        dto.montoIncapacidad = detalle.getMontoIncapacidad() != null ? detalle.getMontoIncapacidad() : 0.0;
        dto.deduccionCcssIvm = detalle.getDeduccionCcssIvm() != null ? detalle.getDeduccionCcssIvm() : 0.0;
        dto.deduccionCcssSem = detalle.getDeduccionCcssSem() != null ? detalle.getDeduccionCcssSem() : 0.0;
        dto.impuestoDeRenta = detalle.getImpuestoDeRenta() != null ? detalle.getImpuestoDeRenta() : 0.0;
        dto.otrasDeducciones = detalle.getOtrasDeducciones() != null ? detalle.getOtrasDeducciones() : 0.0;

        if (detalle.getEmpleado() != null) {
            dto.nombreEmpleado = detalle.getEmpleado().getNombre();
            dto.primerApellidoEmpleado = detalle.getEmpleado().getPrimerApellido();
            dto.segundoApellidoEmpleado = detalle.getEmpleado().getSegundoApellido();
        }

        return dto;
    }

    public RespuestaPlanillaEncabezadoDTO obtenerPorId(Long id) {
        PlanillaEncabezado planilla = consulta.obtenerPorId(id);
        if(planilla == null){
            log.warn("No se ha encontrado la planilla con ID: " + id);
            throw new ResourceNotFoundException("PlanillaEncabezado", "id", id);
        }
        log.info("Se ha encontrado la planilla con ID: " + id);
        return deEntidadDtoARespuesta(planilla);
    }

    public List<RespuestaPlanillaEncabezadoDTO> obtenerTodos() {
        List<PlanillaEncabezado> entidades = consulta.obtenerTodos();
        log.info("Se han obtenido todas las planillas. La cantidad de registros es: " + entidades.size());
        return deListaEntidadADto(entidades);
    }

    public RespuestaPlanillaEncabezadoDTO guardar(SolicitudPlanillaEncabezadoDTO entidad) {
        PlanillaEncabezado nuevaPlanilla = deSolicitudDtoAEntidad(entidad);
        PlanillaEncabezado planillaGuardada = mantenimiento.crear(nuevaPlanilla);
        log.info("Se ha guardado una nueva planilla con ID: " + planillaGuardada.getId());
        return deEntidadDtoARespuesta(planillaGuardada);
    }

        /**
         * Genera una planilla completa para todos los empleados activos en el periodo.
         */
        @Transactional
        public RespuestaPlanillaEncabezadoDTO generarPlanilla(SolicitudGenerarPlanillaDTO solicitud) {
        if (solicitud == null) {
            throw new BadRequestException("La solicitud de planilla es requerida");
        }
        if (solicitud.mes() == null || solicitud.anio() == null || solicitud.tipoQuincena() == null) {
            throw new BadRequestException("El mes, año y la quincena son requeridos");
        }

        LocalDate fechaInicioPeriodo = calcularFechaInicioPeriodo(solicitud.anio(), solicitud.mes(),
            solicitud.tipoQuincena());
        LocalDate fechaFinPeriodo = calcularFechaFinPeriodo(solicitud.anio(), solicitud.mes(),
            solicitud.tipoQuincena());
        LocalDate fechaPago = calcularFechaPago(solicitud.anio(), solicitud.mes());

        List<Empleados> empleadosActivos = empleadosRepositorio.findByEstaActivoTrue();
        if (empleadosActivos.isEmpty()) {
            throw new BadRequestException("No hay empleados activos para generar la planilla");
        }

        List<DiasFeriados> feriadosEnRango = diasFeriadosRepositorio.findByFechaBetween(
            fechaInicioPeriodo,
            fechaFinPeriodo);
        Set<LocalDate> fechasFeriados = feriadosEnRango.stream()
            .map(DiasFeriados::getFecha)
            .collect(Collectors.toCollection(HashSet::new));

        List<ConfiguracionRenta> tramosRenta = consultasConfiguracionRentas.obtenerTodos().stream()
            .sorted((a, b) -> Double.compare(a.getMontoMinimo(), b.getMontoMinimo()))
            .toList();

        PlanillaEncabezado encabezado = PlanillaEncabezado.builder()
            .fechaInicioPeriodo(fechaInicioPeriodo)
            .fechaFinPeriodo(fechaFinPeriodo)
            .fechaPago(fechaPago)
            .tipoQuincena(solicitud.tipoQuincena())
            .totalPlanillaBruto(0.0)
            .totalPlanillaNeto(0.0)
            .estadoPlanilla(EstadoPlanilla.BORRADOR)
            .build();

        PlanillaEncabezado encabezadoGuardado = mantenimiento.crear(encabezado);

        double totalPlanillaBruto = 0.0;
        double totalPlanillaNeto = 0.0;
        List<PlanillaDetalle> detalles = empleadosActivos.stream()
            .map(empleado -> calcularDetallePlanilla(empleado, fechaInicioPeriodo,
                fechaFinPeriodo, fechasFeriados, tramosRenta, encabezadoGuardado))
            .toList();

        planillaDetalleRepo.saveAll(detalles);

        for (PlanillaDetalle detalle : detalles) {
            double totalDevengado = safe(detalle.getSalarioBasePeriodo())
                + safe(detalle.getMontoHorasExtra())
                + safe(detalle.getMontoIncapacidad());
            double totalDeducciones = safe(detalle.getDeduccionCcssIvm())
                + safe(detalle.getDeduccionCcssSem())
                + safe(detalle.getOtrasDeducciones())
                + safe(detalle.getImpuestoDeRenta());
            totalPlanillaBruto += totalDevengado;
            totalPlanillaNeto += (totalDevengado - totalDeducciones);
        }

        encabezadoGuardado.setTotalPlanillaBruto(totalPlanillaBruto);
        encabezadoGuardado.setTotalPlanillaNeto(totalPlanillaNeto);
        PlanillaEncabezado encabezadoActualizado = mantenimiento.actualizar(encabezadoGuardado);

        log.info("Se generó la planilla {} para el periodo {} a {} con {} detalles",
            encabezadoActualizado.getId(),
            fechaInicioPeriodo,
            fechaFinPeriodo,
            detalles.size());
        return deEntidadDtoARespuesta(encabezadoActualizado);
        }

    public RespuestaPlanillaEncabezadoDTO actualizar(Long id, SolicitudPlanillaEncabezadoDTO entidad) {
        PlanillaEncabezado planillaExistente = consulta.obtenerPorId(id);
        if(planillaExistente == null){
            log.warn("No se ha encontrado la planilla con ID: " + id + " para actualizar");
            return null;
        }
        planillaExistente.setFechaInicioPeriodo(entidad.getFechaInicioPeriodo());
        planillaExistente.setFechaFinPeriodo(entidad.getFechaFinPeriodo());
        planillaExistente.setFechaPago(entidad.getFechaPago());
        TipoQuincena tipoQuincena = obtenerTipoQuincena(entidad.getTipoQuincena());
        if (tipoQuincena != null) {
            planillaExistente.setTipoQuincena(tipoQuincena);
        }
        planillaExistente.setTotalPlanillaBruto(entidad.getTotalPlanillaBruto());
        planillaExistente.setTotalPlanillaNeto(entidad.getTotalPlanillaNeto());
        
        EstadoPlanilla estadoPlanilla = obtenerEstadoPlanilla(entidad.getEstadoPlanilla());
        if(estadoPlanilla != null){
            planillaExistente.setEstadoPlanilla(estadoPlanilla);
        }
        
        PlanillaEncabezado planillaActualizada = mantenimiento.actualizar(planillaExistente);
        log.info("Se ha actualizado la planilla con ID: " + id);
        return deEntidadDtoARespuesta(planillaActualizada);
    }

    public void eliminar(Long id) {
        mantenimiento.eliminar(id);
        log.info("Se ha eliminado la planilla con ID: " + id);
    }

    public PlanillaEncabezado deSolicitudDtoAEntidad(SolicitudPlanillaEncabezadoDTO solicitud) {
        if(solicitud == null){
            log.warn("El DTO de solicitud es nulo, no se puede convertir a entidad PlanillaEncabezado.");
            return null;
        }
        
        EstadoPlanilla estadoPlanilla = obtenerEstadoPlanilla(solicitud.estadoPlanilla);
        if(estadoPlanilla == null){
            log.warn("No se ha encontrado el estado de planilla: " + solicitud.estadoPlanilla);
            return null;
        }
        
        TipoQuincena tipoQuincena = obtenerTipoQuincena(solicitud.tipoQuincena);
        if (tipoQuincena == null) {
            log.warn("No se ha encontrado el tipo de quincena: " + solicitud.tipoQuincena);
            return null;
        }

        PlanillaEncabezado planilla = PlanillaEncabezado.builder()
                    .id(solicitud.getId())
                    .fechaInicioPeriodo(solicitud.getFechaInicioPeriodo())
                    .fechaFinPeriodo(solicitud.getFechaFinPeriodo())
                    .fechaPago(solicitud.getFechaPago())
                    .tipoQuincena(tipoQuincena)
                    .totalPlanillaBruto(solicitud.getTotalPlanillaBruto())
                    .totalPlanillaNeto(solicitud.getTotalPlanillaNeto())
                    .estadoPlanilla(estadoPlanilla)
                    .build();
        log.info("Se ha convertido el DTO de solicitud a entidad PlanillaEncabezado: {}", planilla);
        return planilla;
    }

    public RespuestaPlanillaEncabezadoDTO deEntidadDtoARespuesta(PlanillaEncabezado entidad) {
        if(entidad == null){
            log.warn("La entidad PlanillaEncabezado es nula, no se puede convertir a DTO de respuesta.");
            return null;
        }
        RespuestaPlanillaEncabezadoDTO respuesta = new RespuestaPlanillaEncabezadoDTO();
        respuesta.id = entidad.getId();
        respuesta.fechaInicioPeriodo = entidad.getFechaInicioPeriodo();
        respuesta.fechaFinPeriodo = entidad.getFechaFinPeriodo();
        respuesta.fechaPago = entidad.getFechaPago();
        if (entidad.getTipoQuincena() != null) {
            respuesta.tipoQuincena = entidad.getTipoQuincena().name();
        }
        respuesta.totalPlanillaBruto = entidad.getTotalPlanillaBruto();
        respuesta.totalPlanillaNeto = entidad.getTotalPlanillaNeto();
        
        if(entidad.getEstadoPlanilla() != null){
            respuesta.estadoPlanilla = entidad.getEstadoPlanilla().name();
        }
        
        log.info("Se ha convertido la entidad PlanillaEncabezado a DTO de respuesta: {}", respuesta);
        return respuesta;
    }

    public List<RespuestaPlanillaEncabezadoDTO> deListaEntidadADto(List<PlanillaEncabezado> entidades) {
        return entidades.stream()
                .map(this::deEntidadDtoARespuesta)
                .toList();
    }
    
    private EstadoPlanilla obtenerEstadoPlanilla(String estado) {
        try {
            return EstadoPlanilla.valueOf(estado.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    private TipoQuincena obtenerTipoQuincena(String tipoQuincena) {
        try {
            return TipoQuincena.valueOf(tipoQuincena.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDate calcularFechaInicioPeriodo(int anio, int mes, TipoQuincena tipoQuincena) {
        if (tipoQuincena == TipoQuincena.PRIMERA) {
            return LocalDate.of(anio, mes, 1);
        }
        return LocalDate.of(anio, mes, 16);
    }

    private LocalDate calcularFechaFinPeriodo(int anio, int mes, TipoQuincena tipoQuincena) {
        if (tipoQuincena == TipoQuincena.PRIMERA) {
            return LocalDate.of(anio, mes, 15);
        }
        YearMonth yearMonth = YearMonth.of(anio, mes);
        return LocalDate.of(anio, mes, yearMonth.lengthOfMonth());
    }

    private LocalDate calcularFechaPago(int anio, int mes) {
        YearMonth yearMonth = YearMonth.of(anio, mes);
        return LocalDate.of(anio, mes, yearMonth.lengthOfMonth());
    }

    private PlanillaDetalle calcularDetallePlanilla(Empleados empleado,
                                                    LocalDate fechaInicio,
                                                    LocalDate fechaFin,
                                                    Set<LocalDate> feriados,
                                                    List<ConfiguracionRenta> tramosRenta,
                                                    PlanillaEncabezado encabezado) {
        double salarioMensual = obtenerSalarioMensual(empleado);
        double salarioDiario = salarioMensual / 30.0;
        double salarioHora = salarioDiario / 8.0;

        double basePeriodo = salarioMensual / 2.0;
        double horasFaltantes = 0.0;
        double totalHorasExtra = 0.0;
        int cantidadDiasFeriados = 0;
        double montoIncapacidad = 0.0;

        List<JornadaDiaria> jornadas = jornadaDiariaRepositorio.findByEmpleadoIdAndFechaBetween(
                empleado.getId(), fechaInicio, fechaFin);
        Map<LocalDate, JornadaDiaria> jornadasPorFecha = mapearJornadas(jornadas);

        LocalDate fecha = fechaInicio;
        while (!fecha.isAfter(fechaFin)) {
            JornadaDiaria jornada = jornadasPorFecha.get(fecha);
            double horasRegulares = jornada != null && jornada.getHorasRegulares() != null
                    ? jornada.getHorasRegulares()
                    : 0.0;
            double horasExtra = jornada != null && jornada.getHorasExtra() != null
                    ? jornada.getHorasExtra()
                    : 0.0;
            totalHorasExtra += horasExtra;

            boolean esFeriado = feriados.contains(fecha);
            boolean esFinSemana = esFinDeSemana(fecha);
            boolean tieneHorasTrabajadas = horasRegulares > 0 || horasExtra > 0;

            if (esFeriado && tieneHorasTrabajadas) {
                cantidadDiasFeriados++;
            }

            boolean esVacaciones = jornada != null
                    && jornada.getPermiso() != null
                    && jornada.getPermiso().getTipoPermiso() == TipoPermiso.VACACIONES;
            boolean esIncapacidad = jornada != null && jornada.getIncapacidad() != null;

            if (esIncapacidad) {
                Integer diaIncapacidad = jornada.getDiaPermiso();
                if (diaIncapacidad != null && diaIncapacidad <= 3) {
                    TipoEntidadEmisora entidad = jornada.getIncapacidad().getEntidadEmisora();
                    double factorPago = entidad == TipoEntidadEmisora.CCSS ? 0.5
                            : entidad == TipoEntidadEmisora.INS ? 1.0
                            : 0.0;
                    montoIncapacidad += salarioDiario * factorPago;
                }
            }

            if (!esFinSemana && !esFeriado && !esVacaciones && !esIncapacidad) {
                if (horasRegulares < 8.0) {
                    horasFaltantes += (8.0 - horasRegulares);
                }
            }

            fecha = fecha.plusDays(1);
        }

        double montoHorasExtra = totalHorasExtra * salarioHora * 0.5;
        double montoDiasFeriados = cantidadDiasFeriados * salarioDiario;
        double salarioBasePeriodo = Math.max(0.0, basePeriodo - (horasFaltantes * salarioHora)) + montoDiasFeriados;
        double totalDevengado = salarioBasePeriodo + montoHorasExtra + montoIncapacidad;

        double deduccionCcssSem = totalDevengado * 0.055;
        double deduccionCcssIvm = totalDevengado * 0.0433;
        double otrasDeducciones = totalDevengado * 0.01;
        double impuestoRenta = calcularImpuestoRenta(totalDevengado, tramosRenta);

        return PlanillaDetalle.builder()
                .salarioBasePeriodo(salarioBasePeriodo)
                .cantidadDiasFeriados(cantidadDiasFeriados)
                .montoHorasExtra(montoHorasExtra)
                .montoIncapacidad(montoIncapacidad)
                .deduccionCcssIvm(deduccionCcssIvm)
                .deduccionCcssSem(deduccionCcssSem)
                .impuestoDeRenta(impuestoRenta)
                .otrasDeducciones(otrasDeducciones)
                .empleado(empleado)
                .planillaEncabezado(encabezado)
                .build();
    }

    private double calcularImpuestoRenta(double salario, List<ConfiguracionRenta> tramosRenta) {
        if (tramosRenta.isEmpty()) {
            return 0.0;
        }
        double impuesto = 0.0;
        for (ConfiguracionRenta tramo : tramosRenta) {
            double minimo = tramo.getMontoMinimo() != null ? tramo.getMontoMinimo() : 0.0;
            double maximo = tramo.getMontoMaximo() != null ? tramo.getMontoMaximo() : Double.MAX_VALUE;
            if (salario <= minimo) {
                break;
            }
            double base = Math.min(salario, maximo) - minimo;
            if (base > 0) {
                double porcentaje = tramo.getPorcentaje() != null ? tramo.getPorcentaje() : 0.0;
                impuesto += base * (porcentaje / 100.0);
            }
            if (salario <= maximo) {
                break;
            }
        }
        return impuesto;
    }

    private boolean esFinDeSemana(LocalDate fecha) {
        DayOfWeek dia = fecha.getDayOfWeek();
        return dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY;
    }

    private Map<LocalDate, JornadaDiaria> mapearJornadas(List<JornadaDiaria> jornadas) {
        Map<LocalDate, JornadaDiaria> resultado = new HashMap<>();
        for (JornadaDiaria jornada : jornadas) {
            if (jornada.getFecha() != null) {
                resultado.put(jornada.getFecha(), jornada);
            }
        }
        return resultado;
    }

    private double obtenerSalarioMensual(Empleados empleado) {
        if (empleado == null) {
            return 0.0;
        }
        if (empleado.getPuesto() != null && empleado.getPuesto().getSalarioMinimo() != null) {
            return empleado.getPuesto().getSalarioMinimo();
        }
        if (empleado.getSalarioBase() != null) {
            return empleado.getSalarioBase();
        }
        log.warn("El empleado {} no tiene salario mensual definido", empleado.getId());
        return 0.0;
    }

    private double safe(Double value) {
        return value == null ? 0.0 : value;
    }

}
