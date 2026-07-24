import { auth as nextAuth } from "@/auth";

/**
 * Clerk-compatible replacements backed by Auth.js + Keycloak, so the existing
 * pages/components keep working with minimal edits.
 *
 * Note: unlike Clerk's synchronous `auth()`, these are async — call sites must
 * `await` them.
 */

export const auth = async () => {
  const session = await nextAuth();
  const user = session?.user as { id?: string; role?: string } | undefined;
  return {
    userId: user?.id ?? null,
    sessionClaims: session
      ? { metadata: { role: user?.role ?? null } }
      : null,
  };
};

export const currentUser = async () => {
  const session = await nextAuth();
  const user = session?.user as { id?: string; role?: string } | undefined;
  if (!user) return null;
  return {
    id: user.id ?? null,
    publicMetadata: { role: user.role ?? null },
  };
};
