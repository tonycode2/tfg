package com.anthony.tfg.tfg.DTOs.Respuesta;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

public class RespuestaPlanillaEncabezadoDTO {
    public Long id;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public LocalDate fechaInicioPeriodo;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public LocalDate fechaFinPeriodo;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public LocalDate fechaPago;
    public String tipoQuincena;
    public Double totalPlanillaBruto;
    public Double totalPlanillaNeto;
    public String estadoPlanilla;
}
