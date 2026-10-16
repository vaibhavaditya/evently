
package com.evently.evtcoreservice.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionServiceV1;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcServerConfig {

    @Bean
    public Server grpcServer(EventGrpcService eventGrpcService) {
        return ServerBuilder
                .forPort(9090)
                .addService(eventGrpcService)
                .intercept(new TraceIdServerInterceptor())
                .addService(ProtoReflectionServiceV1.newInstance())
                .build();
    }
}
