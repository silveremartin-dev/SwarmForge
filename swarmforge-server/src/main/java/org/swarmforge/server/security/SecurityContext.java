/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.server.security;

import io.grpc.Context;
import java.util.List;

/**
 * gRPC Thread-Local Security Context accessor for SwarmForge distributed servers.
 *
 * <p>Stores authenticated user identities and security role principals populated by
 * {@link JwtServerInterceptor} during gRPC transport calls.</p>
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public final class SecurityContext {

    private SecurityContext() {
        // Static utility context accessor
    }

    public static final Context.Key<String> USER_ID_KEY = Context.key("userId");
    public static final Context.Key<List<String>> ROLES_KEY = Context.key("roles");

    /**
     * Gets the authenticated user ID associated with the current gRPC thread context.
     *
     * @return User ID string or null if unauthenticated.
     */
    public static String getCurrentUser() {
        return USER_ID_KEY.get();
    }

    /**
     * Gets the security roles assigned to the current authenticated caller.
     *
     * @return List of role names, or null if unauthenticated.
     */
    public static List<String> getCurrentRoles() {
        return ROLES_KEY.get();
    }

    /**
     * Checks if the authenticated user has the specified security role.
     *
     * @param role Role name to verify (e.g. "ADMIN", "RESEARCHER").
     * @return true if caller possesses the role.
     */
    public static boolean hasRole(String role) {
        List<String> roles = getCurrentRoles();
        return roles != null && roles.contains(role);
    }

    /**
     * Checks if the current thread context has an authenticated user principal.
     *
     * @return true if authenticated.
     */
    public static boolean isAuthenticated() {
        return getCurrentUser() != null;
    }
}
