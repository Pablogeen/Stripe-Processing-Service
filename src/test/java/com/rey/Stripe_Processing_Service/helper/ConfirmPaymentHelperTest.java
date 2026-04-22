package com.rey.Stripe_Processing_Service.helper;

import com.rey.Stripe_Processing_Service.client.StripeProviderClient;
import com.rey.Stripe_Processing_Service.constants.ErrorCodeEnum;
import com.rey.Stripe_Processing_Service.dto.StripeProviderConfirmPaymentRequest;
import com.rey.Stripe_Processing_Service.dto.StripeProviderOrderResponse;
import com.rey.Stripe_Processing_Service.entity.TransactionStatus;
import com.rey.Stripe_Processing_Service.exception.StripeProcessingException;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfirmPaymentHelperTest {

    @Mock
    private StripeProviderClient providerClient;

    @InjectMocks
    private ConfirmPaymentHelper confirmPaymentHelper;

    private StripeProviderConfirmPaymentRequest paymentRequest;
    private String providerReference;

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
        providerReference = "stripe-provider-ref-123";
        paymentRequest = new StripeProviderConfirmPaymentRequest();
        paymentRequest.setReturnUrl("https://example.com/return");
    }

    // makeConfirmOrderCall

    @Test
    @DisplayName("makeConfirmOrderCall: should return order response on success")
    void makeConfirmOrderCall_success() {
        StripeProviderOrderResponse expectedResponse = new StripeProviderOrderResponse();
        expectedResponse.setId("stripe-order-id");
        expectedResponse.setClientSecret("pi_secret_abc");

        when(providerClient.confirmOrder(providerReference, paymentRequest))
                .thenReturn(expectedResponse);

        StripeProviderOrderResponse result =
                confirmPaymentHelper.makeConfirmOrderCall(providerReference, paymentRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("stripe-order-id");
        assertThat(result.getClientSecret()).isEqualTo("pi_secret_abc");

        verify(providerClient).confirmOrder(providerReference, paymentRequest);
    }

    // makeConfirmOrderCall — FeignException.BadRequest

    @Test
    @DisplayName("makeConfirmOrderCall: should throw StripeProcessingException on BadRequest")
    void makeConfirmOrderCall_badRequest_throwsException() {
        String errorBody = "invalid payment method";
        FeignException.BadRequest badRequest = new FeignException.BadRequest(
                "Bad Request",
                dummyRequest(),
                errorBody.getBytes(StandardCharsets.UTF_8),
                Collections.emptyMap()
        );

        when(providerClient.confirmOrder(providerReference, paymentRequest)).thenThrow(badRequest);

        assertThatThrownBy(() -> confirmPaymentHelper.makeConfirmOrderCall(providerReference, paymentRequest))
                .isInstanceOf(StripeProcessingException.class)
                .satisfies(ex -> {
                    StripeProcessingException spe = (StripeProcessingException) ex;
                    assertThat(spe.getErrorCode())
                            .isEqualTo(ErrorCodeEnum.INVALID_REQUEST.getErrorCode());
                    assertThat(spe.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(spe.getErrorMessage()).isEqualTo(errorBody);
                });
    }

    @Test
    @DisplayName("makeConfirmOrderCall: should set transaction to FAILED on BadRequest")
    void makeConfirmOrderCall_badRequest_setsTransactionFailed() {
        String errorBody = "invalid payment method";
        FeignException.BadRequest badRequest = new FeignException.BadRequest(
                "Bad Request",
                dummyRequest(),
                errorBody.getBytes(StandardCharsets.UTF_8),
                Collections.emptyMap()
        );

        when(providerClient.confirmOrder(providerReference, paymentRequest)).thenThrow(badRequest);

        assertThatThrownBy(() -> confirmPaymentHelper.makeConfirmOrderCall(providerReference, paymentRequest))
                .isInstanceOf(StripeProcessingException.class);

        assertThat(confirmPaymentHelper.transaction.getStatus())
                .isEqualTo(TransactionStatus.FAILED);
        assertThat(confirmPaymentHelper.transaction.getErrorCode())
                .isEqualTo(ErrorCodeEnum.INVALID_REQUEST_ERROR.getErrorCode());
        assertThat(confirmPaymentHelper.transaction.getErrorMessage())
                .isEqualTo(errorBody);
    }

    // makeConfirmOrderCall — FeignException.FeignServerException

    @Test
    @DisplayName("makeConfirmOrderCall: should throw StripeProcessingException on server error")
    void makeConfirmOrderCall_serverError_throwsException() {
        String errorBody = "stripe service down";
        FeignException.InternalServerError serverError = new FeignException.InternalServerError(
                "Internal Server Error",
                dummyRequest(),
                errorBody.getBytes(StandardCharsets.UTF_8),
                Collections.emptyMap()
        );

        when(providerClient.confirmOrder(providerReference, paymentRequest)).thenThrow(serverError);

        assertThatThrownBy(() -> confirmPaymentHelper.makeConfirmOrderCall(providerReference, paymentRequest))
                .isInstanceOf(StripeProcessingException.class)
                .satisfies(ex -> {
                    StripeProcessingException spe = (StripeProcessingException) ex;
                    assertThat(spe.getErrorCode())
                            .isEqualTo(ErrorCodeEnum.STRIPE_PROVIDER_SERVICE_UNAVAILABLE.getErrorCode());
                    assertThat(spe.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                    assertThat(spe.getErrorMessage()).isEqualTo(errorBody);
                });
    }

    @Test
    @DisplayName("makeConfirmOrderCall: should set transaction to FAILED on server error")
    void makeConfirmOrderCall_serverError_setsTransactionFailed() {
        String errorBody = "stripe service down";
        FeignException.InternalServerError serverError = new FeignException.InternalServerError(
                "Internal Server Error",
                dummyRequest(),
                errorBody.getBytes(StandardCharsets.UTF_8),
                Collections.emptyMap()
        );

        when(providerClient.confirmOrder(providerReference, paymentRequest)).thenThrow(serverError);

        assertThatThrownBy(() -> confirmPaymentHelper.makeConfirmOrderCall(providerReference, paymentRequest))
                .isInstanceOf(StripeProcessingException.class);

        assertThat(confirmPaymentHelper.transaction.getStatus())
                .isEqualTo(TransactionStatus.FAILED);
        assertThat(confirmPaymentHelper.transaction.getErrorCode())
                .isEqualTo(ErrorCodeEnum.STRIPE_PROVIDER_SERVICE_UNAVAILABLE.getErrorCode());
        assertThat(confirmPaymentHelper.transaction.getErrorMessage())
                .isEqualTo(errorBody);
    }

    // confirmOrderFallBack

    @Test
    @DisplayName("confirmOrderFallBack: should throw StripeProcessingException with SERVICE_UNAVAILABLE")
    void confirmOrderFallBack_throwsStripeProcessingException() {
        Throwable cause = new RuntimeException("Circuit open");

        assertThatThrownBy(() -> confirmPaymentHelper.confirmOrderFallBack(cause))
                .isInstanceOf(StripeProcessingException.class)
                .satisfies(ex -> {
                    StripeProcessingException spe = (StripeProcessingException) ex;
                    assertThat(spe.getErrorCode())
                            .isEqualTo(ErrorCodeEnum.STRIPE_PROVIDER_SERVICE_UNAVAILABLE.getErrorCode());
                    assertThat(spe.getErrorMessage())
                            .isEqualTo(ErrorCodeEnum.STRIPE_PROVIDER_SERVICE_UNAVAILABLE.getErrorMessage());
                    assertThat(spe.getHttpStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                });
    }
}
