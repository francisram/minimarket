package com.heladeria.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación de validación Bean Validation para verificar que un campo de texto contenga un RUC paraguayo
 * válido conforme al algoritmo oficial Módulo 11 de la DNIT (ex SET).
 */
@Documented
@Constraint(validatedBy = RucConstraintValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RucValido {

    String message() default "El RUC ingresado no es válido según el algoritmo Módulo 11 de la DNIT";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Si es true, el campo no puede ser nulo ni estar en blanco.
     * Si es false, valores nulos o vacíos se consideran válidos (campo opcional).
     */
    boolean obligatorio() default false;

    /**
     * Si es true, admite números de cédula sin guion para personas físicas particulares.
     * Si es false, exige estrictamente el formato "BASE-DV" con dígito verificador.
     */
    boolean permitirCedulaSinDv() default false;
}
