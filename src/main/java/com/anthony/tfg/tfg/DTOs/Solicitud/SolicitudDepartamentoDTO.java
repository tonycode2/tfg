package com.anthony.tfg.tfg.DTOs.Solicitud;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO inmutable para solicitudes de creación/actualización de departamentos.
 */
public record SolicitudDepartamentoDTO(
    Long id,
    @NotNull
    @Size(min = 3, max = 100)
    String nombre
) {}
