package com.anthony.tfg.tfg.DTOs.Respuesta;

import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class RespuestaIncapacidadesDTO {
    public Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date fechaInicio;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date fechaFin;

    public Integer diasTotales;
    public String tipoIncapacidad;
    public String estadoSolicitud;
    public Double porcentajePago;
    public String entidadEmisora;
    public String numeroDocumento;
    public String observaciones;
    public String urlDocumentoAdjunto;
    public String nombreEmpleado;
    public String primerApellidoEmpleado;
    public String segundoApellidoEmpleado;
}
