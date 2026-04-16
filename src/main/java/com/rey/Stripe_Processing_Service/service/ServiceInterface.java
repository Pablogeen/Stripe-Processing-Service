package com.rey.Stripe_Processing_Service.service;

import com.rey.Stripe_Processing_Service.dto.CreatePaymentRequest;
import com.rey.Stripe_Processing_Service.dto.PaymentResponse;
import org.springframework.stereotype.Service;

@Service
public interface ServiceInterface {
    PaymentResponse makePayment(CreatePaymentRequest paymentRequest);

    PaymentResponse initiatePayment(String txnReference);
}
