package com.evently.evtcoreservice.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
      super(message);
    }
}
