package com.anthony.tfg.tfg.DTOs.Solicitud;

import com.anthony.tfg.tfg.Entidades.Enums.TipoQuincena;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SolicitudGenerarPlanillaDTO(
        @NotNull
        @Min(1)
        @Max(12)
        Integer mes,
        @NotNull
        @Min(2000)
        Integer anio,
        @NotNull
        TipoQuincena tipoQuincena) {
}
