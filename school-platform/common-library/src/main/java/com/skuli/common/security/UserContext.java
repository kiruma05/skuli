package com.skuli.common.security;

import java.util.Set;

/**
 * Holds the current request's authenticated principal and its <em>row-level visibility scope</em>,
 * populated once per request (from the JWT plus a lookup of the user's relationships) and read by
 * module services to filter what the caller may see (§7 row-level access).
 *
 * <p>The scope is resolved in the app layer — which can see every module — and exposed here as
 * plain data, so a module service can filter its own rows without depending on another module
 * (avoiding a module dependency cycle).
 *
 * <p>Semantics by role:
 * <ul>
 *   <li><b>admin</b> — {@code role="admin"}; no restriction.</li>
 *   <li><b>teacher</b> — {@code role="teacher"}; a module scopes by teacher ownership using
 *       {@link #userId()} (teacher id == username).</li>
 *   <li><b>student</b> — {@link #classIds()} = the student's class, {@link #studentIds()} = the
 *       student's own id.</li>
 *   <li><b>parent</b> — {@link #classIds()}/{@link #studentIds()} = those of the parent's children.</li>
 * </ul>
 */
public final class UserContext {

    /** The authenticated principal and its resolved visibility scope. */
    public record Principal(String userId, String role, Set<Integer> classIds, Set<String> studentIds) {

        public boolean isAdmin() {
            return "admin".equals(role);
        }

        public boolean isTeacher() {
            return "teacher".equals(role);
        }
    }

    private static final ThreadLocal<Principal> CURRENT = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(Principal principal) {
        CURRENT.set(principal);
    }

    public static Principal get() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }
}
