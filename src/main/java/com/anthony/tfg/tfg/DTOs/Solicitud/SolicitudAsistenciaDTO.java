package com.anthony.tfg.tfg.DTOs.Solicitud;

import java.sql.Date;
import java.sql.Time;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class SolicitudAsistenciaDTO {
    public Long id;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date fecha;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    public Time horaEntrada;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    public Time horaSalida;

    @NotNull
    @Positive
    public Double horasTrabajadas;
    
    @NotNull
    @Positive
    public Long idEmpleado;
}
