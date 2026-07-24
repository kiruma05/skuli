"use client";

import { signOut } from "next-auth/react";
import Image from "next/image";

const SignOutButton = () => {
  return (
    <button
      onClick={() => signOut({ callbackUrl: "/" })}
      title="Logout"
      className="bg-white rounded-full w-7 h-7 flex items-center justify-center cursor-pointer"
    >
      <Image src="/logout.png" alt="Logout" width={20} height={20} />
    </button>
  );
};

export default SignOutButton;
