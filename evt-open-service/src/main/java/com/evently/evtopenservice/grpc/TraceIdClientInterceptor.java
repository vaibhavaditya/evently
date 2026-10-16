
package com.evently.evtopenservice.grpc;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class TraceIdClientInterceptor implements ClientInterceptor {

    private static final Logger log =
            LoggerFactory.getLogger(TraceIdClientInterceptor.class);

    private static final Metadata.Key<String> TRACE_ID_KEY =
            Metadata.Key.of(
                    "x-trace-id",
                    Metadata.ASCII_STRING_MARSHALLER
            );

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next
    ) {
        return new ForwardingClientCall.SimpleForwardingClientCall<>(
                next.newCall(method, callOptions)
        ) {
            @Override
            public void start(
                    Listener<RespT> responseListener,
                    Metadata headers
            ) {
                String traceId = MDC.get("traceId");

                if (traceId != null && !traceId.isBlank()) {
                    headers.put(TRACE_ID_KEY, traceId);

                    log.info(
                            "Propagating trace ID {} to Core Service via gRPC method {}",
                            traceId,
                            method.getFullMethodName()
                    );
                } else {
                    log.warn(
                            "No trace ID found in MDC for gRPC method {}",
                            method.getFullMethodName()
                    );
                }

                super.start(responseListener, headers);
            }
        };
    }
}
