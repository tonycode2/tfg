package com.anthony.tfg.tfg.Modulos.Vacaciones.Controlador;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.anthony.tfg.tfg.Modulos.Vacaciones.Servicio.ServicioVacaciones;

/**
 * Controlador REST para gestión de saldo de vacaciones.
 * Proporciona endpoints para consultar saldo de vacaciones.
 */
@RestController
@RequestMapping("/api/vacaciones")
public class ControladorVacaciones {

    private final ServicioVacaciones servicioVacaciones;

    public ControladorVacaciones(ServicioVacaciones servicioVacaciones) {
        this.servicioVacaciones = servicioVacaciones;
    }

    /**
     * Obtiene el saldo de vacaciones del empleado autenticado.
     * Todos los empleados pueden consultar su propio saldo.
     */
    @GetMapping("/mi-saldo")
    public ResponseEntity<Map<String, Integer>> obtenerMiSaldo(Authentication authentication) {
        Integer saldo = servicioVacaciones.obtenerMiSaldo(authentication);
        return ResponseEntity.ok(Map.of("diasDisponibles", saldo));
    }

    /**
     * Obtiene el saldo de vacaciones de un empleado específico.
     * Solo HR, ADMIN o jefes del departamento del empleado pueden consultar.
     */
    @GetMapping("/saldo/{idEmpleado}")
    @PreAuthorize("hasAnyRole('JEFE', 'HR', 'ADMIN')")
    public ResponseEntity<Map<String, Integer>> obtenerSaldoEmpleado(
            @PathVariable Long idEmpleado,
            Authentication authentication) {
        Integer saldo = servicioVacaciones.obtenerSaldoPorEmpleado(idEmpleado, authentication);
        return ResponseEntity.ok(Map.of("diasDisponibles", saldo));
    }

    /**
     * Ejecuta manualmente la acumulación de 1 día de vacaciones para todos los empleados activos.
     * Solo HR y ADMIN pueden ejecutar esta acción.
     */
    @PostMapping("/acumular-manual")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<Map<String, String>> ejecutarAcumulacionManual(Authentication authentication) {
        servicioVacaciones.ejecutarAcumulacionManual(authentication);
        return ResponseEntity.ok(Map.of(
            "message", "Acumulación de vacaciones ejecutada exitosamente",
            "status", "success"
        ));
    }
}
