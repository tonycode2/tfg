package com.anthony.tfg.tfg.DTOs.Solicitud;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SolicitudLiquidacionesDTO {
    public Long id;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public LocalDate fechaSalida;

    @NotNull
    @Positive
    public Double montoPreaviso;

    @NotNull
    @Positive
    public Double montoCesantia;

    @NotNull
    @Positive
    public Double montoVacacionesPendientes;

    @NotNull
    @Positive
    public Double montoAguinaldoPendiente;

    @NotNull
    @Positive
    public Double totalLiquidacion;

    @NotBlank
    public String motivoSalida;

    @NotNull
    @Positive
    public Long idEmpleado;
}
