package com.anthony.tfg.tfg.Modulos.DiasFeriados.Controlador;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaDiasFeriadosDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudDiasFeriadosDTO;
import com.anthony.tfg.tfg.Modulos.DiasFeriados.Servicio.ServicioDiasFeriados;

import jakarta.validation.Valid;

/**
 * Controlador REST para gestión de días feriados.
 * Endpoints públicos para consulta, endpoints protegidos para CRUD (HR/ADMIN).
 */
@RestController
@RequestMapping("/api/dias-feriados")
@Validated
public class ControladorDiasFeriados {
    
    private final ServicioDiasFeriados servicio;
    
    public ControladorDiasFeriados(ServicioDiasFeriados servicio) {
        this.servicio = servicio;
    }
    
    /**
     * Obtiene un feriado por su ID.
     * Accesible para todos los usuarios autenticados.
     */
    @GetMapping("/{id}")
    public ResponseEntity<RespuestaDiasFeriadosDTO> obtenerPorId(@PathVariable Long id) {
        RespuestaDiasFeriadosDTO respuesta = servicio.obtenerPorId(id);
        return ResponseEntity.ok(respuesta);
    }
    
    /**
     * Obtiene todos los feriados.
     * Accesible para todos los usuarios autenticados.
     */
    @GetMapping
    public ResponseEntity<List<RespuestaDiasFeriadosDTO>> obtenerTodos() {
        List<RespuestaDiasFeriadosDTO> lista = servicio.obtenerTodos();
        return ResponseEntity.ok(lista);
    }
    
    /**
     * Obtiene feriados en un rango de fechas.
     * Accesible para todos los usuarios autenticados.
     */
    @GetMapping("/rango")
    public ResponseEntity<List<RespuestaDiasFeriadosDTO>> obtenerPorRango(
            @RequestParam("fechaInicio") String fechaInicio,
            @RequestParam("fechaFin") String fechaFin) {
        LocalDate inicio = LocalDate.parse(fechaInicio);
        LocalDate fin = LocalDate.parse(fechaFin);
        List<RespuestaDiasFeriadosDTO> lista = servicio.obtenerPorRango(inicio, fin);
        return ResponseEntity.ok(lista);
    }
    
    /**
     * Obtiene feriados de un año específico.
     * Accesible para todos los usuarios autenticados.
     */
    @GetMapping("/anio/{anio}")
    public ResponseEntity<List<RespuestaDiasFeriadosDTO>> obtenerPorAnio(@PathVariable int anio) {
        List<RespuestaDiasFeriadosDTO> lista = servicio.obtenerPorAnio(anio);
        return ResponseEntity.ok(lista);
    }
    
    /**
     * Valida si una fecha específica es un día feriado.
     * Accesible para todos los usuarios autenticados.
     * @return JSON con campo "esFeriado" (boolean)
     */
    @GetMapping("/validar-fecha")
    public ResponseEntity<Map<String, Object>> validarFecha(@RequestParam("fecha") String fecha) {
        LocalDate fechaDate = LocalDate.parse(fecha);
        boolean esFeriado = servicio.esFeriado(fechaDate);
        return ResponseEntity.ok(Map.of("fecha", fecha, "esFeriado", esFeriado));
    }
    
    /**
     * Crea un nuevo feriado.
     * Solo accesible para HR y ADMIN.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<RespuestaDiasFeriadosDTO> crear(@Valid @RequestBody SolicitudDiasFeriadosDTO solicitud) {
        RespuestaDiasFeriadosDTO respuesta = servicio.guardar(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }
    
    /**
     * Actualiza un feriado existente.
     * Solo accesible para HR y ADMIN.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<RespuestaDiasFeriadosDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudDiasFeriadosDTO solicitud) {
        RespuestaDiasFeriadosDTO respuesta = servicio.actualizar(id, solicitud);
        return ResponseEntity.ok(respuesta);
    }
    
    /**
     * Elimina un feriado.
     * Solo accesible para HR y ADMIN.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
