package com.rey.Stripe_Processing_Service.helper;

import com.rey.Stripe_Processing_Service.client.StripeProviderClient;
import com.rey.Stripe_Processing_Service.constants.ErrorCodeEnum;
import com.rey.Stripe_Processing_Service.dto.StripeProviderCreateOrderRequest;
import com.rey.Stripe_Processing_Service.dto.StripeProviderOrderResponse;
import com.rey.Stripe_Processing_Service.entity.TransactionStatus;
import com.rey.Stripe_Processing_Service.exception.StripeProcessingException;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InitiatePaymentHelperTest {

    @Mock
    private StripeProviderClient providerClient;

    @InjectMocks
    private InitiatePaymentHelper initiatePaymentHelper;

    private StripeProviderCreateOrderRequest orderRequest;

    // Helper to build a dummy Feign Request (required to construct FeignExceptions)
    private Request dummyRequest() {
        return Request.create(
                Request.HttpMethod.POST,
                "/test",
                Collections.emptyMap(),
                null,
                StandardCharsets.UTF_8,
                new RequestTemplate()
        );
    }

    @BeforeEach
    void setUp() {
        orderRequest = new StripeProviderCreateOrderRequest();
        orderRequest.setAmount(10000);
        orderRequest.setCurrency("USD");
    }

    // makeCreateOrderCall — happy path

    @Test
    @DisplayName("makeCreateOrderCall: should return order response on success")
    void makeCreateOrderCall_success() {
        StripeProviderOrderResponse expectedResponse = new StripeProviderOrderResponse();
        expectedResponse.setId("stripe-order-id");
        expectedResponse.setClientSecret("pi_secret_abc");

        when(providerClient.createStripeOrder(orderRequest)).thenReturn(expectedResponse);

        StripeProviderOrderResponse result = initiatePaymentHelper.makeCreateOrderCall(orderRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("stripe-order-id");
        assertThat(result.getClientSecret()).isEqualTo("pi_secret_abc");

        verify(providerClient).createStripeOrder(orderRequest);
    }

    // makeCreateOrderCall — FeignException.BadRequest

    @Test
    @DisplayName("makeCreateOrderCall: should throw StripeProcessingException on BadRequest")
    void makeCreateOrderCall_badRequest_throwsException() {
        FeignException.BadRequest badRequest = new FeignException.BadRequest(
                "Bad Request",
                dummyRequest(),
                "bad request body".getBytes(StandardCharsets.UTF_8),
                Collections.emptyMap()
        );

        when(providerClient.createStripeOrder(orderRequest)).thenThrow(badRequest);

        assertThatThrownBy(() -> initiatePaymentHelper.makeCreateOrderCall(orderRequest))
                .isInstanceOf(StripeProcessingException.class)
                .satisfies(ex -> {
                    StripeProcessingException spe = (StripeProcessingException) ex;
                    assertThat(spe.getErrorCode())
                            .isEqualTo(ErrorCodeEnum.INVALID_REQUEST.getErrorCode());
                    assertThat(spe.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                });
    }

    @Test
    @DisplayName("makeCreateOrderCall: should set transaction to FAILED on BadRequest")
    void makeCreateOrderCall_badRequest_setsTransactionFailed() {
        FeignException.BadRequest badRequest = new FeignException.BadRequest(
                "Bad Request",
                dummyRequest(),
                "bad request body".getBytes(StandardCharsets.UTF_8),
                Collections.emptyMap()
        );

        when(providerClient.createStripeOrder(orderRequest)).thenThrow(badRequest);

        assertThatThrownBy(() -> initiatePaymentHelper.makeCreateOrderCall(orderRequest))
                .isInstanceOf(StripeProcessingException.class);

        assertThat(initiatePaymentHelper.transaction.getStatus())
                .isEqualTo(TransactionStatus.FAILED);
        assertThat(initiatePaymentHelper.transaction.getErrorCode())
                .isEqualTo(ErrorCodeEnum.INVALID_REQUEST_ERROR.getErrorCode());
        assertThat(initiatePaymentHelper.transaction.getErrorMessage())
                .isEqualTo(ErrorCodeEnum.INVALID_REQUEST_ERROR.getErrorMessage());
    }

    // makeCreateOrderCall — FeignException.FeignServerException

    @Test
    @DisplayName("makeCreateOrderCall: should throw StripeProcessingException on server error")
    void makeCreateOrderCall_serverError_throwsException() {
        FeignException.InternalServerError serverError = new FeignException.InternalServerError(
                "Internal Server Error",
                dummyRequest(),
                "server error body".getBytes(StandardCharsets.UTF_8),
                Collections.emptyMap()
        );

        when(providerClient.createStripeOrder(orderRequest)).thenThrow(serverError);

        assertThatThrownBy(() -> initiatePaymentHelper.makeCreateOrderCall(orderRequest))
                .isInstanceOf(StripeProcessingException.class)
                .satisfies(ex -> {
                    StripeProcessingException spe = (StripeProcessingException) ex;
                    assertThat(spe.getErrorCode())
                            .isEqualTo(ErrorCodeEnum.STRIPE_PROVIDER_SERVICE_UNAVAILABLE.getErrorCode());
                    assertThat(spe.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    @Test
    @DisplayName("makeCreateOrderCall: should set transaction to FAILED on server error")
    void makeCreateOrderCall_serverError_setsTransactionFailed() {
        FeignException.InternalServerError serverError = new FeignException.InternalServerError(
                "Internal Server Error",
                dummyRequest(),
                "server error body".getBytes(StandardCharsets.UTF_8),
                Collections.emptyMap()
        );

        when(providerClient.createStripeOrder(orderRequest)).thenThrow(serverError);

        assertThatThrownBy(() -> initiatePaymentHelper.makeCreateOrderCall(orderRequest))
                .isInstanceOf(StripeProcessingException.class);

        assertThat(initiatePaymentHelper.transaction.getStatus())
                .isEqualTo(TransactionStatus.FAILED);
        assertThat(initiatePaymentHelper.transaction.getErrorCode())
                .isEqualTo(ErrorCodeEnum.STRIPE_PROVIDER_SERVICE_UNAVAILABLE.getErrorCode());
        assertThat(initiatePaymentHelper.transaction.getErrorMessage())
                .isEqualTo(ErrorCodeEnum.STRIPE_PROVIDER_SERVICE_UNAVAILABLE.getErrorMessage());
    }

    // createOrderFallBack

    @Test
    @DisplayName("createOrderFallBack: should throw StripeProcessingException with SERVICE_UNAVAILABLE")
    void createOrderFallBack_throwsStripeProcessingException() {
        Throwable cause = new RuntimeException("Circuit open");

        assertThatThrownBy(() -> initiatePaymentHelper.createOrderFallBack(cause))
                .isInstanceOf(StripeProcessingException.class)
                .satisfies(ex -> {
                    StripeProcessingException spe = (StripeProcessingException) ex;
                    assertThat(spe.getErrorCode())
                            .isEqualTo(ErrorCodeEnum.STRIPE_PROVIDER_SERVICE_UNAVAILABLE.getErrorCode());
                    assertThat(spe.getHttpStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                });
    }
}
