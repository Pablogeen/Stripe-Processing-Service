package com.rey.Stripe_Processing_Service.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE)
                .setSkipNullEnabled(false);

        return modelMapper;
    }


//    @Bean
//    RestClient restClient(RestClient.Builder builder) {
//
//        PoolingHttpClientConnectionManager connectionManager =
//                new PoolingHttpClientConnectionManager();
//        connectionManager.setMaxTotal(100);
//        connectionManager.setDefaultMaxPerRoute(100);
//
//        RequestConfig requestConfig = RequestConfig.custom()
//                .setConnectionRequestTimeout(Timeout.ofSeconds(10)) // wait for pool connection
//                .setConnectTimeout(Timeout.ofSeconds(10))           // TCP connect timeout
//                .setResponseTimeout(Timeout.ofSeconds(15))          // server response timeout
//                .build();
//
//        CloseableHttpClient httpClient = HttpClients.custom()
//                .setConnectionManager(connectionManager)
//                .setDefaultRequestConfig(requestConfig)
//                .evictIdleConnections(TimeValue.ofSeconds(30))
//                .build();
//
//        HttpComponentsClientHttpRequestFactory requestFactory =
//                new HttpComponentsClientHttpRequestFactory(httpClient);
//
//        return builder
//                .requestFactory(requestFactory)
//                .build();
//
//    }
}

