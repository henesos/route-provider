package com.aviation.routeprovider.api.rest.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class AtLeastOneFieldValidator implements ConstraintValidator<AtLeastOneField, Object> {
    
    private Set<String> requiredFields;
    private String message;
    
    @Override
    public void initialize(AtLeastOneField constraintAnnotation) {
        this.requiredFields = new HashSet<>(Arrays.asList(constraintAnnotation.fields()));
        this.message = constraintAnnotation.message();
    }
    
    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        if (obj == null) {
            return false;
        }
        
        try {
            for (Field field : obj.getClass().getDeclaredFields()) {
                if (!requiredFields.isEmpty() && !requiredFields.contains(field.getName())) {
                    continue;
                }
                
                field.setAccessible(true);
                Object value = field.get(obj);
                
                if (value != null) {
                    // For arrays, check if non-empty
                    if (value.getClass().isArray()) {
                        // FIX [ISSUE 3]: Use type-safe Array.getLength() instead of unsafe cast to int[]
                        return Array.getLength(value) > 0;
                    } else if (value instanceof String str && !str.isBlank()) {
                        return true;
                    } else if (!(value instanceof String)) {
                        // Non-string, non-null value
                        return true;
                    }
                }
            }
        } catch (IllegalAccessException e) {
            return false;
        }
        
        // Use the message from the annotation (FIX: was previously hardcoded)
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
            .addConstraintViolation();
        
        return false;
    }
}
