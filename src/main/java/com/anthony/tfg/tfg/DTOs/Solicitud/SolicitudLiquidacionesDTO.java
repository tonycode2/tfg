package com.anthony.tfg.tfg.DTOs.Solicitud;

import java.sql.Date;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;

public class SolicitudLiquidacionesDTO {
    @NotBlank
    Long id;
    @NotBlank
    @Past
    Date fechaSalida;
    @NotBlank
    @Positive
    Double montoPreaviso;
    @NotBlank
    @Positive
    Double montoCesantia;
    @NotBlank
    @Positive
    Double montoVacacionesPendientes;
    @NotBlank
    @Positive
    Double montoAguinaldoPendiente;
    @NotBlank
    @Positive
    Double totalLiquidacion;
    @NotBlank
    String motivoSalida;
    @NotBlank
    @Positive
    Long idEmpleado;
}
