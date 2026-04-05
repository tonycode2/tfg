package com.anthony.tfg.tfg.Modulos.Permisos.Servicio;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaPermisosDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudPermisosDTO;
import com.anthony.tfg.tfg.Entidades.Empleados;
import com.anthony.tfg.tfg.Entidades.JefesDepartamento;
import com.anthony.tfg.tfg.Entidades.Permisos;
import com.anthony.tfg.tfg.Entidades.Enums.EstadoSolicitud;
import com.anthony.tfg.tfg.Entidades.Enums.TipoPermiso;
import com.anthony.tfg.tfg.Entidades.Enums.UnidadTiempo;
import com.anthony.tfg.tfg.Exceptions.BadRequestException;
import com.anthony.tfg.tfg.Exceptions.ForbiddenException;
import com.anthony.tfg.tfg.Exceptions.ResourceNotFoundException;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasEmpleados;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasPermisos;
import com.anthony.tfg.tfg.Modulos.DiasFeriados.Servicio.ServicioDiasFeriados;
import com.anthony.tfg.tfg.Modulos.Empleados.Servicio.ServicioEmail;
import com.anthony.tfg.tfg.Modulos.Interfaces.ServicioInterface;
import com.anthony.tfg.tfg.Modulos.JornadaDiaria.Servicio.ServicioJornadaDiaria;
import com.anthony.tfg.tfg.Modulos.Mantenimientos.MantenimientosPermisos;
import com.anthony.tfg.tfg.Modulos.Seguridad.user.User;
import com.anthony.tfg.tfg.Modulos.Vacaciones.Servicio.ServicioVacaciones;
import com.anthony.tfg.tfg.Repositorios.JefesDepartamentoRepositorio;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ServicioPermisos implements ServicioInterface<RespuestaPermisosDTO, 
                                                        SolicitudPermisosDTO, 
                                                        Permisos>{

    private final ConsultasPermisos consulta;
    private final MantenimientosPermisos mantenimiento;
    private final ConsultasEmpleados consultasEmpleados;
    private final JefesDepartamentoRepositorio jefesDepartamentoRepo;
    private final ServicioEmail servicioEmail;
    private final ServicioVacaciones servicioVacaciones;
    private final ServicioDiasFeriados servicioDiasFeriados;
    private final ServicioJornadaDiaria servicioJornadaDiaria;

    public ServicioPermisos(
            ConsultasPermisos consulta, 
            MantenimientosPermisos mantenimiento, 
            ConsultasEmpleados consultasEmpleados,
            JefesDepartamentoRepositorio jefesDepartamentoRepo,
            ServicioEmail servicioEmail,
            ServicioVacaciones servicioVacaciones,
            ServicioDiasFeriados servicioDiasFeriados,
            ServicioJornadaDiaria servicioJornadaDiaria) {
        this.consulta = consulta;
        this.mantenimiento = mantenimiento;
        this.consultasEmpleados = consultasEmpleados;
        this.jefesDepartamentoRepo = jefesDepartamentoRepo;
        this.servicioEmail = servicioEmail;
        this.servicioVacaciones = servicioVacaciones;
        this.servicioDiasFeriados = servicioDiasFeriados;
        this.servicioJornadaDiaria = servicioJornadaDiaria;
    }

    /**
     * Obtiene un registro por su identificador.
     * @param id parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public RespuestaPermisosDTO obtenerPorId(Long id) {
        Permisos permiso = consulta.obtenerPorId(id);
        if(permiso == null){
            log.warn("No se ha encontrado el permiso con ID: " + id);
            throw new ResourceNotFoundException("Permisos", "id", id);
        }
        log.info("Se ha encontrado el permiso con ID: " + id);
        return deEntidadDtoARespuesta(permiso);
    }

    /**
     * Obtiene todos los registros disponibles.
     * @return resultado de la operacion.
     */
    public List<RespuestaPermisosDTO> obtenerTodos() {
        List<Permisos> entidades = consulta.obtenerTodos();
        log.info("Se han obtenido todos los permisos. La cantidad de registros es: " + entidades.size());
        return deListaEntidadADto(entidades);
    }

    /**
     * Implementación de la interfaz - usa el idEmpleado del DTO directamente
     */
    @Override
    public RespuestaPermisosDTO guardar(SolicitudPermisosDTO entidad) {
        return guardarInterno(entidad);
    }

    /**
     * Crea una nueva solicitud de permiso desde el endpoint público (autenticado).
     * Obtiene automáticamente el empleado del usuario autenticado para mayor seguridad.
     * Determina automáticamente el estado inicial:
     * - PENDIENTE si hay jefe en el departamento
     * - PENDIENTE_RH si no hay jefe o el solicitante es jefe
     */
    public RespuestaPermisosDTO guardar(SolicitudPermisosDTO entidad, Authentication auth) {
        // Obtener el empleado autenticado automáticamente
        Empleados empleadoAutenticado = obtenerEmpleadoAutenticado(auth);
        
        // Sobrescribir el idEmpleado del DTO con el empleado autenticado
        // Esto previene que un usuario pueda crear permisos para otro empleado
        entidad.setIdEmpleado(empleadoAutenticado.getId());
        
        return guardarInterno(entidad);
    }

    /**
     * Crea una nueva solicitud de permiso (método interno).
     * Determina automáticamente el estado inicial:
     * - PENDIENTE si hay jefe en el departamento
     * - PENDIENTE_RH si no hay jefe o el solicitante es jefe
     */
    private RespuestaPermisosDTO guardarInterno(SolicitudPermisosDTO entidad) {
        // Validar fechas no retroactivas
        LocalDate hoy = LocalDate.now();
        
        if (entidad.getFechaInicio().isBefore(hoy)) {
            throw new BadRequestException("No se permiten solicitudes de permisos con fechas pasadas");
        }
        
        // Validar que fechaFin >= fechaInicio
        if (entidad.getFechaFin().isBefore(entidad.getFechaInicio())) {
            throw new BadRequestException("La fecha de fin debe ser igual o posterior a la fecha de inicio");
        }
        
        // Validar que no haya días feriados en el rango de fechas
        servicioDiasFeriados.validarNoFeriadosEnRango(entidad.getFechaInicio(), entidad.getFechaFin());
        log.info("Validación de días feriados exitosa para rango {} - {}", entidad.getFechaInicio(), entidad.getFechaFin());
        
        String unidadTiempo = entidad.getUnidadTiempo() != null ? entidad.getUnidadTiempo() : "DIAS";
        
        Permisos nuevoPermiso = deSolicitudDtoAEntidad(entidad);
        
        // Calcular según unidad de tiempo
        if ("HORAS".equals(unidadTiempo)) {
            // Las vacaciones no pueden solicitarse por horas
            TipoPermiso tipoPermiso = obtenerTipoPermiso(entidad.getTipoPermiso());
            if (tipoPermiso == TipoPermiso.VACACIONES) {
                throw new BadRequestException("Las vacaciones solo pueden solicitarse por días completos, no por horas");
            }
            
            // Validar que las fechas sean el mismo día
            if (!entidad.getFechaInicio().equals(entidad.getFechaFin())) {
                throw new BadRequestException("Los permisos por horas deben ser en el mismo día");
            }
            
            // Validar que horaInicio y horaFin estén presentes
            if (entidad.getHoraInicio() == null || entidad.getHoraFin() == null) {
                throw new BadRequestException("Debe especificar hora de inicio y fin para permisos por horas");
            }
            
            // Calcular total de horas
            double totalHoras = calcularHoras(entidad.getHoraInicio(), entidad.getHoraFin());
            if (totalHoras <= 0) {
                throw new BadRequestException("La hora de fin debe ser posterior a la hora de inicio");
            }
            
            nuevoPermiso.setTotalHoras(totalHoras);
            nuevoPermiso.setDiasTotales(0); // No aplica para permisos por horas
            log.info("Permiso por horas: {} horas desde {} hasta {}", totalHoras, entidad.getHoraInicio(), entidad.getHoraFin());
        } else {
            // Calcular días hábiles automáticamente (el backend es la fuente de verdad)
            int diasHabiles = calcularDiasHabiles(entidad.getFechaInicio(), entidad.getFechaFin());
            nuevoPermiso.setDiasTotales(diasHabiles);
            nuevoPermiso.setTotalHoras(null); // No aplica para permisos por días
            log.info("Permiso por días: {} días hábiles", diasHabiles);
            
            // Validar saldo de vacaciones si el tipo de permiso es VACACIONES
            if (nuevoPermiso.getTipoPermiso() == TipoPermiso.VACACIONES) {
                servicioVacaciones.validarSaldoDisponible(entidad.getIdEmpleado(), diasHabiles);
                log.info("Saldo de vacaciones validado para empleado {}", entidad.getIdEmpleado());
            }
        }
        
        // Determinar estado inicial automáticamente
        Empleados solicitante = nuevoPermiso.getEmpleado();
        EstadoSolicitud estadoInicial = determinarEstadoInicial(solicitante);
        nuevoPermiso.setEstadoSolicitud(estadoInicial);
        
        // Asignar fecha de solicitud
        nuevoPermiso.setFechaSolicitud(LocalDate.now());
        
        Permisos permisoGuardado = mantenimiento.crear(nuevoPermiso);
        log.info("Se ha guardado un nuevo permiso con ID: {} y estado: {}", 
                permisoGuardado.getId(), permisoGuardado.getEstadoSolicitud());

        if (permisoGuardado.getUnidadTiempo() == null || permisoGuardado.getUnidadTiempo() == UnidadTiempo.DIAS) {
            servicioJornadaDiaria.generarJornadasParaPermiso(permisoGuardado);
        }

        return deEntidadDtoARespuesta(permisoGuardado);
    }

    /**
     * Determina el estado inicial de una solicitud de permiso
     */
    private EstadoSolicitud determinarEstadoInicial(Empleados empleado) {
        // Verificar si el empleado es jefe
        List<JefesDepartamento> esJefe = jefesDepartamentoRepo.findByEmpleadoIdAndEstaActivoTrue(empleado.getId());
        if (!esJefe.isEmpty()) {
            log.info("El empleado {} es jefe, solicitud va directo a RH", empleado.getId());
            return EstadoSolicitud.PENDIENTE_RH;
        }
        
        // Verificar si hay jefe en el departamento del empleado
        if (empleado.getPuesto() == null || empleado.getPuesto().getDepartamento() == null) {
            log.warn("El empleado {} no tiene departamento asignado, solicitud va directo a RH", empleado.getId());
            return EstadoSolicitud.PENDIENTE_RH;
        }
        
        Long idDepartamento = empleado.getPuesto().getDepartamento().getId();
        // Aquí asumimos que si no hay jefe activo, devuelve lista vacía
        List<JefesDepartamento> jefes = jefesDepartamentoRepo.findAll().stream()
                .filter(jd -> jd.getDepartamento().getId().equals(idDepartamento) && jd.getEstaActivo())
                .toList();
        
        if (jefes.isEmpty()) {
            log.info("No hay jefe en el departamento {}, solicitud va directo a RH", idDepartamento);
            return EstadoSolicitud.PENDIENTE_RH;
        }
        
        log.info("Hay jefe en el departamento {}, solicitud queda PENDIENTE", idDepartamento);
        return EstadoSolicitud.PENDIENTE;
    }

    /**
     * Obtiene las solicitudes del empleado autenticado
     */
    public List<RespuestaPermisosDTO> obtenerMisSolicitudes(Authentication auth) {
        Empleados empleado = obtenerEmpleadoAutenticado(auth);
        List<Permisos> solicitudes = consulta.obtenerPorEmpleadoId(empleado.getId());
        log.info("Se obtuvieron {} solicitudes para el empleado {}", solicitudes.size(), empleado.getId());
        return deListaEntidadADto(solicitudes);
    }

    /**
     * Obtiene las solicitudes pendientes del departamento que maneja el jefe autenticado
     */
    public List<RespuestaPermisosDTO> obtenerSolicitudesPendientesDepartamento(Authentication auth) {
        Empleados jefe = obtenerEmpleadoAutenticado(auth);
        
        // Verificar que el usuario sea jefe
        List<Long> departamentosQueManeja = jefesDepartamentoRepo.findDepartamentoIdsByEmpleadoId(jefe.getId());
        if (departamentosQueManeja.isEmpty()) {
            throw new ForbiddenException("El usuario no es jefe de ningún departamento");
        }
        
        // Obtener solicitudes pendientes de todos los departamentos que maneja
        List<Permisos> solicitudes = departamentosQueManeja.stream()
                .flatMap(idDep -> consulta.obtenerPermisosPendientesByDepartamento(idDep).stream())
                .toList();
        
        log.info("Se obtuvieron {} solicitudes pendientes para el jefe {}", solicitudes.size(), jefe.getId());
        return deListaEntidadADto(solicitudes);
    }

    /**
     * Obtiene las solicitudes que necesitan aprobación de RH
     */
    public List<RespuestaPermisosDTO> obtenerSolicitudesParaRH() {
        List<Permisos> solicitudes = consulta.obtenerPermisosParaRH();
        log.info("Se obtuvieron {} solicitudes pendientes para RH", solicitudes.size());
        return deListaEntidadADto(solicitudes);
    }

    /**
     * Aprueba una solicitud como jefe
     */
    public RespuestaPermisosDTO aprobarPorJefe(Long idPermiso, String comentarios, Authentication auth) {
        Empleados jefe = obtenerEmpleadoAutenticado(auth);
        Permisos permiso = consulta.obtenerPorId(idPermiso);
        
        if (permiso == null) {
            throw new ResourceNotFoundException("Permisos", "id", idPermiso);
        }
        
        // Validar que el permiso esté en estado PENDIENTE
        if (permiso.getEstadoSolicitud() != EstadoSolicitud.PENDIENTE) {
            throw new BadRequestException("La solicitud no está en estado PENDIENTE");
        }
        
        // Validar que el jefe sea del departamento del empleado
        Long idDepartamento = permiso.getEmpleado().getPuesto().getDepartamento().getId();
        if (!jefesDepartamentoRepo.findByEmpleadoIdAndDepartamentoIdAndEstaActivoTrue(jefe.getId(), idDepartamento).isPresent()) {
            throw new ForbiddenException("No tiene permisos para aprobar esta solicitud");
        }
        
        // Actualizar permiso
        permiso.setEstadoSolicitud(EstadoSolicitud.APROBADA_POR_JEFE);
        permiso.setComentariosJefe(comentarios);
        permiso.setFechaAprobacionJefe(LocalDate.now());
        permiso.setAprobadorJefe(jefe);
        
        Permisos permisoActualizado = mantenimiento.actualizar(permiso);
        log.info("Permiso {} aprobado por jefe {}", idPermiso, jefe.getId());
        return deEntidadDtoARespuesta(permisoActualizado);
    }

    /**
     * Rechaza una solicitud como jefe
     */
    public RespuestaPermisosDTO rechazarPorJefe(Long idPermiso, String comentarios, Authentication auth) {
        Empleados jefe = obtenerEmpleadoAutenticado(auth);
        Permisos permiso = consulta.obtenerPorId(idPermiso);
        
        if (permiso == null) {
            throw new ResourceNotFoundException("Permisos", "id", idPermiso);
        }
        
        // Validar que el permiso esté en estado PENDIENTE
        if (permiso.getEstadoSolicitud() != EstadoSolicitud.PENDIENTE) {
            throw new BadRequestException("La solicitud no está en estado PENDIENTE");
        }
        
        // Validar que el jefe sea del departamento del empleado
        Long idDepartamento = permiso.getEmpleado().getPuesto().getDepartamento().getId();
        if (!jefesDepartamentoRepo.findByEmpleadoIdAndDepartamentoIdAndEstaActivoTrue(jefe.getId(), idDepartamento).isPresent()) {
            throw new ForbiddenException("No tiene permisos para rechazar esta solicitud");
        }
        
        // Actualizar permiso
        permiso.setEstadoSolicitud(EstadoSolicitud.RECHAZADA_POR_JEFE);
        permiso.setComentariosJefe(comentarios);
        permiso.setFechaAprobacionJefe(LocalDate.now());
        
        Permisos permisoActualizado = mantenimiento.actualizar(permiso);
        log.info("Permiso {} rechazado por jefe {}", idPermiso, jefe.getId());
        return deEntidadDtoARespuesta(permisoActualizado);
    }

    /**
     * Aprueba una solicitud como RH (aprobación final)
     */
    public RespuestaPermisosDTO aprobarPorRH(Long idPermiso, String comentarios, Authentication auth) {
        Empleados rh = obtenerEmpleadoAutenticado(auth);
        Permisos permiso = consulta.obtenerPorId(idPermiso);
        
        if (permiso == null) {
            throw new ResourceNotFoundException("Permisos", "id", idPermiso);
        }
        
        // Validar que el permiso esté en estado APROBADA_POR_JEFE o PENDIENTE_RH
        if (permiso.getEstadoSolicitud() != EstadoSolicitud.APROBADA_POR_JEFE && 
            permiso.getEstadoSolicitud() != EstadoSolicitud.PENDIENTE_RH) {
            throw new BadRequestException("La solicitud no está pendiente de aprobación de RH");
        }
        
        // Actualizar permiso
        permiso.setEstadoSolicitud(EstadoSolicitud.APROBADA);
        permiso.setComentariosRH(comentarios);
        permiso.setFechaAprobacionRH(LocalDate.now());
        permiso.setAprobadorRH(rh);
        
        Permisos permisoActualizado = mantenimiento.actualizar(permiso);
        log.info("Permiso {} aprobado por RH {}", idPermiso, rh.getId());
        
        // Si es solicitud de VACACIONES, descontar días del saldo
        if (permisoActualizado.getTipoPermiso() == TipoPermiso.VACACIONES) {
            Long idEmpleado = permisoActualizado.getEmpleado().getId();
            Integer diasAprobados = permisoActualizado.getDiasTotales();
            servicioVacaciones.descontarDias(idEmpleado, diasAprobados);
            log.info("Se descontaron {} días de vacaciones al empleado {} por aprobación del permiso {}",
                    diasAprobados, idEmpleado, idPermiso);
        }
        
        // Enviar notificación por email
        enviarEmailAprobacion(permisoActualizado);
        
        return deEntidadDtoARespuesta(permisoActualizado);
    }

    /**
     * Rechaza una solicitud como RH
     */
    public RespuestaPermisosDTO rechazarPorRH(Long idPermiso, String comentarios, Authentication auth) {
        Empleados rh = obtenerEmpleadoAutenticado(auth);
        Permisos permiso = consulta.obtenerPorId(idPermiso);
        
        if (permiso == null) {
            throw new ResourceNotFoundException("Permisos", "id", idPermiso);
        }
        
        // Validar que el permiso esté en estado APROBADA_POR_JEFE o PENDIENTE_RH
        if (permiso.getEstadoSolicitud() != EstadoSolicitud.APROBADA_POR_JEFE && 
            permiso.getEstadoSolicitud() != EstadoSolicitud.PENDIENTE_RH) {
            throw new BadRequestException("La solicitud no está pendiente de aprobación de RH");
        }
        
        // Actualizar permiso
        permiso.setEstadoSolicitud(EstadoSolicitud.RECHAZADA_POR_RH);
        permiso.setComentariosRH(comentarios);
        permiso.setFechaAprobacionRH(LocalDate.now());
        
        Permisos permisoActualizado = mantenimiento.actualizar(permiso);
        log.info("Permiso {} rechazado por RH {}", idPermiso, rh.getId());
        
        // Enviar notificación por email
        enviarEmailRechazo(permisoActualizado);
        
        return deEntidadDtoARespuesta(permisoActualizado);
    }

    /**
     * Cancela una solicitud (solo RH puede cancelar aprobadas)
     */
    public RespuestaPermisosDTO cancelarSolicitud(Long idPermiso, Authentication auth) {
        Empleados rh = obtenerEmpleadoAutenticado(auth);
        Permisos permiso = consulta.obtenerPorId(idPermiso);
        
        if (permiso == null) {
            throw new ResourceNotFoundException("Permisos", "id", idPermiso);
        }
        
        // Solo RH puede cancelar solicitudes aprobadas
        if (permiso.getEstadoSolicitud() != EstadoSolicitud.APROBADA) {
            throw new BadRequestException("Solo se pueden cancelar solicitudes aprobadas");
        }
        
        // Si es vacaciones aprobadas, restaurar los días descontados
        if (permiso.getTipoPermiso() == TipoPermiso.VACACIONES) {
            Long idEmpleado = permiso.getEmpleado().getId();
            Integer diasAprobados = permiso.getDiasTotales();
            servicioVacaciones.restaurarDias(idEmpleado, diasAprobados);
            log.info("Se restauraron {} días de vacaciones al empleado {} por cancelación del permiso {}",
                    diasAprobados, idEmpleado, idPermiso);
        }
        
        permiso.setEstadoSolicitud(EstadoSolicitud.CANCELADA);
        Permisos permisoActualizado = mantenimiento.actualizar(permiso);
        log.info("Permiso {} cancelado por RH {}", idPermiso, rh.getId());
        
        // Enviar notificación de cancelación
        enviarEmailCancelacion(permisoActualizado);
        
        return deEntidadDtoARespuesta(permisoActualizado);
    }

    /**
     * Calcula días hábiles entre dos fechas (excluye fines de semana)
     */
    private int calcularDiasHabiles(LocalDate fechaInicio, LocalDate fechaFin) {
        int diasHabiles = 0;
        LocalDate fecha = fechaInicio;
        
        while (!fecha.isAfter(fechaFin)) {
            DayOfWeek diaSemana = fecha.getDayOfWeek();
            if (diaSemana != DayOfWeek.SATURDAY && diaSemana != DayOfWeek.SUNDAY) {
                diasHabiles++;
            }
            fecha = fecha.plusDays(1);
        }
        
        return diasHabiles;
    }

    /**
     * Calcula el total de horas entre dos horas en formato HH:mm
     */
    private double calcularHoras(String horaInicio, String horaFin) {
        try {
            String[] inicioPartes = horaInicio.split(":");
            String[] finPartes = horaFin.split(":");
            
            int horaIni = Integer.parseInt(inicioPartes[0]);
            int minIni = Integer.parseInt(inicioPartes[1]);
            int horaFi = Integer.parseInt(finPartes[0]);
            int minFi = Integer.parseInt(finPartes[1]);
            
            int minutosInicio = horaIni * 60 + minIni;
            int minutosFin = horaFi * 60 + minFi;
            
            int diferenciaMinutos = minutosFin - minutosInicio;
            return diferenciaMinutos / 60.0;
        } catch (Exception e) {
            throw new BadRequestException("Formato de hora inválido. Use HH:mm (ejemplo: 08:00)");
        }
    }

    /**
     * Envía email de notificación cuando RH aprueba un permiso
     */
    private void enviarEmailAprobacion(Permisos permiso) {
        try {
            Empleados empleado = permiso.getEmpleado();
            if (empleado.getCorreoPersonal() == null || empleado.getCorreoPersonal().isEmpty()) {
                log.warn("El empleado {} no tiene correo registrado", empleado.getId());
                return;
            }
            
            String nombreCompleto = empleado.getNombre() + " " + empleado.getPrimerApellido();
            String tipoPermiso = permiso.getTipoPermiso().toString();
            DateTimeFormatter fechaFormato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            servicioEmail.enviarNotificacionPermiso(
                empleado.getCorreoPersonal(),
                nombreCompleto,
                tipoPermiso,
                true,
                permiso.getComentariosRH(),
                permiso.getDiasTotales(),
                permiso.getFechaInicio().format(fechaFormato),
                permiso.getFechaFin().format(fechaFormato)
            );
            
            log.info("Email de aprobación de permiso enviado a {}", empleado.getCorreoPersonal());
        } catch (Exception e) {
            log.error("Error al enviar email de aprobación de permiso: {}", e.getMessage());
        }
    }

    /**
     * Envía email de notificación cuando RH rechaza un permiso
     */
    private void enviarEmailRechazo(Permisos permiso) {
        try {
            Empleados empleado = permiso.getEmpleado();
            if (empleado.getCorreoPersonal() == null || empleado.getCorreoPersonal().isEmpty()) {
                log.warn("El empleado {} no tiene correo registrado", empleado.getId());
                return;
            }
            
            String nombreCompleto = empleado.getNombre() + " " + empleado.getPrimerApellido();
            String tipoPermiso = permiso.getTipoPermiso().toString();
            DateTimeFormatter fechaFormato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            servicioEmail.enviarNotificacionPermiso(
                empleado.getCorreoPersonal(),
                nombreCompleto,
                tipoPermiso,
                false,
                permiso.getComentariosRH(),
                permiso.getDiasTotales(),
                permiso.getFechaInicio().format(fechaFormato),
                permiso.getFechaFin().format(fechaFormato)
            );
            
            log.info("Email de rechazo de permiso enviado a {}", empleado.getCorreoPersonal());
        } catch (Exception e) {
            log.error("Error al enviar email de rechazo de permiso: {}", e.getMessage());
        }
    }

    /**
     * Envía email de notificación cuando RH cancela un permiso aprobado
     */
    private void enviarEmailCancelacion(Permisos permiso) {
        try {
            Empleados empleado = permiso.getEmpleado();
            if (empleado.getCorreoPersonal() == null || empleado.getCorreoPersonal().isEmpty()) {
                log.warn("El empleado {} no tiene correo registrado", empleado.getId());
                return;
            }
            
            String nombreCompleto = empleado.getNombre() + " " + empleado.getPrimerApellido();
            String tipoPermiso = permiso.getTipoPermiso().toString();
            DateTimeFormatter fechaFormato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            servicioEmail.enviarNotificacionPermiso(
                empleado.getCorreoPersonal(),
                nombreCompleto,
                tipoPermiso,
                false,
                "Su solicitud de " + tipoPermiso.toLowerCase() + " aprobada ha sido cancelada.",
                permiso.getDiasTotales(),
                permiso.getFechaInicio().format(fechaFormato),
                permiso.getFechaFin().format(fechaFormato)
            );
            
            log.info("Email de cancelación de permiso enviado a {}", empleado.getCorreoPersonal());
        } catch (Exception e) {
            log.error("Error al enviar email de cancelación de permiso: {}", e.getMessage());
        }
    }

    /**
     * Actualiza un registro existente.
     * @param id parametro de entrada de la operacion.
     * @param entidad parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public RespuestaPermisosDTO actualizar(Long id, SolicitudPermisosDTO entidad) {
        Permisos permisoExistente = consulta.obtenerPorId(id);
        if(permisoExistente == null){
            log.warn("No se ha encontrado el permiso con ID: " + id + " para actualizar");
            throw new ResourceNotFoundException("Permisos", "id", id);
        }
        permisoExistente.setFechaInicio(entidad.getFechaInicio());
        permisoExistente.setFechaFin(entidad.getFechaFin());
        permisoExistente.setDiasTotales(entidad.getDiasTotales());
        permisoExistente.setMotivo(entidad.getMotivo());
        permisoExistente.setObservacionesEmpleado(entidad.getObservacionesEmpleado());
        permisoExistente.setUrlDocumentoAdjunto(entidad.getUrlDocumentoAdjunto());
        
        TipoPermiso tipoPermiso = obtenerTipoPermiso(entidad.getTipoPermiso());
        if(tipoPermiso != null){
            permisoExistente.setTipoPermiso(tipoPermiso);
        }
        
        Empleados empleado = consultasEmpleados.obtenerPorId(entidad.getIdEmpleado());
        if(empleado != null){
            permisoExistente.setEmpleado(empleado);
        }
        
        Permisos permisoActualizado = mantenimiento.actualizar(permisoExistente);
        log.info("Se ha actualizado el permiso con ID: " + id);
        return deEntidadDtoARespuesta(permisoActualizado);
    }

    /**
     * Elimina un registro por su identificador.
     * @param id parametro de entrada de la operacion.
     */
    @Override
    public void eliminar(Long id) {
        throw new BadRequestException("No se permite eliminar solicitudes de permisos. Use el endpoint autenticado.");
    }

    /**
     * Elimina una solicitud de permiso (solo ADMIN)
     */
    public void eliminar(Long id, Authentication auth) {
        // Verificar que el usuario sea ADMIN
        User usuario = obtenerUsuarioAutenticado(auth);
        if (!usuario.getRole().name().equals("ADMIN")) {
            throw new com.anthony.tfg.tfg.Exceptions.ForbiddenException(
                "Solo los administradores pueden eliminar solicitudes de permisos");
        }

        Permisos permiso = consulta.obtenerPorId(id);
        if (permiso == null) {
            throw new ResourceNotFoundException("Permisos", "id", id);
        }

        mantenimiento.eliminar(id);
        log.info("El administrador {} eliminó la solicitud de permiso con ID: {}", 
                usuario.getUsername(), id);
    }

    /**
     * Convierte un DTO de solicitud a entidad.
     * @param solicitud parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public Permisos deSolicitudDtoAEntidad(SolicitudPermisosDTO solicitud) {
        if(solicitud == null){
            log.warn("El DTO de solicitud es nulo, no se puede convertir a entidad Permisos.");
            throw new BadRequestException("La solicitud de permiso no puede ser nula");
        }
        
        Empleados empleado = consultasEmpleados.obtenerPorId(solicitud.getIdEmpleado());
        if(empleado == null){
            log.warn("No se ha encontrado el empleado con ID: " + solicitud.getIdEmpleado());
            throw new ResourceNotFoundException("Empleados", "id", solicitud.getIdEmpleado());
        }
        
        TipoPermiso tipoPermiso = obtenerTipoPermiso(solicitud.getTipoPermiso());
        if(tipoPermiso == null){
            log.warn("No se ha encontrado el tipo de permiso: " + solicitud.getTipoPermiso());
            throw new BadRequestException("Tipo de permiso inválido: " + solicitud.getTipoPermiso());
        }
        
        Permisos permiso = Permisos.builder()
                    .id(solicitud.getId())
                    .fechaInicio(solicitud.getFechaInicio())
                    .fechaFin(solicitud.getFechaFin())
                    .diasTotales(solicitud.getDiasTotales())
                    .unidadTiempo(solicitud.getUnidadTiempo() != null ? 
                        com.anthony.tfg.tfg.Entidades.Enums.UnidadTiempo.valueOf(solicitud.getUnidadTiempo()) : 
                        com.anthony.tfg.tfg.Entidades.Enums.UnidadTiempo.DIAS)
                    .horaInicio(solicitud.getHoraInicio())
                    .horaFin(solicitud.getHoraFin())
                    .totalHoras(solicitud.getTotalHoras())
                    .motivo(solicitud.getMotivo())
                    .observacionesEmpleado(solicitud.getObservacionesEmpleado())
                    .urlDocumentoAdjunto(solicitud.getUrlDocumentoAdjunto())
                    .tipoPermiso(tipoPermiso)
                    .empleado(empleado)
                    .build();
        log.info("Se ha convertido el DTO de solicitud a entidad Permisos: {}", permiso);
        return permiso;
    }

    /**
     * Convierte una entidad a DTO de respuesta.
     * @param entidad parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public RespuestaPermisosDTO deEntidadDtoARespuesta(Permisos entidad) {
        if(entidad == null){
            log.warn("La entidad Permisos es nula, no se puede convertir a DTO de respuesta.");
            return null;
        }
        RespuestaPermisosDTO respuesta = new RespuestaPermisosDTO();
        respuesta.id = entidad.getId();
        respuesta.fechaInicio = entidad.getFechaInicio();
        respuesta.fechaFin = entidad.getFechaFin();
        respuesta.diasTotales = entidad.getDiasTotales();
        respuesta.unidadTiempo = entidad.getUnidadTiempo() != null ? entidad.getUnidadTiempo().name() : "DIAS";
        respuesta.horaInicio = entidad.getHoraInicio();
        respuesta.horaFin = entidad.getHoraFin();
        respuesta.totalHoras = entidad.getTotalHoras();
        respuesta.motivo = entidad.getMotivo();
        respuesta.observacionesEmpleado = entidad.getObservacionesEmpleado();
        respuesta.urlDocumentoAdjunto = entidad.getUrlDocumentoAdjunto();
        respuesta.fechaSolicitud = entidad.getFechaSolicitud();
        respuesta.fechaAprobacionJefe = entidad.getFechaAprobacionJefe();
        respuesta.fechaAprobacionRH = entidad.getFechaAprobacionRH();
        respuesta.comentariosJefe = entidad.getComentariosJefe();
        respuesta.comentariosRH = entidad.getComentariosRH();
        
        if(entidad.getEstadoSolicitud() != null){
            respuesta.estadoSolicitud = entidad.getEstadoSolicitud().name();
        }
        
        if(entidad.getTipoPermiso() != null){
            respuesta.tipoPermiso = entidad.getTipoPermiso().name();
        }
        
        if(entidad.getEmpleado() != null){
            respuesta.idEmpleado = entidad.getEmpleado().getId();
            respuesta.nombreEmpleado = entidad.getEmpleado().getNombre();
            respuesta.primerApellidoEmpleado = entidad.getEmpleado().getPrimerApellido();
            respuesta.segundApellidoEmpleado = entidad.getEmpleado().getSegundoApellido();
        }
        
        if(entidad.getAprobadorJefe() != null){
            respuesta.nombreAprobadorJefe = entidad.getAprobadorJefe().getNombre();
            respuesta.primerApellidoAprobadorJefe = entidad.getAprobadorJefe().getPrimerApellido();
            respuesta.segundoApellidoAprobadorJefe = entidad.getAprobadorJefe().getSegundoApellido();
        }
        
        if(entidad.getAprobadorRH() != null){
            respuesta.nombreAprobadorRH = entidad.getAprobadorRH().getNombre();
            respuesta.primerApellidoAprobadorRH = entidad.getAprobadorRH().getPrimerApellido();
            respuesta.segundoApellidoAprobadorRH = entidad.getAprobadorRH().getSegundoApellido();
        }
        
        log.info("Se ha convertido la entidad Permisos a DTO de respuesta: {}", respuesta);
        return respuesta;
    }

    /**
     * Convierte una lista de entidades a DTOs de respuesta.
     * @param entidades parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public List<RespuestaPermisosDTO> deListaEntidadADto(List<Permisos> entidades) {
        return entidades.stream()
                .map(this::deEntidadDtoARespuesta)
                .toList();
    }
    
    /**
     * Obtiene informacion necesaria para la operacion.
     * @param tipo parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    private TipoPermiso obtenerTipoPermiso(String tipo) {
        try {
            return TipoPermiso.valueOf(tipo.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    // ==================== AUTHENTICATION & AUTHORIZATION ====================

    /**
     * Obtiene el usuario autenticado del SecurityContext
     */
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

    /**
     * Obtiene el empleado asociado al usuario autenticado
     */
    private Empleados obtenerEmpleadoAutenticado(Authentication auth) {
        User user = obtenerUsuarioAutenticado(auth);
        Empleados empleado = user.getEmpleado();
        
        if (empleado == null) {
            throw new ForbiddenException("El usuario no tiene un empleado asociado");
        }
        
        return empleado;
    }
}
