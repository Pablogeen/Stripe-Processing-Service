package com.rey.Stripe_Processing_Service.paymentServiceImpl;

import com.rey.Stripe_Processing_Service.constants.ErrorCodeEnum;
import com.rey.Stripe_Processing_Service.entity.Transaction;
import com.rey.Stripe_Processing_Service.entity.TransactionStatus;
import com.rey.Stripe_Processing_Service.exception.StripeProcessingException;
import com.rey.Stripe_Processing_Service.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentReconScheduler {

    private final TransactionRepository transactionRepo;


    @Scheduled(fixedDelay = 86400000)
    public void reconcilePendingStatus() {

        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);
        log.info("Cut off Time: {}",cutoffTime);

        List<Transaction> payments =
                transactionRepo.findByStatusNotAndCreationDateBefore(TransactionStatus.SUCCESS, cutoffTime);
        log.info("Got all transactions that are of not success status and created in the last 24 hours: {}", payments.size());

        if (payments.isEmpty()) {
            log.info("No stale payments found");
            throw new StripeProcessingException(
                    ErrorCodeEnum.NO_TRANSACTION_IS_LEFT_HANGING.getErrorCode(),
                    ErrorCodeEnum.NO_TRANSACTION_IS_LEFT_HANGING.getErrorMessage(),
                    HttpStatus.CONFLICT);
        }

        payments.forEach(payment -> payment.setStatus(TransactionStatus.FAILED));
        log.info("Set status to FAILED");

        transactionRepo.saveAll(payments);
        log.info("Transaction saved");

    }

}