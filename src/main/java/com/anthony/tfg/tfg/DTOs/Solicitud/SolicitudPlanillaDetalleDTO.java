package com.anthony.tfg.tfg.DTOs.Solicitud;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class SolicitudPlanillaDetalleDTO {
    @NotBlank
    Long id;
    @NotBlank
    @Positive
    Double salarioBasePeriodo;
    @NotBlank
    @Positive
    Integer cantidadDiasFeriados;
    @NotBlank
    @Positive
    Double montoHorasExtra;
    @NotBlank
    @Positive
    Double montoIncapacidad;
    @NotBlank
    @Positive
    Double deduccionCcssIvm;
    @NotBlank
    @Positive
    Double deduccionCcssSem;
    @NotBlank
    @Positive
    Double impuestoDeRenta;
    @NotBlank
    @Positive
    Double otrasDeducciones;
    @NotBlank
    @Positive
    Long idEmpleado;
    @NotBlank
    @Positive
    Long idPlanillaEncabezado;
}
