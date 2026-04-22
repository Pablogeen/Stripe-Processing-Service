package com.rey.Stripe_Processing_Service.controller;

import com.rey.Stripe_Processing_Service.dto.CreatePaymentRequest;
import com.rey.Stripe_Processing_Service.dto.PaymentResponse;
import com.rey.Stripe_Processing_Service.service.ServiceInterface;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Payment Processing", description = "Endpoints for processing and managing payments")
public class PaymentController {

    private final ServiceInterface serviceInterface;

    @Operation(
            summary = "Create Payment",
            description = "Creates a new payment request"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("create-payment/")
    public ResponseEntity<PaymentResponse> createPayment(
            @RequestBody @Valid CreatePaymentRequest paymentRequest) {
        log.info("Request made to create Payment: {}", paymentRequest);
        PaymentResponse paymentResponse = serviceInterface.makePayment(paymentRequest);
        log.info("Payment Response: {}", paymentResponse);
        return new ResponseEntity<>(paymentResponse, HttpStatus.OK);
    }

    @Operation(
            summary = "Initiate Payment",
            description = "Initiates a payment process using a transaction reference"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment initiated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid transaction reference"),
            @ApiResponse(responseCode = "404", description = "Transaction not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("{txnReference}/initiate/")
    public ResponseEntity<PaymentResponse> initiatePayment(
            @Parameter(description = "Unique transaction reference for the payment", required = true)
            @PathVariable String txnReference) {
        log.info("Request made to initiate Payment with txnReference: {}", txnReference);
        PaymentResponse paymentResponse = serviceInterface.initiatePayment(txnReference);
        log.info("Payment has been initiated Successfully: {}", paymentResponse);

        return new ResponseEntity<>(paymentResponse, HttpStatus.OK);
    }

    @Operation(
            summary = "Confirm Payment",
            description = "Confirms a previously initiated payment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment confirmed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid transaction reference"),
            @ApiResponse(responseCode = "404", description = "Transaction not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/{txnReference}/confirm-payment/")
    public ResponseEntity<PaymentResponse> confirmPayment(
            @Parameter(description = "Unique transaction reference for the payment", required = true)
            @PathVariable String txnReference) {
        log.info("Request made to confirm Payment with txnReference: {}", txnReference);
        PaymentResponse paymentResponse = serviceInterface.confirmPayment(txnReference);
        log.info("Payment has been confirmed Successfully: {}", paymentResponse);
        return new ResponseEntity<>(paymentResponse, HttpStatus.OK);
    }
}