package com.anthony.tfg.tfg.DTOs.Respuesta;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

public class RespuestaHorasExtraDTO {
    public Long id;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public LocalDate fechaSolicitud;
    public Integer cantidadDeHoras;
    public String motivo;
    public Boolean aprobado;
    public Boolean procesado;
    public String estadoSolicitud;
    public String tipoTarifa;
    public Long idEmpleado;
    public String nombreEmpleado;
    public String primerApellidoEmpleado;
    public String segundoApellidoEmpleado;
}
