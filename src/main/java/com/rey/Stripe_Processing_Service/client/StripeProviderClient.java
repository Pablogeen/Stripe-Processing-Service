package com.rey.Stripe_Processing_Service.client;

import com.rey.Stripe_Processing_Service.dto.StripeProviderCreateOrderRequest;
import com.rey.Stripe_Processing_Service.dto.StripeProviderCreateOrderResponse;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "STRIPE-PROVIDER-SERVICE")
public interface StripeProviderClient {

      @PostMapping("/api/v1/payments/create-order/")
      StripeProviderCreateOrderResponse createStripeOrder(@RequestBody
                                                          @Valid StripeProviderCreateOrderRequest requestDto);


     //StripeConfirmOrderResponse confirmOrder(@PathVariable String txnProviderReference,
              //                                     @RequestBody @Valid StripeConfirmOrderRequest orderRequest);
}
