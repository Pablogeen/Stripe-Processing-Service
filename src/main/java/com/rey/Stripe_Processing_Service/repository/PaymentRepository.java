package com.rey.Stripe_Processing_Service.repository;

import com.rey.Stripe_Processing_Service.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Transaction, Long> {



}
