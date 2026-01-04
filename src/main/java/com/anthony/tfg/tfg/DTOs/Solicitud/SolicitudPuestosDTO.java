package com.anthony.tfg.tfg.DTOs.Solicitud;

import java.sql.Time;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class SolicitudPuestosDTO {
    public Long id;
    @NotBlank
    @Size(min = 3, max = 50)
    public String nombre;
    @NotNull
    @Positive
    public Double salarioMinimo;
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    public Time horaEntrada;
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    public Time horaSalida;
    @NotNull
    public Long idDepartamento;
}
