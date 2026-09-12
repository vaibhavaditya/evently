package com.evently.evtbff.config;

import com.evently.evtbff.exception.OpenServiceException;
import feign.Response;
import feign.codec.ErrorDecoder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {

        String message = "Open service returned HTTP " + response.status();

        if (response.body() != null) {
            try {
                message = new String(
                        response.body().asInputStream().readAllBytes(),
                        StandardCharsets.UTF_8
                );
            } catch (IOException ignored) {
            }
        }

        return new OpenServiceException(
                response.status(),
                message
        );
    }
}