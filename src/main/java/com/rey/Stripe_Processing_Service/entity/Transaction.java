package com.rey.Stripe_Processing_Service.entity;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    private Long id;
    private Integer userId;

    private Integer paymentMethodId;
    private Integer providerId;
    private Integer paymentTypeId;
    private Integer txnStatusId;

    private BigDecimal amount;
    private String currency;

    private String txnReference;
    private String providerReference;

    private String errorCode;
    private String errorMessage;

    private LocalDateTime creationDate;
    private Integer retryCount;
}