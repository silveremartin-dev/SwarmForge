package org.swarmforge.server.grpc;

import io.grpc.stub.StreamObserver;
import org.swarmforge.protocol.grpc.AuthServiceGrpc;
import org.swarmforge.protocol.grpc.LoginRequest;
import org.swarmforge.protocol.grpc.LoginResponse;
import org.swarmforge.server.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AuthServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {

    private static final Logger LOG = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Override
    public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
        String username = request.getUsername();
        String password = request.getPassword();

        // MVP: Hardcoded users map or allow "admin" with specific password
        // In real impl, check database using Bcrypt
        if (isValidUser(username, password)) {
            List<String> roles = username.equals("admin") ? List.of("ADMIN", "USER") : List.of("USER");
            String token = JwtUtil.generateToken(username, roles);

            LoginResponse response = LoginResponse.newBuilder()
                    .setToken(token)
                    .setExpiresAtEpochSeconds(System.currentTimeMillis() / 1000 + 86400)
                    .addAllRoles(roles)
                    .build();

            LOG.info("User logged in: " + username);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            LOG.warn("Failed login attempt for: " + username);
            responseObserver.onError(io.grpc.Status.UNAUTHENTICATED
                    .withDescription("Invalid username or password")
                    .asRuntimeException());
        }
    }

    private boolean isValidUser(String username, String password) {
        String adminPass = System.getenv().getOrDefault("SWARMFORGE_ADMIN_PASSWORD", "admin123");
        String userPass = System.getenv().getOrDefault("SWARMFORGE_USER_PASSWORD", "user123");

        if ("admin".equals(username) && adminPass.equals(password))
            return true;
        if ("user".equals(username) && userPass.equals(password))
            return true;
        return false;
    }
}
