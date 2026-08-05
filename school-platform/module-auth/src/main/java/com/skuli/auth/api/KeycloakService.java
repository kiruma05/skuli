package com.skuli.auth.api;

import java.util.Optional;

/**
 * Provisioning gateway to Keycloak, wrapping the Keycloak Admin REST API. This is the module's
 * exposed contract — other modules depend on this interface, never on the implementation. It
 * replaces the legacy {@code keycloak.ts} fetch calls.
 *
 * <p>Keycloak is not transactional with the database, so callers own the ordering and the
 * compensation: create the Keycloak user first then the DB row (deleting the Keycloak user if the
 * DB write fails), and on delete remove the DB row first then the Keycloak user (§5 of the plan).
 */
public interface KeycloakService {

    /**
     * Creates an enabled user with a permanent password and returns the generated Keycloak id.
     *
     * @throws KeycloakProvisioningException if Keycloak rejects the creation
     */
    String createUser(NewUser user);

    /** Grants a realm role (e.g. {@code teacher}, {@code student}) to an existing user. */
    void assignRealmRole(String userId, String role);

    /** Resolves a Keycloak user id from its (unique) username, if the user exists. */
    Optional<String> findUserId(String username);

    /** Propagates changed profile fields (and, when present, a new password) to Keycloak. */
    void updateUser(String userId, UpdateUser update);

    /** Removes a user from Keycloak. Used both for real deletes and for create-compensation. */
    void deleteUser(String userId);

    /** Attributes for creating a user. The username doubles as the application id (id == username). */
    record NewUser(String username, String email, String firstName, String lastName, String password) {
    }

    /** Mutable profile attributes; any {@code null} field is left unchanged in Keycloak. */
    record UpdateUser(String email, String firstName, String lastName, String password) {
    }
}
