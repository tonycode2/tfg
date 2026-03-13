package com.anthony.tfg.tfg.Modulos.JornadaDiaria.Servicio;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaJornadaDiariaDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudJornadaDiariaDTO;
import com.anthony.tfg.tfg.Entidades.Asistencia;
import com.anthony.tfg.tfg.Entidades.Empleados;
import com.anthony.tfg.tfg.Entidades.HorasExtra;
import com.anthony.tfg.tfg.Entidades.Incapacidades;
import com.anthony.tfg.tfg.Entidades.JornadaDiaria;
import com.anthony.tfg.tfg.Entidades.Permisos;
import com.anthony.tfg.tfg.Entidades.Puestos;
import com.anthony.tfg.tfg.Entidades.Enums.UnidadTiempo;
import com.anthony.tfg.tfg.Exceptions.ResourceNotFoundException;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasEmpleados;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasJornadaDiaria;
import com.anthony.tfg.tfg.Modulos.Interfaces.ServicioInterface;
import com.anthony.tfg.tfg.Modulos.Mantenimientos.MantenimientosJornadaDiaria;
import com.anthony.tfg.tfg.Modulos.Seguridad.user.User;
import com.anthony.tfg.tfg.Repositorios.AsistenciaRepositorio;
import com.anthony.tfg.tfg.Repositorios.HorasExtraRepositorio;
import com.anthony.tfg.tfg.Repositorios.EmpleadosRepositorio;
import com.anthony.tfg.tfg.Repositorios.IncapacidadesRepositorio;
import com.anthony.tfg.tfg.Repositorios.JornadaDiariaRepositorio;
import com.anthony.tfg.tfg.Repositorios.PermisosRepositorio;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ServicioJornadaDiaria implements ServicioInterface<RespuestaJornadaDiariaDTO, 
                                                                SolicitudJornadaDiariaDTO, 
                                                                JornadaDiaria> {

    private final ConsultasJornadaDiaria consulta;
    private final MantenimientosJornadaDiaria mantenimiento;
    private final ConsultasEmpleados consultasEmpleados;
    private final AsistenciaRepositorio asistenciaRepositorio;
    private final HorasExtraRepositorio horasExtraRepositorio;
    private final JornadaDiariaRepositorio jornadaDiariaRepositorio;
    private final EmpleadosRepositorio empleadosRepositorio;
    private final PermisosRepositorio permisosRepositorio;
    private final IncapacidadesRepositorio incapacidadesRepositorio;

    public ServicioJornadaDiaria(
            ConsultasJornadaDiaria consulta,
            MantenimientosJornadaDiaria mantenimiento,
            ConsultasEmpleados consultasEmpleados,
            AsistenciaRepositorio asistenciaRepositorio,
            HorasExtraRepositorio horasExtraRepositorio,
            JornadaDiariaRepositorio jornadaDiariaRepositorio,
            EmpleadosRepositorio empleadosRepositorio,
            PermisosRepositorio permisosRepositorio,
            IncapacidadesRepositorio incapacidadesRepositorio) {
        this.consulta = consulta;
        this.mantenimiento = mantenimiento;
        this.consultasEmpleados = consultasEmpleados;
        this.asistenciaRepositorio = asistenciaRepositorio;
        this.horasExtraRepositorio = horasExtraRepositorio;
        this.jornadaDiariaRepositorio = jornadaDiariaRepositorio;
        this.empleadosRepositorio = empleadosRepositorio;
        this.permisosRepositorio = permisosRepositorio;
        this.incapacidadesRepositorio = incapacidadesRepositorio;
    }

    /**
     * Obtiene un registro por su identificador.
     * @param id parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public RespuestaJornadaDiariaDTO obtenerPorId(Long id) {
        JornadaDiaria jornada = consulta.obtenerPorId(id);
        if (jornada == null) {
            log.warn("No se ha encontrado la jornada diaria con ID: " + id);
            throw new ResourceNotFoundException("JornadaDiaria", "id", id);
        }
        log.info("Se ha encontrado la jornada diaria con ID: " + id);
        return deEntidadDtoARespuesta(jornada);
    }

    /**
     * Obtiene todos los registros disponibles.
     * @return resultado de la operacion.
     */
    public List<RespuestaJornadaDiariaDTO> obtenerTodos() {
        List<JornadaDiaria> entidades = consulta.obtenerTodos();
        log.info("Se han obtenido todas las jornadas diarias. La cantidad de registros es: " + entidades.size());
        return deListaEntidadADto(entidades);
    }

    /**
     * Guarda un nuevo registro.
     * @param entidad parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public RespuestaJornadaDiariaDTO guardar(SolicitudJornadaDiariaDTO entidad) {
        JornadaDiaria nuevaJornada = deSolicitudDtoAEntidad(entidad);
        JornadaDiaria jornadaGuardada = mantenimiento.crear(nuevaJornada);
        log.info("Se ha guardado una nueva jornada diaria con ID: " + jornadaGuardada.getId());
        return deEntidadDtoARespuesta(jornadaGuardada);
    }

    /**
     * Genera informacion requerida por el proceso.
     * @param permiso parametro de entrada de la operacion.
     */
    @Transactional
    public void generarJornadasParaPermiso(Permisos permiso) {
        if (permiso == null) {
            log.warn("No se pudo generar jornadas: el permiso es nulo");
            return;
        }

        if (permiso.getUnidadTiempo() != null && permiso.getUnidadTiempo() != UnidadTiempo.DIAS) {
            log.info("Permiso {} no genera jornadas porque es por horas", permiso.getId());
            return;
        }

        if (permiso.getEmpleado() == null || permiso.getFechaInicio() == null || permiso.getFechaFin() == null) {
            log.warn("Datos incompletos para generar jornadas del permiso {}", permiso.getId());
            return;
        }

        int diaInicial = obtenerSiguienteDiaPermiso(permiso.getId());
        generarJornadasEnRango(
                permiso.getEmpleado(),
                permiso.getFechaInicio(),
                permiso.getFechaFin(),
                permiso,
                null,
                diaInicial,
                "Día de permiso"
        );
    }

    /**
     * Genera informacion requerida por el proceso.
     * @param incapacidad parametro de entrada de la operacion.
     * @param fechaInicio parametro de entrada de la operacion.
     * @param fechaFin parametro de entrada de la operacion.
     */
    @Transactional
    public void generarJornadasParaIncapacidad(Incapacidades incapacidad, LocalDate fechaInicio, LocalDate fechaFin) {
        if (incapacidad == null) {
            log.warn("No se pudo generar jornadas: la incapacidad es nula");
            return;
        }

        LocalDate inicio = fechaInicio != null ? fechaInicio : incapacidad.getFechaInicio();
        LocalDate fin = fechaFin != null ? fechaFin : incapacidad.getFechaFin();

        if (incapacidad.getEmpleado() == null || inicio == null || fin == null) {
            log.warn("Datos incompletos para generar jornadas de la incapacidad {}", incapacidad.getId());
            return;
        }

        int diaInicial = obtenerSiguienteDiaIncapacidad(incapacidad.getId());
        generarJornadasEnRango(
                incapacidad.getEmpleado(),
                inicio,
                fin,
                null,
                incapacidad,
                diaInicial,
                "Día de incapacidad"
        );
    }

    /** 
     * @param empleado
     * @param fechaInicio
     * @param fechaFin
     * @param permiso
     * @param incapacidad
     * @param diaInicial
     * @param observacionBase
     */
    private void generarJornadasEnRango(Empleados empleado,
                                        LocalDate fechaInicio,
                                        LocalDate fechaFin,
                                        Permisos permiso,
                                        Incapacidades incapacidad,
                                        int diaInicial,
                                        String observacionBase) {
        if (fechaInicio.isAfter(fechaFin)) {
            log.warn("El rango de fechas es inválido: inicio {} después de fin {}", fechaInicio, fechaFin);
            return;
        }

        LocalDate fecha = fechaInicio;
        int diaPermiso = diaInicial;
        while (!fecha.isAfter(fechaFin)) {
            // No crear jornadas en fines de semana
            if (fecha.getDayOfWeek() == DayOfWeek.SATURDAY || fecha.getDayOfWeek() == DayOfWeek.SUNDAY) {
                fecha = fecha.plusDays(1);
                continue;
            }
            crearOActualizarJornadaEnCero(empleado, fecha, permiso, incapacidad, diaPermiso, observacionBase);
            fecha = fecha.plusDays(1);
            diaPermiso++;
        }
    }

    /** 
     * @param empleado
     * @param fecha
     * @param permiso
     * @param incapacidad
     * @param diaPermiso
     * @param observacionBase
     */
    private void crearOActualizarJornadaEnCero(Empleados empleado,
                                               LocalDate fecha,
                                               Permisos permiso,
                                               Incapacidades incapacidad,
                                               int diaPermiso,
                                               String observacionBase) {
        Optional<JornadaDiaria> existente = jornadaDiariaRepositorio.findByEmpleadoIdAndFecha(empleado.getId(), fecha);
        String observacion = observacionBase != null ? observacionBase + " - día " + diaPermiso : null;

        if (existente.isPresent()) {
            JornadaDiaria jornada = existente.get();
            jornada.setHoraEntrada(null);
            jornada.setHoraSalida(null);
            jornada.setHorasRegulares(0.0);
            jornada.setHorasExtra(0.0);
            jornada.setObservaciones(observacion);
            jornada.setPermiso(permiso);
            jornada.setIncapacidad(incapacidad);
            jornada.setDiaPermiso(diaPermiso);
            mantenimiento.actualizar(jornada);
        } else {
            JornadaDiaria jornada = JornadaDiaria.builder()
                    .fecha(fecha)
                    .horaEntrada(null)
                    .horaSalida(null)
                    .horasRegulares(0.0)
                    .horasExtra(0.0)
                    .observaciones(observacion)
                    .empleado(empleado)
                    .permiso(permiso)
                    .incapacidad(incapacidad)
                    .diaPermiso(diaPermiso)
                    .build();
            mantenimiento.crear(jornada);
        }
    }

    /**
     * Obtiene informacion necesaria para la operacion.
     * @param idPermiso parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    private int obtenerSiguienteDiaPermiso(Long idPermiso) {
        if (idPermiso == null) {
            return 1;
        }
        Integer maxDia = jornadaDiariaRepositorio.findMaxDiaPermisoByPermisoId(idPermiso);
        return (maxDia == null || maxDia == 0) ? 1 : maxDia + 1;
    }

    /**
     * Obtiene informacion necesaria para la operacion.
     * @param idIncapacidad parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    private int obtenerSiguienteDiaIncapacidad(Long idIncapacidad) {
        if (idIncapacidad == null) {
            return 1;
        }
        Integer maxDia = jornadaDiariaRepositorio.findMaxDiaPermisoByIncapacidadId(idIncapacidad);
        return (maxDia == null || maxDia == 0) ? 1 : maxDia + 1;
    }

    /**
     * Actualiza un registro existente.
     * @param id parametro de entrada de la operacion.
     * @param entidad parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public RespuestaJornadaDiariaDTO actualizar(Long id, SolicitudJornadaDiariaDTO entidad) {
        JornadaDiaria jornadaExistente = consulta.obtenerPorId(id);
        if (jornadaExistente == null) {
            log.warn("No se ha encontrado la jornada diaria con ID: " + id + " para actualizar");
            throw new ResourceNotFoundException("JornadaDiaria", "id", id);
        }
        
        jornadaExistente.setFecha(entidad.getFecha());
        jornadaExistente.setHoraEntrada(entidad.getHoraEntrada());
        jornadaExistente.setHoraSalida(entidad.getHoraSalida());
        jornadaExistente.setHorasRegulares(entidad.getHorasRegulares());
        jornadaExistente.setHorasExtra(entidad.getHorasExtra());
        jornadaExistente.setObservaciones(entidad.getObservaciones());
        jornadaExistente.setDiaPermiso(entidad.getDiaPermiso());
        
        Empleados empleado = consultasEmpleados.obtenerPorId(entidad.getIdEmpleado());
        if (empleado != null) {
            jornadaExistente.setEmpleado(empleado);
        }
        
        if (entidad.getIdPermiso() != null) {
            Permisos permiso = permisosRepositorio.findById(entidad.getIdPermiso()).orElse(null);
            jornadaExistente.setPermiso(permiso);
        }
        
        if (entidad.getIdIncapacidad() != null) {
            Incapacidades incapacidad = incapacidadesRepositorio.findById(entidad.getIdIncapacidad()).orElse(null);
            jornadaExistente.setIncapacidad(incapacidad);
        }
        
        JornadaDiaria jornadaActualizada = mantenimiento.actualizar(jornadaExistente);
        log.info("Se ha actualizado la jornada diaria con ID: " + id);
        return deEntidadDtoARespuesta(jornadaActualizada);
    }

    /**
     * Elimina un registro por su identificador.
     * @param id parametro de entrada de la operacion.
     */
    public void eliminar(Long id) {
        mantenimiento.eliminar(id);
        log.info("Se ha eliminado la jornada diaria con ID: " + id);
    }

    /**
     * Registra la jornada diaria cuando un empleado hace clock out.
     * Calcula automáticamente las horas regulares y extras basándose en:
     * - La hora de entrada y salida del puesto
     * - Las horas extra aprobadas para ese día
     * - Descuenta 1 hora de almuerzo
     */
    @Transactional
    public RespuestaJornadaDiariaDTO registrarJornadaPorClockOut(Long idEmpleado, LocalDateTime fechaHoraSalida) {
        log.info("Iniciando registro de jornada diaria para empleado ID: {} con salida: {}", idEmpleado, fechaHoraSalida);
        
        Empleados empleado = consultasEmpleados.obtenerPorId(idEmpleado);
        if (empleado == null) {
            log.error("No se encontró el empleado con ID: {}", idEmpleado);
            throw new ResourceNotFoundException("Empleados", "id", idEmpleado);
        }
        
        LocalDate fecha = fechaHoraSalida.toLocalDate();
        Permisos permisoHoras = obtenerPermisoHorasAprobado(idEmpleado, fecha);
        
        // Verificar si ya existe un registro para este día
        Optional<JornadaDiaria> jornadaExistente = jornadaDiariaRepositorio.findByEmpleadoIdAndFecha(idEmpleado, fecha);
        if (jornadaExistente.isPresent()) {
            JornadaDiaria jornada = jornadaExistente.get();
            if (permisoHoras != null && jornada.getPermiso() == null) {
                jornada.setPermiso(permisoHoras);
                jornada = mantenimiento.actualizar(jornada);
            }
            log.warn("Ya existe un registro de jornada diaria para empleado {} en fecha {}", idEmpleado, fecha);
            return deEntidadDtoARespuesta(jornada);
        }
        
        // Buscar la entrada del día
        LocalDateTime inicioDia = fecha.atStartOfDay();
        LocalDateTime finDia = fecha.atTime(23, 59, 59);
        List<Asistencia> asistenciasDelDia = asistenciaRepositorio.findByEmpleadoIdAndFechaHoraBetween(
                idEmpleado, inicioDia, finDia);
        
        Optional<Asistencia> entradaOpt = asistenciasDelDia.stream()
                .filter(a -> a.getTipoEvento().toString().equals("ENTRADA"))
                .findFirst();
        
        if (entradaOpt.isEmpty()) {
            log.error("No se encontró registro de entrada para empleado {} en fecha {}", idEmpleado, fecha);
            return null;
        }
        
        LocalDateTime fechaHoraEntrada = entradaOpt.get().getFechaHora();
        LocalTime horaEntrada = fechaHoraEntrada.toLocalTime();
        LocalTime horaSalida = fechaHoraSalida.toLocalTime();
        
        // Obtener horario del puesto
        Puestos puesto = empleado.getPuesto();
        if (puesto == null) {
            log.error("El empleado {} no tiene un puesto asignado", idEmpleado);
            return null;
        }
        
        LocalTime horaEntradaPuesto = puesto.getHoraEntrada().toLocalTime();
        LocalTime horaSalidaPuesto = puesto.getHoraSalida().toLocalTime();
        
        // Calcular horas trabajadas totales (descontando 1 hora de almuerzo)
        Duration duracionTotal = Duration.between(fechaHoraEntrada, fechaHoraSalida);
        double horasTotalesTrabajadas = (duracionTotal.toMinutes() / 60.0) - 1.0; // Descuento de almuerzo
        
        // Calcular horas de jornada regular esperadas (sin almuerzo)
        Duration duracionJornadaRegular = Duration.between(horaEntradaPuesto, horaSalidaPuesto);
        double horasJornadaRegular = (duracionJornadaRegular.toMinutes() / 60.0) - 1.0; // Sin almuerzo
        
        // Verificar si tiene horas extra aprobadas para este día
        List<HorasExtra> horasExtraAprobadas = horasExtraRepositorio.findByEmpleadoIdAndFechaSolicitudAndAprobadoTrue(
                idEmpleado, fecha);
        
        double totalHorasExtraAprobadas = horasExtraAprobadas.stream()
                .mapToInt(HorasExtra::getCantidadDeHoras)
                .sum();
        
        // Calcular horas regulares y extras
        double horasRegulares;
        double horasExtra;
        String observaciones = "";
        
        // Si trabajó menos o igual que la jornada regular
        if (horasTotalesTrabajadas <= horasJornadaRegular) {
            horasRegulares = horasTotalesTrabajadas;
            horasExtra = 0.0;
            if (horasTotalesTrabajadas >= horasJornadaRegular) {
                observaciones = "Jornada regular completa";
            } else {
                observaciones = String.format("Jornada parcial: %.2f horas", horasTotalesTrabajadas);
            }
        } else {
            // Trabajó más de la jornada regular
            double horasAdicionales = horasTotalesTrabajadas - horasJornadaRegular;
            horasRegulares = horasJornadaRegular;
            
            if (totalHorasExtraAprobadas > 0 && horasAdicionales > 0) {
                // Tiene horas extra aprobadas
                horasExtra = Math.min(horasAdicionales, totalHorasExtraAprobadas);
                observaciones = String.format("Horas extra aprobadas: %.2f", horasExtra);
                
                // Si trabajó más horas de las aprobadas
                if (horasAdicionales > totalHorasExtraAprobadas) {
                    double horasSinAprobar = horasAdicionales - totalHorasExtraAprobadas;
                    observaciones += String.format(" | Horas sin aprobar: %.2f", horasSinAprobar);
                }
            } else {
                // No tiene horas extra aprobadas
                horasExtra = 0.0;
                observaciones = String.format("Horas adicionales sin aprobar: %.2f", horasAdicionales);
            }
        }

            if (permisoHoras != null) {
                observaciones = observaciones == null || observaciones.isBlank()
                    ? "Permiso por horas aplicado"
                    : observaciones + " | Permiso por horas aplicado";
            }
        
        // Crear el registro de jornada diaria
        JornadaDiaria jornada = JornadaDiaria.builder()
                .fecha(fecha)
                .horaEntrada(horaEntrada)
                .horaSalida(horaSalida)
                .horasRegulares(horasRegulares)
                .horasExtra(horasExtra)
                .observaciones(observaciones)
                .empleado(empleado)
                .permiso(permisoHoras)
                .build();
        
        JornadaDiaria jornadaGuardada = mantenimiento.crear(jornada);
        log.info("Jornada diaria registrada exitosamente - Empleado: {}, Fecha: {}, Regulares: {}, Extras: {}", 
                idEmpleado, fecha, horasRegulares, horasExtra);
        
        return deEntidadDtoARespuesta(jornadaGuardada);
    }

    /**
     * Convierte un DTO de solicitud a entidad.
     * @param solicitud parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public JornadaDiaria deSolicitudDtoAEntidad(SolicitudJornadaDiariaDTO solicitud) {
        if (solicitud == null) {
            log.warn("El DTO de solicitud es nulo, no se puede convertir a entidad JornadaDiaria.");
            return null;
        }
        
        Empleados empleado = consultasEmpleados.obtenerPorId(solicitud.getIdEmpleado());
        if (empleado == null) {
            log.warn("No se ha encontrado el empleado con ID: " + solicitud.getIdEmpleado());
            return null;
        }
        
        Permisos permiso = null;
        if (solicitud.getIdPermiso() != null) {
            permiso = permisosRepositorio.findById(solicitud.getIdPermiso()).orElse(null);
        }
        
        Incapacidades incapacidad = null;
        if (solicitud.getIdIncapacidad() != null) {
            incapacidad = incapacidadesRepositorio.findById(solicitud.getIdIncapacidad()).orElse(null);
        }
        
        return JornadaDiaria.builder()
                .id(solicitud.getId())
                .fecha(solicitud.getFecha())
                .horaEntrada(solicitud.getHoraEntrada())
                .horaSalida(solicitud.getHoraSalida())
                .horasRegulares(solicitud.getHorasRegulares())
                .horasExtra(solicitud.getHorasExtra())
                .observaciones(solicitud.getObservaciones())
                .diaPermiso(solicitud.getDiaPermiso())
                .empleado(empleado)
                .permiso(permiso)
                .incapacidad(incapacidad)
                .build();
    }

    /**
     * Convierte una entidad a DTO de respuesta.
     * @param entidad parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public RespuestaJornadaDiariaDTO deEntidadDtoARespuesta(JornadaDiaria entidad) {
        if (entidad == null) {
            log.warn("La entidad JornadaDiaria es nula, no se puede convertir a DTO.");
            return null;
        }
        
        RespuestaJornadaDiariaDTO respuesta = new RespuestaJornadaDiariaDTO();
        respuesta.setId(entidad.getId());
        respuesta.setFecha(entidad.getFecha());
        respuesta.setHoraEntrada(entidad.getHoraEntrada());
        respuesta.setHoraSalida(entidad.getHoraSalida());
        respuesta.setHorasRegulares(entidad.getHorasRegulares());
        respuesta.setHorasExtra(entidad.getHorasExtra());
        respuesta.setObservaciones(entidad.getObservaciones());
        respuesta.setDiaPermiso(entidad.getDiaPermiso());
        
        if (entidad.getEmpleado() != null) {
            respuesta.setIdEmpleado(entidad.getEmpleado().getId());
            String nombreCompleto = String.format("%s %s %s",
                    entidad.getEmpleado().getNombre(),
                    entidad.getEmpleado().getPrimerApellido(),
                    entidad.getEmpleado().getSegundoApellido());
            respuesta.setNombreCompleto(nombreCompleto);
        }
        
        if (entidad.getPermiso() != null) {
            respuesta.setIdPermiso(entidad.getPermiso().getId());
        }
        
        if (entidad.getIncapacidad() != null) {
            respuesta.setIdIncapacidad(entidad.getIncapacidad().getId());
        }
        
        return respuesta;
    }

    /**
     * Convierte una lista de entidades a DTOs de respuesta.
     * @param entidades parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public List<RespuestaJornadaDiariaDTO> deListaEntidadADto(List<JornadaDiaria> entidades) {
        return entidades.stream()
                .map(this::deEntidadDtoARespuesta)
                .toList();
    }

    /**
     * Previsualiza la jornada diaria del empleado autenticado sin guardarla.
     * Calcula las horas regulares y extras basándose en la entrada actual y la hora de salida proporcionada.
     * 
     * @param fechaHoraSalida Hora de salida para el cálculo (si es null, usa la hora actual)
     */
    public RespuestaJornadaDiariaDTO previsualizarJornadaDiaria(LocalDateTime fechaHoraSalida) {
        Empleados empleado = obtenerEmpleadoAutenticado();
        
        // Si no se proporciona fecha/hora de salida, usar la actual
        if (fechaHoraSalida == null) {
            fechaHoraSalida = LocalDateTime.now();
        }
        
        log.info("Iniciando previsualización de jornada diaria para empleado ID: {} con hora salida: {}", 
                empleado.getId(), fechaHoraSalida);
        
        LocalDate fecha = fechaHoraSalida.toLocalDate();
        
        // Buscar la entrada del día
        LocalDateTime inicioDia = fecha.atStartOfDay();
        LocalDateTime finDia = fecha.atTime(23, 59, 59);
        List<Asistencia> asistenciasDelDia = asistenciaRepositorio.findByEmpleadoIdAndFechaHoraBetween(
                empleado.getId(), inicioDia, finDia);
        
        Optional<Asistencia> entradaOpt = asistenciasDelDia.stream()
                .filter(a -> a.getTipoEvento().toString().equals("ENTRADA"))
                .findFirst();
        
        if (entradaOpt.isEmpty()) {
            log.warn("No se encontró registro de entrada para empleado {} en fecha {}", empleado.getId(), fecha);
            return null;
        }
        
        LocalDateTime fechaHoraEntrada = entradaOpt.get().getFechaHora();
        LocalTime horaEntrada = fechaHoraEntrada.toLocalTime();
        LocalTime horaSalida = fechaHoraSalida.toLocalTime();
        
        // Obtener horario del puesto
        Puestos puesto = empleado.getPuesto();
        if (puesto == null) {
            log.error("El empleado {} no tiene un puesto asignado", empleado.getId());
            return null;
        }
        
        LocalTime horaEntradaPuesto = puesto.getHoraEntrada().toLocalTime();
        LocalTime horaSalidaPuesto = puesto.getHoraSalida().toLocalTime();
        
        // Calcular horas trabajadas totales (descontando 1 hora de almuerzo)
        Duration duracionTotal = Duration.between(fechaHoraEntrada, fechaHoraSalida);
        double horasTotalesTrabajadas = (duracionTotal.toMinutes() / 60.0) - 1.0; // Descuento de almuerzo
        
        // Calcular horas de jornada regular esperadas (sin almuerzo)
        Duration duracionJornadaRegular = Duration.between(horaEntradaPuesto, horaSalidaPuesto);
        double horasJornadaRegular = (duracionJornadaRegular.toMinutes() / 60.0) - 1.0; // Sin almuerzo
        
        // Verificar si tiene horas extra aprobadas para este día
        List<HorasExtra> horasExtraAprobadas = horasExtraRepositorio.findByEmpleadoIdAndFechaSolicitudAndAprobadoTrue(
                empleado.getId(), fecha);
        
        double totalHorasExtraAprobadas = horasExtraAprobadas.stream()
                .mapToInt(HorasExtra::getCantidadDeHoras)
                .sum();
        
        // Calcular horas regulares y extras
        double horasRegulares;
        double horasExtra;
        String observaciones = "";
        
        // Si trabajó menos o igual que la jornada regular
        if (horasTotalesTrabajadas <= horasJornadaRegular) {
            horasRegulares = horasTotalesTrabajadas;
            horasExtra = 0.0;
            if (horasTotalesTrabajadas >= horasJornadaRegular) {
                observaciones = "Jornada regular completa";
            } else {
                observaciones = String.format("Jornada parcial: %.2f horas", horasTotalesTrabajadas);
            }
        } else {
            // Trabajó más de la jornada regular
            double horasAdicionales = horasTotalesTrabajadas - horasJornadaRegular;
            horasRegulares = horasJornadaRegular;
            
            if (totalHorasExtraAprobadas > 0 && horasAdicionales > 0) {
                // Tiene horas extra aprobadas
                horasExtra = Math.min(horasAdicionales, totalHorasExtraAprobadas);
                observaciones = String.format("Horas extra aprobadas: %.2f", horasExtra);
                
                // Si trabajó más horas de las aprobadas
                if (horasAdicionales > totalHorasExtraAprobadas) {
                    double horasSinAprobar = horasAdicionales - totalHorasExtraAprobadas;
                    observaciones += String.format(" | Horas sin aprobar: %.2f", horasSinAprobar);
                }
            } else {
                // No tiene horas extra aprobadas
                horasExtra = 0.0;
                observaciones = String.format("Horas adicionales sin aprobar: %.2f", horasAdicionales);
            }
        }
        
        // Crear el DTO de respuesta
        RespuestaJornadaDiariaDTO respuesta = new RespuestaJornadaDiariaDTO();
        respuesta.setFecha(fecha);
        respuesta.setHoraEntrada(horaEntrada);
        respuesta.setHoraSalida(horaSalida);
        respuesta.setHorasRegulares(horasRegulares);
        respuesta.setHorasExtra(horasExtra);
        respuesta.setObservaciones(observaciones);
        respuesta.setIdEmpleado(empleado.getId());
        String nombreCompleto = String.format("%s %s %s",
                empleado.getNombre(),
                empleado.getPrimerApellido(),
                empleado.getSegundoApellido());
        respuesta.setNombreCompleto(nombreCompleto);
        
        log.info("Previsualización de jornada diaria calculada - Empleado: {}, Regulares: {}, Extras: {}", 
                empleado.getId(), horasRegulares, horasExtra);
        
        return respuesta;
    }

    /**
     * Obtiene informacion necesaria para la operacion.
     * @param idEmpleado parametro de entrada de la operacion.
     * @param fecha parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    private Permisos obtenerPermisoHorasAprobado(Long idEmpleado, LocalDate fecha) {
        List<Permisos> permisosHoras = permisosRepositorio.findPermisosHorasAprobadosEnFecha(idEmpleado, fecha);
        if (permisosHoras == null || permisosHoras.isEmpty()) {
            return null;
        }
        return permisosHoras.get(0);
    }

    /**
     * Obtiene informacion necesaria para la operacion.
     * @return resultado de la operacion.
     */
    private Empleados obtenerEmpleadoAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        User user = (User) authentication.getPrincipal();
        Empleados empleado = user.getEmpleado();
        
        if (empleado == null) {
            empleado = empleadosRepositorio.findByUsuarioId(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Empleado", "usuarioId", user.getId()));
        }
        
        return empleado;
    }
}
