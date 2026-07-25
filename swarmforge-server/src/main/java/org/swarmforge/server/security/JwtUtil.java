package org.swarmforge.server.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.List;

public class JwtUtil {

    private static final Key SECRET_KEY;
    private static final long EXPIRATION_TIME = 86400000; // 24 hours

    static {
        String envSecret = System.getenv("SWARMFORGE_JWT_SECRET");
        if (envSecret != null && envSecret.length() >= 32) {
            SECRET_KEY = Keys.hmacShaKeyFor(envSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } else {
            SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        }
    }

    public static String generateToken(String username, List<String> roles) {
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    public static Claims validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public static String getUsername(String token) {
        return validateToken(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public static List<String> getRoles(String token) {
        return validateToken(token).get("roles", List.class);
    }
}
