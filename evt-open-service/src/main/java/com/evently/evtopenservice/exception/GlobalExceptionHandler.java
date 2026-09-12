package com.evently.evtopenservice.exception;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handles errors coming from evt-core-service through gRPC
    @ExceptionHandler(StatusRuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleGrpcException(
            StatusRuntimeException ex) {

        Status.Code code = ex.getStatus().getCode();

        HttpStatus httpStatus = switch (code) {
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case ALREADY_EXISTS -> HttpStatus.CONFLICT;
            case INVALID_ARGUMENT -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };

        String message = ex.getStatus().getDescription();

        if (message == null) {
            message = "Internal server error";
        }

        return ResponseEntity
                .status(httpStatus)
                .body(Map.of(
                        "success", false,
                        "message", message
                ));
    }

    // Handles @Valid validation errors from REST requests
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.joining(", "));

        if (message.isBlank()) {
            message = "Validation failed";
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "success", false,
                        "message", message
                ));
    }
}