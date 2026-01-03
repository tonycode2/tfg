package com.anthony.tfg.tfg.DTOs.Solicitud;

import java.sql.Date;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;

public class SolicitudPlanillaEncabezadoDTO {
    @NotBlank
    Long id;
    @NotBlank
    @Past
    Date fechaInicioPeriodo;
    @NotBlank
    @Past
    Date fechaFinPeriodo;
    @NotBlank
    @Future
    Date fechaPago;
    @NotBlank
    @Positive
    Double totalPlanillaBruto;
    @NotBlank
    @Positive
    Double totalPlanillaNeto;
    @NotBlank
    String estadoPlanilla;
}
