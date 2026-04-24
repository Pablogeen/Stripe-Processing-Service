package com.rey.Stripe_Processing_Service.repository;

import com.rey.Stripe_Processing_Service.entity.Transaction;
import com.rey.Stripe_Processing_Service.entity.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {


    Optional<Transaction> findBytxnReference(String txnReference);

    @Modifying
    @Query("UPDATE Transaction t SET t.status = :newStatus WHERE t.txnReference = :ref AND t.status = :currentStatus")
    int updateStatus(String ref, TransactionStatus newStatus, TransactionStatus currentStatus);
}
