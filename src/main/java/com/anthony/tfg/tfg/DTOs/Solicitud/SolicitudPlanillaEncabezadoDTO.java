package com.anthony.tfg.tfg.DTOs.Solicitud;

import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;

public class SolicitudPlanillaEncabezadoDTO {
    @NotBlank
    public Long id;
    @NotBlank
    @Past
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date fechaInicioPeriodo;
    @NotBlank
    @Past
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date fechaFinPeriodo;
    @NotBlank
    @Future
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
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
