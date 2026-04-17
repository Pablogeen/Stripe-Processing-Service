package com.rey.Stripe_Processing_Service.constants;

import lombok.Getter;

@Getter
public enum ErrorCodeEnum {

    GENERIC_ERROR("2000","Ooops, Something Went Wrong!!!"),
    INVALID_REQUEST("2001","INVALID REQUEST"),
    STRIPE_SERVICE_UNAVAILABLE("2002","STRIPE SERVICE UNAVAILABLE"),
    STRIPE_UNKNOWN_ERROR("2003","UNKNOWN PROBLEM WHILE PROCESSING PAYMENT"),
    INVALID_TRANSACTION_REFERENCE("2004","INVALID TRANSACTION REFERENCE"),
    INVALID_REQUEST_ERROR("2009","INVALID_REQUEST_ERROR"),
    PAYMENT_INTENT_UNEXPECTED_STATE("2010","Payment_intent_unexpected_state");
    private String errorCode;
    private String errorMessage;

    ErrorCodeEnum(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
