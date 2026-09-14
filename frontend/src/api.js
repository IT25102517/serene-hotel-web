let credentials = "";
export function setCredentials(user, password) {
  credentials = btoa(`${user}:${password}`);
}
export function clearCredentials() {
  credentials = "";
}
export async function api(path, options = {}) {
  const response = await fetch(`/api${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(credentials ? { Authorization: `Basic ${credentials}` } : {}),
      ...options.headers,
    },
  });
  if (response.status === 204) return null;
  const data = await response.json().catch(() => ({}));
  if (!response.ok)
    throw new Error(
      data.message ||
        (response.status === 401
          ? "Please sign in with a valid evaluation account."
          : response.status === 403
            ? "Your role cannot access this action."
            : "The request failed. Please try again."),
    );
  return data;
}
