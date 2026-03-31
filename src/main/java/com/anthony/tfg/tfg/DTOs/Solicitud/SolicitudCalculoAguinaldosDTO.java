package com.anthony.tfg.tfg.DTOs.Solicitud;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SolicitudCalculoAguinaldosDTO {
    
    @NotNull(message = "El año es requerido")
    @Min(value = 1900, message = "El año debe ser mayor a 1900")
    private Integer anio;
    
    public SolicitudCalculoAguinaldosDTO(Integer anio) {
        this.anio = anio;
    }
}
