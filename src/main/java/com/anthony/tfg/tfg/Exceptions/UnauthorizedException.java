package com.anthony.tfg.tfg.Exceptions;

/**
 * Excepción lanzada cuando el usuario no está autenticado
 */
public class UnauthorizedException extends RuntimeException {
    
    public UnauthorizedException(String message) {
        super(message);
    }
    
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
