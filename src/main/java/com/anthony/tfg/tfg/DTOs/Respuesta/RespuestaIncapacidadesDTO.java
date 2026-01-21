package com.anthony.tfg.tfg.DTOs.Respuesta;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

public class RespuestaIncapacidadesDTO {
    
    public Long id;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public LocalDate fechaInicio;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public LocalDate fechaFin;
    
    public Integer diasTotales;
    
    public String tipoIncapacidad;
    
    public String estadoSolicitud;
    
    public Double porcentajePago;
    
    public String entidadEmisora;
    
    public String numeroDocumento;
    
    public String observaciones;
    
    public String urlDocumentoAdjunto;
    
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
    
    // Campos de extensión
    public Boolean esExtension;
    
    public Long idIncapacidadOriginal;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public LocalDate fechaFinOriginal;
    
    public String comentariosExtension;
    
    // Empleado solicitante
    public Long idEmpleado;
    
    public String nombreEmpleado;
    
    public String primerApellidoEmpleado;
    
    public String segundoApellidoEmpleado;
    
    public String departamentoEmpleado;
    
    // Aprobador jefe
    public String nombreAprobadorJefe;
    
    public String primerApellidoAprobadorJefe;
    
    public String segundoApellidoAprobadorJefe;
    
    // Aprobador RH
    public String nombreAprobadorRH;
    
    public String primerApellidoAprobadorRH;
    
    public String segundoApellidoAprobadorRH;
}
