package com.skuli.common.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Converts a Keycloak JWT into an authentication token, mapping {@code realm_access.roles}
 * to Spring {@code ROLE_*} authorities (mirrors what the current Next.js {@code auth.ts} decodes).
 * The {@code tenant_id} claim is read separately by {@link TenantContextFilter}.
 */
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String REALM_ACCESS = "realm_access";
    private static final String ROLES = "roles";
    private static final String ROLE_PREFIX = "ROLE_";

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        return new JwtAuthenticationToken(jwt, extractAuthorities(jwt));
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim(REALM_ACCESS);
        if (realmAccess == null) {
            return List.of();
        }
        Object roles = realmAccess.get(ROLES);
        if (!(roles instanceof Collection<?> roleList)) {
            return List.of();
        }
        return roleList.stream()
                .map(Object::toString)
                .map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role))
                .collect(Collectors.toUnmodifiableList());
    }

    /** Reads the scalar {@code tenant_id} claim (one user = one school). */
    public static String tenantId(Jwt jwt) {
        return jwt.getClaimAsString("tenant_id");
    }
}
