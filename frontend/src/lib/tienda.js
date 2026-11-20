import { writable, derived, get } from "svelte/store";
export const catalogo = writable([]);
export const problema = writable("");
export async function actualizar() {
  try {
    const r = await fetch("/api/productos");
    if (!r.headers.get("content-type")?.includes("application/json")) {
      throw Error("La API /api/productos devolvió una respuesta inesperada (HTTP " + r.status + "). Comprueba el servidor de la tienda.");
    }
    const data = await r.json();
    if (!r.ok) throw Error(data.message);
    catalogo.set(data);
    problema.set("");
  } catch (e) {
    problema.set(e.message);
  }
}
export const moneda = (n) =>
  new Intl.NumberFormat("es-CO", {
    style: "currency",
    currency: "COP",
    maximumFractionDigits: 0,
  }).format(n);
const inicial = () => ({ carrito: {}, vendidos: {}, recibo: null });
export const tienda = writable(inicial());
const clave = "tienda-electronica.demo.v1";
export function restaurar() {
  actualizar();
  try {
    const data = JSON.parse(sessionStorage.getItem(clave));
    if (data?.carrito && data?.vendidos) tienda.set(data);
  } catch {
    /* Una sesión nueva puede empezar sin almacenamiento. */
  }
  return tienda.subscribe((data) => {
    try {
      sessionStorage.setItem(clave, JSON.stringify(data));
    } catch {}
  });
}
export const lineas = derived([tienda, catalogo], ([s, productos]) =>
  productos
    .filter((p) => s.carrito[p.serial] > 0)
    .map((p) => ({ ...p, cantidad: s.carrito[p.serial] })),
);
export const total = derived(lineas, (items) =>
  items.reduce((n, p) => n + p.precio * p.cantidad, 0),
);
export const unidades = derived(lineas, (items) =>
  items.reduce((n, p) => n + p.cantidad, 0),
);
export function disponibles(p, s) {
  return p.stock;
}
export function cantidad(p, valor) {
  tienda.update((s) => ({
    ...s,
    carrito: {
      ...s.carrito,
      [p.serial]: Math.max(0, Math.min(disponibles(p, s), valor)),
    },
  }));
}
export async function confirmar(datos) {
  const items = get(lineas).map(({ serial, cantidad, precio }) => ({
    serial,
    cantidad,
    precio,
  }));
  const signature = JSON.stringify([items, datos.nombre, datos.direccion, datos.numero.slice(-4), datos.vencimiento]);
  let intento = get(tienda).intento;
  if (intento?.signature !== signature) {
    intento = { signature, id: crypto.randomUUID() };
    tienda.update((s) => ({ ...s, intento }));
  }
  const r = await fetch("/api/compra", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ ...datos, items, id: intento.id }),
  });
  const recibo = await r.json();
  if (!r.ok) {
    if (r.status === 409) await actualizar();
    throw Object.assign(Error(recibo.message), { status: r.status });
  }
  tienda.set({ ...inicial(), recibo });
  await actualizar();
}
