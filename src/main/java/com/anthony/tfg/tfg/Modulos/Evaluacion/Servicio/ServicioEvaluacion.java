package com.anthony.tfg.tfg.Modulos.Evaluacion.Servicio;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.DTOs.Respuesta.EmpleadoEvaluacionResumenDTO;
import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaEvaluacionDeDesempenoDTO;
import com.anthony.tfg.tfg.DTOs.Respuesta.ResumenEvaluacionesDepartamentoDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudEvaluacionDeDesempenoDTO;
import com.anthony.tfg.tfg.Entidades.Departamento;
import com.anthony.tfg.tfg.Entidades.Empleados;
import com.anthony.tfg.tfg.Entidades.EvaluacionDeDesempeno;
import com.anthony.tfg.tfg.Entidades.JefesDepartamento;
import com.anthony.tfg.tfg.Exceptions.ForbiddenException;
import com.anthony.tfg.tfg.Exceptions.ResourceNotFoundException;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasEmpleados;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasEvaluacionDeDesempeno;
import com.anthony.tfg.tfg.Modulos.Empleados.Servicio.ServicioEmail;
import com.anthony.tfg.tfg.Modulos.Interfaces.ServicioInterface;
import com.anthony.tfg.tfg.Modulos.Mantenimientos.MantenimientosEvaluacionDeDesempeno;
import com.anthony.tfg.tfg.Modulos.Seguridad.user.Role;
import com.anthony.tfg.tfg.Modulos.Seguridad.user.User;
import com.anthony.tfg.tfg.Repositorios.DepartamentoRepositorio;
import com.anthony.tfg.tfg.Repositorios.JefesDepartamentoRepositorio;
import com.anthony.tfg.tfg.Repositorios.EmpleadosRepositorio;
import com.anthony.tfg.tfg.Modulos.Asistencia.Servicio.ServicioRegistroAsistencia;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ServicioEvaluacion implements ServicioInterface<RespuestaEvaluacionDeDesempenoDTO, 
                                                            SolicitudEvaluacionDeDesempenoDTO, 
                                                            EvaluacionDeDesempeno>{

    private final ConsultasEvaluacionDeDesempeno consulta;
    private final MantenimientosEvaluacionDeDesempeno mantenimiento;
    private final ConsultasEmpleados consultasEmpleados;
    private final JefesDepartamentoRepositorio jefesDepartamentoRepositorio;
    private final EmpleadosRepositorio empleadosRepositorio;
    private final DepartamentoRepositorio departamentoRepositorio;
    private final ServicioEmail servicioEmail;
    private final ServicioRegistroAsistencia servicioRegistroAsistencia;

    public ServicioEvaluacion(ConsultasEvaluacionDeDesempeno consulta,
            MantenimientosEvaluacionDeDesempeno mantenimiento,
            ConsultasEmpleados consultasEmpleados,
            JefesDepartamentoRepositorio jefesDepartamentoRepositorio,
            EmpleadosRepositorio empleadosRepositorio,
            DepartamentoRepositorio departamentoRepositorio,
            ServicioEmail servicioEmail,
            ServicioRegistroAsistencia servicioRegistroAsistencia) {
        this.consulta = consulta;
        this.mantenimiento = mantenimiento;
        this.consultasEmpleados = consultasEmpleados;
        this.jefesDepartamentoRepositorio = jefesDepartamentoRepositorio;
        this.empleadosRepositorio = empleadosRepositorio;
        this.departamentoRepositorio = departamentoRepositorio;
        this.servicioEmail = servicioEmail;
        this.servicioRegistroAsistencia = servicioRegistroAsistencia;
    }

    public RespuestaEvaluacionDeDesempenoDTO obtenerPorId(Long id) {
        EvaluacionDeDesempeno evaluacion = consulta.obtenerPorId(id);
        if(evaluacion == null){
            log.warn("No se ha encontrado la evaluación de desempeño con ID: " + id);
            throw new ResourceNotFoundException("EvaluacionDeDesempeno", "id", id);
        }
        log.info("Se ha encontrado la evaluación de desempeño con ID: " + id);
        return deEntidadDtoARespuesta(evaluacion);
    }

    public List<RespuestaEvaluacionDeDesempenoDTO> obtenerTodos() {
        List<EvaluacionDeDesempeno> entidades = consulta.obtenerTodos();
        log.info("Se han obtenido todas las evaluaciones. La cantidad de registros es: " + entidades.size());
        return deListaEntidadADto(entidades);
    }

    public java.util.List<RespuestaEvaluacionDeDesempenoDTO> obtenerPorEmpleado(Long empleadoId) {
        java.util.List<EvaluacionDeDesempeno> entidades = consulta.obtenerPorEmpleadoId(empleadoId);
        if (entidades == null || entidades.isEmpty()) {
            log.info("No se encontraron evaluaciones para el empleado ID: " + empleadoId);
            return new java.util.ArrayList<>();
        }
        log.info("Se encontraron " + entidades.size() + " evaluaciones para el empleado ID: " + empleadoId);
        return deListaEntidadADto(entidades);
    }

    public RespuestaEvaluacionDeDesempenoDTO guardar(SolicitudEvaluacionDeDesempenoDTO entidad) {
        // Authorization: only JEFE, HR or ADMIN can create evaluations
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenException("Usuario no autenticado");
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            throw new ForbiddenException("Tipo de usuario no válido");
        }
        User currentUser = (User) principal;

        Role role = currentUser.getRole();
        if (!(role == Role.HR || role == Role.ADMIN || role == Role.JEFE)) {
            throw new ForbiddenException("No tiene permisos para crear evaluaciones");
        }

        // If JEFE, ensure the evaluated employee belongs to a department the jefe manages
        Empleados empleadoEvaluado = consultasEmpleados.obtenerPorId(entidad.getIdEmpleado());
        if (empleadoEvaluado == null) {
            throw new ResourceNotFoundException("Empleado", "id", entidad.getIdEmpleado());
        }

        if (role == Role.JEFE) {
            Empleados empleadoActual = obtenerEmpleadoDesdeUsuario(currentUser);
            if (empleadoEvaluado.getPuesto() == null || empleadoEvaluado.getPuesto().getDepartamento() == null) {
                throw new ForbiddenException("El empleado evaluado no pertenece a un departamento válido");
            }
            Long departamentoId = empleadoEvaluado.getPuesto().getDepartamento().getId();
            Optional<JefesDepartamento> jefatura = jefesDepartamentoRepositorio
                    .findByEmpleadoIdAndDepartamentoIdAndEstaActivoTrue(empleadoActual.getId(), departamentoId);
            if (jefatura.isEmpty()) {
                throw new ForbiddenException("No puede evaluar empleados de departamentos que no administra");
            }
        }

        EvaluacionDeDesempeno nuevaEvaluacion = deSolicitudDtoAEntidad(entidad);
        EvaluacionDeDesempeno evaluacionGuardada = mantenimiento.crear(nuevaEvaluacion);
        log.info("Se ha guardado una nueva evaluación de desempeño con ID: " + evaluacionGuardada.getId());

        // Enviar notificación por correo al empleado (resumen solamente)
        String destinatario = empleadoEvaluado.getCorreoPersonal();
        String nombreCompleto = String.format("%s %s %s", empleadoEvaluado.getNombre(), empleadoEvaluado.getPrimerApellido(), empleadoEvaluado.getSegundoApellido());
        try {
            servicioEmail.enviarNotificacionEvaluacion(destinatario,
                    nombreCompleto,
                    evaluacionGuardada.getPuntuacionFinal(),
                    evaluacionGuardada.getPeriodoEvaluado(),
                    evaluacionGuardada.getObservaciones(),
                    evaluacionGuardada.getPlanDeMejora());
        } catch (Exception e) {
            log.error("Error al enviar notificación de evaluación por correo: {}", e.getMessage());
        }

        return deEntidadDtoARespuesta(evaluacionGuardada);
    }

    public ResumenEvaluacionesDepartamentoDTO obtenerResumenDepartamento(Long idDepartamento) {
        // Validate department exists
        Departamento departamento = departamentoRepositorio.findById(idDepartamento)
                .orElseThrow(() -> new ResourceNotFoundException("Departamento", "id", idDepartamento));

        // Authorization: only HR, ADMIN, JEFE (JEFE must manage the department)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenException("Usuario no autenticado");
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            throw new ForbiddenException("Tipo de usuario no válido");
        }
        User currentUser = (User) principal;
        Role role = currentUser.getRole();

        if (role == Role.JEFE) {
            Empleados empleadoActual = obtenerEmpleadoDesdeUsuario(currentUser);
            Optional<JefesDepartamento> jefatura = jefesDepartamentoRepositorio
                    .findByEmpleadoIdAndDepartamentoIdAndEstaActivoTrue(empleadoActual.getId(), idDepartamento);
            if (jefatura.isEmpty()) {
                throw new ForbiddenException("No tiene acceso a este departamento");
            }
        } else if (!(role == Role.HR || role == Role.ADMIN)) {
            throw new ForbiddenException("No tiene permisos para ver este resumen");
        }

        List<EmpleadoEvaluacionResumenDTO> empleadosResumen = consulta.findResumenPorDepartamento(idDepartamento);
        ResumenEvaluacionesDepartamentoDTO resumen = new ResumenEvaluacionesDepartamentoDTO();
        resumen.departamentoId = departamento.getId();
        resumen.departamentoNombre = departamento.getNombre();
        resumen.empleados = empleadosResumen;
        return resumen;
    }

    // Helper to obtain Empleados entity from authenticated User
    private Empleados obtenerEmpleadoDesdeUsuario(User user) {
        Empleados empleado = user.getEmpleado();
        if (empleado == null) {
            empleado = empleadosRepositorio.findByUsuarioId(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Empleado", "usuarioId", user.getId()));
        }
        return empleado;
    }

    public java.util.List<EmpleadoEvaluacionResumenDTO> obtenerEmpleadosMisDepartamentos() {
        java.util.List<Long> departamentos = servicioRegistroAsistencia.obtenerDepartamentosAccesibles();
        java.util.Map<Long, EmpleadoEvaluacionResumenDTO> mapa = new java.util.HashMap<>();
        for (Long depId : departamentos) {
            java.util.List<EmpleadoEvaluacionResumenDTO> res = consulta.findResumenPorDepartamento(depId);
            for (EmpleadoEvaluacionResumenDTO dto : res) {
                mapa.putIfAbsent(dto.empleadoId, dto);
            }
        }
        return new java.util.ArrayList<>(mapa.values());
    }

    public RespuestaEvaluacionDeDesempenoDTO actualizar(Long id, SolicitudEvaluacionDeDesempenoDTO entidad) {
        EvaluacionDeDesempeno evaluacionExistente = consulta.obtenerPorId(id);
        if(evaluacionExistente == null){
            log.warn("No se ha encontrado la evaluación de desempeño con ID: " + id + " para actualizar");
            return null;
        }
        evaluacionExistente.setFechaEvaluacion(entidad.getFechaEvaluacion());
        evaluacionExistente.setPeriodoEvaluado(entidad.getPeriodoEvaluado());
        evaluacionExistente.setPuntuacionFinal(entidad.getPuntuacionFinal());
        evaluacionExistente.setObservaciones(entidad.getObservaciones());
        evaluacionExistente.setPlanDeMejora(entidad.getPlanDeMejora());
        
        Empleados empleado = consultasEmpleados.obtenerPorId(entidad.getIdEmpleado());
        if(empleado != null){
            evaluacionExistente.setEmpleado(empleado);
        }
        
        EvaluacionDeDesempeno evaluacionActualizada = mantenimiento.actualizar(evaluacionExistente);
        log.info("Se ha actualizado la evaluación de desempeño con ID: " + id);
        return deEntidadDtoARespuesta(evaluacionActualizada);
    }

    public void eliminar(Long id) {
        mantenimiento.eliminar(id);
        log.info("Se ha eliminado la evaluación de desempeño con ID: " + id);
    }

    public EvaluacionDeDesempeno deSolicitudDtoAEntidad(SolicitudEvaluacionDeDesempenoDTO solicitud) {
        if(solicitud == null){
            log.warn("El DTO de solicitud es nulo, no se puede convertir a entidad EvaluacionDeDesempeno.");
            return null;
        }
        
        Empleados empleado = consultasEmpleados.obtenerPorId(solicitud.getIdEmpleado());
        if(empleado == null){
            log.warn("No se ha encontrado el empleado con ID: " + solicitud.getIdEmpleado());
            return null;
        }
        
        EvaluacionDeDesempeno evaluacion = EvaluacionDeDesempeno.builder()
                    .id(solicitud.id)
                    .fechaEvaluacion(solicitud.fechaEvaluacion)
                    .periodoEvaluado(solicitud.periodoEvaluado)
                    .puntuacionFinal(solicitud.puntuacionFinal)
                    .observaciones(solicitud.observaciones)
                    .planDeMejora(solicitud.planDeMejora)
                    .empleado(empleado)
                    .build();
        log.info("Se ha convertido el DTO de solicitud a entidad EvaluacionDeDesempeno: {}", evaluacion);
        return evaluacion;
    }

    public RespuestaEvaluacionDeDesempenoDTO deEntidadDtoARespuesta(EvaluacionDeDesempeno entidad) {
        if(entidad == null){
            log.warn("La entidad EvaluacionDeDesempeno es nula, no se puede convertir a DTO de respuesta.");
            return null;
        }
        RespuestaEvaluacionDeDesempenoDTO respuesta = new RespuestaEvaluacionDeDesempenoDTO();
        respuesta.id = entidad.getId();
        respuesta.fechaEvaluacion = entidad.getFechaEvaluacion();
        respuesta.periodoEvaluado = entidad.getPeriodoEvaluado();
        respuesta.puntuacionFinal = entidad.getPuntuacionFinal();
        respuesta.observaciones = entidad.getObservaciones();
        respuesta.planDeMejora = entidad.getPlanDeMejora();
        
        if(entidad.getEmpleado() != null){
            respuesta.nombreEmpleado = entidad.getEmpleado().getNombre();
            respuesta.primerApellidoEmpleado = entidad.getEmpleado().getPrimerApellido();
            respuesta.segundoApellidoEmpleado = entidad.getEmpleado().getSegundoApellido();
        }
        
        log.info("Se ha convertido la entidad EvaluacionDeDesempeno a DTO de respuesta: {}", respuesta);
        return respuesta;
    }

    public List<RespuestaEvaluacionDeDesempenoDTO> deListaEntidadADto(List<EvaluacionDeDesempeno> entidades) {
        return entidades.stream()
                .map(this::deEntidadDtoARespuesta)
                .toList();
    }

}
