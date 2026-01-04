package com.anthony.tfg.tfg.DTOs.Solicitud;

import java.sql.Date;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;

public class SolicitudLiquidacionesDTO {
    @NotBlank
    public Long id;
    @NotBlank
    @Past
    public Date fechaSalida;
    @NotBlank
    @Positive
    public Double montoPreaviso;
    @NotBlank
    @Positive
    public Double montoCesantia;
    @NotBlank
    @Positive
    public Double montoVacacionesPendientes;
    @NotBlank
    @Positive
    public Double montoAguinaldoPendiente;
    @NotBlank
    @Positive
    public Double totalLiquidacion;
    @NotBlank
    public String motivoSalida;
    @NotBlank
    @Positive
    public Long idEmpleado;
}
