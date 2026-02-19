package com.rey.Stripe_Processing_Service.controller;

import com.rey.Stripe_Processing_Service.dto.CreatePaymentRequest;
import com.rey.Stripe_Processing_Service.dto.PaymentResponse;
import com.rey.Stripe_Processing_Service.service.ServiceInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/payments/")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

        private final ServiceInterface serviceInterface;

        @PostMapping("/create-payment")
    public ResponseEntity<PaymentResponse> createPayment(@RequestBody CreatePaymentRequest paymentRequest){
        log.info("Request made to create Payment: {}",paymentRequest);
            PaymentResponse paymentResponse = serviceInterface.makePayment(paymentRequest);
        log.info("Payment Response: {}",paymentResponse);
        return new ResponseEntity<>(paymentResponse, HttpStatus.OK);
    }
}
