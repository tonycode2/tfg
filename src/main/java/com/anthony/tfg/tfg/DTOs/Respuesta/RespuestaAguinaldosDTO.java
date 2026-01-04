package com.anthony.tfg.tfg.DTOs.Respuesta;

import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class RespuestaAguinaldosDTO {
    public Integer anio;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date fechaInicioPeriodo;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date fechaFinPeriodo;
    public Double totalSalariosDevengados;
    public Double montoAguinaldo;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date fechaCalculo;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date fechaPago;
    public String nombreEmpleado;
    public String primerApellidoEmpleado;
    public String segundoApellidoEmpleado;
}
