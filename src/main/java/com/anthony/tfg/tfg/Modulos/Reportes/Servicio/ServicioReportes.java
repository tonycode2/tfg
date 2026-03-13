package com.anthony.tfg.tfg.Modulos.Reportes.Servicio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.DTOs.Respuesta.ColillaPagoDTO;
import com.anthony.tfg.tfg.DTOs.Respuesta.ProyeccionCesantiaDTO;
import com.anthony.tfg.tfg.DTOs.Respuesta.ReporteAntiguedadDTO;
import com.anthony.tfg.tfg.DTOs.Respuesta.ReporteDeduccionesDTO;
import com.anthony.tfg.tfg.DTOs.Respuesta.ReporteIncapacidadesDTO2;
import com.anthony.tfg.tfg.DTOs.Respuesta.ReporteLiquidacionDTO;
import com.anthony.tfg.tfg.DTOs.Respuesta.ReportePlanillaDTO;
import com.anthony.tfg.tfg.DTOs.Respuesta.ReporteVacacionesDTO;
import com.anthony.tfg.tfg.Entidades.Empleados;
import com.anthony.tfg.tfg.Entidades.Incapacidades;
import com.anthony.tfg.tfg.Entidades.Liquidaciones;
import com.anthony.tfg.tfg.Entidades.PlanillaDetalle;
import com.anthony.tfg.tfg.Entidades.PlanillaEncabezado;
import com.anthony.tfg.tfg.Exceptions.BadRequestException;
import com.anthony.tfg.tfg.Exceptions.ResourceNotFoundException;
import com.anthony.tfg.tfg.Repositorios.EmpleadosRepositorio;
import com.anthony.tfg.tfg.Repositorios.IncapacidadesRepositorio;
import com.anthony.tfg.tfg.Repositorios.LiquidacionesRepositorio;
import com.anthony.tfg.tfg.Repositorios.PlanillaDetalleRepositorio;
import com.anthony.tfg.tfg.Repositorios.PlanillaEncabezadoRepositorio;
import com.anthony.tfg.tfg.Util.ReportesConstantes;

import lombok.extern.slf4j.Slf4j;

/**
 * Servicio principal para la generación de datos de reportes de RH.
 * Contiene la lógica de cálculo para los 8 reportes.
 */
@Service
@Slf4j
public class ServicioReportes {

    private final EmpleadosRepositorio empleadosRepositorio;
    private final PlanillaEncabezadoRepositorio planillaEncabezadoRepositorio;
    private final PlanillaDetalleRepositorio planillaDetalleRepositorio;
    private final IncapacidadesRepositorio incapacidadesRepositorio;
    private final LiquidacionesRepositorio liquidacionesRepositorio;

	private static final DateTimeFormatter FORMATO_FECHA_HORA_ES = DateTimeFormatter
		.ofPattern(ReportesConstantes.FORMATO_FECHA_HORA_REPORTE, Locale.of("es", "CR"));

    public ServicioReportes(EmpleadosRepositorio empleadosRepositorio,
			    PlanillaEncabezadoRepositorio planillaEncabezadoRepositorio,
			    PlanillaDetalleRepositorio planillaDetalleRepositorio,
			    IncapacidadesRepositorio incapacidadesRepositorio,
			    LiquidacionesRepositorio liquidacionesRepositorio) {
	this.empleadosRepositorio = empleadosRepositorio;
	this.planillaEncabezadoRepositorio = planillaEncabezadoRepositorio;
	this.planillaDetalleRepositorio = planillaDetalleRepositorio;
	this.incapacidadesRepositorio = incapacidadesRepositorio;
	this.liquidacionesRepositorio = liquidacionesRepositorio;
    }

    // =====================================================================
    // 1. REPORTE DE PLANILLA
    // =====================================================================

    /**
     * Genera los datos del reporte de planilla para un encabezado de planilla específico.
     *
     * @param planillaId ID del encabezado de planilla
     * @return DTO con los datos del reporte de planilla
     */
    public ReportePlanillaDTO generarReportePlanilla(Long planillaId) {
	log.info("Generando reporte de planilla para planilla ID: {}", planillaId);

	PlanillaEncabezado encabezado = planillaEncabezadoRepositorio.findById(planillaId)
		.orElseThrow(() -> new ResourceNotFoundException("PlanillaEncabezado", "id", planillaId));

	List<PlanillaDetalle> detalles = planillaDetalleRepositorio.findByPlanillaEncabezadoId(planillaId);
	if (detalles.isEmpty()) {
	    throw new BadRequestException("La planilla no tiene detalles asociados");
	}

	double totalBruto = 0.0;
	double totalDeducciones = 0.0;
	double totalNeto = 0.0;

	List<ReportePlanillaDTO.DetallePlanillaReporteDTO> empleadosReporte = new ArrayList<>();

	for (PlanillaDetalle detalle : detalles) {
	    Empleados emp = detalle.getEmpleado();

	    double salarioBase = safe(detalle.getSalarioBasePeriodo());
	    double horasExtra = safe(detalle.getMontoHorasExtra());
	    double feriadosTrabajados = safe(detalle.getMontoFeriadosTrabajados());
	    double incapacidad = safe(detalle.getMontoIncapacidad());
	    double devengado = salarioBase + horasExtra + feriadosTrabajados + incapacidad;

	    double ccssIvm = safe(detalle.getDeduccionCcssIvm());
	    double ccssSem = safe(detalle.getDeduccionCcssSem());
	    double renta = safe(detalle.getImpuestoDeRenta());
	    double otras = safe(detalle.getOtrasDeducciones());
	    double deducciones = ccssIvm + ccssSem + renta + otras;

	    double neto = devengado - deducciones;

	    totalBruto += devengado;
	    totalDeducciones += deducciones;
	    totalNeto += neto;

	    empleadosReporte.add(ReportePlanillaDTO.DetallePlanillaReporteDTO.builder()
		    .cedula(emp != null ? emp.getCedula() : "")
		    .nombreCompleto(nombreCompleto(emp))
		    .puesto(emp != null && emp.getPuesto() != null ? emp.getPuesto().getNombre() : "")
		    .departamento(obtenerNombreDepartamento(emp))
		    .salarioBase(salarioBase)
		    .montoHorasExtra(horasExtra)
		    .montoFeriadosTrabajados(feriadosTrabajados)
		    .montoIncapacidad(incapacidad)
		    .totalDevengado(devengado)
		    .deduccionCcssIvm(ccssIvm)
		    .deduccionCcssSem(ccssSem)
		    .impuestoDeRenta(renta)
		    .otrasDeducciones(otras)
		    .totalDeducciones(deducciones)
		    .salarioNeto(neto)
		    .build());
	}

	return ReportePlanillaDTO.builder()
		.nombreEmpresa(ReportesConstantes.NOMBRE_EMPRESA)
		.tituloReporte(ReportesConstantes.TITULO_REPORTE_PLANILLA)
		.fechaInicioPeriodo(encabezado.getFechaInicioPeriodo())
		.fechaFinPeriodo(encabezado.getFechaFinPeriodo())
		.fechaGeneracion(formatearFechaHora(LocalDateTime.now()))
		.tipoQuincena(encabezado.getTipoQuincena() != null ? encabezado.getTipoQuincena().name() : "")
		.empleados(empleadosReporte)
		.totalBruto(totalBruto)
		.totalDeducciones(totalDeducciones)
		.totalNeto(totalNeto)
		.build();
    }

    // =====================================================================
    // 2. COLILLA DE PAGO
    // =====================================================================

    /**
     * Genera la colilla de pago para un detalle de planilla específico.
     *
     * @param detalleId ID del detalle de planilla
     * @return DTO con la colilla de pago
     */
    public ColillaPagoDTO generarColillaPago(Long detalleId) {
	log.info("Generando colilla de pago para detalle ID: {}", detalleId);

	PlanillaDetalle detalle = planillaDetalleRepositorio.findById(detalleId)
		.orElseThrow(() -> new ResourceNotFoundException("PlanillaDetalle", "id", detalleId));

	Empleados emp = detalle.getEmpleado();
	PlanillaEncabezado encabezado = detalle.getPlanillaEncabezado();

	double salarioBase = safe(detalle.getSalarioBasePeriodo());
	double horasExtra = safe(detalle.getMontoHorasExtra());
	double feriadosTrabajados = safe(detalle.getMontoFeriadosTrabajados());
	double incapacidad = safe(detalle.getMontoIncapacidad());
	double devengado = salarioBase + horasExtra + feriadosTrabajados + incapacidad;

	double ccssIvm = safe(detalle.getDeduccionCcssIvm());
	double ccssSem = safe(detalle.getDeduccionCcssSem());
	double renta = safe(detalle.getImpuestoDeRenta());
	double otras = safe(detalle.getOtrasDeducciones());
	double deducciones = ccssIvm + ccssSem + renta + otras;
	double neto = devengado - deducciones;

	return ColillaPagoDTO.builder()
		.nombreEmpresa(ReportesConstantes.NOMBRE_EMPRESA)
		.tituloReporte(ReportesConstantes.TITULO_COLILLA_PAGO)
		.fechaGeneracion(formatearFechaHora(LocalDateTime.now()))
		.idEmpleado(emp != null ? emp.getId() : null)
		.cedula(emp != null ? emp.getCedula() : "")
		.nombreCompleto(nombreCompleto(emp))
		.puesto(emp != null && emp.getPuesto() != null ? emp.getPuesto().getNombre() : "")
		.departamento(obtenerNombreDepartamento(emp))
		.cuentaIban(emp != null ? emp.getCuentaIban() : "")
		.fechaInicioPeriodo(encabezado.getFechaInicioPeriodo())
		.fechaFinPeriodo(encabezado.getFechaFinPeriodo())
		.fechaPago(encabezado.getFechaPago())
		.tipoQuincena(encabezado.getTipoQuincena() != null ? encabezado.getTipoQuincena().name() : "")
		.salarioBase(salarioBase)
		.montoHorasExtra(horasExtra)
		.montoFeriadosTrabajados(feriadosTrabajados)
		.montoIncapacidad(incapacidad)
		.totalDevengado(devengado)
		.deduccionCcssIvm(ccssIvm)
		.deduccionCcssSem(ccssSem)
		.impuestoDeRenta(renta)
		.otrasDeducciones(otras)
		.totalDeducciones(deducciones)
		.salarioNeto(neto)
		.build();
    }

    // =====================================================================
    // 3. REPORTE DE VACACIONES
    // =====================================================================

    /**
     * Genera el reporte de vacaciones para todos los empleados activos.
     *
     * @return DTO con los datos del reporte de vacaciones
     */
    public ReporteVacacionesDTO generarReporteVacaciones() {
	log.info("Generando reporte de vacaciones");

	List<Empleados> empleadosActivos = empleadosRepositorio.findByEstaActivoTrue();
	int totalDiasPendientes = 0;

	List<ReporteVacacionesDTO.DetalleVacacionesDTO> detalles = new ArrayList<>();
	for (Empleados emp : empleadosActivos) {
	    int saldo = emp.getSaldoVacaciones() != null ? emp.getSaldoVacaciones() : 0;

	    long aniosTrabajados = ChronoUnit.YEARS.between(emp.getFechaIngreso(), LocalDate.now());
	    int diasAcumulados = (int) (aniosTrabajados * 14);

	    int diasDisfrutados = diasAcumulados - saldo;
	    if (diasDisfrutados < 0) diasDisfrutados = 0;

	    boolean tieneVencidos = saldo > 28;

	    totalDiasPendientes += saldo;

	    detalles.add(ReporteVacacionesDTO.DetalleVacacionesDTO.builder()
		    .cedula(emp.getCedula())
		    .nombreCompleto(nombreCompleto(emp))
		    .departamento(obtenerNombreDepartamento(emp))
		    .fechaIngreso(emp.getFechaIngreso())
		    .diasAcumulados(diasAcumulados)
		    .diasDisfrutados(diasDisfrutados)
		    .diasPendientes(saldo)
		    .tieneVencidos(tieneVencidos)
		    .build());
	}

	return ReporteVacacionesDTO.builder()
		.nombreEmpresa(ReportesConstantes.NOMBRE_EMPRESA)
		.tituloReporte(ReportesConstantes.TITULO_REPORTE_VACACIONES)
		.fechaGeneracion(formatearFechaHora(LocalDateTime.now()))
		.empleados(detalles)
		.totalDiasPendientes(totalDiasPendientes)
		.build();
    }

    // =====================================================================
    // 4. REPORTE DE ANTIGÜEDAD
    // =====================================================================

    /**
     * Genera el reporte de antigüedad para todos los empleados activos.
     *
     * @return DTO con los datos del reporte de antigüedad
     */
    public ReporteAntiguedadDTO generarReporteAntiguedad() {
	log.info("Generando reporte de antigüedad");

	List<Empleados> empleadosActivos = empleadosRepositorio.findByEstaActivoTrue();

	List<ReporteAntiguedadDTO.DetalleAntiguedadDTO> detalles = new ArrayList<>();
	for (Empleados emp : empleadosActivos) {
	    LocalDate fechaIngreso = emp.getFechaIngreso();
	    LocalDate hoy = LocalDate.now();

	    long totalDias = ChronoUnit.DAYS.between(fechaIngreso, hoy);
	    long anios = totalDias / 365;
	    long meses = (totalDias % 365) / 30;
	    long dias = (totalDias % 365) % 30;

	    String clasificacion = clasificarAntiguedad(anios);

	    detalles.add(ReporteAntiguedadDTO.DetalleAntiguedadDTO.builder()
		    .cedula(emp.getCedula())
		    .nombreCompleto(nombreCompleto(emp))
		    .departamento(obtenerNombreDepartamento(emp))
		    .puesto(emp.getPuesto() != null ? emp.getPuesto().getNombre() : "")
		    .fechaIngreso(fechaIngreso)
		    .anios(anios)
		    .meses(meses)
		    .dias(dias)
		    .clasificacion(clasificacion)
		    .build());
	}

	detalles.sort((a, b) -> {
	    long diasA = a.getAnios() * 365 + a.getMeses() * 30 + a.getDias();
	    long diasB = b.getAnios() * 365 + b.getMeses() * 30 + b.getDias();
	    return Long.compare(diasB, diasA);
	});

	return ReporteAntiguedadDTO.builder()
		.nombreEmpresa(ReportesConstantes.NOMBRE_EMPRESA)
		.tituloReporte(ReportesConstantes.TITULO_REPORTE_ANTIGUEDAD)
		.fechaGeneracion(formatearFechaHora(LocalDateTime.now()))
		.empleados(detalles)
		.build();
    }

    // =====================================================================
    // 5. REPORTE DE DEDUCCIONES LEGALES
    // =====================================================================

    /**
     * Genera el reporte de deducciones legales para una planilla específica.
     *
     * @param planillaId ID del encabezado de planilla
     * @return DTO con los datos del reporte de deducciones
     */
    public ReporteDeduccionesDTO generarReporteDeducciones(Long planillaId) {
	log.info("Generando reporte de deducciones para planilla ID: {}", planillaId);

	PlanillaEncabezado encabezado = planillaEncabezadoRepositorio.findById(planillaId)
		.orElseThrow(() -> new ResourceNotFoundException("PlanillaEncabezado", "id", planillaId));

	List<PlanillaDetalle> detalles = planillaDetalleRepositorio.findByPlanillaEncabezadoId(planillaId);
	if (detalles.isEmpty()) {
	    throw new BadRequestException("La planilla no tiene detalles asociados");
	}

	double totalCcssIvm = 0.0;
	double totalCcssSem = 0.0;
		double totalCcssPatrono = 0.0;
	double totalRenta = 0.0;
	double totalOtras = 0.0;
	double granTotal = 0.0;

	List<ReporteDeduccionesDTO.DetalleDeduccionesDTO> empleadosReporte = new ArrayList<>();

	for (PlanillaDetalle detalle : detalles) {
	    Empleados emp = detalle.getEmpleado();

	    double salarioBase = safe(detalle.getSalarioBasePeriodo());
	    double horasExtra = safe(detalle.getMontoHorasExtra());
	    double incapacidad = safe(detalle.getMontoIncapacidad());
	    double bruto = salarioBase + horasExtra + incapacidad;

	    double ccssIvm = safe(detalle.getDeduccionCcssIvm());
	    double ccssSem = safe(detalle.getDeduccionCcssSem());
		double ccssPatrono = bruto * 0.2683;
	    double renta = safe(detalle.getImpuestoDeRenta());
	    double otras = safe(detalle.getOtrasDeducciones());
	    double total = ccssIvm + ccssSem + renta + otras;

	    totalCcssIvm += ccssIvm;
	    totalCcssSem += ccssSem;
		totalCcssPatrono += ccssPatrono;
	    totalRenta += renta;
	    totalOtras += otras;
	    granTotal += total;

		empleadosReporte.add(ReporteDeduccionesDTO.DetalleDeduccionesDTO.builder()
		    .cedula(emp != null ? emp.getCedula() : "")
		    .nombreCompleto(nombreCompleto(emp))
		    .departamento(obtenerNombreDepartamento(emp))
		    .salarioBruto(bruto)
		    .deduccionCcssIvm(ccssIvm)
			    .deduccionCcssSem(ccssSem)
			    .ccssPatrono(ccssPatrono)
		    .impuestoDeRenta(renta)
		    .otrasDeducciones(otras)
		    .totalDeducciones(total)
		    .build());
	}

	return ReporteDeduccionesDTO.builder()
		.nombreEmpresa(ReportesConstantes.NOMBRE_EMPRESA)
		.tituloReporte(ReportesConstantes.TITULO_REPORTE_DEDUCCIONES)
		.fechaGeneracion(formatearFechaHora(LocalDateTime.now()))
		.fechaInicioPeriodo(encabezado.getFechaInicioPeriodo())
		.fechaFinPeriodo(encabezado.getFechaFinPeriodo())
		.empleados(empleadosReporte)
		.totalCcssIvm(totalCcssIvm)
		.totalCcssSem(totalCcssSem)
			.totalCcssPatrono(totalCcssPatrono)
		.totalImpuestoRenta(totalRenta)
		.totalOtrasDeducciones(totalOtras)
		.granTotal(granTotal)
		.build();
    }

    // =====================================================================
    // 6. REPORTE DE LIQUIDACIÓN
    // =====================================================================

    /**
     * Genera el reporte de liquidación para una liquidación específica.
     *
     * @param liquidacionId ID de la liquidación
     * @return DTO con los datos del reporte de liquidación
     */
    public ReporteLiquidacionDTO generarReporteLiquidacion(Long liquidacionId) {
	log.info("Generando reporte de liquidación para liquidación ID: {}", liquidacionId);

	Liquidaciones liquidacion = liquidacionesRepositorio.findById(liquidacionId)
		.orElseThrow(() -> new ResourceNotFoundException("Liquidaciones", "id", liquidacionId));

	Empleados emp = liquidacion.getEmpleado();

	long totalDias = liquidacion.getDiasTrabajadosTotal() != null ? liquidacion.getDiasTrabajadosTotal() : 0;
	long anios = totalDias / 365;
	long meses = (totalDias % 365) / 30;
	long dias = (totalDias % 365) % 30;
	String antiguedadTexto = anios + " años, " + meses + " meses, " + dias + " días";

	List<ReporteLiquidacionDTO.RubroLiquidacionDTO> rubros = new ArrayList<>();

	rubros.add(ReporteLiquidacionDTO.RubroLiquidacionDTO.builder()
		.concepto("Preaviso")
		.detalle(liquidacion.getPreaviso_pagado() != null && liquidacion.getPreaviso_pagado()
			? "Aplica preaviso" : "No aplica")
		.monto(safe(liquidacion.getMontoPreaviso()))
		.build());

	rubros.add(ReporteLiquidacionDTO.RubroLiquidacionDTO.builder()
		.concepto("Cesantía")
		.detalle("Según tabla Art. 29 Código de Trabajo")
		.monto(safe(liquidacion.getMontoCesantia()))
		.build());

	rubros.add(ReporteLiquidacionDTO.RubroLiquidacionDTO.builder()
		.concepto("Vacaciones pendientes")
		.detalle("Días de vacaciones no disfrutados")
		.monto(safe(liquidacion.getMontoVacacionesPendientes()))
		.build());

	rubros.add(ReporteLiquidacionDTO.RubroLiquidacionDTO.builder()
		.concepto("Aguinaldo proporcional")
		.detalle("Proporcional al tiempo trabajado en el período")
		.monto(safe(liquidacion.getMontoAguinaldoPendiente()))
		.build());

	rubros.add(ReporteLiquidacionDTO.RubroLiquidacionDTO.builder()
		.concepto("Salario proporcional")
		.detalle("Días trabajados del mes de salida")
		.monto(safe(liquidacion.getMontoSalarioProporcional()))
		.build());

	return ReporteLiquidacionDTO.builder()
		.nombreEmpresa(ReportesConstantes.NOMBRE_EMPRESA)
		.tituloReporte(ReportesConstantes.TITULO_REPORTE_LIQUIDACIONES)
		.fechaGeneracion(formatearFechaHora(LocalDateTime.now()))
		.cedula(emp != null ? emp.getCedula() : "")
		.nombreCompleto(nombreCompleto(emp))
		.puesto(emp != null && emp.getPuesto() != null ? emp.getPuesto().getNombre() : "")
		.departamento(obtenerNombreDepartamento(emp))
		.fechaIngreso(emp != null ? emp.getFechaIngreso() : null)
		.fechaSalida(liquidacion.getFechaSalida())
		.motivoSalida(liquidacion.getMotivoSalida() != null ? liquidacion.getMotivoSalida().name() : "")
		.antiguedadTexto(antiguedadTexto)
		.salarioPromedioDiario(safe(liquidacion.getSalarioPromedioDiario()))
		.rubros(rubros)
		.totalLiquidacion(safe(liquidacion.getTotalLiquidacion()))
		.build();
    }

    // =====================================================================
    // 7. REPORTE DE INCAPACIDADES
    // =====================================================================

    /**
     * Genera el reporte de incapacidades para un rango de fechas.
     *
     * @param fechaInicio Fecha de inicio del rango
     * @param fechaFin    Fecha de fin del rango
     * @return DTO con los datos del reporte de incapacidades
     */
    public ReporteIncapacidadesDTO2 generarReporteIncapacidades(LocalDate fechaInicio, LocalDate fechaFin) {
	log.info("Generando reporte de incapacidades del {} al {}", fechaInicio, fechaFin);

	if (fechaInicio.isAfter(fechaFin)) {
	    throw new BadRequestException("La fecha de inicio no puede ser posterior a la fecha de fin");
	}

	List<Incapacidades> incapacidades = incapacidadesRepositorio.findByFechaInicioBetween(fechaInicio, fechaFin);

	int totalDiasCCSS = 0;
	int totalDiasINS = 0;
	int totalDiasOtros = 0;

	List<ReporteIncapacidadesDTO2.DetalleIncapacidadReporteDTO> detalles = new ArrayList<>();
	for (Incapacidades inc : incapacidades) {
	    Empleados emp = inc.getEmpleado();
	    int diasTotales = inc.getDiasTotales() != null ? inc.getDiasTotales() : 0;

	    String entidad = inc.getEntidadEmisora() != null ? inc.getEntidadEmisora().name() : "OTRO";
	    switch (entidad) {
		case "CCSS" -> totalDiasCCSS += diasTotales;
		case "INS" -> totalDiasINS += diasTotales;
		default -> totalDiasOtros += diasTotales;
	    }

	    detalles.add(ReporteIncapacidadesDTO2.DetalleIncapacidadReporteDTO.builder()
		    .cedula(emp != null ? emp.getCedula() : "")
		    .nombreCompleto(nombreCompleto(emp))
		    .departamento(obtenerNombreDepartamento(emp))
		    .tipoIncapacidad(inc.getTipoIncapacidad() != null ? inc.getTipoIncapacidad().name() : "")
		    .entidadEmisora(entidad)
		    .fechaInicio(inc.getFechaInicio())
		    .fechaFin(inc.getFechaFin())
		    .diasTotales(diasTotales)
		    .estado(inc.getEstadoSolicitud() != null ? inc.getEstadoSolicitud().name() : "")
		    .build());
	}

	return ReporteIncapacidadesDTO2.builder()
		.nombreEmpresa(ReportesConstantes.NOMBRE_EMPRESA)
		.tituloReporte(ReportesConstantes.TITULO_REPORTE_INCAPACIDADES)
		.fechaGeneracion(formatearFechaHora(LocalDateTime.now()))
		.fechaInicioPeriodo(fechaInicio)
		.fechaFinPeriodo(fechaFin)
		.incapacidades(detalles)
		.totalDiasCCSS(totalDiasCCSS)
		.totalDiasINS(totalDiasINS)
		.totalDiasOtros(totalDiasOtros)
		.totalDiasGeneral(totalDiasCCSS + totalDiasINS + totalDiasOtros)
		.build();
    }

    // =====================================================================
    // 8. PROYECCIÓN DE CESANTÍA
    // =====================================================================

    /**
     * Genera la proyección de cesantía para todos los empleados activos.
     *
     * @return DTO con los datos de la proyección de cesantía
     */
    public ProyeccionCesantiaDTO generarProyeccionCesantia() {
	log.info("Generando proyección de cesantía");

	List<Empleados> empleadosActivos = empleadosRepositorio.findByEstaActivoTrue();
	LocalDate hoy = LocalDate.now();
	LocalDate hace6Meses = hoy.minusMonths(6);

	double montoTotalProyectado = 0.0;
	List<ProyeccionCesantiaDTO.DetalleCesantiaDTO> detalles = new ArrayList<>();

	for (Empleados emp : empleadosActivos) {
	    LocalDate fechaIngreso = emp.getFechaIngreso();
	    long mesesTrabajados = ChronoUnit.MONTHS.between(fechaIngreso, hoy);

	    long totalDias = ChronoUnit.DAYS.between(fechaIngreso, hoy);
	    long anios = totalDias / 365;
	    long mesesResto = (totalDias % 365) / 30;
	    long diasResto = (totalDias % 365) % 30;
	    String antiguedadTexto = anios + " años, " + mesesResto + " meses, " + diasResto + " días";

	    Double devengado6Meses = planillaDetalleRepositorio
		    .sumDevengadoByEmpleadoAndFechaPagoBetween(emp.getId(), hace6Meses, hoy);
	    double salarioPromedio = devengado6Meses != null && devengado6Meses > 0
		    ? devengado6Meses / 6.0
		    : (emp.getPuesto() != null && emp.getPuesto().getSalarioMinimo() != null
			    ? emp.getPuesto().getSalarioMinimo()
			    : 0.0);

	    double salarioPromedioDiario = salarioPromedio / 30.0;

	    int diasCesantia = ReportesConstantes.diasCesantiaPorAntiguedad(mesesTrabajados);

	    double montoEstimado = diasCesantia * salarioPromedioDiario;
	    montoTotalProyectado += montoEstimado;

	    detalles.add(ProyeccionCesantiaDTO.DetalleCesantiaDTO.builder()
		    .cedula(emp.getCedula())
		    .nombreCompleto(nombreCompleto(emp))
		    .departamento(obtenerNombreDepartamento(emp))
		    .fechaIngreso(fechaIngreso)
		    .antiguedadTexto(antiguedadTexto)
		    .mesesTrabajados(mesesTrabajados)
		    .salarioPromedio(salarioPromedio)
		    .diasCesantia(diasCesantia)
		    .montoEstimado(montoEstimado)
		    .build());
	}

	detalles.sort((a, b) -> Double.compare(b.getMontoEstimado(), a.getMontoEstimado()));

	return ProyeccionCesantiaDTO.builder()
		.nombreEmpresa(ReportesConstantes.NOMBRE_EMPRESA)
		.tituloReporte(ReportesConstantes.TITULO_PROYECCION_CESANTIA)
		.fechaGeneracion(formatearFechaHora(LocalDateTime.now()))
		.empleados(detalles)
		.montoTotalProyectado(montoTotalProyectado)
		.build();
    }

    // =====================================================================
    // UTILITY METHODS
    // =====================================================================

    /**
     * Ejecuta la logica principal de nombreCompleto.
     * @param emp parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    private String nombreCompleto(Empleados emp) {
	if (emp == null) return "";
	StringBuilder sb = new StringBuilder();
	if (emp.getNombre() != null) sb.append(emp.getNombre());
	if (emp.getPrimerApellido() != null) sb.append(" ").append(emp.getPrimerApellido());
	if (emp.getSegundoApellido() != null) sb.append(" ").append(emp.getSegundoApellido());
	return sb.toString().trim();
    }

    /**
     * Obtiene informacion necesaria para la operacion.
     * @param emp parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    private String obtenerNombreDepartamento(Empleados emp) {
	if (emp == null || emp.getPuesto() == null || emp.getPuesto().getDepartamento() == null) {
	    return "";
	}
	return emp.getPuesto().getDepartamento().getNombre();
    }

    /**
     * Ejecuta la logica principal de clasificarAntiguedad.
     * @param anios parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    private String clasificarAntiguedad(long anios) {
	if (anios < 1) return "Menos de 1 año";
	if (anios < 5) return "1-5 años";
	if (anios < 10) return "5-10 años";
	return "Más de 10 años";
    }

    /**
     * Ejecuta la logica principal de formatearFechaHora.
     * @param fechaHora parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    private String formatearFechaHora(LocalDateTime fechaHora) {
	return fechaHora.format(FORMATO_FECHA_HORA_ES);
    }

    /**
     * Ejecuta la logica principal de safe.
     * @param value parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    private double safe(Double value) {
	return value == null ? 0.0 : value;
    }
}

