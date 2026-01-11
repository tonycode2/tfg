package com.anthony.tfg.tfg.DTOs.Respuesta;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * DTO para respuestas de días feriados.
 */
public class RespuestaDiasFeriadosDTO {
    
    public Long id;
    public String nombre;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public LocalDate fecha;
    
    public String descripcion;
}
