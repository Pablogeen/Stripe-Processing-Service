package com.rey.Stripe_Processing_Service.controller;

import com.rey.Stripe_Processing_Service.dto.CreatePaymentRequest;
import com.rey.Stripe_Processing_Service.dto.PaymentResponse;
import com.rey.Stripe_Processing_Service.service.ServiceInterface;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/payments/")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

        private final ServiceInterface serviceInterface;

        @PostMapping("create-payment/")
    public ResponseEntity<PaymentResponse> createPayment(@RequestBody @Valid CreatePaymentRequest paymentRequest){
        log.info("Request made to create Payment: {}",paymentRequest);
            PaymentResponse paymentResponse = serviceInterface.makePayment(paymentRequest);
        log.info("Payment Response: {}",paymentResponse);
        return new ResponseEntity<>(paymentResponse, HttpStatus.OK);
    }


    @PostMapping("{txnReference}/initiate/")
    public ResponseEntity<PaymentResponse> initiatePayment(@PathVariable String txnReference){
            log.info("Request made to initiate Payment with txnReference: {}",txnReference);
            PaymentResponse paymentResponse = serviceInterface.initiatePayment(txnReference);
            log.info("Payment has been initiated Successfully: {}",paymentResponse);
            return new ResponseEntity<>(paymentResponse, HttpStatus.OK);
    }

    @PostMapping("/{txnReference}/confirm-payment/")
    public ResponseEntity<PaymentResponse> confirmPayment(@PathVariable String txnReference){
        log.info("Request made to confirm Payment with txnReference: {}",txnReference);
        PaymentResponse paymentResponse = serviceInterface.confirmPayment(txnReference);
        log.info("Payment has been confirmed Successfully: {}",paymentResponse);
        return new ResponseEntity<>(paymentResponse, HttpStatus.OK);
    }

}
