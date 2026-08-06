package com.skuli.app.config;

import com.skuli.common.security.UserContext;
import com.skuli.student.api.StudentDirectory;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Populates {@link UserContext} for the request: the authenticated user's id + role, and — for
 * students/parents — their row-level visibility scope (own class / children), resolved via
 * {@link StudentDirectory}. Module services read this to filter rows without depending on another
 * module. Registered in the security chain after the tenant filter (so the lookups are
 * tenant-scoped) once authentication is established.
 */
public class UserContextFilter extends OncePerRequestFilter {

    private static final Set<String> APP_ROLES = Set.of("admin", "teacher", "student", "parent");

    private final StudentDirectory studentDirectory;

    public UserContextFilter(StudentDirectory studentDirectory) {
        this.studentDirectory = studentDirectory;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            if (SecurityContextHolder.getContext().getAuthentication()
                    instanceof JwtAuthenticationToken jwt) {
                String userId = jwt.getToken().getClaimAsString("preferred_username");
                String role = jwt.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .filter(a -> a.startsWith("ROLE_"))
                        .map(a -> a.substring("ROLE_".length()))
                        .filter(APP_ROLES::contains)
                        .findFirst()
                        .orElse(null);
                UserContext.set(resolve(userId, role));
            }
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }

    private UserContext.Principal resolve(String userId, String role) {
        Set<Integer> classIds = Set.of();
        Set<String> studentIds = Set.of();
        // In sliced web tests there is no StudentDirectory bean; skip scope resolution then.
        if (studentDirectory == null) {
            return new UserContext.Principal(userId, role, classIds, studentIds);
        }
        if ("student".equals(role) && userId != null) {
            classIds = studentDirectory.classIdOf(userId).map(Set::of).orElse(Set.of());
            studentIds = Set.of(userId);
        } else if ("parent".equals(role) && userId != null) {
            var children = studentDirectory.childrenOf(userId);
            classIds = children.stream().map(StudentDirectory.ChildRef::classId)
                    .filter(Objects::nonNull).collect(Collectors.toSet());
            studentIds = children.stream().map(StudentDirectory.ChildRef::studentId)
                    .collect(Collectors.toSet());
        }
        return new UserContext.Principal(userId, role, classIds, studentIds);
    }
}
