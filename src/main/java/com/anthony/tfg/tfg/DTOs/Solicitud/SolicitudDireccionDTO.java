package com.anthony.tfg.tfg.DTOs.Solicitud;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO inmutable para solicitudes de creación/actualización de direcciones.
 * ID es opcional - se genera automáticamente al crear.
 */
public record SolicitudDireccionDTO(
    Long id,
    @NotNull
    @Size(min = 5, max = 100)
    String provincia,
    @NotNull
    @Size(min = 5, max = 100)
    String canton,
    @NotNull
    @Size(min = 5, max = 100)
    String distrito,
    @NotNull
    @Size(min = 5, max = 100)
    String indicaciones
) {}
