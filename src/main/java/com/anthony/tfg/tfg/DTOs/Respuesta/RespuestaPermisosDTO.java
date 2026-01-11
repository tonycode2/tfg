package com.anthony.tfg.tfg.DTOs.Respuesta;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

public class RespuestaPermisosDTO {
    public Long id;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public LocalDate fechaInicio;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public LocalDate fechaFin;
    public Integer diasTotales;
    public String unidadTiempo;
    public String horaInicio;
    public String horaFin;
    public Double totalHoras;
    public String motivo;
    public String observacionesEmpleado;
    public String urlDocumentoAdjunto;
    public String estadoSolicitud;
    public String tipoPermiso;
    
    // Fechas de auditoría
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public LocalDate fechaSolicitud;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public LocalDate fechaAprobacionJefe;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public LocalDate fechaAprobacionRH;
    
    // Comentarios
    public String comentariosJefe;
    public String comentariosRH;
    
    // Empleado solicitante
    public Long idEmpleado;
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
