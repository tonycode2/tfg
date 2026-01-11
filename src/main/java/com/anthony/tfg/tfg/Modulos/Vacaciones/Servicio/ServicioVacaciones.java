package com.anthony.tfg.tfg.Modulos.Vacaciones.Servicio;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.Entidades.Empleados;
import com.anthony.tfg.tfg.Exceptions.BadRequestException;
import com.anthony.tfg.tfg.Exceptions.ForbiddenException;
import com.anthony.tfg.tfg.Exceptions.ResourceNotFoundException;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasEmpleados;
import com.anthony.tfg.tfg.Modulos.Seguridad.user.User;
import com.anthony.tfg.tfg.Repositorios.EmpleadosRepositorio;
import com.anthony.tfg.tfg.Repositorios.JefesDepartamentoRepositorio;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para gestión de saldo de vacaciones.
 * Maneja consultas de saldo, descuento al aprobar vacaciones, y acumulación mensual automática.
 */
@Service
@Slf4j
public class ServicioVacaciones {

    private final EmpleadosRepositorio empleadosRepositorio;
    private final ConsultasEmpleados consultasEmpleados;
    private final JefesDepartamentoRepositorio jefesDepartamentoRepo;

    public ServicioVacaciones(
            EmpleadosRepositorio empleadosRepositorio,
            ConsultasEmpleados consultasEmpleados,
            JefesDepartamentoRepositorio jefesDepartamentoRepo) {
        this.empleadosRepositorio = empleadosRepositorio;
        this.consultasEmpleados = consultasEmpleados;
        this.jefesDepartamentoRepo = jefesDepartamentoRepo;
    }

    /**
     * Obtiene el saldo de vacaciones del empleado autenticado
     */
    public Integer obtenerMiSaldo(Authentication auth) {
        Empleados empleado = obtenerEmpleadoAutenticado(auth);
        Integer saldo = empleado.getSaldoVacaciones();
        log.info("Saldo de vacaciones para empleado {}: {} días", empleado.getId(), saldo);
        return saldo != null ? saldo : 0;
    }

    /**
     * Obtiene el saldo de vacaciones de un empleado específico.
     * Solo HR, ADMIN o jefes del departamento del empleado pueden consultar.
     */
    public Integer obtenerSaldoPorEmpleado(Long idEmpleado, Authentication auth) {
        User usuario = obtenerUsuarioAutenticado(auth);
        Empleados empleadoConsulta = obtenerEmpleadoAutenticado(auth);
        
        // Verificar permisos
        boolean esHRoAdmin = usuario.getRole().name().equals("HR") || usuario.getRole().name().equals("ADMIN");
        boolean esJefeDelEmpleado = false;
        
        if (!esHRoAdmin) {
            // Verificar si es jefe del departamento del empleado consultado
            Empleados empleadoTarget = consultasEmpleados.obtenerPorId(idEmpleado);
            if (empleadoTarget == null) {
                throw new ResourceNotFoundException("Empleados", "id", idEmpleado);
            }
            
            if (empleadoTarget.getPuesto() != null && empleadoTarget.getPuesto().getDepartamento() != null) {
                Long idDepartamento = empleadoTarget.getPuesto().getDepartamento().getId();
                esJefeDelEmpleado = jefesDepartamentoRepo
                        .findByEmpleadoIdAndDepartamentoIdAndEstaActivoTrue(empleadoConsulta.getId(), idDepartamento)
                        .isPresent();
            }
        }
        
        if (!esHRoAdmin && !esJefeDelEmpleado) {
            throw new ForbiddenException("No tiene permisos para consultar el saldo de este empleado");
        }
        
        Empleados empleado = consultasEmpleados.obtenerPorId(idEmpleado);
        if (empleado == null) {
            throw new ResourceNotFoundException("Empleados", "id", idEmpleado);
        }
        
        Integer saldo = empleado.getSaldoVacaciones();
        log.info("Saldo de vacaciones consultado para empleado {}: {} días", idEmpleado, saldo);
        return saldo != null ? saldo : 0;
    }

    /**
     * Verifica si el empleado tiene suficiente saldo para solicitar vacaciones
     */
    public void validarSaldoDisponible(Long idEmpleado, Integer diasSolicitados) {
        Empleados empleado = consultasEmpleados.obtenerPorId(idEmpleado);
        if (empleado == null) {
            throw new ResourceNotFoundException("Empleados", "id", idEmpleado);
        }
        
        Integer saldoActual = empleado.getSaldoVacaciones() != null ? empleado.getSaldoVacaciones() : 0;
        
        if (diasSolicitados > saldoActual) {
            throw new BadRequestException(
                String.format("Días solicitados (%d) exceden el saldo disponible (%d días)", 
                    diasSolicitados, saldoActual));
        }
        
        log.info("Validación de saldo exitosa para empleado {}: solicitó {} días, tiene {} días disponibles",
                idEmpleado, diasSolicitados, saldoActual);
    }

    /**
     * Descuenta días del saldo de vacaciones cuando se aprueba la solicitud.
     * Se llama desde ServicioPermisos cuando RH aprueba una solicitud de vacaciones.
     */
    @Transactional
    public void descontarDias(Long idEmpleado, Integer dias) {
        Empleados empleado = consultasEmpleados.obtenerPorId(idEmpleado);
        if (empleado == null) {
            throw new ResourceNotFoundException("Empleados", "id", idEmpleado);
        }
        
        Integer saldoActual = empleado.getSaldoVacaciones() != null ? empleado.getSaldoVacaciones() : 0;
        
        if (dias > saldoActual) {
            throw new BadRequestException(
                String.format("No se pueden descontar %d días, el empleado solo tiene %d días disponibles",
                    dias, saldoActual));
        }
        
        empleado.setSaldoVacaciones(saldoActual - dias);
        empleadosRepositorio.save(empleado);
        
        log.info("Se descontaron {} días de vacaciones al empleado {}. Saldo anterior: {}, Saldo nuevo: {}",
                dias, idEmpleado, saldoActual, empleado.getSaldoVacaciones());
    }

    /**
     * Tarea programada que se ejecuta el primer día de cada mes a las 1:00 AM.
     * Agrega 1 día de vacaciones a todos los empleados activos.
     */
    @Scheduled(cron = "0 0 1 1 * ?")
    @Transactional
    public void acumularVacacionesMensual() {
        log.info("Iniciando acumulación mensual de vacaciones...");
        
        List<Empleados> empleadosActivos = empleadosRepositorio.findByEstaActivoTrue();
        int procesados = 0;
        int errores = 0;
        
        for (Empleados empleado : empleadosActivos) {
            try {
                Integer saldoActual = empleado.getSaldoVacaciones() != null ? empleado.getSaldoVacaciones() : 0;
                empleado.setSaldoVacaciones(saldoActual + 1);
                empleadosRepositorio.save(empleado);
                procesados++;
                
                log.debug("Acumulado 1 día de vacaciones para empleado {}. Saldo anterior: {}, Saldo nuevo: {}",
                        empleado.getId(), saldoActual, empleado.getSaldoVacaciones());
            } catch (Exception e) {
                errores++;
                log.error("Error al acumular vacaciones para empleado {}: {}", empleado.getId(), e.getMessage());
            }
        }
        
        log.info("Acumulación mensual de vacaciones completada. Empleados procesados: {}, Errores: {}",
                procesados, errores);
    }

    /**
     * Método para ejecutar manualmente la acumulación (útil para testing)
     */
    @Transactional
    public void ejecutarAcumulacionManual(Authentication auth) {
        User usuario = obtenerUsuarioAutenticado(auth);
        String role = usuario.getRole().name();
        if (!role.equals("ADMIN") && !role.equals("HR")) {
            throw new ForbiddenException("Solo administradores y personal de RH pueden ejecutar la acumulación manual");
        }
        
        log.info("Ejecutando acumulación manual de vacaciones por solicitud de {} ({})",
                usuario.getUsername(), role);
        acumularVacacionesMensual();
    }

    // ==================== AUTHENTICATION HELPERS ====================

    private User obtenerUsuarioAutenticado(Authentication auth) {
        if (auth == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new ForbiddenException("Usuario no autenticado");
            }
            auth = authentication;
        }
        
        Object principal = auth.getPrincipal();
        if (!(principal instanceof User)) {
            throw new ForbiddenException("Tipo de usuario no válido");
        }
        
        return (User) principal;
    }

    private Empleados obtenerEmpleadoAutenticado(Authentication auth) {
        User user = obtenerUsuarioAutenticado(auth);
        Empleados empleado = user.getEmpleado();
        
        if (empleado == null) {
            throw new ForbiddenException("El usuario no tiene un empleado asociado");
        }
        
        return empleado;
    }
}
