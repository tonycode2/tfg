package com.anthony.tfg.tfg.DTOs.Solicitud;

import java.sql.Date;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;

public class SolicitudPlanillaEncabezadoDTO {
    @NotBlank
    public Long id;
    @NotBlank
    @Past
    public Date fechaInicioPeriodo;
    @NotBlank
    @Past
    public Date fechaFinPeriodo;
    @NotBlank
    @Future
    public Date fechaPago;
    @NotBlank
    @Positive
    public Double totalPlanillaBruto;
    @NotBlank
    @Positive
    public Double totalPlanillaNeto;
    @NotBlank
    public String estadoPlanilla;
}
