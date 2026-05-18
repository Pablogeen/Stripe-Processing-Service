package com.rey.Stripe_Processing_Service.helper;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CurrencyValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCurrency {
    String message() default "currency is not supported";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}