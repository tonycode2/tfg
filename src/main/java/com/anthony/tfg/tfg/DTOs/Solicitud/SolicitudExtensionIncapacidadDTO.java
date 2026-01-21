package com.anthony.tfg.tfg.DTOs.Solicitud;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudExtensionIncapacidadDTO {
    
    @NotNull(message = "La nueva fecha de fin es requerida")
    @FutureOrPresent(message = "La fecha de fin debe ser hoy o en el futuro")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate nuevaFechaFin;

    @NotNull(message = "Los días adicionales son requeridos")
    @Positive(message = "Los días adicionales deben ser positivos")
    private Integer diasAdicionales;

    private String numeroDocumento;
    
    private String observaciones;
    
    private String urlDocumentoAdjunto;
}
