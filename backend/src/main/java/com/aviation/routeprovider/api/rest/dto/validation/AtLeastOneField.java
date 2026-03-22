package com.aviation.routeprovider.api.rest.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validation annotation to ensure at least one field is not null/empty.
 * Used for update request DTOs to prevent no-op updates.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AtLeastOneFieldValidator.class)
@Documented
public @interface AtLeastOneField {
    
    String message() default "At least one field must be provided for update";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};

    String[] fields() default {};
}
