package com.rey.Stripe_Processing_Service.dto;


import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreatePaymentRequest {

    @NotNull(message = "userId is required")
    @Min(value = 1, message = "userId must be greater than 0")
    private Integer userId;

    @NotNull(message = "paymentMethodId is required")
    @Min(value = 1, message = "paymentMethodId must be greater than 0")
    private Integer paymentMethodId;

    @NotNull(message = "providerId is required")
    @Min(value = 1, message = "providerId must be greater than 0")
    private Integer providerId;

    @NotNull(message = "paymentTypeId is required")
    @Min(value = 1, message = "paymentTypeId must be greater than 0")
    private Integer paymentTypeId;

    @NotNull(message = "amount is required")
    private Integer amount;

    @NotBlank(message = "currency is required")
    @Size(min = 3, max = 3, message = "currency must be a 3-letter ISO code")
    private String currency;
}
