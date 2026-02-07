package com.anthony.tfg.tfg.DTOs.Respuesta;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

public class RespuestaLiquidacionesDTO {
    public Long id;
    public Long idEmpleado;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public LocalDate fechaSalida;
    public Double montoPreaviso;
    public Double montoCesantia;
    public Double montoVacacionesPendientes;
    public Double montoAguinaldoPendiente;
    public Double montoSalarioProporcional;
    public Double totalLiquidacion;
    public Double salarioPromedioDiario;
    public Long diasTrabajadosTotal;
    public Boolean preaviso_pagado;
    public String descripcion;
    public String motivoSalida;
    public String nombreEmpleado;
    public String primerApellidoEmpleado;
    public String segundoApellidoEmpleado;
}
