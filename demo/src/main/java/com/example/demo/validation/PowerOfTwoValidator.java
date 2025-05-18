package com.example.demo.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PowerOfTwoValidator implements ConstraintValidator<PowerOfTwo, Integer> {

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // @NotNull should handle null values
        }
        return (value & (value - 1)) == 0;
    }
}