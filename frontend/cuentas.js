import { createHash, timingSafeEqual } from "node:crypto";

export async function prepararCuentas(pool, valor) {
  await pool.query(`CREATE TABLE IF NOT EXISTS cuenta_configurada (
    rol VARCHAR(12) PRIMARY KEY,
    usuario VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL UNIQUE,
    clave CHAR(64) NOT NULL
  ) ENGINE=InnoDB`);
  const cuentas = ["admin", "cliente"].map((rol) => ({
    rol,
    usuario: valor(rol + ".usuario")?.trim().toLowerCase(),
    clave: valor(rol + ".clave.sha256"),
  }));
  if (cuentas.some(c => !c.usuario || c.usuario.length > 100 || !/^[a-f0-9]{64}$/i.test(c.clave)) ||
      cuentas[0].usuario === cuentas[1].usuario)
    throw Error("Configuración de cuentas inválida");
  const conn = await pool.getConnection();
  try {
    await conn.beginTransaction();
    for (const { rol } of cuentas)
      await conn.execute("DELETE FROM cuenta_configurada WHERE rol=?", [rol]);
    for (const { rol, usuario, clave } of cuentas)
      await conn.execute("INSERT INTO cuenta_configurada (rol,usuario,clave) VALUES (?,?,?)", [rol, usuario, clave]);
    await conn.commit();
  } catch (error) {
    await conn.rollback();
    throw error;
  } finally {
    conn.release();
  }
}

export async function autenticar(pool, usuario, clave) {
  if (typeof usuario !== "string" || typeof clave !== "string") return null;
  const [filas] = await pool.execute(
    "SELECT rol, usuario, clave FROM cuenta_configurada WHERE usuario=?",
    [usuario.trim().toLowerCase()],
  );
  const cuenta = filas[0];
  const hash = createHash("sha256").update(clave).digest();
  if (!cuenta || !timingSafeEqual(hash, Buffer.from(cuenta.clave, "hex"))) return null;
  return { usuario: cuenta.usuario, rol: cuenta.rol };
}
