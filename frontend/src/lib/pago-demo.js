export const tarjetasDemo = [
  { numero: "4242424242424242", marca: "Visa", aprobada: true },
  { numero: "5555555555554444", marca: "Mastercard", aprobada: true },
  { numero: "4000000000000002", marca: "Visa", aprobada: false },
];

export function validarPago(numero, vencimiento, hoy = new Date()) {
  const tarjeta = tarjetasDemo.find(t => t.numero === numero?.replace?.(/ /g, ""));
  if (!tarjeta) throw Error("Escribe uno de los números de prueba indicados.");
  const partes = new Intl.DateTimeFormat("es-CO", {
    timeZone: "America/Bogota", year: "numeric", month: "2-digit",
  }).formatToParts(hoy);
  const parte = tipo => partes.find(p => p.type === tipo).value;
  if (!/^\d{4}-(0[1-9]|1[0-2])$/.test(vencimiento) || vencimiento < parte("year") + "-" + parte("month"))
    throw Error("Elige un vencimiento del mes actual o posterior.");
  return tarjeta;
}
