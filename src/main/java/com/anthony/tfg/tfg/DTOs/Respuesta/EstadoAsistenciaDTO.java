package com.anthony.tfg.tfg.DTOs.Respuesta;

import java.time.LocalDateTime;

import com.anthony.tfg.tfg.Entidades.Enums.TipoEvento;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * DTO representing the current attendance status of an employee
 */
public class EstadoAsistenciaDTO {
    
    public Long empleadoId;
    public String nombreCompleto;
    public String departamentoNombre;
    public String puestoNombre;
    
    /**
     * Last event type (ENTRADA or SALIDA), null if no records today
     */
    public TipoEvento ultimoEvento;
    
    /**
     * Date and time of the last event
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime fechaHoraUltimoEvento;
    
    /**
     * Current status: LABORANDO (clocked in) or FUERA (clocked out/not clocked in)
     */
    public EstadoActual estadoActual;
    
    /**
     * Observations like "Llegó 15 min tarde" or "Salió 30 min temprano"
     */
    public String observaciones;
    
    /**
     * Time of today's clock-in (if exists)
     */
    @JsonFormat(pattern = "HH:mm:ss")
    public LocalDateTime horaEntradaHoy;
    
    /**
     * Time of today's clock-out (if exists)
     */
    @JsonFormat(pattern = "HH:mm:ss")
    public LocalDateTime horaSalidaHoy;
    
    /**
     * Enum representing current attendance state
     */
    public enum EstadoActual {
        LABORANDO,  // Currently clocked in
        FUERA       // Not clocked in or already clocked out
    }
}
