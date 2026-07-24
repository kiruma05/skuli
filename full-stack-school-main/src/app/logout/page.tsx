"use client";

import { useEffect } from "react";
import { signOut } from "next-auth/react";

/**
 * Real logout route. The sidebar Menu links here (`/logout`); previously no such route
 * existed, so the link fell through to the catch-all sign-in page and never actually signed
 * the user out. Signing out on mount clears the session and returns to the sign-in page.
 */
const LogoutPage = () => {
  useEffect(() => {
    signOut({ callbackUrl: "/" });
  }, []);

  return (
    <div className="h-screen flex items-center justify-center bg-lamaSkyLight text-gray-500">
      Signing out…
    </div>
  );
};

export default LogoutPage;
