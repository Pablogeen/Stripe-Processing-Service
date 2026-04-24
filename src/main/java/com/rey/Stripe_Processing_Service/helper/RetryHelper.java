package com.rey.Stripe_Processing_Service.helper;

import com.rey.Stripe_Processing_Service.constants.ErrorCodeEnum;
import com.rey.Stripe_Processing_Service.dto.PaymentResponse;
import com.rey.Stripe_Processing_Service.dto.StripeProviderCreateOrderRequest;
import com.rey.Stripe_Processing_Service.dto.StripeProviderOrderResponse;
import com.rey.Stripe_Processing_Service.entity.Transaction;
import com.rey.Stripe_Processing_Service.entity.TransactionStatus;
import com.rey.Stripe_Processing_Service.exception.StripeProcessingException;
import com.rey.Stripe_Processing_Service.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j

public class RetryHelper {

    private final TransactionRepository paymentRepo;
    private final InitiatePaymentHelper paymentHelper;
    private final ModelMapper modelMapper;


    public PaymentResponse createOrderRetry(Transaction transaction, String idempotencyKey) {

        StripeProviderCreateOrderRequest providerRequest = new StripeProviderCreateOrderRequest();
        //Retry
        if (transaction.getStatus() == TransactionStatus.INITIATED && transaction.getProviderReference() == null) {
            // Query Stripe using our  idempotency key
            providerRequest.setAmount(transaction.getAmount());
            providerRequest.setCurrency(transaction.getCurrency());
            providerRequest.setIdempotencyKey(idempotencyKey);
            log.info("Set the amount and currency into the Request: {} {}"
                    , providerRequest.getAmount(), providerRequest.getCurrency());

            StripeProviderOrderResponse createOrderResponse =
                    paymentHelper.makeCreateOrderCall(providerRequest);
            log.info("Call made to Stripe Provider to Create Order: {}", providerRequest);

            //Stripe have created or not created a request because of breakages,
            // it creates a new one or with the idempotency key
            if (createOrderResponse != null) {
                transaction.setProviderReference(createOrderResponse.getId());
                transaction.setClientSecret(createOrderResponse.getClientSecret());
                transaction.setStatus(TransactionStatus.PENDING);
                paymentRepo.save(transaction);


                PaymentResponse paymentResponse = modelMapper.map(transaction, PaymentResponse.class);
                log.info("Mapped retried transaction to paymentResponse: {}", paymentResponse);

                return paymentResponse;

            }

        }
        if (transaction.getStatus() == TransactionStatus.PENDING ||
                                               transaction.getStatus() == TransactionStatus.SUCCESS) {
            PaymentResponse paymentResponse = modelMapper.map(transaction, PaymentResponse.class);
            log.info("Mapped transaction to paymentResponse for Pending and success squads: {}", paymentResponse);

            return paymentResponse;
        }
        throw new StripeProcessingException(
                ErrorCodeEnum.INVALID_TRANSACTION_STATE.getErrorCode(),
                ErrorCodeEnum.INVALID_TRANSACTION_STATE.getErrorMessage()+transaction.getStatus(),
                HttpStatus.CONFLICT);
    }


    public PaymentResponse confirmOrderRetry(Transaction transaction, String idempotencyKey) {


        if (transaction.getStatus() == TransactionStatus.SUCCESS) {
            return modelMapper.map(transaction, PaymentResponse.class);
        }

        // Stuck at APPROVED with no confirm result? Proceed to Stripe below
        if (transaction.getStatus() == TransactionStatus.APPROVED) {
            log.info("Recovery: stuck at APPROVED, retrying Stripe confirm");
            // Let's continue to confirm payment.
        }
            throw new StripeProcessingException(
                    ErrorCodeEnum.INVALID_TRANSACTION_STATE.getErrorCode(),
                    ErrorCodeEnum.INVALID_TRANSACTION_STATE.getErrorMessage()+transaction.getStatus(),
                    HttpStatus.CONFLICT);
        }

    }


