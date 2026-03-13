package com.anthony.tfg.tfg.Modulos.Planilla.Controlador;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaPlanillaDetalleDTO;
import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaPlanillaEmpleadoDTO;
import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaPlanillaEncabezadoDTO;
import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaPlanillaPdfDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudGenerarPlanillaDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudPlanillaEncabezadoDTO;
import com.anthony.tfg.tfg.Modulos.Planilla.Servicio.ServicioPlanilla;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/planillas")
@Validated
public class ControladorPlanilla {

    private final ServicioPlanilla servicio;

    public ControladorPlanilla(ServicioPlanilla servicio) {
        this.servicio = servicio;
    }

    /** 
     * @param id
     * @return ResponseEntity<RespuestaPlanillaEncabezadoDTO>
     */
    @GetMapping("/{id}")
    public ResponseEntity<RespuestaPlanillaEncabezadoDTO> obtenerPorId(@PathVariable Long id) {
        RespuestaPlanillaEncabezadoDTO respuesta = servicio.obtenerPorId(id);
        if(respuesta == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(respuesta);
    }

    /** 
     * @return ResponseEntity<List<RespuestaPlanillaEncabezadoDTO>>
     */
    @GetMapping
    public ResponseEntity<List<RespuestaPlanillaEncabezadoDTO>> obtenerTodos() {
        List<RespuestaPlanillaEncabezadoDTO> lista = servicio.obtenerTodos();
        return ResponseEntity.ok(lista);
    }

    /** 
     * @param obtenerPlanillasPorEmpleado(
     * @return ResponseEntity<List<RespuestaPlanillaEmpleadoDTO>>
     */
    @GetMapping("/empleado/{empleadoId}")
    public ResponseEntity<List<RespuestaPlanillaEmpleadoDTO>> obtenerPlanillasPorEmpleado(
            @PathVariable Long empleadoId) {
        List<RespuestaPlanillaEmpleadoDTO> planillas = servicio.obtenerPlanillasPorEmpleado(empleadoId);
        return ResponseEntity.ok(planillas);
    }

    /** 
     * @param id
     * @return ResponseEntity<List<RespuestaPlanillaDetalleDTO>>
     */
    @GetMapping("/{id}/detalles")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<List<RespuestaPlanillaDetalleDTO>> obtenerDetallesPorPlanilla(@PathVariable Long id) {
        List<RespuestaPlanillaDetalleDTO> detalles = servicio.obtenerDetallesPorPlanilla(id);
        return ResponseEntity.ok(detalles);
    }

    /** 
     * @param authentication
     * @return ResponseEntity<RespuestaPlanillaPdfDTO>
     */
    @PostMapping(value = "/detalles/{detalleId}/pdf", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<RespuestaPlanillaPdfDTO> subirPdfPlanilla(
            @PathVariable Long detalleId,
            @org.springframework.web.bind.annotation.RequestPart("archivo") org.springframework.web.multipart.MultipartFile archivo,
            Authentication authentication) {
        RespuestaPlanillaPdfDTO respuesta = servicio.guardarPdfPlanilla(detalleId, archivo, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /** 
     * @param authentication
     * @return ResponseEntity<Resource>
     * @throws UnsupportedEncodingException
     */
    @GetMapping("/detalles/{detalleId}/pdf")
    public ResponseEntity<Resource> descargarPdfPlanilla(
            @PathVariable Long detalleId,
            Authentication authentication) throws UnsupportedEncodingException {
        return servicio.descargarPdfPlanilla(detalleId, authentication);
    }

    /** 
     * @param solicitud
     * @return ResponseEntity<RespuestaPlanillaEncabezadoDTO>
     */
    @PostMapping
    public ResponseEntity<RespuestaPlanillaEncabezadoDTO> crear(@Valid @RequestBody SolicitudPlanillaEncabezadoDTO solicitud) {
        RespuestaPlanillaEncabezadoDTO respuesta = servicio.guardar(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /** 
     * @param generarPlanilla(
     * @return ResponseEntity<RespuestaPlanillaEncabezadoDTO>
     */
    @PostMapping("/generar")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<RespuestaPlanillaEncabezadoDTO> generarPlanilla(
            @Valid @RequestBody SolicitudGenerarPlanillaDTO solicitud) {
        RespuestaPlanillaEncabezadoDTO respuesta = servicio.generarPlanilla(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /** 
     * @param actualizar(
     * @return ResponseEntity<RespuestaPlanillaEncabezadoDTO>
     */
    @PutMapping("/{id}")
    public ResponseEntity<RespuestaPlanillaEncabezadoDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudPlanillaEncabezadoDTO solicitud) {
        RespuestaPlanillaEncabezadoDTO respuesta = servicio.actualizar(id, solicitud);
        if(respuesta == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(respuesta);
    }

    /** 
     * @param id
     * @return ResponseEntity<RespuestaPlanillaEncabezadoDTO>
     */
    @PutMapping("/{id}/pagada")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<RespuestaPlanillaEncabezadoDTO> marcarComoPagada(@PathVariable Long id) {
        RespuestaPlanillaEncabezadoDTO respuesta = servicio.marcarComoPagada(id);
        return ResponseEntity.ok(respuesta);
    }

    /** 
     * @param id
     * @return ResponseEntity<Void>
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
