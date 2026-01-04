package com.anthony.tfg.tfg.DTOs.Solicitud;

import java.sql.Date;
import java.sql.Time;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class SolicitudAsistenciaDTO {
    @NotBlank
    public Long id;

    @NotBlank
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date fecha;

    @NotBlank
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    public Time horaEntrada;

    @NotBlank
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    public Time horaSalida;

    @NotBlank
    @Positive
    public Double horasTrabajadas;
    
    @NotBlank
    @Positive
    public Long idEmpleado;
}
