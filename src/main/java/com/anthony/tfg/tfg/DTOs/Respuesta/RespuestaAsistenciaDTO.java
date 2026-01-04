package com.anthony.tfg.tfg.DTOs.Respuesta;

import java.sql.Date;
import java.sql.Time;

import com.fasterxml.jackson.annotation.JsonFormat;

public class RespuestaAsistenciaDTO {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date fecha;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    public Time horaEntrada;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    public Time horaSalida;
    public Double horasTrabajadas;
    public String nombreEmpleado;
    public String primerApellidoEmpleado;
    public String segundoApellidoEmpleado;
}
