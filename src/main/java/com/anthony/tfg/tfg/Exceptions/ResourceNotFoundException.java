package com.anthony.tfg.tfg.Exceptions;

/**
 * Excepción lanzada cuando un recurso solicitado no se encuentra en la base de datos
 */
public class ResourceNotFoundException extends RuntimeException {
    
    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;
    
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s no encontrado con %s: '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
    
    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceName = null;
        this.fieldName = null;
        this.fieldValue = null;
    }
    
    /** 
     * @return String
     */
    public String getResourceName() {
        return resourceName;
    }
    
    /** 
     * @return String
     */
    public String getFieldName() {
        return fieldName;
    }
    
    /** 
     * @return Object
     */
    public Object getFieldValue() {
        return fieldValue;
    }
}
