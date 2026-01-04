package com.anthony.tfg.tfg.DTOs.Solicitud;

import java.sql.Time;

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
    public Time horaEntrada;
    @NotBlank
    public Time horaSalida;
    @NotBlank
    public Long idDepartamento;
}
