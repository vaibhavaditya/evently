package com.evently.evtcoreservice.grpc;

import com.evently.evtcoreservice.exception.BadRequestException;
import com.evently.evtcoreservice.exception.ConflictException;
import com.evently.evtcoreservice.exception.ResourceNotFoundException;
import io.grpc.Status;

public class GrpcExceptionMapper {

    public static RuntimeException toGrpcException(Exception exception) {

        if (exception instanceof ResourceNotFoundException) {
            return Status.NOT_FOUND
                    .withDescription(exception.getMessage())
                    .asRuntimeException();
        }

        if (exception instanceof ConflictException) {
            return Status.ALREADY_EXISTS
                    .withDescription(exception.getMessage())
                    .asRuntimeException();
        }

        if (exception instanceof BadRequestException) {
            return Status.INVALID_ARGUMENT
                    .withDescription(exception.getMessage())
                    .asRuntimeException();
        }

        return Status.INTERNAL
                .withDescription("Internal server error")
                .asRuntimeException();
    }
}