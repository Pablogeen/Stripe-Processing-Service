package com.rey.Stripe_Processing_Service.paymentServiceImpl;

import com.rey.Stripe_Processing_Service.constants.Constant;
import com.rey.Stripe_Processing_Service.constants.ErrorCodeEnum;
import com.rey.Stripe_Processing_Service.dto.*;
import com.rey.Stripe_Processing_Service.entity.Transaction;
import com.rey.Stripe_Processing_Service.entity.TransactionStatus;
import com.rey.Stripe_Processing_Service.exception.StripeProcessingException;
import com.rey.Stripe_Processing_Service.helper.ConfirmPaymentHelper;
import com.rey.Stripe_Processing_Service.helper.InitiatePaymentHelper;
import com.rey.Stripe_Processing_Service.helper.RetryHelper;
import com.rey.Stripe_Processing_Service.repository.TransactionRepository;
import com.rey.Stripe_Processing_Service.service.ServiceInterface;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements ServiceInterface {

    private final TransactionRepository paymentRepo;
    private final ModelMapper modelMapper;
    private final InitiatePaymentHelper paymentHelper;
    private final ConfirmPaymentHelper confirmPaymentHelper;
    private final RetryHelper retryHelper;


    @Transactional
    @Override
    public PaymentResponse makePayment(CreatePaymentRequest paymentRequest) {

        Transaction transaction = modelMapper.map(paymentRequest, Transaction.class);
        log.info("Mapped paymentRequest to transaction: {}",transaction);

        String txnReference = UUID.randomUUID().toString();
        transaction.setTxnReference(txnReference);

        transaction.setStatus(TransactionStatus.CREATED);
        log.info("Payment Status set to CREATED");
        log.info("UserId: {}",transaction.getUserId());

        transaction.setCreationDate(LocalDateTime.now());

       Transaction savedTransaction = paymentRepo.save(transaction);
       log.info("Transaction has being saved: {}",savedTransaction);

        PaymentResponse paymentResponse = modelMapper.map(savedTransaction, PaymentResponse.class);
        log.info("Mapped paymentDetails from the db to Payment Response: {}",paymentResponse);

        return paymentResponse;
    }

    @Transactional
    @Override
    public PaymentResponse initiatePayment(String txnReference) {

        StripeProviderCreateOrderRequest providerRequest;

        int updatedStatus =
                paymentRepo.updateStatus(txnReference, TransactionStatus.INITIATED, TransactionStatus.CREATED);
        log.info("Updated the status to check Idempotency and race conditions: {}", updatedStatus);

        Transaction transaction = paymentRepo.findBytxnReference(txnReference)
                .orElseThrow(() -> new StripeProcessingException(
                        ErrorCodeEnum.INVALID_TRANSACTION_REFERENCE.getErrorCode(),
                        ErrorCodeEnum.INVALID_TRANSACTION_REFERENCE.getErrorMessage(),
                        HttpStatus.BAD_REQUEST));
        log.info("Gotten Transaction with txnReference: {}", txnReference);


        String idempotencyKey  = txnReference + "_create";
        log.info("Create Idempotency {}", idempotencyKey);

        if (updatedStatus == 0) {
            //Trying to retry with already INITIATED txnReference
            //What if status is INITIATED and got no provider Reference
          retryHelper.createOrderRetry(transaction, idempotencyKey);
        }



        providerRequest = new StripeProviderCreateOrderRequest();
        providerRequest.setAmount(transaction.getAmount());
        providerRequest.setCurrency(transaction.getCurrency());
        providerRequest.setIdempotencyKey(idempotencyKey);

        StripeProviderOrderResponse createOrderResponse =
                paymentHelper.makeCreateOrderCall(providerRequest);
        log.info("Call made to Stripe Provider to Create Order");

        transaction.setProviderReference(createOrderResponse.getId());
        log.info("Set stripe create order id into DB: {}", transaction.getProviderReference());

        transaction.setClientSecret(createOrderResponse.getClientSecret());
        log.info("Set Stripe client secret into the DB");

        transaction.setStatus(TransactionStatus.PENDING);
        log.info("Payment Status set to PENDING");

        paymentRepo.save(transaction);
        log.info("Saved transaction");

        PaymentResponse paymentResponse = modelMapper.map(transaction, PaymentResponse.class);
        log.info("Mapped transaction information into PaymentResponse: ");

        return paymentResponse;

    }

    @Transactional
    @Override
    public PaymentResponse confirmPayment(String txnReference) {

        int updatedStatus =
                paymentRepo.updateStatus(txnReference, TransactionStatus.APPROVED, TransactionStatus.PENDING);
        log.info("Confirm race check: {}", updatedStatus);


        String idempotencyKey = txnReference + "_confirm";
        log.info("Confirm Idempotency {}", idempotencyKey);

        Transaction transaction = paymentRepo.findBytxnReference(txnReference)
                .orElseThrow(() -> new StripeProcessingException(
                        ErrorCodeEnum.INVALID_TRANSACTION_REFERENCE.getErrorCode(),
                        ErrorCodeEnum.INVALID_TRANSACTION_REFERENCE.getErrorMessage(),
                        HttpStatus.BAD_REQUEST));
        log.info(" Transaction with txnReference: {}", txnReference);

        if (updatedStatus == 0) {
            retryHelper.confirmOrderRetry(transaction, idempotencyKey);
            log.info("Checked on retries and idempotency");
            // Already SUCCESS? Return cached
        }

                String providerReference = transaction.getProviderReference();
                log.info("ProviderReference: {}",providerReference);


                if (providerReference == null) {
                    throw new StripeProcessingException(
                            ErrorCodeEnum.MISSING_PROVIDER_REFERENCE.getErrorCode(),
                            ErrorCodeEnum.MISSING_PROVIDER_REFERENCE.getErrorMessage(),
                            HttpStatus.BAD_REQUEST
                    );
                }



                StripeProviderConfirmPaymentRequest paymentRequest = new StripeProviderConfirmPaymentRequest();
                paymentRequest.setReturnUrl(Constant.RETURN_URL);
                paymentRequest.setIdempotencyKey(idempotencyKey);
                log.info("Return Url && Idempotency Key: {}", paymentRequest.getReturnUrl());

                confirmPaymentHelper.makeConfirmOrderCall(providerReference, paymentRequest);
                log.info("Request made to Stripe Provider to confirm Order: ");

                transaction.setStatus(TransactionStatus.SUCCESS);
                log.info("Payment set to Success");

                paymentRepo.save(transaction);

                PaymentResponse paymentResponse = modelMapper.map(transaction, PaymentResponse.class);
                log.info("Mapped transaction details into PaymentResponse: ");

                return paymentResponse;

            }


}


