package com.rey.Stripe_Processing_Service.helper;

import com.rey.Stripe_Processing_Service.client.StripeProviderClient;
import com.rey.Stripe_Processing_Service.constants.ErrorCodeEnum;
import com.rey.Stripe_Processing_Service.dto.StripeProviderCreateOrderRequest;
import com.rey.Stripe_Processing_Service.dto.StripeProviderOrderResponse;
import com.rey.Stripe_Processing_Service.entity.Transaction;
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

    Transaction transaction = new Transaction();

    @CircuitBreaker(name = "stripeProvider", fallbackMethod = "createOrderFallBack")
    public StripeProviderOrderResponse makeCreateOrderCall(StripeProviderCreateOrderRequest orderRequest) {
        log.info("About to make call to Provider Service to create order {}", orderRequest);


        try {
            StripeProviderOrderResponse orderResponse = providerClient.createStripeOrder(orderRequest);
            log.info("Provider Service has created order successfully");
            return orderResponse;

        }catch(FeignException.BadRequest ex){
            transaction.setErrorCode(ErrorCodeEnum.INVALID_REQUEST_ERROR.getErrorCode());
            transaction.setErrorMessage(ErrorCodeEnum.INVALID_REQUEST_ERROR.getErrorMessage());
            throw new StripeProcessingException(ErrorCodeEnum.INVALID_REQUEST.getErrorCode(),
                    ErrorCodeEnum.INVALID_REQUEST.getErrorMessage(),
                    HttpStatus.BAD_REQUEST);

        }catch (FeignException.FeignServerException ex){
            transaction.setErrorCode(ErrorCodeEnum.STRIPE_PROVIDER_SERVICE_UNAVAILABLE.getErrorCode());
            transaction.setErrorMessage(ErrorCodeEnum.STRIPE_PROVIDER_SERVICE_UNAVAILABLE.getErrorMessage());
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
