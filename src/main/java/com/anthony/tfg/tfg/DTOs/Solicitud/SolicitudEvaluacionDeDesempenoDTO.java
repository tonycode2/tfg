package com.anthony.tfg.tfg.DTOs.Solicitud;

import java.sql.Date;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class SolicitudEvaluacionDeDesempenoDTO {
    @NotBlank
    Long id;
    @NotBlank
    Date fechaEvaluacion;
    @NotBlank
    String periodoEvaluado;
    @NotBlank
    @Positive
    Double puntuacionFinal;
    @NotBlank
    @Size(min = 10, max = 500)
    String observaciones;
    @NotBlank
    @Size(min = 10, max = 500)
    String planDeMejora;
    @NotBlank
    Long idEmpleado;
}
