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
    public String observacionesEmpleado;
    public String urlDocumentoAdjunto;
    public String estadoSolicitud;
    public String tipoPermiso;
    
    // Fechas de auditoría
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date fechaSolicitud;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date fechaAprobacionJefe;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date fechaAprobacionRH;
    
    // Comentarios
    public String comentariosJefe;
    public String comentariosRH;
    
    // Empleado solicitante
    public String nombreEmpleado;
    public String primerApellidoEmpleado;
    public String segundApellidoEmpleado;
    
    // Aprobador jefe
    public String nombreAprobadorJefe;
    public String primerApellidoAprobadorJefe;
    public String segundoApellidoAprobadorJefe;
    
    // Aprobador RH
    public String nombreAprobadorRH;
    public String primerApellidoAprobadorRH;
    public String segundoApellidoAprobadorRH;
}
