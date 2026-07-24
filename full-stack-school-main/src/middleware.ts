import { auth } from "@/auth";
import { routeAccessMap } from "@/lib/settings";
import { NextResponse } from "next/server";

// Compile each route pattern from settings into a RegExp. Patterns may contain
// groups like "(.*)", which are already valid regex.
const matchers = Object.entries(routeAccessMap).map(([route, allowedRoles]) => ({
  matcher: new RegExp(`^${route}$`),
  allowedRoles,
}));

export default auth((req) => {
  const { pathname } = req.nextUrl;
  const role = (req.auth?.user as { role?: string } | undefined)?.role;

  const matched = matchers.filter(({ matcher }) => matcher.test(pathname));

  // Public route (e.g. the sign-in page at "/") — let it through.
  if (matched.length === 0) return NextResponse.next();

  // Not signed in and hitting a protected route -> send to sign-in.
  if (!req.auth) {
    return NextResponse.redirect(new URL("/", req.url));
  }

  for (const { allowedRoles } of matched) {
    if (!allowedRoles.includes(role!)) {
      // Signed in but wrong role -> bounce to their own dashboard.
      return NextResponse.redirect(new URL(role ? `/${role}` : "/", req.url));
    }
  }

  return NextResponse.next();
});

export const config = {
  matcher: [
    // Skip Next.js internals and all static files, unless found in search params
    "/((?!_next|[^?]*\\.(?:html?|css|js(?!on)|jpe?g|webp|png|gif|svg|ttf|woff2?|ico|csv|docx?|xlsx?|zip|webmanifest)).*)",
    // Always run for API routes
    "/(api|trpc)(.*)",
  ],
};
