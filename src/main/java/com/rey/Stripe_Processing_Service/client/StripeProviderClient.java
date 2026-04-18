package com.rey.Stripe_Processing_Service.client;

import com.rey.Stripe_Processing_Service.dto.StripeProviderConfirmPaymentRequest;
import com.rey.Stripe_Processing_Service.dto.StripeProviderCreateOrderRequest;
import com.rey.Stripe_Processing_Service.dto.StripeProviderOrderResponse;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "STRIPE-PROVIDER-SERVICE")
public interface StripeProviderClient {

      @PostMapping("/api/v1/payments/create-order/")
      StripeProviderOrderResponse createStripeOrder(@RequestBody
                                                          @Valid StripeProviderCreateOrderRequest requestDto);

      @PostMapping("/api/v1/payments/{providerReference}/confirm-order")
     StripeProviderOrderResponse confirmOrder(@PathVariable String providerReference,
                                                   @RequestBody StripeProviderConfirmPaymentRequest orderRequest);

}
