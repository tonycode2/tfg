package com.anthony.tfg.tfg.Modulos.Planilla.Servicio;

import java.io.UnsupportedEncodingException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaPlanillaDetalleDTO;
import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaPlanillaEmpleadoDTO;
import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaPlanillaEncabezadoDTO;
import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaPlanillaPdfDTO;
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
import com.anthony.tfg.tfg.Exceptions.ConflictException;
import com.anthony.tfg.tfg.Exceptions.ForbiddenException;
import com.anthony.tfg.tfg.Exceptions.ResourceNotFoundException;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasConfiguracionRentas;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasPlanillaEncabezado;
import com.anthony.tfg.tfg.Modulos.Interfaces.ServicioInterface;
import com.anthony.tfg.tfg.Modulos.Mantenimientos.MantenimientosPlanillaEncabezados;
import com.anthony.tfg.tfg.Modulos.Seguridad.user.User;
import com.anthony.tfg.tfg.Repositorios.DiasFeriadosRepositorio;
import com.anthony.tfg.tfg.Repositorios.EmpleadosRepositorio;
import com.anthony.tfg.tfg.Repositorios.JornadaDiariaRepositorio;
import com.anthony.tfg.tfg.Repositorios.PlanillaDetalleRepositorio;
import com.anthony.tfg.tfg.Util.PlanillaPdfStorageService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ServicioPlanilla implements ServicioInterface<RespuestaPlanillaEncabezadoDTO, 
                                                        SolicitudPlanillaEncabezadoDTO, 
                                                        PlanillaEncabezado>{

    private static final double CREDITO_POR_HIJO = 1720.0;
    private static final double CREDITO_POR_CASADO = 2600.0;
    private static final int DIAS_PAGO_PATRONAL_INCAPACIDAD = 3;

    private final ConsultasPlanillaEncabezado consulta;
    private final MantenimientosPlanillaEncabezados mantenimiento;
    private final PlanillaDetalleRepositorio planillaDetalleRepo;
    private final EmpleadosRepositorio empleadosRepositorio;
    private final JornadaDiariaRepositorio jornadaDiariaRepositorio;
    private final DiasFeriadosRepositorio diasFeriadosRepositorio;
    private final ConsultasConfiguracionRentas consultasConfiguracionRentas;
    private final PlanillaPdfStorageService planillaPdfStorageService;

    public ServicioPlanilla(ConsultasPlanillaEncabezado consulta, 
                           MantenimientosPlanillaEncabezados mantenimiento,
                           PlanillaDetalleRepositorio planillaDetalleRepo,
                           EmpleadosRepositorio empleadosRepositorio,
                           JornadaDiariaRepositorio jornadaDiariaRepositorio,
                           DiasFeriadosRepositorio diasFeriadosRepositorio,
                           ConsultasConfiguracionRentas consultasConfiguracionRentas,
                           PlanillaPdfStorageService planillaPdfStorageService) {
        this.consulta = consulta;
        this.mantenimiento = mantenimiento;
        this.planillaDetalleRepo = planillaDetalleRepo;
        this.empleadosRepositorio = empleadosRepositorio;
        this.jornadaDiariaRepositorio = jornadaDiariaRepositorio;
        this.diasFeriadosRepositorio = diasFeriadosRepositorio;
        this.consultasConfiguracionRentas = consultasConfiguracionRentas;
        this.planillaPdfStorageService = planillaPdfStorageService;
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
        dto.cantidadDiasNoTrabajadosEnQuincena = detalle.getCantidadDiasNoTrabajadosEnQuincena() != null
            ? detalle.getCantidadDiasNoTrabajadosEnQuincena()
            : 0;
        dto.montoHorasExtra = detalle.getMontoHorasExtra() != null ? detalle.getMontoHorasExtra() : 0.0;
        dto.montoFeriadosTrabajados = detalle.getMontoFeriadosTrabajados() != null
            ? detalle.getMontoFeriadosTrabajados()
            : 0.0;
        dto.montoIncapacidad = detalle.getMontoIncapacidad() != null ? detalle.getMontoIncapacidad() : 0.0;
        dto.deduccionCcssIvm = detalle.getDeduccionCcssIvm() != null ? detalle.getDeduccionCcssIvm() : 0.0;
        dto.deduccionCcssSem = detalle.getDeduccionCcssSem() != null ? detalle.getDeduccionCcssSem() : 0.0;
        dto.impuestoDeRenta = detalle.getImpuestoDeRenta() != null ? detalle.getImpuestoDeRenta() : 0.0;
        dto.otrasDeducciones = detalle.getOtrasDeducciones() != null ? detalle.getOtrasDeducciones() : 0.0;
        dto.urlPdf = detalle.getUrlPdf() != null
            ? "/api/planillas/detalles/" + detalle.getId() + "/pdf"
            : null;
        
        // Calcular totales
        dto.totalDevengado = dto.salarioBasePeriodo + dto.montoHorasExtra + dto.montoFeriadosTrabajados
            + dto.montoIncapacidad;
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
        dto.cantidadDiasNoTrabajadosEnQuincena = detalle.getCantidadDiasNoTrabajadosEnQuincena() != null
            ? detalle.getCantidadDiasNoTrabajadosEnQuincena()
            : 0;
        dto.montoHorasExtra = detalle.getMontoHorasExtra() != null ? detalle.getMontoHorasExtra() : 0.0;
        dto.montoFeriadosTrabajados = detalle.getMontoFeriadosTrabajados() != null
            ? detalle.getMontoFeriadosTrabajados()
            : 0.0;
        dto.montoIncapacidad = detalle.getMontoIncapacidad() != null ? detalle.getMontoIncapacidad() : 0.0;
        dto.deduccionCcssIvm = detalle.getDeduccionCcssIvm() != null ? detalle.getDeduccionCcssIvm() : 0.0;
        dto.deduccionCcssSem = detalle.getDeduccionCcssSem() != null ? detalle.getDeduccionCcssSem() : 0.0;
        dto.impuestoDeRenta = detalle.getImpuestoDeRenta() != null ? detalle.getImpuestoDeRenta() : 0.0;
        dto.otrasDeducciones = detalle.getOtrasDeducciones() != null ? detalle.getOtrasDeducciones() : 0.0;
        dto.urlPdf = detalle.getUrlPdf() != null
            ? "/api/planillas/detalles/" + detalle.getId() + "/pdf"
            : null;

        if (detalle.getEmpleado() != null) {
            dto.nombreEmpleado = detalle.getEmpleado().getNombre();
            dto.primerApellidoEmpleado = detalle.getEmpleado().getPrimerApellido();
            dto.segundoApellidoEmpleado = detalle.getEmpleado().getSegundoApellido();
        }

        return dto;
    }

    /**
     * Ejecuta la logica principal de guardarPdfPlanilla.
     * @param detalleId parametro de entrada de la operacion.
     * @param archivo parametro de entrada de la operacion.
     * @param auth parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    @Transactional
    public RespuestaPlanillaPdfDTO guardarPdfPlanilla(Long detalleId, MultipartFile archivo, Authentication auth) {
        PlanillaDetalle detalle = planillaDetalleRepo.findById(detalleId)
                .orElseThrow(() -> new ResourceNotFoundException("PlanillaDetalle", "id", detalleId));
        validarAccesoDetalle(detalle, auth);

        if (detalle.getUrlPdf() != null) {
            planillaPdfStorageService.deleteFile(detalle.getUrlPdf());
        }

        String fileName = planillaPdfStorageService.storePdf(archivo, detalleId);
        detalle.setUrlPdf(fileName);
        planillaDetalleRepo.save(detalle);

        String urlPdf = "/api/planillas/detalles/" + detalleId + "/pdf";
        return new RespuestaPlanillaPdfDTO(urlPdf);
    }

    /** 
     * @param detalleId
     * @param auth
     * @return ResponseEntity<Resource>
     * @throws UnsupportedEncodingException
     */
    public ResponseEntity<Resource> descargarPdfPlanilla(Long detalleId, Authentication auth) throws UnsupportedEncodingException {
        PlanillaDetalle detalle = planillaDetalleRepo.findById(detalleId)
                .orElseThrow(() -> new ResourceNotFoundException("PlanillaDetalle", "id", detalleId));
        validarAccesoDetalle(detalle, auth);

        if (detalle.getUrlPdf() == null) {
            throw new BadRequestException("No existe un PDF generado para esta planilla");
        }

        Resource recurso = planillaPdfStorageService.loadFileAsResource(detalle.getUrlPdf());
        String contentType = java.net.URLConnection.guessContentTypeFromName(recurso.getFilename());
        if (contentType == null) {
            contentType = MediaType.APPLICATION_PDF_VALUE;
        }

        String originalFilename = recurso.getFilename();
        String extension = "";
        if (originalFilename != null) {
            int dot = originalFilename.lastIndexOf('.');
            if (dot > -1) {
                extension = originalFilename.substring(dot);
            }
        }

        String empleadoNombre = detalle.getEmpleado() != null && detalle.getEmpleado().getNombre() != null
                ? detalle.getEmpleado().getNombre()
                : "";
        String empleadoApellido = detalle.getEmpleado() != null && detalle.getEmpleado().getPrimerApellido() != null
                ? detalle.getEmpleado().getPrimerApellido()
                : "";
        String suggested = "Planilla " + detalle.getId() + " " + empleadoNombre + " " + empleadoApellido;
        String safe = suggested.replaceAll("[^\\p{L}\\p{N} _.-]", "").replaceAll("\\s+", " ").trim();
        String filename = (safe.isEmpty() ? "Planilla_" + detalle.getId() : safe) + extension;
        String encodedFilename = java.net.URLEncoder.encode(filename, java.nio.charset.StandardCharsets.UTF_8.toString())
                .replaceAll("\\+", "%20");
        String contentDisposition = "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encodedFilename;

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(recurso);
    }

    /**
     * Valida reglas de negocio antes de continuar.
     * @param detalle parametro de entrada de la operacion.
     * @param auth parametro de entrada de la operacion.
     */
    private void validarAccesoDetalle(PlanillaDetalle detalle, Authentication auth) {
        Object principal = auth.getPrincipal();
        User user = (User) principal;
        String role = user.getRole().name();

        if ("ADMIN".equals(role) || "HR".equals(role)) {
            return;
        }

        Empleados empleado = user.getEmpleado();
        if (empleado == null || detalle.getEmpleado() == null || !empleado.getId().equals(detalle.getEmpleado().getId())) {
            throw new ForbiddenException("No tiene permisos para acceder a esta planilla");
        }
    }

    /**
     * Obtiene un registro por su identificador.
     * @param id parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public RespuestaPlanillaEncabezadoDTO obtenerPorId(Long id) {
        PlanillaEncabezado planilla = consulta.obtenerPorId(id);
        if(planilla == null){
            log.warn("No se ha encontrado la planilla con ID: " + id);
            throw new ResourceNotFoundException("PlanillaEncabezado", "id", id);
        }
        log.info("Se ha encontrado la planilla con ID: " + id);
        return deEntidadDtoARespuesta(planilla);
    }

    /**
     * Obtiene todos los registros disponibles.
     * @return resultado de la operacion.
     */
    public List<RespuestaPlanillaEncabezadoDTO> obtenerTodos() {
        List<PlanillaEncabezado> entidades = consulta.obtenerTodos();
        log.info("Se han obtenido todas las planillas. La cantidad de registros es: " + entidades.size());
        return deListaEntidadADto(entidades);
    }

    /**
     * Guarda un nuevo registro.
     * @param entidad parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
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
    LocalDate fechaPago = calcularFechaPago(solicitud.anio(), solicitud.mes(), solicitud.tipoQuincena());

    if (consulta.existePlanillaParaPeriodo(fechaInicioPeriodo, fechaFinPeriodo, solicitud.tipoQuincena())) {
        throw new ConflictException("Ya existe una planilla generada para el periodo seleccionado");
    }

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

    YearMonth yearMonth = YearMonth.of(solicitud.anio(), solicitud.mes());
    LocalDate fechaInicioMes = yearMonth.atDay(1);
    LocalDate fechaFinMes = yearMonth.atEndOfMonth();
    Set<LocalDate> fechasFeriadosMes = diasFeriadosRepositorio.findByFechaBetween(fechaInicioMes, fechaFinMes)
        .stream()
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
            fechaFinPeriodo, fechasFeriados, fechasFeriadosMes, tramosRenta, encabezadoGuardado))
        .toList();

    planillaDetalleRepo.saveAll(detalles);

    for (PlanillaDetalle detalle : detalles) {
        double totalDevengado = safe(detalle.getSalarioBasePeriodo())
            + safe(detalle.getMontoHorasExtra())
            + safe(detalle.getMontoFeriadosTrabajados())
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

    /**
     * Actualiza un registro existente.
     * @param id parametro de entrada de la operacion.
     * @param entidad parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
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

    /**
     * Ejecuta la logica principal de marcarComoPagada.
     * @param id parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    @Transactional
    public RespuestaPlanillaEncabezadoDTO marcarComoPagada(Long id) {
        PlanillaEncabezado planilla = consulta.obtenerPorId(id);
        if (planilla == null) {
            log.warn("No se ha encontrado la planilla con ID: {} para marcar como pagada", id);
            throw new ResourceNotFoundException("PlanillaEncabezado", "id", id);
        }
        
        if (planilla.getEstadoPlanilla() != EstadoPlanilla.BORRADOR) {
            throw new BadRequestException("Solo se pueden marcar como pagadas las planillas en estado BORRADOR");
        }

        planilla.setEstadoPlanilla(EstadoPlanilla.PAGADA);
        PlanillaEncabezado planillaActualizada = mantenimiento.actualizar(planilla);
        log.info("Se marcó como PAGADA la planilla con ID: {}", id);
        return deEntidadDtoARespuesta(planillaActualizada);
    }

    /**
     * Elimina un registro por su identificador.
     * @param id parametro de entrada de la operacion.
     */
    public void eliminar(Long id) {
        mantenimiento.eliminar(id);
        log.info("Se ha eliminado la planilla con ID: " + id);
    }

    /**
     * Convierte un DTO de solicitud a entidad.
     * @param solicitud parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
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

    /**
     * Convierte una entidad a DTO de respuesta.
     * @param entidad parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
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

    /**
     * Convierte una lista de entidades a DTOs de respuesta.
     * @param entidades parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public List<RespuestaPlanillaEncabezadoDTO> deListaEntidadADto(List<PlanillaEncabezado> entidades) {
        return entidades.stream()
                .map(this::deEntidadDtoARespuesta)
                .toList();
    }
    
    /**
     * Obtiene informacion necesaria para la operacion.
     * @param estado parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    private EstadoPlanilla obtenerEstadoPlanilla(String estado) {
        try {
            return EstadoPlanilla.valueOf(estado.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Obtiene informacion necesaria para la operacion.
     * @param tipoQuincena parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    private TipoQuincena obtenerTipoQuincena(String tipoQuincena) {
        try {
            return TipoQuincena.valueOf(tipoQuincena.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Realiza un calculo de negocio segun los datos de entrada.
     * @param anio parametro de entrada de la operacion.
     * @param mes parametro de entrada de la operacion.
     * @param tipoQuincena parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    private LocalDate calcularFechaInicioPeriodo(int anio, int mes, TipoQuincena tipoQuincena) {
        if (tipoQuincena == TipoQuincena.PRIMERA) {
            return YearMonth.of(anio, mes).minusMonths(1).atEndOfMonth();
        }
        return LocalDate.of(anio, mes, 15);
    }

    /**
     * Realiza un calculo de negocio segun los datos de entrada.
     * @param anio parametro de entrada de la operacion.
     * @param mes parametro de entrada de la operacion.
     * @param tipoQuincena parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    private LocalDate calcularFechaFinPeriodo(int anio, int mes, TipoQuincena tipoQuincena) {
        if (tipoQuincena == TipoQuincena.PRIMERA) {
            return LocalDate.of(anio, mes, 14);
        }
        YearMonth yearMonth = YearMonth.of(anio, mes);
        return yearMonth.atEndOfMonth().minusDays(1);
    }

    /**
     * Realiza un calculo de negocio segun los datos de entrada.
     * @param anio parametro de entrada de la operacion.
     * @param mes parametro de entrada de la operacion.
     * @param tipoQuincena parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    private LocalDate calcularFechaPago(int anio, int mes, TipoQuincena tipoQuincena) {
        if (tipoQuincena == TipoQuincena.PRIMERA) {
            return LocalDate.of(anio, mes, 15);
        }
        YearMonth yearMonth = YearMonth.of(anio, mes);
        return yearMonth.atEndOfMonth();
    }

    /** 
     * @param empleado
     * @param fechaInicio
     * @param fechaFin
     * @param feriados
     * @param feriadosMes
     * @param tramosRenta
     * @param encabezado
     * @return PlanillaDetalle
     */
    private PlanillaDetalle calcularDetallePlanilla(Empleados empleado,
                                                    LocalDate fechaInicio,
                                                    LocalDate fechaFin,
                                                    Set<LocalDate> feriados,
                                                    Set<LocalDate> feriadosMes,
                                                    List<ConfiguracionRenta> tramosRenta,
                                                    PlanillaEncabezado encabezado) {
        double salarioMensual = obtenerSalarioMensual(empleado);
        double salarioDiario = salarioMensual / 30.0;
        double salarioHora = salarioDiario / 8.0;

        TipoQuincena tipoQuincena = encabezado.getTipoQuincena();
        YearMonth yearMonth = YearMonth.from(fechaInicio);
        LocalDate inicioMes = yearMonth.atDay(1);
        LocalDate finMes = yearMonth.atEndOfMonth();
        List<JornadaDiaria> jornadas = tipoQuincena == TipoQuincena.SEGUNDA
            ? jornadaDiariaRepositorio.findByEmpleadoIdAndFechaBetween(empleado.getId(), inicioMes, finMes)
            : jornadaDiariaRepositorio.findByEmpleadoIdAndFechaBetween(empleado.getId(), fechaInicio, fechaFin);
        Map<LocalDate, JornadaDiaria> jornadasPorFecha = mapearJornadas(jornadas);
        
        // Validación: Si no hay jornadas diarias, no hay salario que pagar
        if (jornadas.isEmpty()) {
            return crearPlanillaDetalleCero(empleado, encabezado);
        }

        double basePeriodo = salarioMensual / 2.0;
        double totalHorasExtra = 0.0;
        int cantidadDiasFeriados = 0;
        int cantidadDiasNoTrabajados = 0;
        int cantidadDiasRebajables = 0;
        double montoIncapacidad = 0.0;

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
            boolean esLaborable = !esFinSemana && !esFeriado;

            if (esFeriado && tieneHorasTrabajadas) {
                cantidadDiasFeriados++;
            }

            boolean esVacaciones = jornada != null
                    && jornada.getPermiso() != null
                    && jornada.getPermiso().getTipoPermiso() == TipoPermiso.VACACIONES;
            boolean esPermisoSinGoce = jornada != null
                    && jornada.getPermiso() != null
                    && jornada.getPermiso().getTipoPermiso() == TipoPermiso.SIN_GOCE_SALARIO;
            boolean esIncapacidad = jornada != null && jornada.getIncapacidad() != null;

            if (esIncapacidad) {
                Integer diaIncapacidad = jornada.getDiaPermiso();
                if (diaIncapacidad != null && diaIncapacidad <= DIAS_PAGO_PATRONAL_INCAPACIDAD) {
                    TipoEntidadEmisora entidad = jornada.getIncapacidad().getEntidadEmisora();
                    double factorPago = entidad == TipoEntidadEmisora.CCSS ? 0.5
                            : entidad == TipoEntidadEmisora.INS ? 1.0
                            : 0.0;
                    // El patrono solo paga el porcentaje del salario diario en los primeros 3 días.
                    montoIncapacidad += salarioDiario * factorPago;
                }
            }

            if (esLaborable && (esIncapacidad || esPermisoSinGoce)) {
                cantidadDiasRebajables++;
            }

            if (esLaborable && !esVacaciones && (esIncapacidad || !tieneHorasTrabajadas)) {
                cantidadDiasNoTrabajados++;
            }

            fecha = fecha.plusDays(1);
        }

        double montoHorasExtra = totalHorasExtra * salarioHora * 1.5;
        double montoFeriadosTrabajados = cantidadDiasFeriados * salarioDiario;
        double rebajoDiasNoPagados = cantidadDiasRebajables * salarioDiario;
        double salarioBasePeriodo = Math.max(0.0, basePeriodo - rebajoDiasNoPagados);
        double totalDevengado = salarioBasePeriodo + montoHorasExtra + montoFeriadosTrabajados + montoIncapacidad;

        double deduccionCcssSem = totalDevengado * 0.055;
        double deduccionCcssIvm = totalDevengado * 0.0433;
        double otrasDeducciones = totalDevengado * 0.01;
        double ccssMensual = salarioMensual * 0.055 + salarioMensual * 0.0433;
        double impuestoRenta = calcularImpuestoRentaQuincena(tipoQuincena,
            empleado,
            salarioMensual,
            salarioDiario,
            salarioHora,
            inicioMes,
            finMes,
            jornadasPorFecha,
            feriadosMes,
            ccssMensual,
            tramosRenta);

        return PlanillaDetalle.builder()
                .salarioBasePeriodo(salarioBasePeriodo)
                .cantidadDiasFeriados(cantidadDiasFeriados)
                .cantidadDiasNoTrabajadosEnQuincena(cantidadDiasNoTrabajados)
                .montoHorasExtra(montoHorasExtra)
                .montoFeriadosTrabajados(montoFeriadosTrabajados)
                .montoIncapacidad(montoIncapacidad)
                .deduccionCcssIvm(deduccionCcssIvm)
                .deduccionCcssSem(deduccionCcssSem)
                .impuestoDeRenta(impuestoRenta)
                .otrasDeducciones(otrasDeducciones)
                .empleado(empleado)
                .planillaEncabezado(encabezado)
                .build();
    }

    /** 
     * @param tipoQuincena
     * @param empleado
     * @param salarioMensual
     * @param salarioDiario
     * @param salarioHora
     * @param inicioMes
     * @param finMes
     * @param jornadasPorFechaMes
     * @param feriadosMes
     * @param ccssMensual
     * @param tramosRenta
     * @return double
     */
    private double calcularImpuestoRentaQuincena(TipoQuincena tipoQuincena,
                                                 Empleados empleado,
                                                 double salarioMensual,
                                                 double salarioDiario,
                                                 double salarioHora,
                                                 LocalDate inicioMes,
                                                 LocalDate finMes,
                                                 Map<LocalDate, JornadaDiaria> jornadasPorFechaMes,
                                                 Set<LocalDate> feriadosMes,
                                                 double ccssMensual,
                                                 List<ConfiguracionRenta> tramosRenta) {
        if (tipoQuincena == TipoQuincena.PRIMERA) {
            return 0.0;
        }

        if (tipoQuincena == TipoQuincena.SEGUNDA) {
            ResumenRenta resumenMes = calcularResumenRenta(inicioMes, finMes, jornadasPorFechaMes, feriadosMes);
            double montoHorasExtraMes = resumenMes.totalHorasExtra() * salarioHora * 1.5;
            double montoFeriadosMes = resumenMes.cantidadDiasFeriados() * salarioDiario;
            double baseRentaMes = salarioMensual + montoHorasExtraMes + montoFeriadosMes - ccssMensual;
            double impuestoMensual = calcularImpuestoRenta(baseRentaMes, tramosRenta);
            double creditos = calcularCreditosFiscales(empleado);
            return Math.max(0.0, impuestoMensual - creditos);
        }

        return 0.0;
    }

    /**
     * Realiza un calculo de negocio segun los datos de entrada.
     * @param empleado parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    private double calcularCreditosFiscales(Empleados empleado) {
        int cantidadHijos = empleado != null && empleado.getCantidadDeHijos() != null
                ? Math.max(0, empleado.getCantidadDeHijos())
                : 0;
        double creditos = cantidadHijos * CREDITO_POR_HIJO;
        if (empleado != null && Boolean.TRUE.equals(empleado.getEstaCasado())) {
            creditos += CREDITO_POR_CASADO;
        }
        return creditos;
    }

    /**
     * Realiza un calculo de negocio segun los datos de entrada.
     * @param salario parametro de entrada de la operacion.
     * @param tramosRenta parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    private double calcularImpuestoRenta(double salario, List<ConfiguracionRenta> tramosRenta) {
        if (tramosRenta.isEmpty()) {
            return 0.0;
        }
        double impuesto = 0.0;
        for (int i = 0; i < tramosRenta.size(); i++) {
            ConfiguracionRenta tramo = tramosRenta.get(i);
            double minimo = tramo.getMontoMinimo() != null ? tramo.getMontoMinimo() : 0.0;
            double maximo = Double.MAX_VALUE;
            if (i + 1 < tramosRenta.size()) {
                Double siguienteMinimo = tramosRenta.get(i + 1).getMontoMinimo();
                if (siguienteMinimo != null) {
                    maximo = siguienteMinimo;
                }
            } else if (tramo.getMontoMaximo() != null) {
                maximo = tramo.getMontoMaximo();
            }

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

    private record ResumenRenta(double totalHorasExtra, int cantidadDiasFeriados) {
    }

    /** 
     * @param inicio
     * @param fin
     * @param jornadasPorFecha
     * @param feriados
     * @return ResumenRenta
     */
    private ResumenRenta calcularResumenRenta(LocalDate inicio,
                                              LocalDate fin,
                                              Map<LocalDate, JornadaDiaria> jornadasPorFecha,
                                              Set<LocalDate> feriados) {
        double totalHorasExtra = 0.0;
        int cantidadDiasFeriados = 0;

        LocalDate fecha = inicio;
        while (!fecha.isAfter(fin)) {
            JornadaDiaria jornada = jornadasPorFecha.get(fecha);
            double horasRegulares = jornada != null && jornada.getHorasRegulares() != null
                    ? jornada.getHorasRegulares()
                    : 0.0;
            double horasExtra = jornada != null && jornada.getHorasExtra() != null
                    ? jornada.getHorasExtra()
                    : 0.0;
            totalHorasExtra += horasExtra;

            boolean esFeriado = feriados.contains(fecha);
            boolean tieneHorasTrabajadas = horasRegulares > 0 || horasExtra > 0;
            if (esFeriado && tieneHorasTrabajadas) {
                cantidadDiasFeriados++;
            }
            fecha = fecha.plusDays(1);
        }

        return new ResumenRenta(totalHorasExtra, cantidadDiasFeriados);
    }

    /**
     * Ejecuta la logica principal de esFinDeSemana.
     * @param fecha parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    private boolean esFinDeSemana(LocalDate fecha) {
        DayOfWeek dia = fecha.getDayOfWeek();
        return dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY;
    }

    /**
     * Obtiene informacion necesaria para la operacion.
     * @param jornadas parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    private Map<LocalDate, JornadaDiaria> mapearJornadas(List<JornadaDiaria> jornadas) {
        Map<LocalDate, JornadaDiaria> resultado = new HashMap<>();
        for (JornadaDiaria jornada : jornadas) {
            if (jornada.getFecha() != null) {
                resultado.put(jornada.getFecha(), jornada);
            }
        }
        return resultado;
    }

    /**
     * Obtiene informacion necesaria para la operacion.
     * @param empleado parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    private double obtenerSalarioMensual(Empleados empleado) {
        if (empleado == null) {
            return 0.0;
        }
        if (empleado.getPuesto() != null && empleado.getPuesto().getSalarioMinimo() != null) {
            return empleado.getPuesto().getSalarioMinimo();
        }
        log.warn("El empleado {} no tiene salario mensual definido", empleado.getId());
        return 0.0;
    }

    /**
     * Ejecuta la logica principal de safe.
     * @param value parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    private double safe(Double value) {
        return value == null ? 0.0 : value;
    }

    /**
     * Crea un detalle de planilla con todos los valores en cero
     * cuando el empleado no tiene jornadas diarias en el período.
     * 
     * @param empleado empleado sin jornadas
     * @param encabezado encabezado de la planilla
     * @return detalle con valores cero
     */
    private PlanillaDetalle crearPlanillaDetalleCero(Empleados empleado, PlanillaEncabezado encabezado) {
        return PlanillaDetalle.builder()
                .salarioBasePeriodo(0.0)
                .cantidadDiasFeriados(0)
                .cantidadDiasNoTrabajadosEnQuincena(0)
                .montoHorasExtra(0.0)
                .montoFeriadosTrabajados(0.0)
                .montoIncapacidad(0.0)
                .deduccionCcssIvm(0.0)
                .deduccionCcssSem(0.0)
                .impuestoDeRenta(0.0)
                .otrasDeducciones(0.0)
                .empleado(empleado)
                .planillaEncabezado(encabezado)
                .build();
    }
    

}
