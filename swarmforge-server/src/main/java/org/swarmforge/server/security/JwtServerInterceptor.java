package org.swarmforge.server.security;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;

import java.util.List;

public class JwtServerInterceptor implements ServerInterceptor {

    private static final Metadata.Key<String> AUTHORIZATION_KEY = Metadata.Key.of("Authorization",
            Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String token = headers.get(AUTHORIZATION_KEY);

        // Allow unauthenticated access to specific methods (like Login)
        // This logic is simple: if "AuthService" is called, skip check
        String methodName = call.getMethodDescriptor().getFullMethodName();
        if (methodName.contains("AuthService/Login")) {
            return next.startCall(call, headers);
        }

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                String username = JwtUtil.getUsername(token);
                List<String> roles = JwtUtil.getRoles(token);

                Context context = Context.current()
                        .withValue(SecurityContext.USER_ID_KEY, username)
                        .withValue(SecurityContext.ROLES_KEY, roles);

                return Contexts.interceptCall(context, call, headers, next);

            } catch (Exception e) {
                call.close(Status.UNAUTHENTICATED.withDescription("Invalid Token: " + e.getMessage()), headers);
                return new ServerCall.Listener<>() {
                };
            }
        }

        // For Phase 8 MVP, we might allow guest access for some things, but let's be
        // strict for now
        // Or make it optional based on config.
        // Assuming user wants security.
        call.close(Status.UNAUTHENTICATED.withDescription("Missing Token"), headers);
        return new ServerCall.Listener<>() {
        };
    }
}
