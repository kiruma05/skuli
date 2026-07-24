/**
 * Keycloak Admin API helper.
 *
 * Uses the client-credentials (service account) grant of the same Keycloak
 * client the app authenticates with, so no extra admin username/password is
 * needed. Requirements in Keycloak for the `school-app` client:
 *   - "Service accounts roles" (Client authentication) enabled
 *   - Service account assigned the `realm-management` roles:
 *       manage-users, view-users, query-users
 *
 * All calls run server-side (Node runtime) from server actions.
 */

const ISSUER = process.env.AUTH_KEYCLOAK_ISSUER; // e.g. http://localhost:8080/realms/skuli
const CLIENT_ID = process.env.AUTH_KEYCLOAK_ID;
const CLIENT_SECRET = process.env.AUTH_KEYCLOAK_SECRET;

export type KeycloakRole = "admin" | "teacher" | "student" | "parent";

function config() {
  if (!ISSUER || !CLIENT_ID || !CLIENT_SECRET) {
    throw new Error(
      "Keycloak env not configured (AUTH_KEYCLOAK_ISSUER / _ID / _SECRET)."
    );
  }
  const [baseUrl, realm] = ISSUER.split("/realms/");
  if (!realm) throw new Error(`Invalid AUTH_KEYCLOAK_ISSUER: ${ISSUER}`);
  return {
    tokenUrl: `${ISSUER}/protocol/openid-connect/token`,
    adminBase: `${baseUrl}/admin/realms/${realm}`,
  };
}

async function getAdminToken(): Promise<string> {
  const { tokenUrl } = config();
  const res = await fetch(tokenUrl, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({
      grant_type: "client_credentials",
      client_id: CLIENT_ID!,
      client_secret: CLIENT_SECRET!,
    }),
    cache: "no-store",
  });
  if (!res.ok) {
    throw new Error(`Keycloak token request failed: ${res.status}`);
  }
  const json = await res.json();
  return json.access_token as string;
}

async function findUserId(
  token: string,
  username: string
): Promise<string | null> {
  const { adminBase } = config();
  const res = await fetch(
    `${adminBase}/users?username=${encodeURIComponent(username)}&exact=true`,
    { headers: { Authorization: `Bearer ${token}` }, cache: "no-store" }
  );
  if (!res.ok) throw new Error(`Keycloak user lookup failed: ${res.status}`);
  const users = (await res.json()) as Array<{ id: string }>;
  return users[0]?.id ?? null;
}

async function assignRealmRole(
  token: string,
  userId: string,
  role: KeycloakRole
) {
  const { adminBase } = config();
  const roleRes = await fetch(`${adminBase}/roles/${role}`, {
    headers: { Authorization: `Bearer ${token}` },
    cache: "no-store",
  });
  if (!roleRes.ok) {
    throw new Error(
      `Keycloak realm role "${role}" not found (${roleRes.status}). Create it under Realm roles.`
    );
  }
  const roleRep = await roleRes.json();
  const res = await fetch(
    `${adminBase}/users/${userId}/role-mappings/realm`,
    {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify([{ id: roleRep.id, name: roleRep.name }]),
    }
  );
  if (!res.ok) {
    throw new Error(`Keycloak role assignment failed: ${res.status}`);
  }
}

export async function createKeycloakUser(params: {
  username: string;
  password?: string;
  firstName?: string;
  lastName?: string;
  email?: string | null;
  role: KeycloakRole;
}): Promise<string> {
  const { adminBase } = config();
  const token = await getAdminToken();

  const res = await fetch(`${adminBase}/users`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      username: params.username,
      firstName: params.firstName,
      lastName: params.lastName,
      email: params.email || undefined,
      enabled: true,
      ...(params.password
        ? {
            credentials: [
              { type: "password", value: params.password, temporary: false },
            ],
          }
        : {}),
    }),
  });

  if (res.status !== 201) {
    // 409 = user already exists
    throw new Error(`Keycloak user creation failed: ${res.status}`);
  }

  // The new user's id is in the Location header.
  const location = res.headers.get("location");
  let userId = location?.split("/").pop() ?? null;
  if (!userId) userId = await findUserId(token, params.username);
  if (!userId) throw new Error("Could not resolve new Keycloak user id.");

  await assignRealmRole(token, userId, params.role);
  return userId;
}

export async function updateKeycloakUser(
  currentUsername: string,
  params: {
    username?: string;
    password?: string;
    firstName?: string;
    lastName?: string;
    email?: string | null;
  }
): Promise<void> {
  const { adminBase } = config();
  const token = await getAdminToken();
  const userId = await findUserId(token, currentUsername);
  if (!userId) throw new Error(`Keycloak user "${currentUsername}" not found.`);

  const res = await fetch(`${adminBase}/users/${userId}`, {
    method: "PUT",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      ...(params.username ? { username: params.username } : {}),
      ...(params.firstName ? { firstName: params.firstName } : {}),
      ...(params.lastName ? { lastName: params.lastName } : {}),
      ...(params.email !== undefined ? { email: params.email || undefined } : {}),
    }),
  });
  if (!res.ok) throw new Error(`Keycloak user update failed: ${res.status}`);

  if (params.password) {
    const pwRes = await fetch(`${adminBase}/users/${userId}/reset-password`, {
      method: "PUT",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        type: "password",
        value: params.password,
        temporary: false,
      }),
    });
    if (!pwRes.ok) {
      throw new Error(`Keycloak password reset failed: ${pwRes.status}`);
    }
  }
}

export async function deleteKeycloakUser(username: string): Promise<void> {
  const { adminBase } = config();
  const token = await getAdminToken();
  const userId = await findUserId(token, username);
  // Already gone — nothing to do.
  if (!userId) return;

  const res = await fetch(`${adminBase}/users/${userId}`, {
    method: "DELETE",
    headers: { Authorization: `Bearer ${token}` },
  });
  if (!res.ok && res.status !== 404) {
    throw new Error(`Keycloak user deletion failed: ${res.status}`);
  }
}
