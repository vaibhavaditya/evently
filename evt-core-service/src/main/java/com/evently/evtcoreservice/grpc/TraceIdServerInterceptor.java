
package com.evently.evtcoreservice.grpc;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.ForwardingServerCallListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class TraceIdServerInterceptor implements ServerInterceptor {

    private static final Logger log =
            LoggerFactory.getLogger(TraceIdServerInterceptor.class);

    private static final Metadata.Key<String> TRACE_ID_KEY =
            Metadata.Key.of(
                    "x-trace-id",
                    Metadata.ASCII_STRING_MARSHALLER
            );

    private static final Context.Key<String> TRACE_ID_CONTEXT_KEY =
            Context.key("traceId");

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next
    ) {
        String traceId = headers.get(TRACE_ID_KEY);

        if (traceId == null || traceId.isBlank()) {
            log.warn(
                    "No trace ID received for gRPC method {}",
                    call.getMethodDescriptor().getFullMethodName()
            );

            return next.startCall(call, headers);
        }

        try {
            MDC.put("traceId", traceId);

            log.info(
                    "Received trace ID {} for gRPC method {}",
                    traceId,
                    call.getMethodDescriptor().getFullMethodName()
            );
        } finally {
            MDC.remove("traceId");
        }

        Context context = Context.current()
                .withValue(TRACE_ID_CONTEXT_KEY, traceId);

        return Contexts.interceptCall(
                context,
                call,
                headers,
                new ServerCallHandler<>() {
                    @Override
                    public ServerCall.Listener<ReqT> startCall(
                            ServerCall<ReqT, RespT> interceptedCall,
                            Metadata interceptedHeaders
                    ) {
                        ServerCall.Listener<ReqT> listener =
                                next.startCall(interceptedCall, interceptedHeaders);

                        return new ForwardingServerCallListener
                                .SimpleForwardingServerCallListener<>(listener) {

                            @Override
                            public void onMessage(ReqT message) {
                                withTraceId(() -> super.onMessage(message));
                            }

                            @Override
                            public void onHalfClose() {
                                withTraceId(super::onHalfClose);
                            }

                            @Override
                            public void onReady() {
                                withTraceId(super::onReady);
                            }

                            @Override
                            public void onComplete() {
                                withTraceId(super::onComplete);
                            }

                            @Override
                            public void onCancel() {
                                withTraceId(super::onCancel);
                            }

                            private void withTraceId(Runnable action) {
                                String previous = MDC.get("traceId");
                                try {
                                    MDC.put("traceId", traceId);
                                    action.run();
                                } finally {
                                    if (previous == null) {
                                        MDC.remove("traceId");
                                    } else {
                                        MDC.put("traceId", previous);
                                    }
                                }
                            }
                        };
                    }
                }
        );
    }
}
