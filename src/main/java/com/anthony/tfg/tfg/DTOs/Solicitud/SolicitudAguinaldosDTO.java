package com.anthony.tfg.tfg.DTOs.Solicitud;

import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;

public class SolicitudAguinaldosDTO {
    public Long id;
    
    @NotNull
    public Integer anio;
    
    @NotNull
    @Past
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date fechaInicioPeriodo;
    
    @NotNull
    @Past
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date fechaFinPeriodo;
    
    @NotNull
    @Positive
    public Double totalSalariosDevengados;
    
    @NotNull
    @Positive
    public Double montoAguinaldo;
    
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date fechaCalculo;
    
    @NotNull
    @Future
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date fechaPago;
    
    @NotNull
    @Positive
    public Long idEmpleado;
}
