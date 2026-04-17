package com.rey.Stripe_Processing_Service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class StripeProcessingException extends RuntimeException{

    private final String errorCode;
    private final String errorMessage;
    private final HttpStatus httpStatus;

    public StripeProcessingException(String errorCode, String errorMessage, HttpStatus httpStatus) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.httpStatus=httpStatus;
    }


}
