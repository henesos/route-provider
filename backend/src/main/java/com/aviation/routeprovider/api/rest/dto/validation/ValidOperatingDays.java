package com.aviation.routeprovider.api.rest.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = OperatingDaysValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidOperatingDays {
    
    String message() default "Operating days must contain values between 1 (Monday) and 7 (Sunday)";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
