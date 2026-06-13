import { apiRequest } from "@/lib/api/client";

export type AuthResponse = {
  token: string;
  user: UserProfile;
};

export type UserProfile = {
  id: string;
  name: string;
  email: string;
  role: string;
};

export function login(payload: { email: string; password: string }) {
  return apiRequest<AuthResponse>("/auth/login", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export function register(payload: { name: string; email: string; password: string }) {
  return apiRequest<AuthResponse>("/auth/register", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export function getMe() {
  return apiRequest<UserProfile>("/auth/me");
}
