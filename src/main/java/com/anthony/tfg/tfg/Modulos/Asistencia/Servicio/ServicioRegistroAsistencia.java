package com.anthony.tfg.tfg.Modulos.Asistencia.Servicio;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.anthony.tfg.tfg.DTOs.Respuesta.EstadoAsistenciaDTO;
import com.anthony.tfg.tfg.DTOs.Respuesta.EstadoAsistenciaDTO.EstadoActual;
import com.anthony.tfg.tfg.DTOs.Respuesta.ResumenDepartamentoDTO;
import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaAsistenciaDTO;
import com.anthony.tfg.tfg.Entidades.Asistencia;
import com.anthony.tfg.tfg.Entidades.Departamento;
import com.anthony.tfg.tfg.Entidades.Empleados;
import com.anthony.tfg.tfg.Entidades.JefesDepartamento;
import com.anthony.tfg.tfg.Entidades.Puestos;
import com.anthony.tfg.tfg.Entidades.Enums.TipoEvento;
import com.anthony.tfg.tfg.Exceptions.BadRequestException;
import com.anthony.tfg.tfg.Exceptions.ForbiddenException;
import com.anthony.tfg.tfg.Exceptions.ResourceNotFoundException;
import com.anthony.tfg.tfg.Modulos.Seguridad.user.Role;
import com.anthony.tfg.tfg.Modulos.Seguridad.user.User;
import com.anthony.tfg.tfg.Repositorios.AsistenciaRepositorio;
import com.anthony.tfg.tfg.Repositorios.DepartamentoRepositorio;
import com.anthony.tfg.tfg.Repositorios.EmpleadosRepositorio;
import com.anthony.tfg.tfg.Repositorios.JefesDepartamentoRepositorio;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for attendance registration business logic
 * Handles clock-in/clock-out, status checking, and department summaries
 */
@Service
@Slf4j
public class ServicioRegistroAsistencia {

    // Grace period for late arrival detection (in minutes)
    private static final int GRACE_PERIOD_MINUTES = 5;
    
    private final AsistenciaRepositorio asistenciaRepositorio;
    private final EmpleadosRepositorio empleadosRepositorio;
    private final DepartamentoRepositorio departamentoRepositorio;
    private final JefesDepartamentoRepositorio jefesDepartamentoRepositorio;

    public ServicioRegistroAsistencia(
            AsistenciaRepositorio asistenciaRepositorio,
            EmpleadosRepositorio empleadosRepositorio,
            DepartamentoRepositorio departamentoRepositorio,
            JefesDepartamentoRepositorio jefesDepartamentoRepositorio) {
        this.asistenciaRepositorio = asistenciaRepositorio;
        this.empleadosRepositorio = empleadosRepositorio;
        this.departamentoRepositorio = departamentoRepositorio;
        this.jefesDepartamentoRepositorio = jefesDepartamentoRepositorio;
    }

    // ==================== PUBLIC METHODS ====================

    /**
     * Register clock-in (ENTRADA) for the current authenticated user
     * @param fechaHora Optional custom time (for testing), uses current time if null
     * @return The created attendance record
     * @throws BadRequestException if already clocked in without clocking out
     */
    @Transactional
    public RespuestaAsistenciaDTO registrarEntrada(LocalDateTime fechaHora) {
        Empleados empleado = obtenerEmpleadoAutenticado();
        LocalDateTime fechaHoraRegistro = fechaHora != null ? fechaHora : LocalDateTime.now();
        
        // Validate: Check if there's an open ENTRADA (not followed by SALIDA)
        if (tieneEntradaAbierta(empleado.getId())) {
            log.warn("Empleado {} intentó marcar entrada sin haber salido", empleado.getId());
            throw new BadRequestException("Ya tiene una entrada registrada. Debe marcar salida primero.");
        }
        
        // Calculate observations (late arrival)
        String observaciones = calcularObservacionesEntrada(empleado, fechaHoraRegistro);
        
        // Create and save the attendance record
        Asistencia asistencia = Asistencia.builder()
                .tipoEvento(TipoEvento.ENTRADA)
                .fechaHora(fechaHoraRegistro)
                .observaciones(observaciones)
                .empleado(empleado)
                .build();
        
        Asistencia saved = asistenciaRepositorio.save(asistencia);
        log.info("Empleado {} registró entrada a las {}", empleado.getId(), fechaHoraRegistro);
        
        return toRespuestaDTO(saved);
    }

    /**
     * Register clock-out (SALIDA) for the current authenticated user
     * @param fechaHora Optional custom time (for testing), uses current time if null
     * @return The created attendance record
     * @throws BadRequestException if not clocked in
     */
    @Transactional
    public RespuestaAsistenciaDTO registrarSalida(LocalDateTime fechaHora) {
        Empleados empleado = obtenerEmpleadoAutenticado();
        LocalDateTime fechaHoraRegistro = fechaHora != null ? fechaHora : LocalDateTime.now();
        
        // Validate: Check if there's an open ENTRADA
        if (!tieneEntradaAbierta(empleado.getId())) {
            log.warn("Empleado {} intentó marcar salida sin entrada previa", empleado.getId());
            throw new BadRequestException("No tiene una entrada registrada. Debe marcar entrada primero.");
        }
        
        // Calculate observations (early departure or overtime)
        String observaciones = calcularObservacionesSalida(empleado, fechaHoraRegistro);
        
        // Create and save the attendance record
        Asistencia asistencia = Asistencia.builder()
                .tipoEvento(TipoEvento.SALIDA)
                .fechaHora(fechaHoraRegistro)
                .observaciones(observaciones)
                .empleado(empleado)
                .build();
        
        Asistencia saved = asistenciaRepositorio.save(asistencia);
        log.info("Empleado {} registró salida a las {}", empleado.getId(), fechaHoraRegistro);
        
        return toRespuestaDTO(saved);
    }

    /**
     * Get current attendance status for the authenticated user
     * @return Current status including last event, timestamps, and observations
     */
    public EstadoAsistenciaDTO obtenerMiEstado() {
        Empleados empleado = obtenerEmpleadoAutenticado();
        return construirEstadoAsistencia(empleado);
    }

    /**
     * Get attendance summary for a department
     * Validates that the user has permission to view the department
     * @param idDepartamento Department ID
     * @return Department summary with all employees' status
     */
    public ResumenDepartamentoDTO obtenerResumenDepartamento(Long idDepartamento) {
        User currentUser = obtenerUsuarioAutenticado();
        Empleados empleadoActual = obtenerEmpleadoDesdeUsuario(currentUser);
        
        // Validate department exists
        Departamento departamento = departamentoRepositorio.findById(idDepartamento)
                .orElseThrow(() -> new ResourceNotFoundException("Departamento", "id", idDepartamento));
        
        // Validate access based on role
        validarAccesoDepartamento(currentUser, empleadoActual, idDepartamento);
        
        // Get all active employees in the department
        List<Empleados> empleados = empleadosRepositorio.findByDepartamentoIdAndEstaActivoTrue(idDepartamento);
        
        // Build status for each employee
        List<EstadoAsistenciaDTO> estadosEmpleados = empleados.stream()
                .map(this::construirEstadoAsistencia)
                .toList();
        
        // Count statuses
        long laborando = estadosEmpleados.stream()
                .filter(e -> e.estadoActual == EstadoActual.LABORANDO)
                .count();
        
        // Build response
        ResumenDepartamentoDTO resumen = new ResumenDepartamentoDTO();
        resumen.departamentoId = departamento.getId();
        resumen.departamentoNombre = departamento.getNombre();
        resumen.totalEmpleados = empleados.size();
        resumen.empleadosLaborando = (int) laborando;
        resumen.empleadosFuera = empleados.size() - (int) laborando;
        resumen.empleados = estadosEmpleados;
        
        log.info("Usuario {} consultó resumen del departamento {}", currentUser.getUsername(), idDepartamento);
        return resumen;
    }

    /**
     * Get attendance history for a specific employee with date range filters
     * Validates that the user has permission to view the records
     * @param idEmpleado Employee ID (null for own records)
     * @param fechaInicio Start date (optional)
     * @param fechaFin End date (optional)
     * @return List of attendance records
     */
    public List<RespuestaAsistenciaDTO> obtenerHistorial(Long idEmpleado, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        User currentUser = obtenerUsuarioAutenticado();
        Empleados empleadoActual = obtenerEmpleadoDesdeUsuario(currentUser);
        
        // If no employee ID specified, use current user's employee
        Long targetEmpleadoId = idEmpleado != null ? idEmpleado : empleadoActual.getId();
        
        // Validate access
        if (!targetEmpleadoId.equals(empleadoActual.getId())) {
            validarAccesoEmpleado(currentUser, empleadoActual, targetEmpleadoId);
        }
        
        // Build query based on parameters
        List<Asistencia> registros;
        if (fechaInicio != null && fechaFin != null) {
            registros = asistenciaRepositorio.findByEmpleadoIdAndFechaHoraBetween(
                    targetEmpleadoId, fechaInicio, fechaFin);
        } else {
            registros = asistenciaRepositorio.findByEmpleadoId(targetEmpleadoId);
        }
        
        return registros.stream()
                .map(this::toRespuestaDTO)
                .toList();
    }

    /**
     * Get list of department IDs that the current user can access
     * @return List of department IDs
     */
    public List<Long> obtenerDepartamentosAccesibles() {
        User currentUser = obtenerUsuarioAutenticado();
        Empleados empleadoActual = obtenerEmpleadoDesdeUsuario(currentUser);
        
        // HR and ADMIN can access all departments
        if (currentUser.getRole() == Role.HR || currentUser.getRole() == Role.ADMIN) {
            return departamentoRepositorio.findAll().stream()
                    .map(Departamento::getId)
                    .toList();
        }
        
        // JEFE can access departments they manage
        if (currentUser.getRole() == Role.JEFE) {
            return jefesDepartamentoRepositorio.findDepartamentoIdsByEmpleadoId(empleadoActual.getId());
        }
        
        // EMPLEADO can only see their own department
        if (empleadoActual.getPuesto() != null && empleadoActual.getPuesto().getDepartamento() != null) {
            return List.of(empleadoActual.getPuesto().getDepartamento().getId());
        }
        
        return List.of();
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Check if employee has an open ENTRADA (clocked in without clocking out)
     */
    private boolean tieneEntradaAbierta(Long idEmpleado) {
        Optional<Asistencia> ultimoRegistro = asistenciaRepositorio.findUltimoRegistroByEmpleadoId(idEmpleado);
        
        if (ultimoRegistro.isEmpty()) {
            return false; // No records means no open ENTRADA
        }
        
        // If last record is ENTRADA, then there's an open entry
        return ultimoRegistro.get().getTipoEvento() == TipoEvento.ENTRADA;
    }

    /**
     * Calculate observations for a clock-in event
     * Detects late arrival based on position schedule
     */
    private String calcularObservacionesEntrada(Empleados empleado, LocalDateTime fechaHoraRegistro) {
        Puestos puesto = empleado.getPuesto();
        if (puesto == null || puesto.getHoraEntrada() == null) {
            return null; // No schedule defined
        }
        
        Time horaEntradaEsperada = puesto.getHoraEntrada();
        LocalTime horaRegistro = fechaHoraRegistro.toLocalTime();
        LocalTime horaEsperada = horaEntradaEsperada.toLocalTime();
        
        long minutosRetraso = ChronoUnit.MINUTES.between(horaEsperada, horaRegistro);
        
        // Apply grace period
        if (minutosRetraso > GRACE_PERIOD_MINUTES) {
            return String.format("Llegó %d min tarde", minutosRetraso);
        }
        
        return null;
    }

    /**
     * Calculate observations for a clock-out event
     * Detects early departure (any time before scheduled) or overtime
     */
    private String calcularObservacionesSalida(Empleados empleado, LocalDateTime fechaHoraRegistro) {
        Puestos puesto = empleado.getPuesto();
        if (puesto == null || puesto.getHoraSalida() == null) {
            return null; // No schedule defined
        }
        
        Time horaSalidaEsperada = puesto.getHoraSalida();
        LocalTime horaRegistro = fechaHoraRegistro.toLocalTime();
        LocalTime horaEsperada = horaSalidaEsperada.toLocalTime();
        
        long minutosAntes = ChronoUnit.MINUTES.between(horaRegistro, horaEsperada);
        
        if (minutosAntes > 0) {
            // Left early (any time before scheduled, no threshold)
            return String.format("Salió %d min temprano", minutosAntes);
        } else if (minutosAntes < -GRACE_PERIOD_MINUTES) {
            // Stayed late (overtime)
            long minutosExtra = Math.abs(minutosAntes);
            return String.format("Trabajó %d min extra", minutosExtra);
        }
        
        return null;
    }

    /**
     * Build EstadoAsistenciaDTO for an employee
     */
    private EstadoAsistenciaDTO construirEstadoAsistencia(Empleados empleado) {
        EstadoAsistenciaDTO estado = new EstadoAsistenciaDTO();
        estado.empleadoId = empleado.getId();
        estado.nombreCompleto = String.format("%s %s %s",
                empleado.getNombre(),
                empleado.getPrimerApellido(),
                empleado.getSegundoApellido() != null ? empleado.getSegundoApellido() : "").trim();
        
        if (empleado.getPuesto() != null) {
            estado.puestoNombre = empleado.getPuesto().getNombre();
            if (empleado.getPuesto().getDepartamento() != null) {
                estado.departamentoNombre = empleado.getPuesto().getDepartamento().getNombre();
            }
        }
        
        // Get last attendance record
        Optional<Asistencia> ultimoRegistro = asistenciaRepositorio.findUltimoRegistroByEmpleadoId(empleado.getId());
        
        if (ultimoRegistro.isPresent()) {
            Asistencia registro = ultimoRegistro.get();
            estado.ultimoEvento = registro.getTipoEvento();
            estado.fechaHoraUltimoEvento = registro.getFechaHora();
            estado.observaciones = registro.getObservaciones();
            
            // Determine current status
            estado.estadoActual = registro.getTipoEvento() == TipoEvento.ENTRADA
                    ? EstadoActual.LABORANDO
                    : EstadoActual.FUERA;
        } else {
            estado.estadoActual = EstadoActual.FUERA;
        }
        
        // Get today's records for entry/exit times
        LocalDateTime today = LocalDate.now().atStartOfDay();
        List<Asistencia> registrosHoy = asistenciaRepositorio.findByEmpleadoIdAndFecha(
                empleado.getId(), today);
        
        for (Asistencia reg : registrosHoy) {
            if (reg.getTipoEvento() == TipoEvento.ENTRADA && estado.horaEntradaHoy == null) {
                estado.horaEntradaHoy = reg.getFechaHora();
            } else if (reg.getTipoEvento() == TipoEvento.SALIDA) {
                estado.horaSalidaHoy = reg.getFechaHora();
            }
        }
        
        return estado;
    }

    /**
     * Convert Asistencia entity to RespuestaAsistenciaDTO
     */
    private RespuestaAsistenciaDTO toRespuestaDTO(Asistencia asistencia) {
        RespuestaAsistenciaDTO dto = new RespuestaAsistenciaDTO();
        dto.id = asistencia.getId();
        dto.tipoEvento = asistencia.getTipoEvento();
        dto.fechaHora = asistencia.getFechaHora();
        dto.observaciones = asistencia.getObservaciones();
        
        if (asistencia.getEmpleado() != null) {
            dto.nombreEmpleado = asistencia.getEmpleado().getNombre();
            dto.primerApellidoEmpleado = asistencia.getEmpleado().getPrimerApellido();
            dto.segundoApellidoEmpleado = asistencia.getEmpleado().getSegundoApellido();
        }
        
        return dto;
    }

    // ==================== AUTHENTICATION & AUTHORIZATION ====================

    /**
     * Get the current authenticated User from SecurityContext
     */
    private User obtenerUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenException("Usuario no autenticado");
        }
        
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            throw new ForbiddenException("Tipo de usuario no válido");
        }
        
        return (User) principal;
    }

    /**
     * Get the Empleados entity for the current authenticated user
     */
    private Empleados obtenerEmpleadoAutenticado() {
        User user = obtenerUsuarioAutenticado();
        return obtenerEmpleadoDesdeUsuario(user);
    }

    /**
     * Get Empleados entity from User entity
     */
    private Empleados obtenerEmpleadoDesdeUsuario(User user) {
        Empleados empleado = user.getEmpleado();
        if (empleado == null) {
            // Try to find by user ID
            empleado = empleadosRepositorio.findByUsuarioId(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Empleado", "usuarioId", user.getId()));
        }
        return empleado;
    }

    /**
     * Validate that the current user can access a specific department's data
     */
    private void validarAccesoDepartamento(User currentUser, Empleados empleadoActual, Long idDepartamento) {
        Role role = currentUser.getRole();
        
        // HR and ADMIN can access all departments
        if (role == Role.HR || role == Role.ADMIN) {
            return;
        }
        
        // JEFE can access departments they manage
        if (role == Role.JEFE) {
            Optional<JefesDepartamento> jefatura = jefesDepartamentoRepositorio
                    .findByEmpleadoIdAndDepartamentoIdAndEstaActivoTrue(empleadoActual.getId(), idDepartamento);
            
            if (jefatura.isPresent()) {
                return;
            }
            
            throw new ForbiddenException("No tiene acceso a este departamento. Solo puede ver departamentos que gestiona.");
        }
        
        // EMPLEADO can only see their own department (but only their own records, not summary)
        throw new ForbiddenException("No tiene permisos para ver el resumen de departamento.");
    }

    /**
     * Validate that the current user can access a specific employee's data
     */
    private void validarAccesoEmpleado(User currentUser, Empleados empleadoActual, Long targetEmpleadoId) {
        Role role = currentUser.getRole();
        
        // HR and ADMIN can access all employees
        if (role == Role.HR || role == Role.ADMIN) {
            return;
        }
        
        // JEFE can access employees in their department
        if (role == Role.JEFE) {
            // Get target employee
            Empleados targetEmpleado = empleadosRepositorio.findById(targetEmpleadoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Empleado", "id", targetEmpleadoId));
            
            if (targetEmpleado.getPuesto() != null && targetEmpleado.getPuesto().getDepartamento() != null) {
                Long targetDepartamentoId = targetEmpleado.getPuesto().getDepartamento().getId();
                
                Optional<JefesDepartamento> jefatura = jefesDepartamentoRepositorio
                        .findByEmpleadoIdAndDepartamentoIdAndEstaActivoTrue(empleadoActual.getId(), targetDepartamentoId);
                
                if (jefatura.isPresent()) {
                    return;
                }
            }
            
            throw new ForbiddenException("No tiene acceso a los registros de este empleado.");
        }
        
        // EMPLEADO can only access their own records
        throw new ForbiddenException("Solo puede ver sus propios registros de asistencia.");
    }
}
