package com.anthony.tfg.tfg.DTOs.Solicitud;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class SolicitudConfiguracionRentaDTO {
    @NotBlank
    Long id;

    @NotBlank
    @Positive
    Double montoMinimo;

    @NotBlank
    @Positive
    Double montoMaximo;

    @NotBlank
    @Positive
    Double porcentaje;
}
