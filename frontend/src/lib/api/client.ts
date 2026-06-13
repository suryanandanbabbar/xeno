import { tokenStore } from "@/lib/auth/token-store";

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080/api";

export class ApiError extends Error {
  constructor(message: string, public status: number) {
    super(message);
  }
}

export async function apiRequest<T>(path: string, init: RequestInit = {}): Promise<T> {
  const token = tokenStore.get();
  const isFormData = init.body instanceof FormData;
  const response = await fetch(`${API_URL}${path}`, {
    ...init,
    headers: {
      ...(isFormData ? {} : { "Content-Type": "application/json" }),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...init.headers
    }
  });

  if (!response.ok) {
    const body = await response.json().catch(() => ({ message: "Request failed" }));
    throw new ApiError(body.message ?? "Request failed", response.status);
  }

  if (response.status === 204) return undefined as T;
  return response.json() as Promise<T>;
}
