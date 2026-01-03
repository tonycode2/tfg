package com.anthony.tfg.tfg.DTOs.Solicitud;

import java.sql.Date;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class SolicitudHorasExtraDTO {
    @NotBlank
    Long id;

    @NotBlank
    Date fechaSolicitud;

    @NotBlank
    @Positive
    Integer cantidadDeHoras;

    @NotBlank
    @Size(min = 5, max = 200)
    String motivo;

    @NotBlank
    Boolean aprobado;

    @NotBlank
    Boolean procesado;

    @NotBlank
    String estadoSolicitud;

    @NotBlank
    String tipoTarifa;

    @NotBlank
    Long idEmpleado;
}
