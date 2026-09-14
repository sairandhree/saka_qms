export const tokenKey = "saka_qms_token";
export const userKey = "saka_qms_user";

let unauthorizedHandler = null;

export function setUnauthorizedHandler(handler) {
  unauthorizedHandler = handler;
}

export async function request(path, options = {}) {
  const token = localStorage.getItem(tokenKey);
  const response = await fetch(path, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(options.headers || {})
    }
  });

  if (response.status === 401) {
    localStorage.removeItem(tokenKey);
    localStorage.removeItem(userKey);
    unauthorizedHandler?.();
    throw new Error("Your session has expired. Please sign in again.");
  }

  if (!response.ok) {
    let message = "The request could not be completed.";
    try {
      const body = await response.json();
      message = body.message || body.error || message;
    } catch {
      // Keep the fallback when the server has no JSON response.
    }
    throw new Error(message);
  }

  return response.status === 204 ? null : response.json();
}
