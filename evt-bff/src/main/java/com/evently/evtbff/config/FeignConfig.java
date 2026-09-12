package com.evently.evtbff.config;

import feign.Client;
import feign.codec.ErrorDecoder;
import feign.hc5.ApacheHttp5Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    // Allows Feign to send PATCH requests
    @Bean
    public Client feignClient() {
        return new ApacheHttp5Client();
    }

    // Converts Open Service errors into OpenServiceException
    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }
}