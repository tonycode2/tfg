package com.anthony.tfg.tfg.DTOs.Respuesta;

import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class RespuestaHorasExtraDTO {
    public Long id;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date fechaSolicitud;
    public Integer cantidadDeHoras;
    public String motivo;
    public Boolean aprobado;
    public Boolean procesado;
    public String estadoSolicitud;
    public String tipoTarifa;
    public String nombreEmpleado;
    public String primerApellidoEmpleado;
    public String segundoApellidoEmpleado;
}
