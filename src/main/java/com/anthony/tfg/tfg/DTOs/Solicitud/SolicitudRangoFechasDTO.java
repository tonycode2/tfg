package com.anthony.tfg.tfg.DTOs.Solicitud;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO genérico para solicitudes que requieren un rango de fechas.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudRangoFechasDTO {

    @NotNull(message = "La fecha de inicio es requerida")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es requerida")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaFin;
}
