package com.anthony.tfg.tfg.Modulos.Extras.Servicio;

import java.time.LocalDate;
import java.util.List;

// Email sending disabled for Horas Extra module
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaHorasExtraDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudHorasExtraDTO;
import com.anthony.tfg.tfg.Entidades.Empleados;
import com.anthony.tfg.tfg.Entidades.HorasExtra;
import com.anthony.tfg.tfg.Entidades.JefesDepartamento;
import com.anthony.tfg.tfg.Entidades.Enums.EstadoSolicitud;
import com.anthony.tfg.tfg.Entidades.Enums.TipoTarifa;
import com.anthony.tfg.tfg.Exceptions.BadRequestException;
import com.anthony.tfg.tfg.Exceptions.ForbiddenException;
import com.anthony.tfg.tfg.Exceptions.ResourceNotFoundException;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasEmpleados;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasHorasExtras;
import com.anthony.tfg.tfg.Modulos.Interfaces.ServicioInterface;
import com.anthony.tfg.tfg.Modulos.Mantenimientos.MantenimientosHorasExtras;
import com.anthony.tfg.tfg.Repositorios.JefesDepartamentoRepositorio;
import com.anthony.tfg.tfg.Modulos.Seguridad.user.User;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ServicioExtras implements ServicioInterface<RespuestaHorasExtraDTO, 
                                                        SolicitudHorasExtraDTO, 
                                                        HorasExtra>{

    private final ConsultasHorasExtras consulta;
    private final MantenimientosHorasExtras mantenimiento;
    private final ConsultasEmpleados consultasEmpleados;
    private final JefesDepartamentoRepositorio jefesDepartamentoRepo;

    public ServicioExtras(ConsultasHorasExtras consulta,
                          MantenimientosHorasExtras mantenimiento,
                          ConsultasEmpleados consultasEmpleados,
                          JefesDepartamentoRepositorio jefesDepartamentoRepo) {
        this.consulta = consulta;
        this.mantenimiento = mantenimiento;
        this.consultasEmpleados = consultasEmpleados;
        this.jefesDepartamentoRepo = jefesDepartamentoRepo;
    }

    public RespuestaHorasExtraDTO obtenerPorId(Long id) {
        HorasExtra horaExtra = consulta.obtenerPorId(id);
        if(horaExtra == null){
            log.warn("No se ha encontrado la hora extra con ID: " + id);
            throw new ResourceNotFoundException("HorasExtra", "id", id);
        }
        log.info("Se ha encontrado la hora extra con ID: " + id);
        return deEntidadDtoARespuesta(horaExtra);
    }

    public List<RespuestaHorasExtraDTO> obtenerTodos() {
        List<HorasExtra> entidades = consulta.obtenerTodos();
        log.info("Se han obtenido todas las horas extra. La cantidad de registros es: " + entidades.size());
        return deListaEntidadADto(entidades);
    }

    public RespuestaHorasExtraDTO guardar(SolicitudHorasExtraDTO entidad) {
        HorasExtra nuevaHoraExtra = deSolicitudDtoAEntidad(entidad);
        HorasExtra horaExtraGuardada = mantenimiento.crear(nuevaHoraExtra);
        log.info("Se ha guardado una nueva hora extra con ID: " + horaExtraGuardada.getId());
        return deEntidadDtoARespuesta(horaExtraGuardada);
    }

    public RespuestaHorasExtraDTO actualizar(Long id, SolicitudHorasExtraDTO entidad) {
        HorasExtra horaExtraExistente = consulta.obtenerPorId(id);
        if(horaExtraExistente == null){
            log.warn("No se ha encontrado la hora extra con ID: " + id + " para actualizar");
            return null;
        }
        horaExtraExistente.setFechaSolicitud(entidad.getFechaSolicitud());
        horaExtraExistente.setCantidadDeHoras(entidad.getCantidadDeHoras());
        horaExtraExistente.setMotivo(entidad.getMotivo());
        horaExtraExistente.setAprobado(entidad.getAprobado());
        horaExtraExistente.setProcesado(entidad.getProcesado());
        
        EstadoSolicitud estadoSolicitud = obtenerEstadoSolicitud(entidad.getEstadoSolicitud());
        if(estadoSolicitud != null){
            horaExtraExistente.setEstadoSolicitud(estadoSolicitud);
        }
        
        TipoTarifa tipoTarifa = obtenerTipoTarifa(entidad.getTipoTarifa());
        if(tipoTarifa != null){
            horaExtraExistente.setTipoTarifa(tipoTarifa);
        }
        
        Empleados empleado = consultasEmpleados.obtenerPorId(entidad.getIdEmpleado());
        if(empleado != null){
            horaExtraExistente.setEmpleado(empleado);
        }
        
        HorasExtra horaExtraActualizada = mantenimiento.actualizar(horaExtraExistente);
        log.info("Se ha actualizado la hora extra con ID: " + id);
        return deEntidadDtoARespuesta(horaExtraActualizada);
    }

    public void eliminar(Long id) {
        mantenimiento.eliminar(id);
        log.info("Se ha eliminado la hora extra con ID: " + id);
    }

    /**
     * Guarda una solicitud autenticada (el idEmpleado se toma del usuario logueado)
     */
    public RespuestaHorasExtraDTO guardar(SolicitudHorasExtraDTO entidad, Authentication auth) {
        Empleados empleadoAut = obtenerEmpleadoAutenticado(auth);
        entidad.setIdEmpleado(empleadoAut.getId());

        validarSolicitudBasica(entidad);

        HorasExtra nuevaHoraExtra = deSolicitudDtoAEntidad(entidad);

        // Determinar estado inicial: si el solicitante es jefe o no existe jefe -> PENDIENTE_RH, sino PENDIENTE
        EstadoSolicitud estadoInicial = determinarEstadoInicial(empleadoAut);
        nuevaHoraExtra.setEstadoSolicitud(estadoInicial);

        HorasExtra horaExtraGuardada = mantenimiento.crear(nuevaHoraExtra);
        log.info("Se ha guardado una nueva hora extra con ID: " + horaExtraGuardada.getId() + " y estado: " + horaExtraGuardada.getEstadoSolicitud());

        // Notificar por correo al solicitante
        enviarEmailCambioEstado(horaExtraGuardada);

        return deEntidadDtoARespuesta(horaExtraGuardada);
    }

    private void validarSolicitudBasica(SolicitudHorasExtraDTO entidad) {
        if (entidad.getCantidadDeHoras() == null || entidad.getCantidadDeHoras() <= 0) {
            throw new BadRequestException("La cantidad de horas debe ser mayor que cero");
        }
        if (entidad.getCantidadDeHoras() > 3) {
            throw new BadRequestException("No se permiten más de 3 horas extra por solicitud");
        }

        if (entidad.getFechaSolicitud() == null) {
            throw new BadRequestException("La fecha de solicitud es requerida");
        }

        LocalDate fecha = entidad.getFechaSolicitud();
        LocalDate hoy = LocalDate.now();
        LocalDate ayer = hoy.minusDays(1);

        if (!(fecha.equals(hoy) || fecha.equals(ayer))) {
            throw new BadRequestException("La fecha de solicitud debe ser hoy o el día anterior");
        }

        // Validar que exista el empleado
        if (entidad.getIdEmpleado() == null) {
            throw new BadRequestException("El id del empleado es requerido");
        }

        Empleados empleado = consultasEmpleados.obtenerPorId(entidad.getIdEmpleado());
        if (empleado == null) {
            throw new BadRequestException("Empleado no encontrado con id: " + entidad.getIdEmpleado());
        }

        // Validación: máximo 3 horas por día por empleado (sumando solicitudes existentes)
        List<HorasExtra> yaSolicitadas = consulta.obtenerPorEmpleadoYFecha(entidad.getIdEmpleado(), fecha);
        int totalExistente = yaSolicitadas.stream()
                .filter(h -> h.getCantidadDeHoras() != null)
                .mapToInt(HorasExtra::getCantidadDeHoras)
                .sum();

        int nuevoTotal = totalExistente + entidad.getCantidadDeHoras();
        if (nuevoTotal > 3) {
            throw new BadRequestException("El total de horas extra para la fecha " + fecha + " excede el máximo permitido (3 horas). Horas ya solicitadas: " + totalExistente);
        }
    }

    private EstadoSolicitud determinarEstadoInicial(Empleados empleado) {
        // Si el empleado es jefe asignado -> va directo a RH
        List<JefesDepartamento> listaJefes = jefesDepartamentoRepo.findByEmpleadoIdAndEstaActivoTrue(empleado.getId());
        if (!listaJefes.isEmpty()) {
            return EstadoSolicitud.PENDIENTE_RH;
        }

        if (empleado.getPuesto() == null || empleado.getPuesto().getDepartamento() == null) {
            return EstadoSolicitud.PENDIENTE_RH;
        }

        Long idDept = empleado.getPuesto().getDepartamento().getId();
        // Si no existen jefes activos para el departamento, ir a RH
        boolean hayJefe = jefesDepartamentoRepo.findAll().stream()
                .anyMatch(jd -> jd.getDepartamento().getId().equals(idDept) && jd.getEstaActivo());

        return hayJefe ? EstadoSolicitud.PENDIENTE : EstadoSolicitud.PENDIENTE_RH;
    }

    // =================== APROBACIONES =====================
    public RespuestaHorasExtraDTO aprobarPorJefe(Long id, Authentication auth) {
        Empleados jefe = obtenerEmpleadoAutenticado(auth);
        HorasExtra he = consulta.obtenerPorId(id);
        if (he == null) throw new ResourceNotFoundException("HorasExtra", "id", id);

        // Verificar que el autenticado es jefe del empleado de la solicitud
        Empleados solicitante = he.getEmpleado();
        if (solicitante == null) throw new BadRequestException("Solicitud sin empleado asociado");

        if (!esJefeDelEmpleado(jefe, solicitante)) {
            throw new ForbiddenException("No tiene permisos para aprobar esta solicitud");
        }

        he.setEstadoSolicitud(EstadoSolicitud.APROBADA);
        he.setAprobado(true);
        HorasExtra actualizado = mantenimiento.actualizar(he);
        enviarEmailCambioEstado(actualizado);
        return deEntidadDtoARespuesta(actualizado);
    }

    public RespuestaHorasExtraDTO rechazarPorJefe(Long id, Authentication auth) {
        Empleados jefe = obtenerEmpleadoAutenticado(auth);
        HorasExtra he = consulta.obtenerPorId(id);
        if (he == null) throw new ResourceNotFoundException("HorasExtra", "id", id);

        Empleados solicitante = he.getEmpleado();
        if (solicitante == null) throw new BadRequestException("Solicitud sin empleado asociado");

        if (!esJefeDelEmpleado(jefe, solicitante)) {
            throw new ForbiddenException("No tiene permisos para rechazar esta solicitud");
        }

        he.setEstadoSolicitud(EstadoSolicitud.RECHAZADA_POR_JEFE);
        HorasExtra actualizado = mantenimiento.actualizar(he);
        enviarEmailCambioEstado(actualizado);
        return deEntidadDtoARespuesta(actualizado);
    }

    public RespuestaHorasExtraDTO aprobarPorRH(Long id, Authentication auth) {
        User usuario = obtenerUsuarioAutenticado(auth);
        String role = usuario.getRole().name();
        if (!role.equals("HR") && !role.equals("ADMIN")) {
            throw new ForbiddenException("Solo personal de RH o ADMIN puede aprobar por RH");
        }

        HorasExtra he = consulta.obtenerPorId(id);
        if (he == null) throw new ResourceNotFoundException("HorasExtra", "id", id);

        he.setEstadoSolicitud(EstadoSolicitud.APROBADA);
        HorasExtra actualizado = mantenimiento.actualizar(he);
        enviarEmailCambioEstado(actualizado);
        return deEntidadDtoARespuesta(actualizado);
    }

    public RespuestaHorasExtraDTO rechazarPorRH(Long id, Authentication auth) {
        User usuario = obtenerUsuarioAutenticado(auth);
        String role = usuario.getRole().name();
        if (!role.equals("HR") && !role.equals("ADMIN")) {
            throw new ForbiddenException("Solo personal de RH o ADMIN puede rechazar por RH");
        }

        HorasExtra he = consulta.obtenerPorId(id);
        if (he == null) throw new ResourceNotFoundException("HorasExtra", "id", id);

        he.setEstadoSolicitud(EstadoSolicitud.RECHAZADA_POR_RH);
        HorasExtra actualizado = mantenimiento.actualizar(he);
        enviarEmailCambioEstado(actualizado);
        return deEntidadDtoARespuesta(actualizado);
    }

    private boolean esJefeDelEmpleado(Empleados posibleJefe, Empleados empleado) {
        if (posibleJefe == null || empleado == null) return false;
        if (empleado.getPuesto() == null || empleado.getPuesto().getDepartamento() == null) return false;
        Long idDepartamento = empleado.getPuesto().getDepartamento().getId();
        return jefesDepartamentoRepo.findByEmpleadoIdAndDepartamentoIdAndEstaActivoTrue(posibleJefe.getId(), idDepartamento).isPresent();
    }

    // Email notifications for Horas Extra are intentionally disabled.
    private void enviarEmailCambioEstado(HorasExtra horaExtra) {
        Long id = horaExtra != null ? horaExtra.getId() : null;
        log.info("Notificación por email deshabilitada para HorasExtra (ID: {})", id);
    }

    // =================== HELPERS AUTH ====================
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

    public HorasExtra deSolicitudDtoAEntidad(SolicitudHorasExtraDTO solicitud) {
        if(solicitud == null){
            log.warn("El DTO de solicitud es nulo, no se puede convertir a entidad HorasExtra.");
            return null;
        }
        
        Empleados empleado = consultasEmpleados.obtenerPorId(solicitud.getIdEmpleado());
        if(empleado == null){
            log.warn("No se ha encontrado el empleado con ID: " + solicitud.getIdEmpleado());
            return null;
        }
        
        EstadoSolicitud estadoSolicitud = null;
        if (solicitud.estadoSolicitud != null) {
            estadoSolicitud = obtenerEstadoSolicitud(solicitud.estadoSolicitud);
            if(estadoSolicitud == null){
                log.warn("No se ha encontrado el estado de solicitud: " + solicitud.estadoSolicitud);
            }
        }
        
        TipoTarifa tipoTarifa = null;
        if (solicitud.tipoTarifa != null) {
            tipoTarifa = obtenerTipoTarifa(solicitud.tipoTarifa);
            if(tipoTarifa == null){
                log.warn("No se ha encontrado el tipo de tarifa: " + solicitud.tipoTarifa);
            }
        }
        // Forzar SIMPLE por default si no se especifica
        if (tipoTarifa == null) {
            tipoTarifa = TipoTarifa.SIMPLE;
        }
        
        HorasExtra horaExtra = HorasExtra.builder()
                    .id(solicitud.id)
                    .fechaSolicitud(solicitud.fechaSolicitud)
                    .cantidadDeHoras(solicitud.cantidadDeHoras)
                    .motivo(solicitud.motivo)
                    .aprobado(solicitud.aprobado)
                    .procesado(solicitud.procesado)
                    .estadoSolicitud(estadoSolicitud)
                    .tipoTarifa(tipoTarifa)
                    .empleado(empleado)
                    .build();
        log.info("Se ha convertido el DTO de solicitud a entidad HorasExtra: {}", horaExtra);
        return horaExtra;
    }

    public RespuestaHorasExtraDTO deEntidadDtoARespuesta(HorasExtra entidad) {
        if(entidad == null){
            log.warn("La entidad HorasExtra es nula, no se puede convertir a DTO de respuesta.");
            return null;
        }
        RespuestaHorasExtraDTO respuesta = new RespuestaHorasExtraDTO();
        respuesta.id = entidad.getId();
        respuesta.fechaSolicitud = entidad.getFechaSolicitud();
        respuesta.cantidadDeHoras = entidad.getCantidadDeHoras();
        respuesta.motivo = entidad.getMotivo();
        respuesta.aprobado = entidad.getAprobado();
        respuesta.procesado = entidad.getProcesado();
        
        if(entidad.getEstadoSolicitud() != null){
            respuesta.estadoSolicitud = entidad.getEstadoSolicitud().name();
        }
        
        if(entidad.getTipoTarifa() != null){
            respuesta.tipoTarifa = entidad.getTipoTarifa().name();
        }
        
        if(entidad.getEmpleado() != null){
            respuesta.idEmpleado = entidad.getEmpleado().getId();
            respuesta.nombreEmpleado = entidad.getEmpleado().getNombre();
            respuesta.primerApellidoEmpleado = entidad.getEmpleado().getPrimerApellido();
            respuesta.segundoApellidoEmpleado = entidad.getEmpleado().getSegundoApellido();
        }
        
        log.info("Se ha convertido la entidad HorasExtra a DTO de respuesta: {}", respuesta);
        return respuesta;
    }

    public List<RespuestaHorasExtraDTO> deListaEntidadADto(List<HorasExtra> entidades) {
        return entidades.stream()
                .map(this::deEntidadDtoARespuesta)
                .toList();
    }
    
    private EstadoSolicitud obtenerEstadoSolicitud(String estado) {
        try {
            return EstadoSolicitud.valueOf(estado.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }
    
    private TipoTarifa obtenerTipoTarifa(String tipo) {
        try {
            return TipoTarifa.valueOf(tipo.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

}
