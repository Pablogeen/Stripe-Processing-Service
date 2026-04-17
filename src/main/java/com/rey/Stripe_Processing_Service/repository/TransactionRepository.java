package com.rey.Stripe_Processing_Service.repository;

import com.rey.Stripe_Processing_Service.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {


    Optional<Transaction> findBytxnReference(String txnReference);
}
