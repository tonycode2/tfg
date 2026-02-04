package com.anthony.tfg.tfg.DTOs.Respuesta;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * DTO para la respuesta de planilla de un empleado específico
 * Combina datos del encabezado y detalle de la planilla
 */
public class RespuestaPlanillaEmpleadoDTO {
    // Datos del encabezado
    public Long idEncabezado;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public LocalDate fechaInicioPeriodo;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public LocalDate fechaFinPeriodo;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public LocalDate fechaPago;
    public String tipoQuincena;
    public String estadoPlanilla;
    
    // Datos del detalle
    public Long idDetalle;
    public Double salarioBasePeriodo;
    public Integer cantidadDiasFeriados;
    public Double montoHorasExtra;
    public Double montoIncapacidad;
    public Double deduccionCcssIvm;
    public Double deduccionCcssSem;
    public Double impuestoDeRenta;
    public Double otrasDeducciones;
    
    // Totales calculados
    public Double totalDevengado;
    public Double totalDeducciones;
    public Double salarioNeto;
}
