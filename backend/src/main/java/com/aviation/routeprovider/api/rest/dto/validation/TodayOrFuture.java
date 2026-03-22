package com.aviation.routeprovider.api.rest.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = TodayOrFutureValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface TodayOrFuture {
    
    String message() default "Date must be today or in the future";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
