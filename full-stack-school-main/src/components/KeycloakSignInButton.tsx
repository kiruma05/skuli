"use client";

import { signIn } from "next-auth/react";

const KeycloakSignInButton = () => {
  return (
    <button
      onClick={() => signIn("keycloak", { callbackUrl: "/" })}
      className="bg-blue-500 text-white my-1 rounded-md text-sm p-[10px]"
    >
      Sign in with Keycloak
    </button>
  );
};

export default KeycloakSignInButton;
