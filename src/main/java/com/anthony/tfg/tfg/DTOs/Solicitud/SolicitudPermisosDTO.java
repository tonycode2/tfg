package com.anthony.tfg.tfg.DTOs.Solicitud;

import java.sql.Date;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class SolicitudPermisosDTO {
    @NotBlank
    Long id;
    @NotBlank
    @Future
    Date fechaInicio;
    @NotBlank
    @Future
    Date fechaFin;
    @NotBlank
    @Positive
    Integer diasTotales;
    @NotBlank
    @Size(min = 10, max = 500)
    String motivo;
    @NotBlank
    @Size(min = 5, max = 200)
    String urlDocumentoAdjunto;
    @NotBlank
    String estadoSolicitud;
    @NotBlank
    String tipoPermiso;
    @NotBlank
    Long idEmpleado;
}
