const TOKEN_KEY = "xenopilot.access_token";

export const tokenStore = {
  get() {
    if (typeof window === "undefined") return null;
    return window.localStorage.getItem(TOKEN_KEY);
  },
  set(token: string) {
    window.localStorage.setItem(TOKEN_KEY, token);
  },
  clear() {
    if (typeof window !== "undefined") {
      window.localStorage.removeItem(TOKEN_KEY);
    }
  }
};
