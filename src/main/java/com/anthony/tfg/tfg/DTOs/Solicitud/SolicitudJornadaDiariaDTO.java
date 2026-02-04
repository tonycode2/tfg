package com.anthony.tfg.tfg.DTOs.Solicitud;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SolicitudJornadaDiariaDTO {
    Long id;
    
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate fecha;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    LocalTime horaEntrada;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    LocalTime horaSalida;
    
    @PositiveOrZero
    Double horasRegulares;
    
    @PositiveOrZero
    Double horasExtra;
    
    String observaciones;
    
    @NotNull
    Long idEmpleado;
    
    Long idPermiso;
    
    Long idIncapacidad;
}
