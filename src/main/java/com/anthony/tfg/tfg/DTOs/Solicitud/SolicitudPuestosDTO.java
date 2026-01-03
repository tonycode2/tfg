package com.anthony.tfg.tfg.DTOs.Solicitud;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class SolicitudPuestosDTO {
    @NotBlank
    Long id;
    @NotBlank
    @Size(min = 3, max = 50)
    String nombre;
    @NotBlank
    @Positive
    Double salarioMinimo;
    @NotBlank
    Long idDepartamento;
}
