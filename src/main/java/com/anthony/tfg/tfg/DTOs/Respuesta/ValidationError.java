package com.anthony.tfg.tfg.DTOs.Respuesta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para detalles de errores de validación
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationError {
    
    private String field;
    
    private String message;
    
    private Object rejectedValue;
}
