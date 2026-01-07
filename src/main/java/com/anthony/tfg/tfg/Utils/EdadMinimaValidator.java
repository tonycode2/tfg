package com.anthony.tfg.tfg.Utils;

import java.sql.Date;
import java.time.LocalDate;
import java.time.Period;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EdadMinimaValidator implements ConstraintValidator<EdadMinima, Date> {

    private int edadMinima;

    @Override
    public void initialize(EdadMinima constraintAnnotation) {
        this.edadMinima = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(Date value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Dejar que @NotNull maneje la validación de nulidad
        }

        LocalDate fechaNacimiento = value.toLocalDate();
        LocalDate hoy = LocalDate.now();
        
        int edad = Period.between(fechaNacimiento, hoy).getYears();
        
        return edad >= edadMinima;
    }
}
