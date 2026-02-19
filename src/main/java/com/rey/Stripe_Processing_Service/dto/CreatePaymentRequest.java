package com.rey.Stripe_Processing_Service.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePaymentRequest {

    private int userId;
    private int paymentMethodId;
    private int providerId;
    private int paymentTypeId;
    private BigDecimal amount;
    private String currency;

}
