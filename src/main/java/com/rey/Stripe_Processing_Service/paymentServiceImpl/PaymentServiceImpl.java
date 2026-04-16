package com.rey.Stripe_Processing_Service.paymentServiceImpl;

import com.rey.Stripe_Processing_Service.dto.CreatePaymentRequest;
import com.rey.Stripe_Processing_Service.dto.PaymentResponse;
import com.rey.Stripe_Processing_Service.entity.Transaction;
import com.rey.Stripe_Processing_Service.repository.TransactionRepository;
import com.rey.Stripe_Processing_Service.service.ServiceInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements ServiceInterface {

    private final TransactionRepository paymentRepo;
    private final ModelMapper modelMapper;

    @Override
    public PaymentResponse makePayment(CreatePaymentRequest paymentRequest) {

        Transaction transaction = modelMapper.map(paymentRequest, Transaction.class);
        log.info("Mapped paymentRequest to transaction: {}",transaction);

        String txnReference = UUID.randomUUID().toString();
        transaction.setTxnReference(txnReference);

        transaction.setTxnStatusId(1);

       Transaction savedTransaction = paymentRepo.save(transaction);

        PaymentResponse paymentResponse = modelMapper.map(savedTransaction, PaymentResponse.class);
        log.info("Mapped paymentDetails from the db to Payment Response: {}",paymentResponse);

        return paymentResponse;
    }

    @Override
    public PaymentResponse initiatePayment(String txnReference) {

       Transaction  transaction = paymentRepo.findBytxnReference(txnReference);
       log.info("Gotten Transaction with txnReference: {}",transaction);

       transaction.setTxnStatusId(2);



        return null;
    }
}
