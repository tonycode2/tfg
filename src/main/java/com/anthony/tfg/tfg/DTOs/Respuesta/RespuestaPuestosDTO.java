package com.anthony.tfg.tfg.DTOs.Respuesta;

import java.sql.Time;

import com.fasterxml.jackson.annotation.JsonFormat;

public class RespuestaPuestosDTO {

    public String nombre;
    public Double salarioMinimo;
    public String nombreDepartamento;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    public Time horaEntrada;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    public Time horaSalida;
}
