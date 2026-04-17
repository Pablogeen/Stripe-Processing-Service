package com.rey.Stripe_Processing_Service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class StripeProcessingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(StripeProcessingServiceApplication.class, args);
	}

}
