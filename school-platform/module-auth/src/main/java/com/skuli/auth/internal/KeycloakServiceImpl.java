package com.skuli.auth.internal;

import com.skuli.auth.api.KeycloakProvisioningException;
import com.skuli.auth.api.KeycloakService;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

/**
 * Keycloak Admin API implementation of {@link KeycloakService}. Stateless: all state lives in
 * Keycloak; this only issues admin calls against the configured realm.
 */
@Service
public class KeycloakServiceImpl implements KeycloakService {

    private final Keycloak keycloak;
    private final String realm;

    public KeycloakServiceImpl(Keycloak keycloak, KeycloakProperties properties) {
        this.keycloak = keycloak;
        this.realm = properties.realm();
    }

    @Override
    public String createUser(NewUser user) {
        UserRepresentation representation = new UserRepresentation();
        representation.setUsername(user.username());
        representation.setEmail(user.email());
        representation.setFirstName(user.firstName());
        representation.setLastName(user.lastName());
        representation.setEnabled(true);
        representation.setEmailVerified(false);
        if (user.password() != null) {
            representation.setCredentials(List.of(passwordCredential(user.password())));
        }
        try (Response response = keycloak.realm(realm).users().create(representation)) {
            if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
                throw new KeycloakProvisioningException(
                        "Failed to create Keycloak user '" + user.username() + "': HTTP "
                                + response.getStatus());
            }
            return CreatedResponseUtil.getCreatedId(response);
        }
    }

    @Override
    public void assignRealmRole(String userId, String role) {
        RoleRepresentation roleRepresentation =
                keycloak.realm(realm).roles().get(role).toRepresentation();
        keycloak.realm(realm).users().get(userId).roles().realmLevel()
                .add(List.of(roleRepresentation));
    }

    @Override
    public Optional<String> findUserId(String username) {
        return keycloak.realm(realm).users().search(username, true).stream()
                .filter(u -> username.equalsIgnoreCase(u.getUsername()))
                .map(UserRepresentation::getId)
                .findFirst();
    }

    @Override
    public void updateUser(String userId, UpdateUser update) {
        UserResource userResource = keycloak.realm(realm).users().get(userId);
        UserRepresentation representation = userResource.toRepresentation();
        if (update.email() != null) {
            representation.setEmail(update.email());
        }
        if (update.firstName() != null) {
            representation.setFirstName(update.firstName());
        }
        if (update.lastName() != null) {
            representation.setLastName(update.lastName());
        }
        userResource.update(representation);
        if (update.password() != null) {
            userResource.resetPassword(passwordCredential(update.password()));
        }
    }

    @Override
    public void deleteUser(String userId) {
        try (Response response = keycloak.realm(realm).users().delete(userId)) {
            if (response.getStatus() >= Response.Status.BAD_REQUEST.getStatusCode()) {
                throw new KeycloakProvisioningException(
                        "Failed to delete Keycloak user '" + userId + "': HTTP "
                                + response.getStatus());
            }
        }
    }

    private static CredentialRepresentation passwordCredential(String password) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);
        return credential;
    }
}
