package com.anthony.tfg.tfg.DTOs.Solicitud;

import jakarta.validation.constraints.Size;

public class AccionIncapacidadDTO {
    
    @Size(max = 500, message = "Los comentarios no pueden exceder 500 caracteres")
    public String comentarios; // Opcional para aprobaciones, requerido para rechazos
}
