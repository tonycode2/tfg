package com.anthony.tfg.tfg.DTOs.Solicitud;

import java.sql.Date;
import java.sql.Time;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class SolicitudAsistenciaDTO {
    @NotBlank
    public Long id;

    @NotBlank
    public Date fecha;

    @NotBlank
    public Time horaEntrada;

    @NotBlank
    public Time horaSalida;

    @NotBlank
    @Positive
    public Double horasTrabajadas;
    
    @NotBlank
    @Positive
    public Long idEmpleado;
}
