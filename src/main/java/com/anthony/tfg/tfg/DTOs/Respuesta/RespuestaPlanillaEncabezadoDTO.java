package com.anthony.tfg.tfg.DTOs.Respuesta;

import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class RespuestaPlanillaEncabezadoDTO {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date fechaInicioPeriodo;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date fechaFinPeriodo;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date fechaPago;
    public Double totalPlanillaBruto;
    public Double totalPlanillaNeto;
    public String estadoPlanilla;
}
