package com.anthony.tfg.tfg.DTOs.Respuesta;

import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class RespuestaLiquidacionesDTO {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date fechaSalida;
    public Double montoPreaviso;
    public Double montoCesantia;
    public Double montoVacacionesPendientes;
    public Double montoAguinaldoPendiente;
    public Double totalLiquidacion;
    public String motivoSalida;
    public String nombreEmpleado;
    public String primerApellidoEmpleado;
    public String segundoApellidoEmpleado;
}
