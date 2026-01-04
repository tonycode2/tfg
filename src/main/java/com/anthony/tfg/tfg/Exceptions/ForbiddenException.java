package com.anthony.tfg.tfg.Exceptions;

/**
 * Excepción lanzada cuando el usuario no tiene permisos para realizar la operación
 */
public class ForbiddenException extends RuntimeException {
    
    public ForbiddenException(String message) {
        super(message);
    }
    
    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
