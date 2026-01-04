package com.anthony.tfg.tfg.DTOs.Solicitud;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class SolicitudConfiguracionRentaDTO {
    @NotBlank
    public Long id;

    @NotBlank
    @Positive
    public Double montoMinimo;

    @NotBlank
    @Positive
    public Double montoMaximo;

    @NotBlank
    @Positive
    public Double porcentaje;
}
