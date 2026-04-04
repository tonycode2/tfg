package com.anthony.tfg.tfg.Modulos.Incapacidad.Servicio;

import java.io.UnsupportedEncodingException;
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
import com.anthony.tfg.tfg.Modulos.JornadaDiaria.Servicio.ServicioJornadaDiaria;
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
    private final com.anthony.tfg.tfg.Util.FileStorageService fileStorageService;
    private final ServicioJornadaDiaria servicioJornadaDiaria;
    private final com.anthony.tfg.tfg.Modulos.Empleados.Servicio.ServicioEmail servicioEmail;

    public ServicioIncapacidad(ConsultasIncapacidades consulta, 
                               MantenimientosIncapacidades mantenimiento, 
                               ConsultasEmpleados consultasEmpleados,
                               JefesDepartamentoRepositorio jefesDepartamentoRepo,
                               com.anthony.tfg.tfg.Util.FileStorageService fileStorageService,
                               ServicioJornadaDiaria servicioJornadaDiaria,
                               com.anthony.tfg.tfg.Modulos.Empleados.Servicio.ServicioEmail servicioEmail) {
        this.consulta = consulta;
        this.mantenimiento = mantenimiento;
        this.consultasEmpleados = consultasEmpleados;
        this.jefesDepartamentoRepo = jefesDepartamentoRepo;
        this.fileStorageService = fileStorageService;
        this.servicioJornadaDiaria = servicioJornadaDiaria;
        this.servicioEmail = servicioEmail;
    }

    // ==================== MÉTODOS BÁSICOS (CRUD) ====================

    /**
     * Obtiene un registro por su identificador.
     * @param id parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public RespuestaIncapacidadesDTO obtenerPorId(Long id) {
        Incapacidades incapacidad = consulta.obtenerPorId(id);
        if (incapacidad == null) {
            log.warn("No se ha encontrado la incapacidad con ID: " + id);
            throw new ResourceNotFoundException("Incapacidades", "id", id);
        }
        log.info("Se ha encontrado la incapacidad con ID: " + id);
        return deEntidadDtoARespuesta(incapacidad);
    }

    /**
     * Obtiene todos los registros disponibles.
     * @return resultado de la operacion.
     */
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

        // If caller provided an idEmpleado explicitly, allow it only when the caller
        // has sufficient role/permission (JEFE managing that employee, or HR/ADMIN).
        Object principal = auth.getPrincipal();
        com.anthony.tfg.tfg.Modulos.Seguridad.user.User user = (com.anthony.tfg.tfg.Modulos.Seguridad.user.User) principal;

        if (entidad.getIdEmpleado() != null) {
            // Admins and HR can create on behalf of any employee
            if (user.getRole() != null && (user.getRole().name().equals("HR") || user.getRole().name().equals("ADMIN"))) {
                return guardarInterno(entidad);
            }

            // If caller is JEFE, verify they manage the department of the target employee
            if (user.getRole() != null && user.getRole().name().equals("JEFE")) {
                Empleados target = consultasEmpleados.obtenerPorId(entidad.getIdEmpleado());
                if (target == null) {
                    throw new ResourceNotFoundException("Empleados", "id", entidad.getIdEmpleado());
                }
                Long idDepartamento = null;
                if (target.getPuesto() != null && target.getPuesto().getDepartamento() != null) {
                    idDepartamento = target.getPuesto().getDepartamento().getId();
                }
                boolean esJefeDelDepartamento = idDepartamento != null && jefesDepartamentoRepo.findByEmpleadoIdAndDepartamentoIdAndEstaActivoTrue(empleadoAutenticado.getId(), idDepartamento).isPresent();
                if (!esJefeDelDepartamento) {
                    throw new com.anthony.tfg.tfg.Exceptions.ForbiddenException("No tiene permisos para crear incapacidades para este empleado");
                }

                // Allowed: proceed without overriding idEmpleado
                return guardarInterno(entidad);
            }

            // Otherwise (regular employee), ignore provided idEmpleado and use authenticated employee
            entidad.setIdEmpleado(empleadoAutenticado.getId());
            return guardarInterno(entidad);
        }

        // No idEmpleado provided: use authenticated employee
        entidad.setIdEmpleado(empleadoAutenticado.getId());
        return guardarInterno(entidad);
    }

    /**
     * Método interno para guardar incapacidad con lógica de estado inicial
     */
    private RespuestaIncapacidadesDTO guardarInterno(SolicitudIncapacidadesDTO entidad) {
        // Validar fechas
        if (entidad.getFechaFin().isBefore(entidad.getFechaInicio())) {
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

        servicioJornadaDiaria.generarJornadasParaIncapacidad(
            incapacidadGuardada,
            incapacidadGuardada.getFechaInicio(),
            incapacidadGuardada.getFechaFin());

        return deEntidadDtoARespuesta(incapacidadGuardada);
    }

    /**
     * Actualiza un registro existente.
     * @param id parametro de entrada de la operacion.
     * @param entidad parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public RespuestaIncapacidadesDTO actualizar(Long id, SolicitudIncapacidadesDTO entidad) {
        Incapacidades incapacidadExistente = consulta.obtenerPorId(id);
        if (incapacidadExistente == null) {
            log.warn("No se ha encontrado la incapacidad con ID: " + id + " para actualizar");
            throw new ResourceNotFoundException("Incapacidades", "id", id);
        }
        
        incapacidadExistente.setFechaInicio(entidad.getFechaInicio());
        incapacidadExistente.setFechaFin(entidad.getFechaFin());
        incapacidadExistente.setDiasTotales(entidad.getDiasTotales());
        incapacidadExistente.setPorcentajePago(entidad.getPorcentajePago());
        incapacidadExistente.setNumeroDocumento(entidad.getNumeroDocumento());
        incapacidadExistente.setObservaciones(entidad.getObservaciones());
        incapacidadExistente.setUrlDocumentoAdjunto(entidad.getUrlDocumentoAdjunto());
        
        TipoIncapacidad tipoIncapacidad = obtenerTipoIncapacidad(entidad.getTipoIncapacidad());
        if (tipoIncapacidad != null) {
            incapacidadExistente.setTipoIncapacidad(tipoIncapacidad);
        }
        
        TipoEntidadEmisora entidadEmisora = obtenerTipoEntidadEmisora(entidad.getEntidadEmisora());
        if (entidadEmisora != null) {
            incapacidadExistente.setEntidadEmisora(entidadEmisora);
        }
        
        Empleados empleado = consultasEmpleados.obtenerPorId(entidad.getIdEmpleado());
        if (empleado != null) {
            incapacidadExistente.setEmpleado(empleado);
        }
        
        Incapacidades incapacidadActualizada = mantenimiento.actualizar(incapacidadExistente);
        log.info("Se ha actualizado la incapacidad con ID: " + id);
        return deEntidadDtoARespuesta(incapacidadActualizada);
    }

    /**
     * Elimina un registro por su identificador.
     * @param id parametro de entrada de la operacion.
     */
    public void eliminar(Long id) {
        Incapacidades incapacidad = consulta.obtenerPorId(id);
        if (incapacidad == null) {
            throw new ResourceNotFoundException("Incapacidades", "id", id);
        }
        // Eliminar archivo asociado si existe
        if (incapacidad.getUrlDocumentoAdjunto() != null) {
            try {
                fileStorageService.deleteFile(incapacidad.getUrlDocumentoAdjunto());
            } catch (Exception e) {
                log.warn("No se pudo eliminar el archivo adjunto de la incapacidad {}: {}", id, e.getMessage());
            }
        }
        mantenimiento.eliminar(id);
        log.info("Se ha eliminado la incapacidad con ID: " + id);
    }

    /**
     * Elimina un registro por su identificador.
     * @param id parametro de entrada de la operacion.
     * @param auth parametro de entrada de la operacion.
     */
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
        Empleados solicitante = obtenerEmpleadoAutenticado(auth);
        Incapacidades incapacidadOriginal = consulta.obtenerPorId(idIncapacidad);
        
        if (incapacidadOriginal == null) {
            throw new ResourceNotFoundException("Incapacidades", "id", idIncapacidad);
        }
        
        // Validar que la incapacidad esté aprobada y activa
        if (incapacidadOriginal.getEstadoSolicitud() != EstadoSolicitud.APROBADA) {
            throw new BadRequestException("Solo se pueden extender incapacidades aprobadas");
        }
        
        Long idDepartamento = incapacidadOriginal.getEmpleado().getPuesto().getDepartamento().getId();
        boolean esPropietario = solicitante.getId().equals(incapacidadOriginal.getEmpleado().getId());
        boolean esJefeConPermiso = jefesDepartamentoRepo
                .findByEmpleadoIdAndDepartamentoIdAndEstaActivoTrue(solicitante.getId(), idDepartamento)
                .isPresent();

        if (!esPropietario && !esJefeConPermiso) {
            throw new ForbiddenException("No tiene permisos para extender esta incapacidad");
        }
        
        // Validar que la nueva fecha de fin sea posterior a la fecha fin actual
        if (!solicitudExtension.getNuevaFechaFin().isAfter(incapacidadOriginal.getFechaFin())) {
            throw new BadRequestException("La nueva fecha de fin debe ser posterior a la fecha fin actual");
        }
        
        // Crear nueva solicitud de incapacidad como extensión
        String comentarioExtension = esJefeConPermiso
            ? "Extensión solicitada por jefe: " + solicitante.getNombre() + " " + solicitante.getPrimerApellido()
            : "Extensión solicitada por empleado";

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
                .comentariosExtension(comentarioExtension)
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
        
        // Si la solicitud es una extensión, aplicar los cambios sobre la incapacidad original
        if (Boolean.TRUE.equals(incapacidad.getEsExtension())) {
            Incapacidades original = incapacidad.getIncapacidadOriginal();
            if (original == null) {
                throw new BadRequestException("La extensión no referencia una incapacidad original válida");
            }

            LocalDate fechaInicioNuevosDias = original.getFechaFin() != null
                    ? original.getFechaFin().plusDays(1)
                    : incapacidad.getFechaInicio();

            // Actualizar la incapacidad original con la nueva fecha fin y sumar días
            original.setFechaFin(incapacidad.getFechaFin());
            if (original.getDiasTotales() == null) {
                original.setDiasTotales(incapacidad.getDiasTotales());
            } else {
                original.setDiasTotales(original.getDiasTotales() + (incapacidad.getDiasTotales() == null ? 0 : incapacidad.getDiasTotales()));
            }

            // Marcar la aprobación en la incapacidad original para mantener consistencia
            original.setFechaAprobacionRH(LocalDate.now());
            original.setAprobadorRH(rh);
            original.setEstadoSolicitud(EstadoSolicitud.APROBADA);

            Incapacidades originalActualizada = mantenimiento.actualizar(original);

            LocalDate nuevaFechaFin = incapacidad.getFechaFin();
            if (nuevaFechaFin != null
                    && fechaInicioNuevosDias != null
                    && !fechaInicioNuevosDias.isAfter(nuevaFechaFin)) {
                servicioJornadaDiaria.generarJornadasParaIncapacidad(
                        originalActualizada,
                        fechaInicioNuevosDias,
                        nuevaFechaFin);
            }

            // Eliminar la entrada de extensión para evitar líneas duplicadas en la base de datos
            try {
                mantenimiento.eliminar(incapacidad.getId());
            } catch (Exception e) {
                // No interrumpir el flujo por un error al eliminar; solo registrar
                log.warn("No se pudo eliminar la entrada de extensión con ID {}: {}", incapacidad.getId(), e.getMessage());
            }

            // Enviar correo de aprobación al empleado
            try {
                Empleados empleado = originalActualizada.getEmpleado();
                if (empleado != null && empleado.getCorreoPersonal() != null) {
                    servicioEmail.enviarNotificacionIncapacidad(
                        empleado.getCorreoPersonal(),
                        empleado.getNombre() + " " + empleado.getPrimerApellido(),
                        originalActualizada.getTipoIncapacidad().name(),
                        true,
                        comentarios,
                        originalActualizada.getDiasTotales(),
                        originalActualizada.getFechaInicio().toString(),
                        originalActualizada.getFechaFin().toString()
                    );
                } else if (empleado != null) {
                    log.warn("El empleado {} no tiene correo personal registrado", empleado.getId());
                }
            } catch (Exception e) {
                log.error("Error al enviar correo de aprobación de incapacidad: {}", e.getMessage());
            }

            log.info("Extensión de incapacidad {} aprobada por RH {}. Se actualizaron los datos de la incapacidad original {}", idIncapacidad, rh.getId(), original.getId());
            return deEntidadDtoARespuesta(originalActualizada);
        }

        Incapacidades incapacidadActualizada = mantenimiento.actualizar(incapacidad);
        
        // Enviar correo de aprobación al empleado
        try {
            Empleados empleado = incapacidadActualizada.getEmpleado();
            if (empleado != null && empleado.getCorreoPersonal() != null) {
                servicioEmail.enviarNotificacionIncapacidad(
                    empleado.getCorreoPersonal(),
                    empleado.getNombre() + " " + empleado.getPrimerApellido(),
                    incapacidadActualizada.getTipoIncapacidad().name(),
                    true,
                    comentarios,
                    incapacidadActualizada.getDiasTotales(),
                    incapacidadActualizada.getFechaInicio().toString(),
                    incapacidadActualizada.getFechaFin().toString()
                );
            } else if (empleado != null) {
                log.warn("El empleado {} no tiene correo personal registrado", empleado.getId());
            }
        } catch (Exception e) {
            log.error("Error al enviar correo de aprobación de incapacidad: {}", e.getMessage());
        }
        
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
        
        // Enviar correo de rechazo al empleado
        try {
            Empleados empleado = incapacidadActualizada.getEmpleado();
            if (empleado != null && empleado.getCorreoPersonal() != null) {
                servicioEmail.enviarNotificacionIncapacidad(
                    empleado.getCorreoPersonal(),
                    empleado.getNombre() + " " + empleado.getPrimerApellido(),
                    incapacidadActualizada.getTipoIncapacidad().name(),
                    false,
                    comentarios,
                    incapacidadActualizada.getDiasTotales(),
                    incapacidadActualizada.getFechaInicio().toString(),
                    incapacidadActualizada.getFechaFin().toString()
                );
            } else if (empleado != null) {
                log.warn("El empleado {} no tiene correo personal registrado", empleado.getId());
            }
        } catch (Exception e) {
            log.error("Error al enviar correo de rechazo de incapacidad: {}", e.getMessage());
        }
        
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

    /**
     * Descarga el archivo adjunto de la incapacidad si el usuario tiene permisos
     * @throws UnsupportedEncodingException 
     */
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> descargarArchivo(Long idIncapacidad, Authentication auth) throws UnsupportedEncodingException {
        Incapacidades incapacidad = consulta.obtenerPorId(idIncapacidad);
        if (incapacidad == null) {
            throw new ResourceNotFoundException("Incapacidades", "id", idIncapacidad);
        }

        // Validar que exista archivo
        if (incapacidad.getUrlDocumentoAdjunto() == null) {
            throw new BadRequestException("No existe un archivo adjunto para esta incapacidad");
        }

        // Verificar permisos: ADMIN y RH siempre pueden; JEFE si es jefe del departamento; el propio empleado puede ver su archivo
        Object principal = auth.getPrincipal();
        com.anthony.tfg.tfg.Modulos.Seguridad.user.User user = (com.anthony.tfg.tfg.Modulos.Seguridad.user.User) principal;
        String role = user.getRole().name();
        boolean permitido = false;

        if ("ADMIN".equals(role) || "HR".equals(role)) {
            permitido = true;
        } else if ("JEFE".equals(role)) {
            Empleados jefe = obtenerEmpleadoAutenticado(auth);
            Long idDepartamento = incapacidad.getEmpleado().getPuesto().getDepartamento().getId();
            if (jefesDepartamentoRepo.findByEmpleadoIdAndDepartamentoIdAndEstaActivoTrue(jefe.getId(), idDepartamento).isPresent()) {
                permitido = true;
            }
        } else {
            // Permitir al propio empleado
            Empleados empleado = user.getEmpleado();
            if (empleado != null && empleado.getId().equals(incapacidad.getEmpleado().getId())) {
                permitido = true;
            }
        }

        if (!permitido) {
            throw new ForbiddenException("No tiene permisos para descargar el archivo de esta solicitud");
        }

        org.springframework.core.io.Resource recurso = fileStorageService.loadFileAsResource(incapacidad.getUrlDocumentoAdjunto());
        String contentType = java.net.URLConnection.guessContentTypeFromName(recurso.getFilename());
        if (contentType == null) {
            contentType = org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        // Construir un nombre de archivo sugerido: "Incapacidad id {id} {Nombre} {PrimerApellido}.{ext}"
        String originalFilename = recurso.getFilename();
        String extension = "";
        if (originalFilename != null) {
            int dot = originalFilename.lastIndexOf('.');
            if (dot > -1) {
                extension = originalFilename.substring(dot);
            }
        }
        String empleadoNombre = (incapacidad.getEmpleado() != null && incapacidad.getEmpleado().getNombre() != null) ? incapacidad.getEmpleado().getNombre() : "";
        String empleadoApellido = (incapacidad.getEmpleado() != null && incapacidad.getEmpleado().getPrimerApellido() != null) ? incapacidad.getEmpleado().getPrimerApellido() : "";
        String suggested = "Incapacidad id " + incapacidad.getId() + " " + empleadoNombre + " " + empleadoApellido;
        // Mantener solo caracteres legibles y espacios, normalizar espacios múltiples
        String safe = suggested.replaceAll("[^\\p{L}\\p{N} _.-]", "").replaceAll("\\s+", " ").trim();
        String filename = (safe.isEmpty() ? "Incapacidad_" + incapacidad.getId() : safe) + extension;
        String encodedFilename = java.net.URLEncoder.encode(filename, java.nio.charset.StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20");
        String contentDisposition = "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encodedFilename;

        return org.springframework.http.ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(recurso);
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

    /**
     * Convierte un DTO de solicitud a entidad.
     * @param solicitud parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public Incapacidades deSolicitudDtoAEntidad(SolicitudIncapacidadesDTO solicitud) {
        if (solicitud == null) {
            log.warn("El DTO de solicitud es nulo, no se puede convertir a entidad Incapacidades.");
            return null;
        }
        
        Empleados empleado = consultasEmpleados.obtenerPorId(solicitud.getIdEmpleado());
        if (empleado == null) {
            log.warn("No se ha encontrado el empleado con ID: " + solicitud.getIdEmpleado());
            throw new ResourceNotFoundException("Empleados", "id", solicitud.getIdEmpleado());
        }
        
        TipoIncapacidad tipoIncapacidad = obtenerTipoIncapacidad(solicitud.getTipoIncapacidad());
        if (tipoIncapacidad == null) {
            log.warn("No se ha encontrado el tipo de incapacidad: " + solicitud.getTipoIncapacidad());
            throw new BadRequestException("Tipo de incapacidad inválido: " + solicitud.getTipoIncapacidad());
        }
        
        TipoEntidadEmisora entidadEmisora = obtenerTipoEntidadEmisora(solicitud.getEntidadEmisora());
        if (entidadEmisora == null) {
            log.warn("No se ha encontrado la entidad emisora: " + solicitud.getEntidadEmisora());
            throw new BadRequestException("Entidad emisora inválida: " + solicitud.getEntidadEmisora());
        }
        
        Incapacidades incapacidad = Incapacidades.builder()
                .id(solicitud.getId())
                .fechaInicio(solicitud.getFechaInicio())
                .fechaFin(solicitud.getFechaFin())
                .diasTotales(solicitud.getDiasTotales())
                .tipoIncapacidad(tipoIncapacidad)
                .porcentajePago(solicitud.getPorcentajePago())
                .entidadEmisora(entidadEmisora)
                .numeroDocumento(solicitud.getNumeroDocumento())
                .observaciones(solicitud.getObservaciones())
                .urlDocumentoAdjunto(solicitud.getUrlDocumentoAdjunto())
                .empleado(empleado)
                .build();
        
        log.info("Se ha convertido el DTO de solicitud a entidad Incapacidades");
        return incapacidad;
    }

    /**
     * Convierte una entidad a DTO de respuesta.
     * @param entidad parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
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
        // Si hay un archivo almacenado, exponer una URL de descarga segura
        if (entidad.getUrlDocumentoAdjunto() != null) {
            respuesta.urlDocumentoAdjunto = "/api/incapacidades/" + entidad.getId() + "/archivo";
        } else {
            respuesta.urlDocumentoAdjunto = null;
        }
        
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

    /**
     * Convierte una lista de entidades a DTOs de respuesta.
     * @param entidades parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public List<RespuestaIncapacidadesDTO> deListaEntidadADto(List<Incapacidades> entidades) {
        return entidades.stream()
                .map(this::deEntidadDtoARespuesta)
                .toList();
    }
    
    /**
     * Obtiene informacion necesaria para la operacion.
     * @param tipo parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    private TipoIncapacidad obtenerTipoIncapacidad(String tipo) {
        try {
            return TipoIncapacidad.valueOf(tipo.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Obtiene informacion necesaria para la operacion.
     * @param entidad parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    private TipoEntidadEmisora obtenerTipoEntidadEmisora(String entidad) {
        try {
            return TipoEntidadEmisora.valueOf(entidad.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }
}
