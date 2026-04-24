package com.rey.Stripe_Processing_Service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StripeProviderCreateOrderRequest {



    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.50", inclusive = true, message = "Amount must be at least 0.50")
    @DecimalMax(value = "999999.99", inclusive = true, message = "Amount cannot exceed 999,999.99")
    @Digits(integer = 6, fraction = 2, message = "Amount must have up to 6 digits and 2 decimal places")
    @Positive(message = "Amount must be positive")
    private Integer amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    @Pattern(regexp = "^[a-zA-Z]{3}$", message = "Currency must contain only letters")
    private String currency;

    private String idempotencyKey;

}
