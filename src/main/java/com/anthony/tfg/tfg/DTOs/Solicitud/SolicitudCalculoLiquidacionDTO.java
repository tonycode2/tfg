package com.anthony.tfg.tfg.DTOs.Solicitud;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class SolicitudCalculoLiquidacionDTO {

    @NotNull(message = "El ID del empleado es requerido")
    @Positive(message = "El ID del empleado debe ser positivo")
    private Long idEmpleado;

    @NotNull(message = "La fecha de salida es requerida")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaSalida;

    @NotBlank(message = "El motivo de salida es requerido")
    private String motivoSalida;

    @NotNull(message = "Debe indicar si el preaviso es pagado")
    private Boolean preaviso_pagado;

    private String descripcion;
}
