package com.rey.Stripe_Processing_Service.serviceImpl;


import com.rey.Stripe_Processing_Service.constants.ErrorCodeEnum;
import com.rey.Stripe_Processing_Service.dto.*;
import com.rey.Stripe_Processing_Service.entity.Transaction;
import com.rey.Stripe_Processing_Service.entity.TransactionStatus;
import com.rey.Stripe_Processing_Service.exception.StripeProcessingException;
import com.rey.Stripe_Processing_Service.helper.ConfirmPaymentHelper;
import com.rey.Stripe_Processing_Service.helper.InitiatePaymentHelper;
import com.rey.Stripe_Processing_Service.paymentServiceImpl.PaymentServiceImpl;
import com.rey.Stripe_Processing_Service.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock private TransactionRepository paymentRepo;
    @Mock private ModelMapper modelMapper;
    @Mock private InitiatePaymentHelper paymentHelper;
    @Mock private ConfirmPaymentHelper confirmPaymentHelper;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private CreatePaymentRequest createPaymentRequest;
    private Transaction transaction;
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

        transaction = new Transaction();
        transaction.setId(1);
        transaction.setUserId(1);
        transaction.setAmount(10000);
        transaction.setCurrency("USD");

        paymentResponse = new PaymentResponse();
        paymentResponse.setTxnReference("test-txn-ref");
    }

    // makePayment

    @Test
    @DisplayName("makePayment: should save transaction with CREATED status and return response")
    void makePayment_success() {
        when(modelMapper.map(createPaymentRequest, Transaction.class)).thenReturn(transaction);
        when(paymentRepo.save(any(Transaction.class))).thenReturn(transaction);
        when(modelMapper.map(transaction, PaymentResponse.class)).thenReturn(paymentResponse);

        PaymentResponse result = paymentService.makePayment(createPaymentRequest);

        assertThat(result).isNotNull();
        assertThat(result.getTxnReference()).isEqualTo("test-txn-ref");

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(paymentRepo).save(captor.capture());

        assertThat(captor.getValue().getStatus()).isEqualTo(TransactionStatus.CREATED);
        assertThat(captor.getValue().getTxnReference()).isNotNull();
        assertThat(captor.getValue().getCreationDate()).isNotNull();
    }

    @Test
    @DisplayName("makePayment: should generate unique txnReference for each call")
    void makePayment_generatesDifferentTxnReferences() {
        Transaction tx1 = new Transaction();
        Transaction tx2 = new Transaction();

        when(modelMapper.map(any(CreatePaymentRequest.class), eq(Transaction.class)))
                .thenReturn(tx1).thenReturn(tx2);
        when(paymentRepo.save(any(Transaction.class)))
                .thenReturn(tx1).thenReturn(tx2);
        when(modelMapper.map(any(Transaction.class), eq(PaymentResponse.class)))
                .thenReturn(new PaymentResponse());

        paymentService.makePayment(createPaymentRequest);
        paymentService.makePayment(createPaymentRequest);

        assertThat(tx1.getTxnReference()).isNotEqualTo(tx2.getTxnReference());
    }

    // initiatePayment

    @Test
    @DisplayName("initiatePayment: should call Stripe, set PENDING and return response")
    void initiatePayment_success() {
        String txnReference = "test-txn-ref";
        transaction.setTxnReference(txnReference);

        StripeProviderOrderResponse orderResponse = new StripeProviderOrderResponse();
        orderResponse.setId("stripe-provider-ref-123");
        orderResponse.setClientSecret("pi_secret_abc123_secret_xyz");

        when(paymentRepo.findBytxnReference(txnReference)).thenReturn(Optional.of(transaction));
        when(paymentHelper.makeCreateOrderCall(any())).thenReturn(orderResponse);
        when(paymentRepo.save(any(Transaction.class))).thenReturn(transaction);
        when(modelMapper.map(transaction, PaymentResponse.class)).thenReturn(paymentResponse);

        PaymentResponse result = paymentService.initiatePayment(txnReference);

        assertThat(result).isNotNull();

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(paymentRepo).save(captor.capture());
        Transaction saved = captor.getValue();

        assertThat(saved.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(saved.getProviderReference()).isEqualTo("stripe-provider-ref-123");
        assertThat(saved.getClientSecret()).isEqualTo("pi_secret_abc123_secret_xyz");
    }

    @Test
    @DisplayName("initiatePayment: should pass correct amount and currency to Stripe")
    void initiatePayment_passesCorrectRequestToStripe() {
        String txnReference = "test-txn-ref";
        transaction.setTxnReference(txnReference);

        StripeProviderOrderResponse orderResponse = new StripeProviderOrderResponse();
        orderResponse.setId("stripe-id");
        orderResponse.setClientSecret("secret");

        when(paymentRepo.findBytxnReference(txnReference)).thenReturn(Optional.of(transaction));
        when(paymentHelper.makeCreateOrderCall(any())).thenReturn(orderResponse);
        when(paymentRepo.save(any())).thenReturn(transaction);
        when(modelMapper.map(any(Transaction.class), eq(PaymentResponse.class))).thenReturn(paymentResponse);

        paymentService.initiatePayment(txnReference);

        ArgumentCaptor<StripeProviderCreateOrderRequest> captor =
                ArgumentCaptor.forClass(StripeProviderCreateOrderRequest.class);
        verify(paymentHelper).makeCreateOrderCall(captor.capture());

        assertThat(captor.getValue().getAmount()).isEqualTo(10000);
        assertThat(captor.getValue().getCurrency()).isEqualTo("USD");
    }

    @Test
    @DisplayName("initiatePayment: should throw exception when txnReference not found")
    void initiatePayment_notFound_throwsException() {
        when(paymentRepo.findBytxnReference("bad-ref")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.initiatePayment("bad-ref"))
                .isInstanceOf(StripeProcessingException.class)
                .satisfies(ex -> {
                    StripeProcessingException spe = (StripeProcessingException) ex;
                    assertThat(spe.getErrorCode())
                            .isEqualTo(ErrorCodeEnum.INVALID_TRANSACTION_REFERENCE.getErrorCode());
                    assertThat(spe.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                });

        verify(paymentHelper, never()).makeCreateOrderCall(any());
        verify(paymentRepo, never()).save(any());
    }

    // confirmPayment

    @Test
    @DisplayName("confirmPayment: should confirm with Stripe, set SUCCESS and return response")
    void confirmPayment_success() {
        String txnReference = "test-txn-ref";
        transaction.setTxnReference(txnReference);
        transaction.setProviderReference("stripe-provider-ref-123");

        when(paymentRepo.findBytxnReference(txnReference)).thenReturn(Optional.of(transaction));
        when(paymentRepo.save(any(Transaction.class))).thenReturn(transaction);
        when(modelMapper.map(transaction, PaymentResponse.class)).thenReturn(paymentResponse);

        PaymentResponse result = paymentService.confirmPayment(txnReference);

        assertThat(result).isNotNull();

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(paymentRepo).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(TransactionStatus.SUCCESS);
    }

    @Test
    @DisplayName("confirmPayment: should pass correct providerReference and returnUrl to Stripe")
    void confirmPayment_passesCorrectRequestToStripe() {
        String txnReference = "test-txn-ref";
        transaction.setTxnReference(txnReference);
        transaction.setProviderReference("stripe-provider-ref-123");

        when(paymentRepo.findBytxnReference(txnReference)).thenReturn(Optional.of(transaction));
        when(paymentRepo.save(any())).thenReturn(transaction);
        when(modelMapper.map(any(Transaction.class), eq(PaymentResponse.class))).thenReturn(paymentResponse);

        paymentService.confirmPayment(txnReference);

        ArgumentCaptor<StripeProviderConfirmPaymentRequest> requestCaptor =
                ArgumentCaptor.forClass(StripeProviderConfirmPaymentRequest.class);
        verify(confirmPaymentHelper).makeConfirmOrderCall(eq("stripe-provider-ref-123"), requestCaptor.capture());

        assertThat(requestCaptor.getValue().getReturnUrl()).isNotNull();
    }

    @Test
    @DisplayName("confirmPayment: should throw exception when txnReference not found")
    void confirmPayment_notFound_throwsException() {
        when(paymentRepo.findBytxnReference("bad-ref")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.confirmPayment("bad-ref"))
                .isInstanceOf(StripeProcessingException.class)
                .satisfies(ex -> {
                    StripeProcessingException spe = (StripeProcessingException) ex;
                    assertThat(spe.getErrorCode())
                            .isEqualTo(ErrorCodeEnum.INVALID_TRANSACTION_REFERENCE.getErrorCode());
                    assertThat(spe.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                });

        verify(confirmPaymentHelper, never()).makeConfirmOrderCall(any(), any());
        verify(paymentRepo, never()).save(any());
    }
}
