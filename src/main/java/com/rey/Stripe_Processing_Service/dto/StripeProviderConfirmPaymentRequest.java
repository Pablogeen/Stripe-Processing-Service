package com.rey.Stripe_Processing_Service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class StripeProviderConfirmPaymentRequest {

    @JsonProperty("return_url")
    private String returnUrl;

    private String idempotencyKey;

}
