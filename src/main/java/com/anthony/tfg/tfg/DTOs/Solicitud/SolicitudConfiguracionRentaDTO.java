package com.anthony.tfg.tfg.DTOs.Solicitud;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class SolicitudConfiguracionRentaDTO {
    public Long id;

    @NotNull
    @Positive
    public Double montoMinimo;

    @NotNull
    @Positive
    public Double montoMaximo;

    @NotNull
    @Positive
    public Double porcentaje;
}
