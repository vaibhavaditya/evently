package com.evently.evtbff.exception;

import lombok.Getter;

@Getter
public class OpenServiceException extends RuntimeException {

    private final int status;

    public OpenServiceException(int status, String message) {
        super(message);
        this.status = status;
    }
}