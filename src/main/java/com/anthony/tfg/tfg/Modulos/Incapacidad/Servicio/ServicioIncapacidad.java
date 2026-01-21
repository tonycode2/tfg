package com.anthony.tfg.tfg.Modulos.Incapacidad.Servicio;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaIncapacidadesDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudExtensionIncapacidadDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudIncapacidadesDTO;
import com.anthony.tfg.tfg.Entidades.Empleados;
import com.anthony.tfg.tfg.Entidades.Incapacidades;
import com.anthony.tfg.tfg.Entidades.JefesDepartamento;
import com.anthony.tfg.tfg.Entidades.Enums.EstadoSolicitud;
import com.anthony.tfg.tfg.Entidades.Enums.TipoEntidadEmisora;
import com.anthony.tfg.tfg.Entidades.Enums.TipoIncapacidad;
import com.anthony.tfg.tfg.Exceptions.BadRequestException;
import com.anthony.tfg.tfg.Exceptions.ForbiddenException;
import com.anthony.tfg.tfg.Exceptions.ResourceNotFoundException;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasEmpleados;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasIncapacidades;
import com.anthony.tfg.tfg.Modulos.Interfaces.ServicioInterface;
import com.anthony.tfg.tfg.Modulos.Mantenimientos.MantenimientosIncapacidades;
import com.anthony.tfg.tfg.Modulos.Seguridad.user.User;
import com.anthony.tfg.tfg.Repositorios.JefesDepartamentoRepositorio;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ServicioIncapacidad implements ServicioInterface<RespuestaIncapacidadesDTO, 
                                                              SolicitudIncapacidadesDTO, 
                                                              Incapacidades> {

    private final ConsultasIncapacidades consulta;
    private final MantenimientosIncapacidades mantenimiento;
    private final ConsultasEmpleados consultasEmpleados;
    private final JefesDepartamentoRepositorio jefesDepartamentoRepo;

    public ServicioIncapacidad(ConsultasIncapacidades consulta, 
                               MantenimientosIncapacidades mantenimiento, 
                               ConsultasEmpleados consultasEmpleados,
                               JefesDepartamentoRepositorio jefesDepartamentoRepo) {
        this.consulta = consulta;
        this.mantenimiento = mantenimiento;
        this.consultasEmpleados = consultasEmpleados;
        this.jefesDepartamentoRepo = jefesDepartamentoRepo;
    }

    // ==================== MÉTODOS BÁSICOS (CRUD) ====================

    public RespuestaIncapacidadesDTO obtenerPorId(Long id) {
        Incapacidades incapacidad = consulta.obtenerPorId(id);
        if (incapacidad == null) {
            log.warn("No se ha encontrado la incapacidad con ID: " + id);
            throw new ResourceNotFoundException("Incapacidades", "id", id);
        }
        log.info("Se ha encontrado la incapacidad con ID: " + id);
        return deEntidadDtoARespuesta(incapacidad);
    }

    public List<RespuestaIncapacidadesDTO> obtenerTodos() {
        List<Incapacidades> entidades = consulta.obtenerTodos();
        log.info("Se han obtenido todas las incapacidades. La cantidad de registros es: " + entidades.size());
        return deListaEntidadADto(entidades);
    }

    /**
     * Implementación de la interfaz - usa el idEmpleado del DTO directamente
     */
    @Override
    public RespuestaIncapacidadesDTO guardar(SolicitudIncapacidadesDTO entidad) {
        return guardarInterno(entidad);
    }

    /**
     * Crea una nueva solicitud de incapacidad desde el endpoint público (autenticado).
     * Obtiene automáticamente el empleado del usuario autenticado para mayor seguridad.
     */
    public RespuestaIncapacidadesDTO guardar(SolicitudIncapacidadesDTO entidad, Authentication auth) {
        Empleados empleadoAutenticado = obtenerEmpleadoAutenticado(auth);
        entidad.idEmpleado = empleadoAutenticado.getId();
        return guardarInterno(entidad);
    }

    /**
     * Método interno para guardar incapacidad con lógica de estado inicial
     */
    private RespuestaIncapacidadesDTO guardarInterno(SolicitudIncapacidadesDTO entidad) {
        // Validar fechas
        if (entidad.fechaFin.isBefore(entidad.fechaInicio)) {
            throw new BadRequestException("La fecha de fin debe ser igual o posterior a la fecha de inicio");
        }
        
        Incapacidades nuevaIncapacidad = deSolicitudDtoAEntidad(entidad);
        if (nuevaIncapacidad == null) {
            throw new BadRequestException("No se pudo procesar la solicitud de incapacidad");
        }
        
        // Determinar estado inicial automáticamente
        Empleados solicitante = nuevaIncapacidad.getEmpleado();
        EstadoSolicitud estadoInicial = determinarEstadoInicial(solicitante);
        nuevaIncapacidad.setEstadoSolicitud(estadoInicial);
        
        // Asignar fecha de solicitud
        nuevaIncapacidad.setFechaSolicitud(LocalDate.now());
        
        Incapacidades incapacidadGuardada = mantenimiento.crear(nuevaIncapacidad);
        log.info("Se ha guardado una nueva incapacidad con ID: {} y estado: {}", 
                incapacidadGuardada.getId(), incapacidadGuardada.getEstadoSolicitud());
        return deEntidadDtoARespuesta(incapacidadGuardada);
    }

    public RespuestaIncapacidadesDTO actualizar(Long id, SolicitudIncapacidadesDTO entidad) {
        Incapacidades incapacidadExistente = consulta.obtenerPorId(id);
        if (incapacidadExistente == null) {
            log.warn("No se ha encontrado la incapacidad con ID: " + id + " para actualizar");
            throw new ResourceNotFoundException("Incapacidades", "id", id);
        }
        
        incapacidadExistente.setFechaInicio(entidad.fechaInicio);
        incapacidadExistente.setFechaFin(entidad.fechaFin);
        incapacidadExistente.setDiasTotales(entidad.diasTotales);
        incapacidadExistente.setPorcentajePago(entidad.porcentajePago);
        incapacidadExistente.setNumeroDocumento(entidad.numeroDocumento);
        incapacidadExistente.setObservaciones(entidad.observaciones);
        incapacidadExistente.setUrlDocumentoAdjunto(entidad.urlDocumentoAdjunto);
        
        TipoIncapacidad tipoIncapacidad = obtenerTipoIncapacidad(entidad.tipoIncapacidad);
        if (tipoIncapacidad != null) {
            incapacidadExistente.setTipoIncapacidad(tipoIncapacidad);
        }
        
        TipoEntidadEmisora entidadEmisora = obtenerTipoEntidadEmisora(entidad.entidadEmisora);
        if (entidadEmisora != null) {
            incapacidadExistente.setEntidadEmisora(entidadEmisora);
        }
        
        Empleados empleado = consultasEmpleados.obtenerPorId(entidad.idEmpleado);
        if (empleado != null) {
            incapacidadExistente.setEmpleado(empleado);
        }
        
        Incapacidades incapacidadActualizada = mantenimiento.actualizar(incapacidadExistente);
        log.info("Se ha actualizado la incapacidad con ID: " + id);
        return deEntidadDtoARespuesta(incapacidadActualizada);
    }

    public void eliminar(Long id) {
        Incapacidades incapacidad = consulta.obtenerPorId(id);
        if (incapacidad == null) {
            throw new ResourceNotFoundException("Incapacidades", "id", id);
        }
        mantenimiento.eliminar(id);
        log.info("Se ha eliminado la incapacidad con ID: " + id);
    }

    public void eliminar(Long id, Authentication auth) {
        eliminar(id);
    }

    // ==================== MÉTODOS PARA EMPLEADOS ====================

    /**
     * Obtiene las solicitudes del empleado autenticado
     */
    public List<RespuestaIncapacidadesDTO> obtenerMisSolicitudes(Authentication auth) {
        Empleados empleado = obtenerEmpleadoAutenticado(auth);
        List<Incapacidades> solicitudes = consulta.obtenerPorEmpleadoId(empleado.getId());
        log.info("Se obtuvieron {} solicitudes de incapacidad para el empleado {}", solicitudes.size(), empleado.getId());
        return deListaEntidadADto(solicitudes);
    }

    // ==================== MÉTODOS PARA JEFES ====================

    /**
     * Obtiene las solicitudes pendientes del departamento que maneja el jefe autenticado
     */
    public List<RespuestaIncapacidadesDTO> obtenerSolicitudesPendientesDepartamento(Authentication auth) {
        Empleados jefe = obtenerEmpleadoAutenticado(auth);
        
        List<Long> departamentosQueManeja = jefesDepartamentoRepo.findDepartamentoIdsByEmpleadoId(jefe.getId());
        if (departamentosQueManeja.isEmpty()) {
            throw new ForbiddenException("El usuario no es jefe de ningún departamento");
        }
        
        List<Incapacidades> solicitudes = departamentosQueManeja.stream()
                .flatMap(idDep -> consulta.obtenerIncapacidadesPendientesByDepartamento(idDep).stream())
                .toList();
        
        log.info("Se obtuvieron {} solicitudes de incapacidad pendientes para el jefe {}", solicitudes.size(), jefe.getId());
        return deListaEntidadADto(solicitudes);
    }

    /**
     * Obtiene los empleados actualmente incapacitados del departamento que maneja el jefe autenticado
     */
    public List<RespuestaIncapacidadesDTO> obtenerEmpleadosIncapacitadosDepartamento(Authentication auth) {
        Empleados jefe = obtenerEmpleadoAutenticado(auth);
        
        List<Long> departamentosQueManeja = jefesDepartamentoRepo.findDepartamentoIdsByEmpleadoId(jefe.getId());
        if (departamentosQueManeja.isEmpty()) {
            throw new ForbiddenException("El usuario no es jefe de ningún departamento");
        }
        
        LocalDate hoy = LocalDate.now();
        List<Incapacidades> incapacitados = departamentosQueManeja.stream()
                .flatMap(idDep -> consulta.obtenerIncapacidadesActivasByDepartamento(idDep, hoy).stream())
                .toList();
        
        log.info("Se obtuvieron {} empleados incapacitados en departamentos del jefe {}", incapacitados.size(), jefe.getId());
        return deListaEntidadADto(incapacitados);
    }

    /**
     * Solicita una extensión de una incapacidad existente
     */
    public RespuestaIncapacidadesDTO solicitarExtension(Long idIncapacidad, 
                                                        SolicitudExtensionIncapacidadDTO solicitudExtension, 
                                                        Authentication auth) {
        Empleados jefe = obtenerEmpleadoAutenticado(auth);
        Incapacidades incapacidadOriginal = consulta.obtenerPorId(idIncapacidad);
        
        if (incapacidadOriginal == null) {
            throw new ResourceNotFoundException("Incapacidades", "id", idIncapacidad);
        }
        
        // Validar que la incapacidad esté aprobada y activa
        if (incapacidadOriginal.getEstadoSolicitud() != EstadoSolicitud.APROBADA) {
            throw new BadRequestException("Solo se pueden extender incapacidades aprobadas");
        }
        
        // Validar que el jefe tenga permiso sobre el departamento del empleado
        Long idDepartamento = incapacidadOriginal.getEmpleado().getPuesto().getDepartamento().getId();
        if (!jefesDepartamentoRepo.findByEmpleadoIdAndDepartamentoIdAndEstaActivoTrue(jefe.getId(), idDepartamento).isPresent()) {
            throw new ForbiddenException("No tiene permisos para extender esta incapacidad");
        }
        
        // Validar que la nueva fecha de fin sea posterior a la fecha fin actual
        if (!solicitudExtension.getNuevaFechaFin().isAfter(incapacidadOriginal.getFechaFin())) {
            throw new BadRequestException("La nueva fecha de fin debe ser posterior a la fecha fin actual");
        }
        
        // Crear nueva solicitud de incapacidad como extensión
        Incapacidades extension = Incapacidades.builder()
                .fechaInicio(incapacidadOriginal.getFechaFin().plusDays(1)) // Empieza el día siguiente al fin de la original
                .fechaFin(solicitudExtension.getNuevaFechaFin())
                .diasTotales(solicitudExtension.getDiasAdicionales())
                .tipoIncapacidad(incapacidadOriginal.getTipoIncapacidad())
                .porcentajePago(incapacidadOriginal.getPorcentajePago())
                .entidadEmisora(incapacidadOriginal.getEntidadEmisora())
                .numeroDocumento(solicitudExtension.getNumeroDocumento())
                .observaciones(solicitudExtension.getObservaciones())
                .urlDocumentoAdjunto(solicitudExtension.getUrlDocumentoAdjunto())
                .empleado(incapacidadOriginal.getEmpleado())
                .estadoSolicitud(EstadoSolicitud.PENDIENTE_RH) // Las extensiones van directo a RH
                .fechaSolicitud(LocalDate.now())
                .esExtension(true)
                .incapacidadOriginal(incapacidadOriginal)
                .fechaFinOriginal(incapacidadOriginal.getFechaFin())
                .comentariosExtension("Extensión solicitada por jefe: " + jefe.getNombre() + " " + jefe.getPrimerApellido())
                .build();
        
        Incapacidades extensionGuardada = mantenimiento.crear(extension);
        log.info("Se creó una extensión de incapacidad con ID: {} para la incapacidad original ID: {}", 
                extensionGuardada.getId(), idIncapacidad);
        return deEntidadDtoARespuesta(extensionGuardada);
    }

    /**
     * Aprueba una solicitud de incapacidad como jefe
     */
    public RespuestaIncapacidadesDTO aprobarPorJefe(Long idIncapacidad, String comentarios, Authentication auth) {
        Empleados jefe = obtenerEmpleadoAutenticado(auth);
        Incapacidades incapacidad = consulta.obtenerPorId(idIncapacidad);
        
        if (incapacidad == null) {
            throw new ResourceNotFoundException("Incapacidades", "id", idIncapacidad);
        }
        
        if (incapacidad.getEstadoSolicitud() != EstadoSolicitud.PENDIENTE) {
            throw new BadRequestException("La solicitud no está en estado PENDIENTE");
        }
        
        Long idDepartamento = incapacidad.getEmpleado().getPuesto().getDepartamento().getId();
        if (!jefesDepartamentoRepo.findByEmpleadoIdAndDepartamentoIdAndEstaActivoTrue(jefe.getId(), idDepartamento).isPresent()) {
            throw new ForbiddenException("No tiene permisos para aprobar esta solicitud");
        }
        
        incapacidad.setEstadoSolicitud(EstadoSolicitud.APROBADA_POR_JEFE);
        incapacidad.setComentariosJefe(comentarios);
        incapacidad.setFechaAprobacionJefe(LocalDate.now());
        incapacidad.setAprobadorJefe(jefe);
        
        Incapacidades incapacidadActualizada = mantenimiento.actualizar(incapacidad);
        log.info("Incapacidad {} aprobada por jefe {}", idIncapacidad, jefe.getId());
        return deEntidadDtoARespuesta(incapacidadActualizada);
    }

    /**
     * Rechaza una solicitud de incapacidad como jefe
     */
    public RespuestaIncapacidadesDTO rechazarPorJefe(Long idIncapacidad, String comentarios, Authentication auth) {
        Empleados jefe = obtenerEmpleadoAutenticado(auth);
        Incapacidades incapacidad = consulta.obtenerPorId(idIncapacidad);
        
        if (incapacidad == null) {
            throw new ResourceNotFoundException("Incapacidades", "id", idIncapacidad);
        }
        
        if (incapacidad.getEstadoSolicitud() != EstadoSolicitud.PENDIENTE) {
            throw new BadRequestException("La solicitud no está en estado PENDIENTE");
        }
        
        Long idDepartamento = incapacidad.getEmpleado().getPuesto().getDepartamento().getId();
        if (!jefesDepartamentoRepo.findByEmpleadoIdAndDepartamentoIdAndEstaActivoTrue(jefe.getId(), idDepartamento).isPresent()) {
            throw new ForbiddenException("No tiene permisos para rechazar esta solicitud");
        }
        
        incapacidad.setEstadoSolicitud(EstadoSolicitud.RECHAZADA_POR_JEFE);
        incapacidad.setComentariosJefe(comentarios);
        incapacidad.setFechaAprobacionJefe(LocalDate.now());
        incapacidad.setAprobadorJefe(jefe);
        
        Incapacidades incapacidadActualizada = mantenimiento.actualizar(incapacidad);
        log.info("Incapacidad {} rechazada por jefe {}", idIncapacidad, jefe.getId());
        return deEntidadDtoARespuesta(incapacidadActualizada);
    }

    // ==================== MÉTODOS PARA RH ====================

    /**
     * Obtiene las solicitudes que necesitan aprobación de RH
     */
    public List<RespuestaIncapacidadesDTO> obtenerSolicitudesParaRH() {
        List<Incapacidades> solicitudes = consulta.obtenerIncapacidadesParaRH();
        log.info("Se obtuvieron {} solicitudes de incapacidad pendientes para RH", solicitudes.size());
        return deListaEntidadADto(solicitudes);
    }

    /**
     * Obtiene todas las solicitudes ordenadas (para auditoría)
     */
    public List<RespuestaIncapacidadesDTO> obtenerTodasLasSolicitudes() {
        List<Incapacidades> solicitudes = consulta.obtenerTodosOrdenados();
        log.info("Se obtuvieron {} solicitudes de incapacidad (todas)", solicitudes.size());
        return deListaEntidadADto(solicitudes);
    }

    /**
     * Obtiene las incapacidades activas (aprobadas y en curso)
     */
    public List<RespuestaIncapacidadesDTO> obtenerIncapacidadesActivas() {
        List<Incapacidades> solicitudes = consulta.obtenerIncapacidadesActivas(LocalDate.now());
        log.info("Se obtuvieron {} incapacidades activas", solicitudes.size());
        return deListaEntidadADto(solicitudes);
    }

    /**
     * Aprueba una solicitud de incapacidad como RH (aprobación final)
     */
    public RespuestaIncapacidadesDTO aprobarPorRH(Long idIncapacidad, String comentarios, Authentication auth) {
        Empleados rh = obtenerEmpleadoAutenticado(auth);
        Incapacidades incapacidad = consulta.obtenerPorId(idIncapacidad);
        
        if (incapacidad == null) {
            throw new ResourceNotFoundException("Incapacidades", "id", idIncapacidad);
        }
        
        if (incapacidad.getEstadoSolicitud() != EstadoSolicitud.APROBADA_POR_JEFE && 
            incapacidad.getEstadoSolicitud() != EstadoSolicitud.PENDIENTE_RH) {
            throw new BadRequestException("La solicitud no está pendiente de aprobación de RH");
        }
        
        incapacidad.setEstadoSolicitud(EstadoSolicitud.APROBADA);
        incapacidad.setComentariosRH(comentarios);
        incapacidad.setFechaAprobacionRH(LocalDate.now());
        incapacidad.setAprobadorRH(rh);
        
        Incapacidades incapacidadActualizada = mantenimiento.actualizar(incapacidad);
        log.info("Incapacidad {} aprobada por RH {}", idIncapacidad, rh.getId());
        return deEntidadDtoARespuesta(incapacidadActualizada);
    }

    /**
     * Rechaza una solicitud de incapacidad como RH
     */
    public RespuestaIncapacidadesDTO rechazarPorRH(Long idIncapacidad, String comentarios, Authentication auth) {
        Empleados rh = obtenerEmpleadoAutenticado(auth);
        Incapacidades incapacidad = consulta.obtenerPorId(idIncapacidad);
        
        if (incapacidad == null) {
            throw new ResourceNotFoundException("Incapacidades", "id", idIncapacidad);
        }
        
        if (incapacidad.getEstadoSolicitud() != EstadoSolicitud.APROBADA_POR_JEFE && 
            incapacidad.getEstadoSolicitud() != EstadoSolicitud.PENDIENTE_RH) {
            throw new BadRequestException("La solicitud no está pendiente de aprobación de RH");
        }
        
        incapacidad.setEstadoSolicitud(EstadoSolicitud.RECHAZADA_POR_RH);
        incapacidad.setComentariosRH(comentarios);
        incapacidad.setFechaAprobacionRH(LocalDate.now());
        incapacidad.setAprobadorRH(rh);
        
        Incapacidades incapacidadActualizada = mantenimiento.actualizar(incapacidad);
        log.info("Incapacidad {} rechazada por RH {}", idIncapacidad, rh.getId());
        return deEntidadDtoARespuesta(incapacidadActualizada);
    }

    /**
     * Cancela una solicitud aprobada (solo RH puede cancelar)
     */
    public RespuestaIncapacidadesDTO cancelarSolicitud(Long idIncapacidad, Authentication auth) {
        Empleados rh = obtenerEmpleadoAutenticado(auth);
        Incapacidades incapacidad = consulta.obtenerPorId(idIncapacidad);
        
        if (incapacidad == null) {
            throw new ResourceNotFoundException("Incapacidades", "id", idIncapacidad);
        }
        
        if (incapacidad.getEstadoSolicitud() != EstadoSolicitud.APROBADA) {
            throw new BadRequestException("Solo se pueden cancelar solicitudes aprobadas");
        }
        
        incapacidad.setEstadoSolicitud(EstadoSolicitud.CANCELADA);
        incapacidad.setComentariosRH("Cancelada por RH: " + rh.getNombre() + " " + rh.getPrimerApellido());
        
        Incapacidades incapacidadActualizada = mantenimiento.actualizar(incapacidad);
        log.info("Incapacidad {} cancelada por RH {}", idIncapacidad, rh.getId());
        return deEntidadDtoARespuesta(incapacidadActualizada);
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Determina el estado inicial de una solicitud de incapacidad
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
     * Obtiene el empleado asociado al usuario autenticado
     */
    private Empleados obtenerEmpleadoAutenticado(Authentication auth) {
        User user = (User) auth.getPrincipal();
        Empleados empleado = consultasEmpleados.obtenerPorId(user.getEmpleado().getId());
        if (empleado == null) {
            throw new ResourceNotFoundException("Empleados", "id", user.getEmpleado().getId());
        }
        return empleado;
    }

    // ==================== MÉTODOS DE CONVERSIÓN ====================

    public Incapacidades deSolicitudDtoAEntidad(SolicitudIncapacidadesDTO solicitud) {
        if (solicitud == null) {
            log.warn("El DTO de solicitud es nulo, no se puede convertir a entidad Incapacidades.");
            return null;
        }
        
        Empleados empleado = consultasEmpleados.obtenerPorId(solicitud.idEmpleado);
        if (empleado == null) {
            log.warn("No se ha encontrado el empleado con ID: " + solicitud.idEmpleado);
            throw new ResourceNotFoundException("Empleados", "id", solicitud.idEmpleado);
        }
        
        TipoIncapacidad tipoIncapacidad = obtenerTipoIncapacidad(solicitud.tipoIncapacidad);
        if (tipoIncapacidad == null) {
            log.warn("No se ha encontrado el tipo de incapacidad: " + solicitud.tipoIncapacidad);
            throw new BadRequestException("Tipo de incapacidad inválido: " + solicitud.tipoIncapacidad);
        }
        
        TipoEntidadEmisora entidadEmisora = obtenerTipoEntidadEmisora(solicitud.entidadEmisora);
        if (entidadEmisora == null) {
            log.warn("No se ha encontrado la entidad emisora: " + solicitud.entidadEmisora);
            throw new BadRequestException("Entidad emisora inválida: " + solicitud.entidadEmisora);
        }
        
        Incapacidades incapacidad = Incapacidades.builder()
                .id(solicitud.id)
                .fechaInicio(solicitud.fechaInicio)
                .fechaFin(solicitud.fechaFin)
                .diasTotales(solicitud.diasTotales)
                .tipoIncapacidad(tipoIncapacidad)
                .porcentajePago(solicitud.porcentajePago)
                .entidadEmisora(entidadEmisora)
                .numeroDocumento(solicitud.numeroDocumento)
                .observaciones(solicitud.observaciones)
                .urlDocumentoAdjunto(solicitud.urlDocumentoAdjunto)
                .empleado(empleado)
                .build();
        
        log.info("Se ha convertido el DTO de solicitud a entidad Incapacidades");
        return incapacidad;
    }

    public RespuestaIncapacidadesDTO deEntidadDtoARespuesta(Incapacidades entidad) {
        if (entidad == null) {
            log.warn("La entidad Incapacidades es nula, no se puede convertir a DTO de respuesta.");
            return null;
        }
        
        RespuestaIncapacidadesDTO respuesta = new RespuestaIncapacidadesDTO();
        respuesta.id = entidad.getId();
        respuesta.fechaInicio = entidad.getFechaInicio();
        respuesta.fechaFin = entidad.getFechaFin();
        respuesta.diasTotales = entidad.getDiasTotales();
        respuesta.porcentajePago = entidad.getPorcentajePago();
        respuesta.numeroDocumento = entidad.getNumeroDocumento();
        respuesta.observaciones = entidad.getObservaciones();
        respuesta.urlDocumentoAdjunto = entidad.getUrlDocumentoAdjunto();
        
        // Fechas de auditoría
        respuesta.fechaSolicitud = entidad.getFechaSolicitud();
        respuesta.fechaAprobacionJefe = entidad.getFechaAprobacionJefe();
        respuesta.fechaAprobacionRH = entidad.getFechaAprobacionRH();
        
        // Comentarios
        respuesta.comentariosJefe = entidad.getComentariosJefe();
        respuesta.comentariosRH = entidad.getComentariosRH();
        
        // Campos de extensión
        respuesta.esExtension = entidad.getEsExtension();
        if (entidad.getIncapacidadOriginal() != null) {
            respuesta.idIncapacidadOriginal = entidad.getIncapacidadOriginal().getId();
        }
        respuesta.fechaFinOriginal = entidad.getFechaFinOriginal();
        respuesta.comentariosExtension = entidad.getComentariosExtension();
        
        if (entidad.getTipoIncapacidad() != null) {
            respuesta.tipoIncapacidad = entidad.getTipoIncapacidad().name();
        }
        
        if (entidad.getEstadoSolicitud() != null) {
            respuesta.estadoSolicitud = entidad.getEstadoSolicitud().name();
        }
        
        if (entidad.getEntidadEmisora() != null) {
            respuesta.entidadEmisora = entidad.getEntidadEmisora().name();
        }
        
        // Empleado solicitante
        if (entidad.getEmpleado() != null) {
            respuesta.idEmpleado = entidad.getEmpleado().getId();
            respuesta.nombreEmpleado = entidad.getEmpleado().getNombre();
            respuesta.primerApellidoEmpleado = entidad.getEmpleado().getPrimerApellido();
            respuesta.segundoApellidoEmpleado = entidad.getEmpleado().getSegundoApellido();
            if (entidad.getEmpleado().getPuesto() != null && 
                entidad.getEmpleado().getPuesto().getDepartamento() != null) {
                respuesta.departamentoEmpleado = entidad.getEmpleado().getPuesto().getDepartamento().getNombre();
            }
        }
        
        // Aprobador jefe
        if (entidad.getAprobadorJefe() != null) {
            respuesta.nombreAprobadorJefe = entidad.getAprobadorJefe().getNombre();
            respuesta.primerApellidoAprobadorJefe = entidad.getAprobadorJefe().getPrimerApellido();
            respuesta.segundoApellidoAprobadorJefe = entidad.getAprobadorJefe().getSegundoApellido();
        }
        
        // Aprobador RH
        if (entidad.getAprobadorRH() != null) {
            respuesta.nombreAprobadorRH = entidad.getAprobadorRH().getNombre();
            respuesta.primerApellidoAprobadorRH = entidad.getAprobadorRH().getPrimerApellido();
            respuesta.segundoApellidoAprobadorRH = entidad.getAprobadorRH().getSegundoApellido();
        }
        
        return respuesta;
    }

    public List<RespuestaIncapacidadesDTO> deListaEntidadADto(List<Incapacidades> entidades) {
        return entidades.stream()
                .map(this::deEntidadDtoARespuesta)
                .toList();
    }
    
    private TipoIncapacidad obtenerTipoIncapacidad(String tipo) {
        try {
            return TipoIncapacidad.valueOf(tipo.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }
    
    private TipoEntidadEmisora obtenerTipoEntidadEmisora(String entidad) {
        try {
            return TipoEntidadEmisora.valueOf(entidad.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }
}
