package com.anthony.tfg.tfg.Exceptions;

/**
 * Excepción lanzada cuando hay un conflicto con el estado actual del recurso
 * Por ejemplo: duplicados, violaciones de integridad, etc.
 */
public class ConflictException extends RuntimeException {
    
    public ConflictException(String message) {
        super(message);
    }
    
    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
