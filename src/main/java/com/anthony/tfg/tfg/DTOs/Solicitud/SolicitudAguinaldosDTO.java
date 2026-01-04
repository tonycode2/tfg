package com.anthony.tfg.tfg.DTOs.Solicitud;

import java.sql.Date;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;

public class SolicitudAguinaldosDTO {
    @NotBlank
    public Long id;
    
    @NotBlank
    public Integer anio;
    
    @NotBlank
    @Past
    public Date fechaInicioPeriodo;
    
    @NotBlank
    @Past
    public Date fechaFinPeriodo;
    
    @NotBlank
    @Positive
    public Double totalSalariosDevengados;
    
    @NotBlank
    @Positive
    public Double montoAguinaldo;
    
    @NotBlank
    public Date fechaCalculo;
    
    @NotBlank
    @Future
    public Date fechaPago;
    
    @NotBlank
    @Positive
    public Long idEmpleado;
}
