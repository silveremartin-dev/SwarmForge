package org.swarmforge.server.grpc;

import io.grpc.stub.StreamObserver;
import org.mindrot.jbcrypt.BCrypt;
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

        if (isValidUser(username, password)) {
            List<String> roles = "admin".equalsIgnoreCase(username) ? List.of("ADMIN", "USER") : List.of("USER");
            String token = JwtUtil.generateToken(username, roles);

            LoginResponse response = LoginResponse.newBuilder()
                    .setToken(token)
                    .setExpiresAtEpochSeconds(System.currentTimeMillis() / 1000 + 86400)
                    .addAllRoles(roles)
                    .build();

            LOG.info("User authenticated successfully: " + username);
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
        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            return false;
        }

        String adminSecret = System.getenv().getOrDefault("SWARMFORGE_ADMIN_PASSWORD", "admin123");
        String userSecret = System.getenv().getOrDefault("SWARMFORGE_USER_PASSWORD", "user123");

        if ("admin123".equals(adminSecret) || "user123".equals(userSecret)) {
            LOG.warn("⚠️ Security Notice: Using default credentials in non-production environment.");
        }

        if ("admin".equalsIgnoreCase(username)) {
            return verifyPassword(password, adminSecret);
        } else if ("user".equalsIgnoreCase(username)) {
            return verifyPassword(password, userSecret);
        }
        return false;
    }

    private boolean verifyPassword(String rawPassword, String expectedHashOrPlain) {
        if (expectedHashOrPlain.startsWith("$2a$") || expectedHashOrPlain.startsWith("$2b$") || expectedHashOrPlain.startsWith("$2y$")) {
            try {
                return BCrypt.checkpw(rawPassword, expectedHashOrPlain);
            } catch (Exception e) {
                LOG.error("BCrypt validation error: " + e.getMessage());
                return false;
            }
        }
        return expectedHashOrPlain.equals(rawPassword);
    }
}
