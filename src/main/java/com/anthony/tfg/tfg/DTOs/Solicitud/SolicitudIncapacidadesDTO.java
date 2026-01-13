package com.anthony.tfg.tfg.DTOs.Solicitud;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public class SolicitudIncapacidadesDTO {
    
    public Long id;
    
    @NotNull(message = "La fecha de inicio es requerida")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public LocalDate fechaInicio;
    
    @NotNull(message = "La fecha de fin es requerida")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public LocalDate fechaFin;
    
    @NotNull(message = "Los días totales son requeridos")
    @PositiveOrZero(message = "Los días deben ser 0 o un valor positivo")
    public Integer diasTotales;
    
    @NotBlank(message = "El tipo de incapacidad es requerido")
    public String tipoIncapacidad; // ENFERMEDAD_COMUN, ACCIDENTE_LABORAL, etc.
    
    @NotNull(message = "El porcentaje de pago es requerido")
    @Min(value = 0, message = "El porcentaje de pago debe ser al menos 0")
    @Max(value = 100, message = "El porcentaje de pago no puede ser mayor a 100")
    public Double porcentajePago;
    
    @NotBlank(message = "La entidad emisora es requerida")
    public String entidadEmisora; // CCSS, INS, CLINICA_PRIVADA, OTRO
    
    @Size(max = 100, message = "El número de documento no puede exceder 100 caracteres")
    public String numeroDocumento;
    
    @Size(max = 1000, message = "Las observaciones no pueden exceder 1000 caracteres")
    public String observaciones;
    
    @Size(max = 500, message = "La URL del documento no puede exceder 500 caracteres")
    public String urlDocumentoAdjunto;
    
    @NotNull(message = "El ID del empleado es requerido")
    public Long idEmpleado;
}

