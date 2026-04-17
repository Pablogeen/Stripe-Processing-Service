package com.rey.Stripe_Processing_Service.entity;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transaction")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "user_id")
    private Integer userId;

    private Integer paymentMethodId;
    private Integer providerId;
    private Integer paymentTypeId;
    private Integer txnStatusId;

    private Integer amount;
    private String currency;

    private String txnReference;
    private String providerReference;
    private String clientSecret;

    private String errorCode;
    private String errorMessage;

    private LocalDateTime creationDate;
    private Integer retryCount;
}