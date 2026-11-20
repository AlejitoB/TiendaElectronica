import http from "node:http";
import { readFile } from "node:fs/promises";
import { randomBytes, createHash, timingSafeEqual } from "node:crypto";
import { resolve, sep, extname } from "node:path";
import { fileURLToPath } from "node:url";

const root = fileURLToPath(new URL("./build/", import.meta.url));
const port = Number(process.env.PORT || 4173);
const password =
  process.env.TIENDA_ADMIN_CLAVE || randomBytes(12).toString("base64url");
if (!process.env.TIENDA_ADMIN_CLAVE)
  console.log("Clave temporal de admin:", password);
const sessions = new Map();
const types = {
  ".html": "text/html; charset=utf-8",
  ".js": "text/javascript",
  ".css": "text/css",
  ".png": "image/png",
  ".json": "application/json",
};
const hash = (value) => createHash("sha256").update(value).digest();

http
  .createServer(async (req, res) => {
    const reply = (status, message) => {
      res.writeHead(status, {
        "Content-Type": "application/json",
        "Cache-Control": "no-store",
      });
      res.end(JSON.stringify({ message }));
    };
    try {
      const localHosts = ["127.0.0.1:" + port, "localhost:" + port];
      if (!localHosts.includes(req.headers.host))
        return reply(403, "Host desconocido.");
      const url = new URL(req.url, "http://" + req.headers.host);
      if (url.pathname === "/api/admin") {
        for (const [key, expiry] of sessions)
          if (expiry <= Date.now()) sessions.delete(key);
        const token = (req.headers.cookie || "").match(
          /(?:^|; *)tienda_admin=([\w-]+)/,
        )?.[1];
        if (req.method === "GET")
          return reply(sessions.has(token) ? 200 : 401, "Sesión");
        if (req.headers.origin !== url.origin)
          return reply(403, "Origen desconocido.");
        const cookie = (value) =>
          "tienda_admin=" +
          value +
          "; HttpOnly; SameSite=Strict; Path=/api/admin; Max-Age=" +
          (value ? 3600 : 0);
        if (req.method === "DELETE") {
          sessions.delete(token);
          res.setHeader("Set-Cookie", cookie(""));
          return reply(200, "Sesión cerrada.");
        }
        if (req.method !== "POST") return reply(405, "Método desconocido.");
        let body = "";
        for await (const chunk of req) {
          body += chunk;
          if (body.length > 4096)
            return reply(413, "Formulario demasiado largo.");
        }
        const data = JSON.parse(body);
        if (
          data.usuario !== "admin" ||
          typeof data.clave !== "string" ||
          !timingSafeEqual(hash(data.clave), hash(password))
        )
          return reply(401, "Usuario o contraseña incorrectos.");
        const id = randomBytes(32).toString("base64url");
        sessions.delete(token);
        sessions.set(id, Date.now() + 3600000);
        res.setHeader("Set-Cookie", cookie(id));
        return reply(200, "Acceso concedido.");
      }
      if (req.method !== "GET" && req.method !== "HEAD")
        return reply(405, "Método desconocido.");
      const path = decodeURIComponent(url.pathname);
      let file = resolve(root, "." + path);
      if (file !== resolve(root) && !file.startsWith(resolve(root) + sep))
        return reply(404, "Página desconocida.");
      if (!extname(file)) file = resolve(file, "index.html");
      const content = await readFile(file);
      res.writeHead(200, {
        "Content-Type": types[extname(file)] || "application/octet-stream",
        "X-Content-Type-Options": "nosniff",
      });
      res.end(req.method === "HEAD" ? undefined : content);
    } catch (error) {
      reply(error.code === "ENOENT" ? 404 : 400, "Solicitud no disponible.");
    }
  })
  .listen(port, "127.0.0.1", () =>
    console.log("Circuito en http://127.0.0.1:" + port),
  );
