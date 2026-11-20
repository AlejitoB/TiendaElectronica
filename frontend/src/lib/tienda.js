import { writable, derived, get } from "svelte/store";
import catalogo from "./catalogo.json";
export { catalogo };
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
export const lineas = derived(tienda, (s) =>
  catalogo
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
  return p.stock - (s.vendidos[p.serial] || 0);
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
export function confirmar() {
  const items = get(lineas);
  if (!items.length) return false;
  const s = get(tienda);
  const vendidos = { ...s.vendidos };
  for (const p of items)
    vendidos[p.serial] = (vendidos[p.serial] || 0) + p.cantidad;
  tienda.set({
    carrito: {},
    vendidos,
    recibo: {
      id: crypto.randomUUID().slice(0, 8).toUpperCase(),
      fecha: new Date().toISOString(),
      items,
      total: get(total),
    },
  });
  return true;
}
