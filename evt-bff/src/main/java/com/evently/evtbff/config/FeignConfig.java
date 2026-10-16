
package com.evently.evtbff.config;

import feign.Client;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import feign.hc5.ApacheHttp5Client;
import org.slf4j.MDC;
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

    // Forwards the current Trace ID to Open Service
    @Bean
    public RequestInterceptor traceIdRequestInterceptor() {
        return requestTemplate -> {
            String traceId = MDC.get("traceId");

            if (traceId != null && !traceId.isBlank()) {
                requestTemplate.header("X-Trace-Id", traceId);
            }
        };
    }
}
