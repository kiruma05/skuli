import { auth } from "@/auth";

/**
 * Server-side client for the Spring backend. Attaches the signed-in user's Keycloak access token as
 * a Bearer credential so the backend can authenticate and tenant-scope the request. Used by server
 * components and server actions during the Prisma -> REST cutover (Phase 4).
 *
 * The base URL is the backend service: localhost for `npm run dev`, or the compose service name
 * when the Next app runs in Docker (set API_BASE_URL accordingly).
 */
const BASE_URL = process.env.API_BASE_URL ?? "http://localhost:8080";

async function bearer(): Promise<Record<string, string>> {
  const session = await auth();
  const token = (session as any)?.accessToken as string | undefined;
  return token ? { Authorization: `Bearer ${token}` } : {};
}

/** GET a JSON resource; throws on a non-2xx response. */
export async function apiGet<T>(path: string): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`, {
    method: "GET",
    headers: { ...(await bearer()) },
    // Always fetch fresh data for these dynamic dashboard reads.
    cache: "no-store",
  });
  if (!res.ok) {
    throw new Error(`GET ${path} failed: ${res.status}`);
  }
  return res.json() as Promise<T>;
}

/**
 * Send a JSON body with the given method. Returns the raw Response so callers can branch on status
 * (e.g. surface a 409 conflict differently from a 400 validation error).
 */
export async function apiSend(
  method: "POST" | "PUT" | "DELETE",
  path: string,
  body?: unknown
): Promise<Response> {
  return fetch(`${BASE_URL}${path}`, {
    method,
    headers: {
      "Content-Type": "application/json",
      ...(await bearer()),
    },
    body: body === undefined ? undefined : JSON.stringify(body),
    cache: "no-store",
  });
}
