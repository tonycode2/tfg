package com.anthony.tfg.tfg.DTOs.Respuesta;

import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class RespuestaPermisosDTO {
    public Long id;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date fechaInicio;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date fechaFin;
    public Integer diasTotales;
    public String motivo;
    public String urlDocumentoAdjunto;
    public String estadoSolicitud;
    public String tipoPermiso;
    public String nombreEmpleado;
    public String primerApellidoEmpleado;
    public String segundApellidoEmpleado;
}
