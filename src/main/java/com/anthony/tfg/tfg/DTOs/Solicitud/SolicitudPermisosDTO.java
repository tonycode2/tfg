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
    @Future(message = "La fecha de inicio debe ser futura")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date fechaInicio;
    @NotNull
    @Future(message = "La fecha de fin debe ser futura")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date fechaFin;
    @NotNull
    @Positive
    public Integer diasTotales;
    @NotBlank
    @Size(min = 10, max = 500, message = "El motivo debe tener entre 10 y 500 caracteres")
    public String motivo;
    // Observaciones opcionales del empleado (pueden agregarse al crear o posteriormente)
    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    public String observacionesEmpleado;
    @Size(max = 200, message = "La URL del documento no puede exceder 200 caracteres")
    public String urlDocumentoAdjunto; // Opcional
    @NotBlank
    public String tipoPermiso;
    @NotNull
    public Long idEmpleado;
}
