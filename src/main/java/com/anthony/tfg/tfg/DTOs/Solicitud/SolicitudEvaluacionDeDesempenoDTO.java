package com.anthony.tfg.tfg.DTOs.Solicitud;

import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SolicitudEvaluacionDeDesempenoDTO {
    public Long id;
    @NotNull(message = "La fecha de evaluación es obligatoria")
    @PastOrPresent(message = "La fecha no puede ser futura")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date fechaEvaluacion;

    @NotBlank(message = "El periodo es obligatorio")
    @Pattern(regexp = "^\\d{4}-Q[1-4]$", message = "Formato de periodo inválido. Ej: 2025-Q4")
    public String periodoEvaluado;

    @NotNull(message = "La puntuación final es obligatoria")
    @DecimalMin(value = "0.0", message = "La puntuación debe ser mayor o igual a 0")
    @DecimalMax(value = "100.0", message = "La puntuación debe ser menor o igual a 100")
    public Double puntuacionFinal;

    @NotBlank(message = "Las observaciones son obligatorias")
    @Size(min = 10, max = 500, message = "Las observaciones deben tener entre 10 y 500 caracteres")
    public String observaciones;

    @NotBlank(message = "El plan de mejora es obligatorio")
    @Size(min = 10, max = 500, message = "El plan de mejora debe tener entre 10 y 500 caracteres")
    public String planDeMejora;

    @NotNull(message = "Seleccione el empleado a evaluar")
    public Long idEmpleado;
}
