package com.anthony.tfg.tfg.Util;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EdadMinimaValidator.class)
@Documented
public @interface EdadMinima {
    String message() default "La edad mínima requerida es de {value} años";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    int value() default 18;
}
