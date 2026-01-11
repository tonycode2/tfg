package com.anthony.tfg.tfg.DTOs.Solicitud;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public class SolicitudPermisosDTO {
    public Long id;
    @NotNull
    @Future(message = "La fecha de inicio debe ser futura")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public LocalDate fechaInicio;
    @NotNull
    @Future(message = "La fecha de fin debe ser futura")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public LocalDate fechaFin;
    @NotNull
    @PositiveOrZero(message = "Los días deben ser 0 o un valor positivo")
    public Integer diasTotales;
    public String unidadTiempo; // DIAS o HORAS
    public String horaInicio; // Formato HH:mm (opcional, solo para permisos por horas)
    public String horaFin; // Formato HH:mm (opcional, solo para permisos por horas)
    public Double totalHoras; // Total de horas (opcional, calculado para permisos por horas)
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
