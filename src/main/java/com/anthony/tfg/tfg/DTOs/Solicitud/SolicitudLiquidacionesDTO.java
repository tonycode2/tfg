package com.anthony.tfg.tfg.DTOs.Solicitud;

import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;

public class SolicitudLiquidacionesDTO {
    public Long id;
    @NotNull
    @Past
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date fechaSalida;
    @NotNull
    @Positive
    public Double montoPreaviso;
    @NotNull
    @Positive
    public Double montoCesantia;
    @NotNull
    @Positive
    public Double montoVacacionesPendientes;
    @NotNull
    @Positive
    public Double montoAguinaldoPendiente;
    @NotNull
    @Positive
    public Double totalLiquidacion;
    @NotBlank
    public String motivoSalida;
    @NotNull
    @Positive
    public Long idEmpleado;
}
