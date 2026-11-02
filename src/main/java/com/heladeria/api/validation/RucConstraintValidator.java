package com.heladeria.api.validation;

import com.heladeria.api.util.RucValidator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validador de restricción para la anotación {@link RucValido}.
 * Aplica el algoritmo oficial Módulo 11 de la DNIT delegando en {@link RucValidator}.
 */
public class RucConstraintValidator implements ConstraintValidator<RucValido, String> {

    private boolean obligatorio;
    private boolean permitirCedulaSinDv;

    @Override
    public void initialize(RucValido constraintAnnotation) {
        this.obligatorio = constraintAnnotation.obligatorio();
        this.permitirCedulaSinDv = constraintAnnotation.permitirCedulaSinDv();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return !obligatorio;
        }

        return RucValidator.esValido(value, permitirCedulaSinDv);
    }
}
