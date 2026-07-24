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

export const { handlers, auth, signIn, signOut } = NextAuth({
  // Needed when running behind Docker / a reverse proxy.
  trustHost: true,
  // Reads AUTH_KEYCLOAK_ID / AUTH_KEYCLOAK_SECRET / AUTH_KEYCLOAK_ISSUER from env.
  providers: [Keycloak],
  callbacks: {
    async jwt({ token, account }) {
      // `account` is only present on the initial sign-in.
      if (account?.access_token) {
        const decoded = decodeJwtPayload(account.access_token);
        const roles: string[] = decoded?.realm_access?.roles ?? [];
        // Pick the first Keycloak realm role that this app cares about.
        token.role =
          roles.find((r) => (APP_ROLES as readonly string[]).includes(r)) ??
          null;
        // The app links people by username (DB id === username in the seed).
        token.username = decoded?.preferred_username ?? token.username;
      }
      return token;
    },
    async session({ session, token }) {
      if (session.user) {
        // Clerk used the user id as the app identity; we use the Keycloak username.
        (session.user as any).id = (token as any).username ?? token.sub;
        (session.user as any).role = (token as any).role ?? null;
      }
      return session;
    },
  },
});
