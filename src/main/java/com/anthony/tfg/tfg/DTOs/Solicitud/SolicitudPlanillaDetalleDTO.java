package com.anthony.tfg.tfg.DTOs.Solicitud;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SolicitudPlanillaDetalleDTO {
    public Long id;
    @NotBlank
    @Positive
    public Double salarioBasePeriodo;
    @NotBlank
    @Positive
    public Integer cantidadDiasFeriados;
    @NotBlank
    @Positive
    public Double montoHorasExtra;
    @NotBlank
    @Positive
    public Double montoIncapacidad;
    @NotBlank
    @Positive
    public Double deduccionCcssIvm;
    @NotBlank
    @Positive
    public Double deduccionCcssSem;
    @NotBlank
    @Positive
    public Double impuestoDeRenta;
    @NotBlank
    @Positive
    public Double otrasDeducciones;
    @NotBlank
    @Positive
    public Long idEmpleado;
    @NotBlank
    @Positive
    public Long idPlanillaEncabezado;
}
