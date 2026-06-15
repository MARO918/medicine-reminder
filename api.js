const API = "http://localhost:8080/api/medicines";

async function request(path = "", options = {}) {
  const res = await fetch(`${API}${path}`, {
    headers: { "Content-Type": "application/json" },
    ...options
  });

  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    const message = body.error || "API error";
    throw new Error(message);
  }

  if (res.status === 204) return null;
  return res.json();
}

export function listMedicines() {
  return request();
}

export function getMedicine(id) {
  return request(`/${id}`);
}

export function listToday() {
  return request("/today");
}

export function createMedicine(payload) {
  return request("", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export function updateMedicine(id, payload) {
  return request(`/${id}`, {
    method: "PUT",
    body: JSON.stringify(payload)
  });
}

export function deleteMedicine(id) {
  return request(`/${id}`, { method: "DELETE" });
}
