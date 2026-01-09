package com.anthony.tfg.tfg.DTOs.Solicitud;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;


/**
 * DTO for clock-in/clock-out requests
 * Note: idEmpleado is NOT included because it's extracted from JWT token
 */
public class SolicitudRegistroAsistenciaDTO {
    
    /**
     * The date and time of the attendance event. Optional - if null, server will use current time.
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime fechaHora;
}
