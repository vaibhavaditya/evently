package com.evently.evtcoreservice.grpc;

import io.grpc.Server;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Component
@RequiredArgsConstructor
public class GrpcServerRunner {
    private final Server grpcServer;

    @PostConstruct
    public void start() throws Exception {
        grpcServer.start();

        System.out.println("gRPC server started on port 9090");
    }

    @PreDestroy
    public void stop() throws Exception {
        grpcServer.shutdown();
    }
}
