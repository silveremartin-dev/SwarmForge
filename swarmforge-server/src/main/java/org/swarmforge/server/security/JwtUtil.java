package org.swarmforge.server.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Key;
import java.util.Date;
import java.util.List;

/**
 * Utility for JSON Web Token (JWT) issuing, cryptographic signing, and verification.
 * Follows RFC 7519 standards and HMAC SHA-256 signatures for gRPC microservice security.
 */
public final class JwtUtil {

    private static final Logger LOG = LoggerFactory.getLogger(JwtUtil.class);
    private static final Key SECRET_KEY;
    private static final long EXPIRATION_TIME = 86_400_000L; // 24 hours

    static {
        String envSecret = System.getenv("SWARMFORGE_JWT_SECRET");
        if (envSecret != null && envSecret.length() >= 32) {
            SECRET_KEY = Keys.hmacShaKeyFor(envSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } else {
            LOG.warn("[SECURITY] SWARMFORGE_JWT_SECRET not configured (requires at least 32 characters). Generating ephemeral cryptographically secure in-memory signing key.");
            SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        }
    }

    private JwtUtil() {
        // Utility class
    }

    /**
     * Issues a signed JWT with subject identity and assigned authorization roles.
     *
     * @param username the user identifier
     * @param roles    the list of granted security roles
     * @return the serialized signed compact JWT string
     */
    public static String generateToken(String username, List<String> roles) {
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * Validates and parses claims from the given compact JWT string.
     *
     * @param token the serialized JWT string
     * @return the verified claims body
     */
    public static Claims validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extracts the subject username from a valid JWT.
     *
     * @param token the serialized JWT string
     * @return the subject username
     */
    public static String getUsername(String token) {
        return validateToken(token).getSubject();
    }

    /**
     * Extracts the granted authorization roles from a valid JWT.
     *
     * @param token the serialized JWT string
     * @return the list of roles
     */
    @SuppressWarnings("unchecked")
    public static List<String> getRoles(String token) {
        return validateToken(token).get("roles", List.class);
    }
}
