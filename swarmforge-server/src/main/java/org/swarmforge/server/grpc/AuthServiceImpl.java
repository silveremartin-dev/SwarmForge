package org.swarmforge.server.grpc;

import io.grpc.stub.StreamObserver;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swarmforge.protocol.grpc.AuthServiceGrpc;
import org.swarmforge.protocol.grpc.LoginRequest;
import org.swarmforge.protocol.grpc.LoginResponse;
import org.swarmforge.server.security.JwtUtil;

import java.util.List;

/**
 * gRPC service implementation for user authentication and JWT token issuance.
 * Validates credentials via BCrypt salted hashes or secure environment variables.
 */
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

            LOG.info("User authenticated successfully: {}", username);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            LOG.warn("Failed login attempt for: {}", username);
            responseObserver.onError(io.grpc.Status.UNAUTHENTICATED
                    .withDescription("Invalid username or password")
                    .asRuntimeException());
        }
    }

    private boolean isValidUser(String username, String password) {
        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            return false;
        }

        boolean isProduction = "production".equalsIgnoreCase(System.getenv("SWARMFORGE_ENV"))
                || "prod".equalsIgnoreCase(System.getenv("SWARMFORGE_ENV"));

        String adminSecret = System.getenv("SWARMFORGE_ADMIN_PASSWORD");
        String userSecret = System.getenv("SWARMFORGE_USER_PASSWORD");

        if (adminSecret == null || userSecret == null) {
            if (isProduction) {
                LOG.error("[SECURITY] Refusing default credentials in production environment! SWARMFORGE_ADMIN_PASSWORD and SWARMFORGE_USER_PASSWORD must be configured.");
                return false;
            }
            if (adminSecret == null) adminSecret = "admin123";
            if (userSecret == null) userSecret = "user123";
            LOG.warn("⚠️ Security Notice: Using default credentials in non-production development mode.");
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
                LOG.error("BCrypt validation error: {}", e.getMessage());
                return false;
            }
        }
        return expectedHashOrPlain.equals(rawPassword);
    }
}
