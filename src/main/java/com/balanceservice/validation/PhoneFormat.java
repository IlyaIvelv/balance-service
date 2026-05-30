package com.balanceservice.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneFormatValidator.class)
@Documented
public @interface PhoneFormat {
    String message() default "Phone must match format: 79207865432";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}