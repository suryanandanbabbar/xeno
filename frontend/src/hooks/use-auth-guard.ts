"use client";

import { useRouter } from "next/navigation";
import { useEffect } from "react";
import { tokenStore } from "@/lib/auth/token-store";

export function useAuthGuard() {
  const router = useRouter();
  useEffect(() => {
    if (!tokenStore.get()) router.replace("/login");
  }, [router]);
}
