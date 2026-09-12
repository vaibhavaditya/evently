package com.evently.evtopenservice.grpc;

import com.evently.grpc.EventServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {

    @Value("${event.core.grpc.host}")
    private String host;

    @Value("${event.core.grpc.port}")
    private int port;

    @Bean
    public ManagedChannel eventCoreChannel() {

        return ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build();
    }

    @Bean
    public EventServiceGrpc.EventServiceBlockingStub eventServiceStub(
            ManagedChannel eventCoreChannel) {

        return EventServiceGrpc
                .newBlockingStub(eventCoreChannel);
    }
}