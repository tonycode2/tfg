package com.anthony.tfg.tfg.DTOs.Respuesta;

/**
 * DTO inmutable para respuestas de direcciones.
 * Utiliza Java Record para mayor concisión e inmutabilidad.
 */
public record RespuestaDireccionDTO(
    Long id,
    String provincia,
    String canton,
    String distrito,
    String indicaciones
) {}
