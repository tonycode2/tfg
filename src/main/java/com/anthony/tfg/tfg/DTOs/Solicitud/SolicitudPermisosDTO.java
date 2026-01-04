package com.anthony.tfg.tfg.DTOs.Solicitud;

import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class SolicitudPermisosDTO {
    @NotBlank
    public Long id;
    @NotBlank
    @Future
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date fechaInicio;
    @NotBlank
    @Future
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date fechaFin;
    @NotBlank
    @Positive
    public Integer diasTotales;
    @NotBlank
    @Size(min = 10, max = 500)
    public String motivo;
    @NotBlank
    @Size(min = 5, max = 200)
    public String urlDocumentoAdjunto;
    @NotBlank
    public String estadoSolicitud;
    @NotBlank
    public String tipoPermiso;
    @NotBlank
    public Long idEmpleado;
}
