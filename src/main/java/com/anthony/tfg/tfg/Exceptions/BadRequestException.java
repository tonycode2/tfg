package com.anthony.tfg.tfg.Exceptions;

/**
 * Excepción lanzada cuando la solicitud del cliente contiene datos inválidos
 */
public class BadRequestException extends RuntimeException {
    
    public BadRequestException(String message) {
        super(message);
    }
    
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
