package com.aviation.routeprovider.api.rest.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class TodayOrFutureValidator implements ConstraintValidator<TodayOrFuture, LocalDate> {
    
    @Override
    public void initialize(TodayOrFuture constraintAnnotation) {
    }
    
    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext context) {
        if (date == null) {
            return true;
        }
        
        return !date.isBefore(LocalDate.now());
    }
}
