package com.rey.Stripe_Processing_Service.helper;

import com.rey.Stripe_Processing_Service.client.StripeProviderClient;
import com.rey.Stripe_Processing_Service.constants.ErrorCodeEnum;
import com.rey.Stripe_Processing_Service.dto.StripeProviderConfirmPaymentRequest;
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
public class ConfirmPaymentHelper {

    private final StripeProviderClient providerClient;

    Transaction transaction = new Transaction();

    @CircuitBreaker(name = "stripeProvider", fallbackMethod = "confirmOrderFallBack")
    public StripeProviderOrderResponse makeConfirmOrderCall(
                        String providerReference, StripeProviderConfirmPaymentRequest paymentRequest){
        log.info("About to make call to Provider service to confirm Order: ");

        try {
            log.info("About to confirm order. providerReference={} request={}", providerReference, paymentRequest);
            StripeProviderOrderResponse orderResponse = providerClient.confirmOrder(providerReference, paymentRequest);
            log.info("Provider Service has created order successfully");
            return orderResponse;

        }catch(FeignException.BadRequest ex){
            transaction.setErrorCode(ErrorCodeEnum.INVALID_REQUEST_ERROR.getErrorCode());
            transaction.setErrorMessage(ex.contentUTF8());
            throw new StripeProcessingException(ErrorCodeEnum.INVALID_REQUEST.getErrorCode(),
                    ex.contentUTF8(),
                    HttpStatus.BAD_REQUEST);



        }catch (FeignException.FeignServerException ex){
            transaction.setErrorCode(ErrorCodeEnum.STRIPE_PROVIDER_SERVICE_UNAVAILABLE.getErrorCode());
            transaction.setErrorMessage(ex.contentUTF8());
            throw new StripeProcessingException(ErrorCodeEnum.STRIPE_PROVIDER_SERVICE_UNAVAILABLE.getErrorCode(),
                    ex.contentUTF8(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void confirmOrderFallBack(Throwable throwable) {
        log.error("CircuitBreaker fallback: There's a problem with Provider Service: {}"
                ,throwable.getMessage().toString());

        throw new StripeProcessingException(
                ErrorCodeEnum.STRIPE_PROVIDER_SERVICE_UNAVAILABLE.getErrorCode(),
                ErrorCodeEnum.STRIPE_PROVIDER_SERVICE_UNAVAILABLE.getErrorMessage(),
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }

}
