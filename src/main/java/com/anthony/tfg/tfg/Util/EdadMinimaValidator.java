package com.anthony.tfg.tfg.Util;

import java.time.LocalDate;
import java.time.Period;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EdadMinimaValidator implements ConstraintValidator<EdadMinima, LocalDate> {

    private int edadMinima;

    /** 
     * @param constraintAnnotation
     */
    @Override
    public void initialize(EdadMinima constraintAnnotation) {
        this.edadMinima = constraintAnnotation.value();
    }

    /** 
     * @param value
     * @param context
     * @return boolean
     */
    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Dejar que @NotNull maneje la validación de nulidad
        }

        LocalDate hoy = LocalDate.now();
        
        int edad = Period.between(value, hoy).getYears();
        
        return edad >= edadMinima;
    }
}
