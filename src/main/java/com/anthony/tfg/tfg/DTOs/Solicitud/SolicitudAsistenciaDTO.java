package com.anthony.tfg.tfg.DTOs.Solicitud;

import java.sql.Date;
import java.sql.Time;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class SolicitudAsistenciaDTO {
    @NotBlank
    Long id;

    @NotBlank
    Date fecha;

    @NotBlank
    Time horaEntrada;

    @NotBlank
    Time horaSalida;

    @NotBlank
    @Positive
    Double horasTrabajadas;

    @NotBlank
    @Positive
    Long idEmpleado;
}
