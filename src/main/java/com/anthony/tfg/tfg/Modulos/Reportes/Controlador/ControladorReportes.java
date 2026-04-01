package com.anthony.tfg.tfg.Modulos.Reportes.Controlador;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import com.anthony.tfg.tfg.DTOs.Respuesta.ColillaPagoDTO;
import com.anthony.tfg.tfg.DTOs.Respuesta.ProyeccionCesantiaDTO;
import com.anthony.tfg.tfg.DTOs.Respuesta.ReporteAguinaldoDTO;
import com.anthony.tfg.tfg.DTOs.Respuesta.ReporteAntiguedadDTO;
import com.anthony.tfg.tfg.DTOs.Respuesta.ReporteDeduccionesDTO;
import com.anthony.tfg.tfg.DTOs.Respuesta.ReporteIncapacidadesDTO2;
import com.anthony.tfg.tfg.DTOs.Respuesta.ReporteLiquidacionDTO;
import com.anthony.tfg.tfg.DTOs.Respuesta.ReportePlanillaDTO;
import com.anthony.tfg.tfg.DTOs.Respuesta.ReporteVacacionesDTO;
import com.anthony.tfg.tfg.Modulos.Reportes.Servicio.ServicioReportes;
import com.anthony.tfg.tfg.Modulos.Reportes.Util.ReportePdfGenerator;
import org.springframework.security.core.Authentication;
import com.anthony.tfg.tfg.Modulos.Seguridad.user.User;
import com.anthony.tfg.tfg.Entidades.Empleados;
import com.anthony.tfg.tfg.Exceptions.ForbiddenException;

@RestController
@RequestMapping("/api/reportes")
public class ControladorReportes {

    private final ServicioReportes servicioReportes;
    private final ReportePdfGenerator reportePdfGenerator;

    public ControladorReportes(ServicioReportes servicioReportes, ReportePdfGenerator reportePdfGenerator) {
        this.servicioReportes = servicioReportes;
        this.reportePdfGenerator = reportePdfGenerator;
    }

    /** 
     * @param pdfBytes
     * @param filename
     * @return ResponseEntity<byte[]>
     */
    private ResponseEntity<byte[]> buildPdfResponse(byte[] pdfBytes, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(pdfBytes.length);
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    /** 
     * @param planillaId
     * @return ResponseEntity<byte[]>
     * @throws Exception
     */
    @GetMapping("/planilla/{planillaId}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','JEFE')")
    public ResponseEntity<byte[]> planilla(@PathVariable Long planillaId) throws Exception {
        ReportePlanillaDTO dto = servicioReportes.generarReportePlanilla(planillaId);
        byte[] pdf = reportePdfGenerator.generarPdf("reporte-planilla", Map.of("dto", dto));
        return buildPdfResponse(pdf, "reporte-planilla-" + planillaId + ".pdf");
    }

    /** 
     * @param detalleId
     * @param authentication
     * @return ResponseEntity<byte[]>
     * @throws Exception
     */
    @GetMapping("/colilla/{detalleId}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','JEFE','EMPLEADO')")
    public ResponseEntity<byte[]> colilla(@PathVariable Long detalleId, Authentication authentication) throws Exception {
        ColillaPagoDTO dto = servicioReportes.generarColillaPago(detalleId);

        // If the authenticated user is an EMPLEADO, ensure they can only access their own colilla
        Object principal = authentication != null ? authentication.getPrincipal() : null;
        if (principal instanceof User) {
            User user = (User) principal;
            String roleName = user.getRole() != null ? user.getRole().name() : "";
            if ("EMPLEADO".equals(roleName)) {
                Empleados empleado = user.getEmpleado();
                if (empleado == null || empleado.getId() == null || dto.getIdEmpleado() == null || !empleado.getId().equals(dto.getIdEmpleado())) {
                    throw new ForbiddenException("No autorizado para descargar esta colilla");
                }
            }
        }

        byte[] pdf = reportePdfGenerator.generarPdf("colilla-pago", Map.of("dto", dto));
        return buildPdfResponse(pdf, "colilla-pago-" + detalleId + ".pdf");
    }

    /** 
     * @param empleadoId
     * @param anio
     * @param authentication
     * @return ResponseEntity<byte[]>
     * @throws Exception
     */
    @GetMapping("/aguinaldo/{empleadoId}/{anio}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','JEFE','EMPLEADO')")
    public ResponseEntity<byte[]> aguinaldo(@PathVariable Long empleadoId, @PathVariable Integer anio, Authentication authentication) throws Exception {
        ReporteAguinaldoDTO dto = servicioReportes.generarReporteAguinaldo(empleadoId, anio);

        // If the authenticated user is an EMPLEADO, ensure they can only access their own aguinaldo
        Object principal = authentication != null ? authentication.getPrincipal() : null;
        if (principal instanceof User) {
            User user = (User) principal;
            String roleName = user.getRole() != null ? user.getRole().name() : "";
            if ("EMPLEADO".equals(roleName)) {
                Empleados empleado = user.getEmpleado();
                if (empleado == null || empleado.getId() == null || !empleado.getId().equals(empleadoId)) {
                    throw new ForbiddenException("No autorizado para descargar este aguinaldo");
                }
            }
        }

        byte[] pdf = reportePdfGenerator.generarPdf("reporte-aguinaldo", Map.of("dto", dto));
        return buildPdfResponse(pdf, "reporte-aguinaldo-" + empleadoId + "-" + anio + ".pdf");
    }

    /** 
     * @return ResponseEntity<byte[]>
     * @throws Exception
     */
    @GetMapping("/vacaciones")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<byte[]> vacaciones() throws Exception {
        ReporteVacacionesDTO dto = servicioReportes.generarReporteVacaciones();
        byte[] pdf = reportePdfGenerator.generarPdf("reporte-vacaciones", Map.of("dto", dto));
        return buildPdfResponse(pdf, "reporte-vacaciones.pdf");
    }

    /** 
     * @return ResponseEntity<byte[]>
     * @throws Exception
     */
    @GetMapping("/antiguedad")
    @PreAuthorize("hasAnyRole('ADMIN','HR','JEFE')")
    public ResponseEntity<byte[]> antiguedad() throws Exception {
        ReporteAntiguedadDTO dto = servicioReportes.generarReporteAntiguedad();
        byte[] pdf = reportePdfGenerator.generarPdf("reporte-antiguedad", Map.of("dto", dto));
        return buildPdfResponse(pdf, "reporte-antiguedad.pdf");
    }

    /** 
     * @param planillaId
     * @return ResponseEntity<byte[]>
     * @throws Exception
     */
    @GetMapping("/deducciones/{planillaId}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<byte[]> deducciones(@PathVariable Long planillaId) throws Exception {
        ReporteDeduccionesDTO dto = servicioReportes.generarReporteDeducciones(planillaId);
        byte[] pdf = reportePdfGenerator.generarPdf("reporte-deducciones", Map.of("dto", dto));
        return buildPdfResponse(pdf, "reporte-deducciones-" + planillaId + ".pdf");
    }

    /** 
     * @param liquidacionId
     * @return ResponseEntity<byte[]>
     * @throws Exception
     */
    @GetMapping("/liquidacion/{liquidacionId}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<byte[]> liquidacion(@PathVariable Long liquidacionId) throws Exception {
        ReporteLiquidacionDTO dto = servicioReportes.generarReporteLiquidacion(liquidacionId);
        byte[] pdf = reportePdfGenerator.generarPdf("reporte-liquidacion", Map.of("dto", dto));
        return buildPdfResponse(pdf, "reporte-liquidacion-" + liquidacionId + ".pdf");
    }

    /** 
     * @param DateTimeFormat.ISO.DATE
     * @return ResponseEntity<byte[]>
     */
    @GetMapping("/incapacidades")
    @PreAuthorize("hasAnyRole('ADMIN','HR','JEFE')")
    public ResponseEntity<byte[]> incapacidades(@RequestParam("fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String fechaInicioStr,
            @RequestParam("fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String fechaFinStr) throws Exception {
        LocalDate fechaInicio;
        LocalDate fechaFin;
        try {
            fechaInicio = LocalDate.parse(fechaInicioStr);
            fechaFin = LocalDate.parse(fechaFinStr);
        } catch (DateTimeParseException ex) {
            throw ex;
        }
        ReporteIncapacidadesDTO2 dto = servicioReportes.generarReporteIncapacidades(fechaInicio, fechaFin);
        Map<String, Object> vars = new HashMap<>();
        vars.put("dto", dto);
        byte[] pdf = reportePdfGenerator.generarPdf("reporte-incapacidades", vars);
        return buildPdfResponse(pdf, "reporte-incapacidades.pdf");
    }

    /** 
     * @return ResponseEntity<byte[]>
     * @throws Exception
     */
    @GetMapping("/proyeccion-cesantia")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<byte[]> proyeccionCesantia() throws Exception {
        ProyeccionCesantiaDTO dto = servicioReportes.generarProyeccionCesantia();
        byte[] pdf = reportePdfGenerator.generarPdf("proyeccion-cesantia", Map.of("dto", dto));
        return buildPdfResponse(pdf, "proyeccion-cesantia.pdf");
    }
}
