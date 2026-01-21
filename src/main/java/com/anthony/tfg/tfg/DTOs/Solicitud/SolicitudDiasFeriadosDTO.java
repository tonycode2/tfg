package com.anthony.tfg.tfg.DTOs.Solicitud;

import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para solicitudes de creación/actualización de días feriados.
 */
@Getter
@Setter
@NoArgsConstructor
public class SolicitudDiasFeriadosDTO {
    
    public Long id;
    
    @NotNull(message = "El nombre del feriado es requerido")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    public String nombre;
    
    @NotNull(message = "La fecha del feriado es requerida")
    @Future(message = "La fecha del feriado debe ser una fecha futura")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public LocalDate fecha;
    
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    public String descripcion;
}
