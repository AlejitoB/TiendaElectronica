import mysql from "mysql2/promise";
import { readFile } from "node:fs/promises";
import inicial from "./src/lib/catalogo.json" with { type: "json" };

let config = "";
try {
  config = await readFile(
    new URL("../config.properties", import.meta.url),
    "utf8",
  );
} catch (e) {
  if (e.code !== "ENOENT") throw e;
}
const props = Object.fromEntries(
  config
    .split(/\r?\n/)
    .filter((s) => /^mysql\./.test(s))
    .map((s) => [
      s.slice(0, s.indexOf("=")).trim(),
      s.slice(s.indexOf("=") + 1).trim(),
    ]),
);
const url = new URL(
  (
    process.env.TIENDA_MYSQL_URL ||
    props["mysql.url"] ||
    "jdbc:mysql://localhost:3306/prueba"
  ).replace(/^jdbc:/, ""),
);
const database = decodeURIComponent(url.pathname.slice(1));
if (!/^[\w-]+$/.test(database)) throw Error("Nombre de base de datos inválido");
const options = {
  host: url.hostname === "localhost" ? "127.0.0.1" : url.hostname,
  port: Number(url.port || 3306),
  user: process.env.TIENDA_MYSQL_USUARIO ?? props["mysql.usuario"] ?? "root",
  password: process.env.TIENDA_MYSQL_CLAVE ?? props["mysql.clave"] ?? "",
  charset: "utf8mb4",
};
const pool = mysql.createPool({ ...options, database, connectionLimit: 5 });
let ready;
async function init() {
  try {
    await pool.query("SELECT 1");
  } catch (e) {
    if (e.code !== "ER_BAD_DB_ERROR") throw e;
    const conn = await mysql.createConnection(options);
    try {
      await conn.query(
        "CREATE DATABASE IF NOT EXISTS ?? CHARACTER SET utf8mb4",
        [database],
      );
    } finally {
      await conn.end();
    }
  }
  await pool.query(
    "CREATE TABLE IF NOT EXISTS producto (Serial VARCHAR(40) PRIMARY KEY, Nombre VARCHAR(120) NOT NULL, Marca VARCHAR(60) NOT NULL, Categoria VARCHAR(60) NOT NULL, Precio BIGINT NOT NULL, Stock INT NOT NULL) ENGINE=InnoDB",
  );
  await pool.query(
    "CREATE TABLE IF NOT EXISTS pedido (Id VARCHAR(40) PRIMARY KEY, Fecha VARCHAR(30) NOT NULL, Cliente VARCHAR(60) NOT NULL, Total BIGINT NOT NULL, Tarjeta VARCHAR(30) NOT NULL, Autorizacion VARCHAR(40) NOT NULL, Detalle TEXT NOT NULL) ENGINE=InnoDB",
  );
  await pool.query(
    "CREATE TABLE IF NOT EXISTS producto_foto (serial VARCHAR(40) PRIMARY KEY, foto MEDIUMTEXT NOT NULL) ENGINE=InnoDB",
  );
  await pool.query(
    "CREATE TABLE IF NOT EXISTS compra_web (id VARCHAR(40) PRIMARY KEY, recibo MEDIUMTEXT NOT NULL) ENGINE=InnoDB",
  );
  await transaccion(async (c) => {
    const [rows] = await c.query(
      "SELECT Serial FROM producto LIMIT 1 FOR UPDATE",
    );
    if (!rows.length)
      await c.query(
        "INSERT INTO producto (Serial,Nombre,Marca,Categoria,Precio,Stock) VALUES ?",
        [
          inicial.map((p) => [
            p.serial,
            p.nombre,
            p.marca,
            p.categoria,
            p.precio,
            p.stock,
          ]),
        ],
      );
  });
}
export async function preparar() {
  await (ready ??= init().catch((e) => {
    ready = null;
    throw e;
  }));
}
async function transaccion(fn) {
  const c = await pool.getConnection();
  try {
    await c.beginTransaction();
    const result = await fn(c);
    await c.commit();
    return result;
  } catch (e) {
    await c.rollback();
    throw e;
  } finally {
    c.release();
  }
}
export function falla(message, status = 400) {
  throw Object.assign(new Error(message), { status });
}
function texto(v, n) {
  if (typeof v !== "string" || !v.trim() || v.trim().length > n)
    falla("Revisa los campos de texto.");
  return v.trim();
}
function entero(v, min, max) {
  if (!Number.isSafeInteger(v) || v < min || v > max)
    falla("Cantidad o precio fuera de rango.");
  return v;
}
export async function listar() {
  const [rows] = await pool.query(
    "SELECT p.Serial AS serial, Nombre AS nombre, Marca AS marca, Categoria AS categoria, Precio AS precio, Stock AS stock, f.serial IS NOT NULL AS tieneFoto FROM producto p LEFT JOIN producto_foto f ON f.serial=p.Serial ORDER BY Nombre",
  );
  return rows;
}
export async function foto(serial) {
  const [rows] = await pool.execute(
    "SELECT foto FROM producto_foto WHERE serial=?",
    [serial],
  );
  if (!rows.length) falla("Foto no disponible.", 404);
  return rows[0].foto;
}
export async function guardar(p, editar) {
  const serial = texto(p.serial, 40),
    campos = [
      texto(p.nombre, 120),
      texto(p.marca, 60),
      texto(p.categoria, 60),
      entero(p.precio, 1, 1e10),
      entero(p.stock, 0, 1000000),
    ];
  if (p.foto != null && p.foto !== "") {
    const m =
      typeof p.foto === "string" &&
      p.foto.match(/^data:image\/(png|jpeg);base64,([A-Za-z0-9+/]+=*)$/);
    if (!m) falla("Selecciona una imagen PNG o JPEG.");
    const bytes = Buffer.from(m[2], "base64");
    if (
      bytes.length > 2 * 1024 * 1024 ||
      !(m[1] === "png"
        ? bytes.subarray(0, 8).equals(Buffer.from("89504e470d0a1a0a", "hex"))
        : bytes.subarray(0, 3).equals(Buffer.from("ffd8ff", "hex")))
    )
      falla("Foto inválida o superior a 2 MB.");
  }
  await transaccion(async (c) => {
    if (editar) {
      const [found] = await c.execute(
        "SELECT Serial FROM producto WHERE Serial=? FOR UPDATE",
        [serial],
      );
      if (!found.length) falla("Producto inexistente.", 404);
      await c.execute(
        "UPDATE producto SET Nombre=?,Marca=?,Categoria=?,Precio=?,Stock=? WHERE Serial=?",
        [...campos, serial],
      );
    } else {
      try {
        await c.execute(
          "INSERT INTO producto (Serial,Nombre,Marca,Categoria,Precio,Stock) VALUES (?,?,?,?,?,?)",
          [serial, ...campos],
        );
      } catch (e) {
        if (e.code === "ER_DUP_ENTRY")
          falla("Ya existe un producto con ese serial.", 409);
        throw e;
      }
    }
    if (p.foto === "")
      await c.execute("DELETE FROM producto_foto WHERE serial=?", [serial]);
    else if (p.foto)
      await c.execute(
        "INSERT INTO producto_foto VALUES (?,?) ON DUPLICATE KEY UPDATE foto=VALUES(foto)",
        [serial, p.foto],
      );
  });
}
export async function comprar(d) {
  if (!/^[0-9a-f-]{36}$/i.test(d.id)) falla("Referencia inválida.");
  const cliente = texto(d.nombre, 60),
    direccion = texto(d.direccion, 200);
  if (!["Visa", "Mastercard", "Rechazo"].includes(d.tarjeta))
    falla("Selecciona una tarjeta de prueba.");
  if (!Array.isArray(d.items) || !d.items.length || d.items.length > 500)
    falla("Carrito inválido.");
  return transaccion(async (c) => {
    try {
      await c.execute("INSERT INTO compra_web VALUES (?, '')", [d.id]);
    } catch (e) {
      if (e.code !== "ER_DUP_ENTRY") throw e;
      const [rows] = await c.execute(
        "SELECT recibo FROM compra_web WHERE id=?",
        [d.id],
      );
      return JSON.parse(rows[0].recibo);
    }
    if (d.tarjeta === "Rechazo")
      falla(
        "Pago rechazado. Elige otra tarjeta de prueba para reintentar.",
        422,
      );
    const items = [];
    let total = 0;
    for (const l of [...d.items].sort((a, b) =>
      String(a.serial).localeCompare(String(b.serial)),
    )) {
      const serial = texto(l.serial, 40),
        cantidad = entero(l.cantidad, 1, 1000000);
      const [rows] = await c.execute(
        "SELECT Nombre AS nombre,Precio AS precio,Stock AS stock FROM producto WHERE Serial=? FOR UPDATE",
        [serial],
      );
      const p = rows[0];
      if (!p || p.stock < cantidad)
        falla("Stock insuficiente. Revisa el carrito.", 409);
      if (p.precio !== l.precio)
        falla("El precio cambió. Revisa el carrito.", 409);
      total += p.precio * cantidad;
      if (!Number.isSafeInteger(total)) falla("Total fuera de rango.");
      items.push({ serial, nombre: p.nombre, precio: p.precio, cantidad });
      await c.execute("UPDATE producto SET Stock=Stock-? WHERE Serial=?", [
        cantidad,
        serial,
      ]);
    }
    const r = {
      id: d.id,
      fecha: new Date().toISOString(),
      cliente,
      direccion,
      tarjeta: d.tarjeta,
      items,
      total,
    };
    await c.execute(
      "INSERT INTO pedido (Id,Fecha,Cliente,Total,Tarjeta,Autorizacion,Detalle) VALUES (?,?,?,?,?,?,?)",
      [
        r.id,
        r.fecha,
        cliente,
        total,
        d.tarjeta,
        "SIM-" + r.id,
        items.map((i) => i.cantidad + " x " + i.nombre).join("\n"),
      ],
    );
    await c.execute("UPDATE compra_web SET recibo=? WHERE id=?", [
      JSON.stringify(r),
      r.id,
    ]);
    return r;
  });
}
