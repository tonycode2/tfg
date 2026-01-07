package com.anthony.tfg.tfg.DTOs.Respuesta;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO inmutable para respuestas de error estandarizadas.
 * Utiliza Java Record para mayor concisión e inmutabilidad.
 */
public record ErrorResponse(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    String path,
    List<ValidationError> errors
) {}
