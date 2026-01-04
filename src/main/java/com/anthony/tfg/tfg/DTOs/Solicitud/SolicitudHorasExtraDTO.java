package com.anthony.tfg.tfg.DTOs.Solicitud;

import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class SolicitudHorasExtraDTO {
    @NotBlank
    public Long id;

    @NotBlank
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date fechaSolicitud;

    @NotBlank
    @Positive
    public Integer cantidadDeHoras;

    @NotBlank
    @Size(min = 5, max = 200)
    public String motivo;

    @NotBlank
    public Boolean aprobado;

    @NotBlank
    public Boolean procesado;

    @NotBlank
    public String estadoSolicitud;

    @NotBlank
    public String tipoTarifa;

    @NotBlank
    public Long idEmpleado;
}
