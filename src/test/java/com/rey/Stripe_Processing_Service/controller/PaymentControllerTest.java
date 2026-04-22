package com.rey.Stripe_Processing_Service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rey.Stripe_Processing_Service.dto.CreatePaymentRequest;
import com.rey.Stripe_Processing_Service.dto.PaymentResponse;
import com.rey.Stripe_Processing_Service.service.ServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ServiceInterface serviceInterface;

    private CreatePaymentRequest createPaymentRequest;
    private PaymentResponse paymentResponse;

    @BeforeEach
    void setUp() {
        createPaymentRequest = new CreatePaymentRequest();
        createPaymentRequest.setUserId(1);
        createPaymentRequest.setPaymentMethodId(1);
        createPaymentRequest.setProviderId(1);
        createPaymentRequest.setPaymentTypeId(1);
        createPaymentRequest.setAmount(10000);
        createPaymentRequest.setCurrency("USD");

        paymentResponse = new PaymentResponse();
        paymentResponse.setTxnReference("test-txn-ref");
    }

    // POST /v1/payments/create-payment/

    @Test
    @DisplayName("createPayment: should return 200 and payment response on success")
    void createPayment_success() throws Exception {
        when(serviceInterface.makePayment(any(CreatePaymentRequest.class)))
                .thenReturn(paymentResponse);

        mockMvc.perform(post("/v1/payments/create-payment/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPaymentRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.txnReference").value("test-txn-ref"));

        verify(serviceInterface).makePayment(any(CreatePaymentRequest.class));
    }

    @Test
    @DisplayName("createPayment: should return 400 when request body is invalid")
    void createPayment_invalidRequest_returns400() throws Exception {
        // Send empty body to trigger @Valid failure
        mockMvc.perform(post("/v1/payments/create-payment/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // POST /v1/payments/{txnReference}/initiate/

    @Test
    @DisplayName("initiatePayment: should return 200 and payment response on success")
    void initiatePayment_success() throws Exception {
        String txnReference = "test-txn-ref";
        when(serviceInterface.initiatePayment(eq(txnReference)))
                .thenReturn(paymentResponse);

        mockMvc.perform(post("/v1/payments/{txnReference}/initiate/", txnReference))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.txnReference").value("test-txn-ref"));

        verify(serviceInterface).initiatePayment(txnReference);
    }

    // POST /v1/payments/{txnReference}/confirm-payment/

    @Test
    @DisplayName("confirmPayment: should return 200 and payment response on success")
    void confirmPayment_success() throws Exception {
        String txnReference = "test-txn-ref";
        when(serviceInterface.confirmPayment(eq(txnReference)))
                .thenReturn(paymentResponse);

        mockMvc.perform(post("/v1/payments/{txnReference}/confirm-payment/", txnReference))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.txnReference").value("test-txn-ref"));

        verify(serviceInterface).confirmPayment(txnReference);
    }
}