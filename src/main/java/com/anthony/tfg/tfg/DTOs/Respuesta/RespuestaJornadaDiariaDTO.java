package com.anthony.tfg.tfg.DTOs.Respuesta;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RespuestaJornadaDiariaDTO {
    Long id;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate fecha;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    LocalTime horaEntrada;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    LocalTime horaSalida;
    
    Double horasRegulares;
    
    Double horasExtra;
    
    String observaciones;
    
    Long idEmpleado;
    String nombreCompleto;
}
