package com.aviation.routeprovider.api.rest.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;

public class OperatingDaysValidator implements ConstraintValidator<ValidOperatingDays, int[]> {
    
    private static final int MIN_DAY = 1;
    private static final int MAX_DAY = 7;
    
    @Override
    public void initialize(ValidOperatingDays constraintAnnotation) {
    }
    
    @Override
    public boolean isValid(int[] operatingDays, ConstraintValidatorContext context) {
        if (operatingDays == null) {
            return true;
        }
        
        for (int day : operatingDays) {
            if (day < MIN_DAY || day > MAX_DAY) {
                return false;
            }
        }
        
        return true;
    }
}
