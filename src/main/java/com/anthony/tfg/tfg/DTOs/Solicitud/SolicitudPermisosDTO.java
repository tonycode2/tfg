package com.anthony.tfg.tfg.DTOs.Solicitud;

import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class SolicitudPermisosDTO {
    public Long id;
    @NotNull
    @Future
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date fechaInicio;
    @NotNull
    @Future
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date fechaFin;
    @NotNull
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
    @NotNull
    public Long idEmpleado;
}
