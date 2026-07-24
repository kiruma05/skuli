import Image from "next/image";
import { redirect } from "next/navigation";
import { auth } from "@/lib/authCompat";
import KeycloakSignInButton from "@/components/KeycloakSignInButton";

const LoginPage = async () => {
  const { sessionClaims } = await auth();
  const role = (sessionClaims?.metadata as { role?: string })?.role;

  // Already signed in -> go straight to the role dashboard.
  if (role) {
    redirect(`/${role}`);
  }

  return (
    <div className="h-screen flex items-center justify-center bg-lamaSkyLight">
      <div className="bg-white p-12 rounded-md shadow-2xl flex flex-col gap-2">
        <h1 className="text-xl font-bold flex items-center gap-2">
          <Image src="/logo.png" alt="" width={24} height={24} />
          Kiruma High School
        </h1>
        <h2 className="text-gray-400">Sign in to your account</h2>
        <KeycloakSignInButton />
      </div>
    </div>
  );
};

export default LoginPage;
