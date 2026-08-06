import NextAuth from "next-auth";
import Keycloak from "next-auth/providers/keycloak";

// Roles this app understands. Assign these as *realm roles* in Keycloak.
const APP_ROLES = ["admin", "teacher", "student", "parent"] as const;

// Edge-safe JWT payload decode (no Buffer / Node crypto).
function decodeJwtPayload(token: string): Record<string, any> {
  try {
    const payload = token.split(".")[1];
    const json = atob(payload.replace(/-/g, "+").replace(/_/g, "/"));
    return JSON.parse(json);
  } catch {
    return {};
  }
}

/**
 * Exchanges the stored refresh token for a fresh access token at the Keycloak token endpoint.
 * Called from the jwt callback once the current access token has expired, so the Bearer token the
 * app sends to the Spring backend stays valid across a session.
 */
async function refreshAccessToken(token: any): Promise<any> {
  try {
    const issuer = process.env.AUTH_KEYCLOAK_ISSUER;
    const res = await fetch(`${issuer}/protocol/openid-connect/token`, {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({
        grant_type: "refresh_token",
        client_id: process.env.AUTH_KEYCLOAK_ID!,
        client_secret: process.env.AUTH_KEYCLOAK_SECRET!,
        refresh_token: token.refreshToken,
      }),
    });
    const refreshed = await res.json();
    if (!res.ok) throw refreshed;
    return {
      ...token,
      accessToken: refreshed.access_token,
      expiresAt: Math.floor(Date.now() / 1000) + refreshed.expires_in,
      // Keycloak rotates refresh tokens; fall back to the old one if none returned.
      refreshToken: refreshed.refresh_token ?? token.refreshToken,
      error: undefined,
    };
  } catch {
    return { ...token, error: "RefreshAccessTokenError" };
  }
}

export const { handlers, auth, signIn, signOut } = NextAuth({
  // Needed when running behind Docker / a reverse proxy.
  trustHost: true,
  // Reads AUTH_KEYCLOAK_ID / AUTH_KEYCLOAK_SECRET / AUTH_KEYCLOAK_ISSUER from env.
  providers: [Keycloak],
  callbacks: {
    async jwt({ token, account }) {
      // `account` is only present on the initial sign-in: capture the tokens then.
      if (account?.access_token) {
        const decoded = decodeJwtPayload(account.access_token);
        const roles: string[] = decoded?.realm_access?.roles ?? [];
        // Pick the first Keycloak realm role that this app cares about.
        token.role =
          roles.find((r) => (APP_ROLES as readonly string[]).includes(r)) ??
          null;
        // The app links people by username (DB id === username in the seed).
        token.username = decoded?.preferred_username ?? token.username;
        token.accessToken = account.access_token;
        token.refreshToken = account.refresh_token;
        token.expiresAt = account.expires_at; // epoch seconds
        return token;
      }

      // Subsequent calls: reuse the access token until it expires, then refresh.
      const expiresAt = (token as any).expiresAt as number | undefined;
      if (expiresAt && Date.now() < expiresAt * 1000) {
        return token;
      }
      return await refreshAccessToken(token);
    },
    async session({ session, token }) {
      if (session.user) {
        // Clerk used the user id as the app identity; we use the Keycloak username.
        (session.user as any).id = (token as any).username ?? token.sub;
        (session.user as any).role = (token as any).role ?? null;
      }
      // Expose the access token so server-side code can call the Spring backend.
      (session as any).accessToken = (token as any).accessToken ?? null;
      (session as any).error = (token as any).error ?? null;
      return session;
    },
  },
});
