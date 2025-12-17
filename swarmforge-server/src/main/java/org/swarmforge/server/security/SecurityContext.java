package org.swarmforge.server.security;

import io.grpc.Context;
import java.util.List;

public class SecurityContext {

    public static final Context.Key<String> USER_ID_KEY = Context.key("userId");
    public static final Context.Key<List<String>> ROLES_KEY = Context.key("roles");

    public static String getCurrentUser() {
        return USER_ID_KEY.get();
    }

    public static List<String> getCurrentRoles() {
        return ROLES_KEY.get();
    }

    public static boolean hasRole(String role) {
        List<String> roles = getCurrentRoles();
        return roles != null && roles.contains(role);
    }

    public static boolean isAuthenticated() {
        return getCurrentUser() != null;
    }
}
