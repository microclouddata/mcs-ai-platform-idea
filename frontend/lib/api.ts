const API_BASE = process.env.NEXT_PUBLIC_API_BASE || "http://localhost:8081/api";

export async function apiFetch<T = unknown>(path: string, options: RequestInit = {}): Promise<T> {
  const token = typeof window !== "undefined" ? localStorage.getItem("token") : null;

  const headers: Record<string, string> = {
    ...(options.headers as Record<string, string> || {}),
  };

  if (!(options.body instanceof FormData)) {
    headers["Content-Type"] = "application/json";
  }

  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  const res = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers,
  });

  let data: Record<string, unknown>;
  try {
    data = await res.json();
  } catch {
    const text = await res.text().catch(() => "Request failed");
    throw new Error(text || "Request failed");
  }

  if (!res.ok || data.success === false) {
    throw new Error((data.message as string) || "Request failed");
  }

  return data.data as T;
}