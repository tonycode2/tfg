package com.anthony.tfg.tfg.DTOs.Solicitud;

import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class SolicitudIncapacidadesDTO {
    public Long id;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date fechaInicio;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date fechaFin;

    @NotNull
    @Positive
    public Integer diasTotales;

    @NotBlank
    public String tipoIncapacidad;

    @NotBlank
    public String estadoSolicitud;

    @NotNull
    @Positive
    public Double porcentajePago;

    @NotBlank
    public String entidadEmisora;

    @Size(max = 100)
    public String numeroDocumento;

    @Size(max = 500)
    public String observaciones;

    @Size(max = 200)
    public String urlDocumentoAdjunto;

    @NotNull
    public Long idEmpleado;
}
