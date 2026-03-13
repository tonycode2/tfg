package com.anthony.tfg.tfg.Modulos.DiasFeriados.Servicio;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaDiasFeriadosDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudDiasFeriadosDTO;
import com.anthony.tfg.tfg.Entidades.DiasFeriados;
import com.anthony.tfg.tfg.Exceptions.BadRequestException;
import com.anthony.tfg.tfg.Exceptions.ConflictException;
import com.anthony.tfg.tfg.Exceptions.ResourceNotFoundException;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasDiasFeriados;
import com.anthony.tfg.tfg.Modulos.Interfaces.ServicioInterface;
import com.anthony.tfg.tfg.Modulos.Mantenimientos.MantenimientosDiasFeriados;

import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para gestión de días feriados.
 * Maneja operaciones CRUD y validaciones de negocio.
 */
@Service
@Slf4j
public class ServicioDiasFeriados implements ServicioInterface<RespuestaDiasFeriadosDTO, 
                                                              SolicitudDiasFeriadosDTO, 
                                                              DiasFeriados> {
    
    private final ConsultasDiasFeriados consulta;
    private final MantenimientosDiasFeriados mantenimiento;
    
    /**
     * Inicializa el servicio con sus dependencias principales.
     * @param consulta parametro de entrada de la operacion.
     * @param mantenimiento parametro de entrada de la operacion.
     */
    public ServicioDiasFeriados(ConsultasDiasFeriados consulta, MantenimientosDiasFeriados mantenimiento) {
        this.consulta = consulta;
        this.mantenimiento = mantenimiento;
    }
    
    /**
     * Obtiene un feriado por su ID.
     */
    public RespuestaDiasFeriadosDTO obtenerPorId(Long id) {
        DiasFeriados feriado = consulta.obtenerPorId(id);
        if (feriado == null) {
            log.warn("No se encontró el día feriado con ID: {}", id);
            throw new ResourceNotFoundException("DiasFeriados", "id", id);
        }
        log.info("Se encontró el día feriado con ID: {}", id);
        return deEntidadDtoARespuesta(feriado);
    }
    
    /**
     * Obtiene todos los feriados.
     */
    public List<RespuestaDiasFeriadosDTO> obtenerTodos() {
        List<DiasFeriados> entidades = consulta.obtenerTodos();
        log.info("Se obtuvieron {} días feriados", entidades.size());
        return deListaEntidadADto(entidades);
    }
    
    /**
     * Guarda un nuevo feriado con validaciones.
     */
    @Override
    public RespuestaDiasFeriadosDTO guardar(SolicitudDiasFeriadosDTO entidad) {
        log.info("📅 Fecha recibida del frontend: {}", entidad.fecha);
        
        // Validar que la fecha sea futura
        LocalDate hoy = LocalDate.now();
        
        if (!entidad.fecha.isAfter(hoy)) {
            throw new BadRequestException("Solo se pueden registrar feriados con fechas futuras");
        }
        
        // Validar que no exista otro feriado en la misma fecha
        if (consulta.esFeriado(entidad.fecha)) {
            throw new ConflictException("Ya existe un feriado registrado para la fecha: " + entidad.fecha);
        }
        
        DiasFeriados nuevoFeriado = deSolicitudDtoAEntidad(entidad);
        DiasFeriados feriadoGuardado = mantenimiento.crear(nuevoFeriado);
        log.info("Se guardó nuevo día feriado: {} - {}", feriadoGuardado.getFecha(), feriadoGuardado.getNombre());
        
        RespuestaDiasFeriadosDTO respuesta = deEntidadDtoARespuesta(feriadoGuardado);
        log.info("📅 Fecha en respuesta DTO: {}", respuesta.fecha);
        
        return respuesta;
    }
    
    /**
     * Actualiza un feriado existente.
     */
    public RespuestaDiasFeriadosDTO actualizar(Long id, SolicitudDiasFeriadosDTO entidad) {
        DiasFeriados feriadoExistente = consulta.obtenerPorId(id);
        if (feriadoExistente == null) {
            log.warn("No se encontró el día feriado con ID: {} para actualizar", id);
            throw new ResourceNotFoundException("DiasFeriados", "id", id);
        }
        
        // Validar que la fecha sea futura
        LocalDate hoy = LocalDate.now();
        
        if (!entidad.fecha.isAfter(hoy)) {
            throw new BadRequestException("Solo se pueden registrar feriados con fechas futuras");
        }
        
        // Si la fecha cambió, verificar que no exista otro feriado con esa fecha
        if (!feriadoExistente.getFecha().equals(entidad.fecha)) {
            if (consulta.esFeriado(entidad.fecha)) {
                throw new ConflictException("Ya existe un feriado registrado para la fecha: " + entidad.fecha);
            }
        }
        
        feriadoExistente.setNombre(entidad.nombre);
        feriadoExistente.setFecha(entidad.fecha);
        feriadoExistente.setDescripcion(entidad.descripcion);
        
        DiasFeriados feriadoActualizado = mantenimiento.actualizar(feriadoExistente);
        log.info("Se actualizó el día feriado con ID: {}", id);
        return deEntidadDtoARespuesta(feriadoActualizado);
    }
    
    /**
     * Elimina un feriado (hard delete según requerimiento del admin).
     */
    public void eliminar(Long id) {
        DiasFeriados feriado = consulta.obtenerPorId(id);
        if (feriado == null) {
            throw new ResourceNotFoundException("DiasFeriados", "id", id);
        }
        mantenimiento.eliminar(id);
        log.info("Se eliminó el día feriado con ID: {}", id);
    }
    
    /**
     * Verifica si una fecha específica es un día feriado.
     */
    public boolean esFeriado(LocalDate fecha) {
        return consulta.esFeriado(fecha);
    }
    
    /**
     * Obtiene todos los feriados en un rango de fechas.
     */
    public List<RespuestaDiasFeriadosDTO> obtenerPorRango(LocalDate fechaInicio, LocalDate fechaFin) {
        List<DiasFeriados> feriados = consulta.obtenerPorRango(fechaInicio, fechaFin);
        log.info("Se encontraron {} feriados entre {} y {}", feriados.size(), fechaInicio, fechaFin);
        return deListaEntidadADto(feriados);
    }
    
    /**
     * Obtiene los feriados de un año específico.
     */
    public List<RespuestaDiasFeriadosDTO> obtenerPorAnio(int anio) {
        List<DiasFeriados> feriados = consulta.obtenerPorAnio(anio);
        log.info("Se encontraron {} feriados para el año {}", feriados.size(), anio);
        return deListaEntidadADto(feriados);
    }
    
    /**
     * Valida que no haya feriados en un rango de fechas.
     * Lanza BadRequestException si encuentra algún feriado.
     */
    public void validarNoFeriadosEnRango(LocalDate fechaInicio, LocalDate fechaFin) {
        List<DiasFeriados> feriadosEnRango = consulta.obtenerPorRango(fechaInicio, fechaFin);
        
        if (!feriadosEnRango.isEmpty()) {
            StringBuilder mensaje = new StringBuilder("Las siguientes fechas son días feriados: ");
            for (int i = 0; i < feriadosEnRango.size(); i++) {
                DiasFeriados feriado = feriadosEnRango.get(i);
                if (i > 0) mensaje.append(", ");
                mensaje.append(feriado.getFecha()).append(" (").append(feriado.getNombre()).append(")");
            }
            mensaje.append(". No se pueden solicitar permisos o vacaciones en días feriados.");
            throw new BadRequestException(mensaje.toString());
        }
    }
    
    // ==================== MÉTODOS DE CONVERSIÓN ====================
    
    /**
     * Convierte un DTO de solicitud a entidad.
     * @param solicitud parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    @Override
    public DiasFeriados deSolicitudDtoAEntidad(SolicitudDiasFeriadosDTO solicitud) {
        if (solicitud == null) {
            log.warn("El DTO de solicitud es nulo");
            return null;
        }
        
        return DiasFeriados.builder()
                .nombre(solicitud.nombre)
                .fecha(solicitud.fecha)
                .descripcion(solicitud.descripcion)
                .build();
    }
    
    /**
     * Convierte una entidad a DTO de respuesta.
     * @param entidad parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    @Override
    public RespuestaDiasFeriadosDTO deEntidadDtoARespuesta(DiasFeriados entidad) {
        if (entidad == null) {
            log.warn("La entidad DiasFeriados es nula");
            return null;
        }
        
        RespuestaDiasFeriadosDTO respuesta = new RespuestaDiasFeriadosDTO();
        respuesta.id = entidad.getId();
        respuesta.nombre = entidad.getNombre();
        respuesta.fecha = entidad.getFecha();
        respuesta.descripcion = entidad.getDescripcion();
        
        return respuesta;
    }
    
    /**
     * Convierte una lista de entidades a DTOs de respuesta.
     * @param entidades parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    @Override
    public List<RespuestaDiasFeriadosDTO> deListaEntidadADto(List<DiasFeriados> entidades) {
        return entidades.stream()
                .map(this::deEntidadDtoARespuesta)
                .toList();
    }
}
