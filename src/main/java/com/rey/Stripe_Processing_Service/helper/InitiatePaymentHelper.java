package com.rey.Stripe_Processing_Service.helper;

import com.rey.Stripe_Processing_Service.client.StripeProviderClient;
import com.rey.Stripe_Processing_Service.constants.ErrorCodeEnum;
import com.rey.Stripe_Processing_Service.dto.StripeProviderCreateOrderRequest;
import com.rey.Stripe_Processing_Service.dto.StripeProviderCreateOrderResponse;
import com.rey.Stripe_Processing_Service.exception.StripeProcessingException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InitiatePaymentHelper {

    private final StripeProviderClient providerClient;

    @CircuitBreaker(name = "stripeProvider", fallbackMethod = "createOrderFallBack")
    public StripeProviderCreateOrderResponse makeCreateOrderCall(StripeProviderCreateOrderRequest orderRequest) {
        log.info("About to make call to Provider Service to create order {}", orderRequest);


        try {
            StripeProviderCreateOrderResponse orderResponse = providerClient.createStripeOrder(orderRequest);
            log.info("Provider Service has created order successfully");
            return orderResponse;

        }catch(FeignException.BadRequest ex){
            throw new StripeProcessingException(ErrorCodeEnum.INVALID_REQUEST.getErrorCode(),
                    ErrorCodeEnum.INVALID_REQUEST.getErrorMessage(),
                    HttpStatus.BAD_REQUEST);

        }catch (FeignException.FeignServerException ex){
            throw new StripeProcessingException(ErrorCodeEnum.STRIPE_PROVIDER_SERVICE_UNAVAILABLE.getErrorCode(),
                    ErrorCodeEnum.STRIPE_PROVIDER_SERVICE_UNAVAILABLE.getErrorMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

        public void createOrderFallBack(Throwable throwable) {
            log.error("CircuitBreaker fallback: There's a problem with Provider Service: {}"
                    ,throwable.getMessage());

            throw new StripeProcessingException(
                    ErrorCodeEnum.STRIPE_PROVIDER_SERVICE_UNAVAILABLE.getErrorCode(),
                    ErrorCodeEnum.STRIPE_PROVIDER_SERVICE_UNAVAILABLE.getErrorMessage(),
                    HttpStatus.SERVICE_UNAVAILABLE
            );
        }
}
