package com.anthony.tfg.tfg.DTOs.Solicitud;

import java.sql.Date;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;

public class SolicitudAguinaldosDTO {
    @NotBlank
    Long id;
    
    @NotBlank
    Integer anio;
    
    @NotBlank
    @Past
    Date fechaInicioPeriodo;
    
    @NotBlank
    @Past
    Date fechaFinPeriodo;
    
    @NotBlank
    @Positive
    Double totalSalariosDevengados;
    
    @NotBlank
    @Positive
    Double montoAguinaldo;
    
    @NotBlank
    Date fechaCalculo;
    
    @NotBlank
    @Future
    Date fechaPago;
    
    @NotBlank
    @Positive
    Long idEmpleado;
}
