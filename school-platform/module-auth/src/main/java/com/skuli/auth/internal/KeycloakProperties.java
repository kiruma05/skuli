package com.skuli.auth.internal;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Keycloak connection settings, bound from {@code keycloak.*} (fed by {@code AUTH_KEYCLOAK_*}
 * environment variables). The admin client needs the server base URL and realm separately, so
 * both are derived from the OIDC issuer URI that the resource server already uses — one source of
 * truth for "where Keycloak is".
 */
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakProperties {

    private static final String REALMS_SEGMENT = "/realms/";

    /** e.g. {@code http://keycloak:8080/realms/skuli}. */
    private String issuerUri;
    private String clientId;
    private String clientSecret;

    /** Server base URL, i.e. the issuer with the {@code /realms/{realm}} suffix stripped. */
    public String serverUrl() {
        int idx = requireIssuer().indexOf(REALMS_SEGMENT);
        return issuerUri.substring(0, idx);
    }

    /** Realm name parsed from the issuer URI. */
    public String realm() {
        int idx = requireIssuer().indexOf(REALMS_SEGMENT);
        return issuerUri.substring(idx + REALMS_SEGMENT.length());
    }

    private String requireIssuer() {
        if (issuerUri == null || !issuerUri.contains(REALMS_SEGMENT)) {
            throw new IllegalStateException(
                    "keycloak.issuer-uri must be set and contain '/realms/<realm>': " + issuerUri);
        }
        return issuerUri;
    }

    public String getIssuerUri() {
        return issuerUri;
    }

    public void setIssuerUri(String issuerUri) {
        this.issuerUri = issuerUri;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
}
