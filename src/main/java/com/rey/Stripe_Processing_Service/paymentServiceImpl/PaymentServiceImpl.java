package com.rey.Stripe_Processing_Service.paymentServiceImpl;

import com.rey.Stripe_Processing_Service.constants.Constant;
import com.rey.Stripe_Processing_Service.constants.ErrorCodeEnum;
import com.rey.Stripe_Processing_Service.dto.*;
import com.rey.Stripe_Processing_Service.entity.Transaction;
import com.rey.Stripe_Processing_Service.entity.TransactionStatus;
import com.rey.Stripe_Processing_Service.exception.StripeProcessingException;
import com.rey.Stripe_Processing_Service.helper.ConfirmPaymentHelper;
import com.rey.Stripe_Processing_Service.helper.InitiatePaymentHelper;
import com.rey.Stripe_Processing_Service.repository.TransactionRepository;
import com.rey.Stripe_Processing_Service.service.ServiceInterface;
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

    @Override
    public PaymentResponse initiatePayment(String txnReference) {

       Transaction  transaction = paymentRepo.findBytxnReference(txnReference)
                       .orElseThrow(() -> new StripeProcessingException(
                               ErrorCodeEnum.INVALID_TRANSACTION_REFERENCE.getErrorCode(),
                               ErrorCodeEnum.INVALID_TRANSACTION_REFERENCE.getErrorMessage(),
                               HttpStatus.BAD_REQUEST
                       ));
       log.info("Gotten Transaction with txnReference: {}",txnReference);

       transaction.setStatus(TransactionStatus.INITIATED);
        log.info("Payment Status set to INITIATED");


      StripeProviderCreateOrderRequest providerRequest = new StripeProviderCreateOrderRequest();

      providerRequest.setAmount(transaction.getAmount());
      providerRequest.setCurrency(transaction.getCurrency());

      StripeProviderOrderResponse createOrderResponse =
                        paymentHelper.makeCreateOrderCall(providerRequest);
      log.info("Call made to Stripe Provider to Create Order");

      transaction.setProviderReference(createOrderResponse.getId());
      log.info("Set stripe create order id into DB: {}",transaction.getProviderReference());

      transaction.setClientSecret(createOrderResponse.getClientSecret());
      log.info("Set Stripe client secret into the DB");

      transaction.setStatus(TransactionStatus.PENDING);
      log.info("Payment Status set to PENDING");

      paymentRepo.save(transaction);
      log.info("Saved transaction detailes");

      PaymentResponse paymentResponse = modelMapper.map(transaction, PaymentResponse.class);
      log.info("Mapped transaction information into PaymentResponse: ");

      return paymentResponse;

    }

    @Override
    public PaymentResponse confirmPayment(String txnReference) {

        Transaction  transaction = paymentRepo.findBytxnReference(txnReference)
                .orElseThrow(() -> new StripeProcessingException(
                        ErrorCodeEnum.INVALID_TRANSACTION_REFERENCE.getErrorCode(),
                        ErrorCodeEnum.INVALID_TRANSACTION_REFERENCE.getErrorMessage(),
                        HttpStatus.BAD_REQUEST
                ));
        log.info(" Transaction with txnReference: {}",txnReference);


        String providerReference = transaction.getProviderReference();
        log.info("Got the provider Reference from the DB: {}",providerReference);

        StripeProviderConfirmPaymentRequest paymentRequest = new StripeProviderConfirmPaymentRequest();
        paymentRequest.setReturnUrl(Constant.RETURN_URL);
        log.info("Return Url: {}",paymentRequest.getReturnUrl());

        transaction.setStatus(TransactionStatus.APPROVED);
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
