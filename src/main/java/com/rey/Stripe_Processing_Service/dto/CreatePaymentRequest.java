package com.rey.Stripe_Processing_Service.dto;


import com.rey.Stripe_Processing_Service.helper.ValidCurrency;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreatePaymentRequest {

    @NotNull(message = "userId is required")
    @Min(value = 1, message = "userId must be greater than 0")
    private Integer userId;

    @NotNull(message = "paymentMethodId is required")
    @Min(value = 1, message = "paymentMethodId must be greater than 0")
    @Max(value = 999999, message = "paymentMethodId is invalid")
    private Integer paymentMethodId;

    @NotNull(message = "providerId is required")
    @Min(value = 1, message = "providerId must be greater than 0")
    @Max(value = 999999, message = "providerId is invalid")
    private Integer providerId;

    @NotNull(message = "paymentTypeId is required")
    @Min(value = 1, message = "paymentTypeId must be greater than 0")
    @Max(value = 999999, message = "paymentTypeId is invalid")
    private Integer paymentTypeId;

    @NotNull(message = "amount is required")
    @Positive(message = "amount must be greater than zero")
    @Max(value = 99999999, message = "amount exceeds maximum allowed limit") // adjust to your business rule
    private Integer amount;

    @NotBlank(message = "currency is required")
    @ValidCurrency(message = "CURRENCY NOT SUPPORTED")
    private String currency;
}
