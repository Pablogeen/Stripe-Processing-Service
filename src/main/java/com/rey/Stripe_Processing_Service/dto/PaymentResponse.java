package com.rey.Stripe_Processing_Service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentResponse {

    private String txnReference;
    private String status;
    private String providerReference;
}