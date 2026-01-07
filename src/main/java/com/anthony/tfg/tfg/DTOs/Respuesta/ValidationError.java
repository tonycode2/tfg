package com.anthony.tfg.tfg.DTOs.Respuesta;

/**
 * DTO inmutable para detalles de errores de validación.
 * Utiliza Java Record para mayor concisión e inmutabilidad.
 */
public record ValidationError(
    String field,
    String message,
    Object rejectedValue
) {}
