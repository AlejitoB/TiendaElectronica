import { tienda } from "./tienda.js";

export async function sesion(method = "GET", datos) {
  const r = await fetch("/api/sesion", {
    method, headers: { "Content-Type": "application/json" },
    body: datos ? JSON.stringify(datos) : undefined,
  });
  const data = await r.json();
  if (method === "GET" && r.status === 401) return null;
  if (!r.ok) throw Error(data.message);
  if (method !== "GET") {
    tienda.update(estado => ({ ...estado, recibo: null, intento: null }));
  }
  return data.rol;
}
