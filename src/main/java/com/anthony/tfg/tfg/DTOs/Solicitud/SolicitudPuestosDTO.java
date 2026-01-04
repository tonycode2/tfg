package com.anthony.tfg.tfg.DTOs.Solicitud;

import java.sql.Time;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class SolicitudPuestosDTO {
    @NotBlank
    public Long id;
    @NotBlank
    @Size(min = 3, max = 50)
    public String nombre;
    @NotBlank
    @Positive
    public Double salarioMinimo;
    @NotBlank
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    public Time horaEntrada;
    @NotBlank
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    public Time horaSalida;
    @NotBlank
    public Long idDepartamento;
}
