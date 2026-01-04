package com.anthony.tfg.tfg.DTOs.Solicitud;

import java.sql.Date;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class SolicitudEvaluacionDeDesempenoDTO {
    @NotBlank
    public Long id;
    @NotBlank
    public Date fechaEvaluacion;
    @NotBlank
    public String periodoEvaluado;
    @NotBlank
    @Positive
    public Double puntuacionFinal;
    @NotBlank
    @Size(min = 10, max = 500)
    public String observaciones;
    @NotBlank
    @Size(min = 10, max = 500)
    public String planDeMejora;
    @NotBlank
    public Long idEmpleado;
}
