import {
  ingresar,
  preparar,
  listar,
  foto,
  guardar,
  comprar,
  falla,
} from "./inventario.js";
import { readFile } from "node:fs/promises";
import { randomBytes } from "node:crypto";
import { resolve, sep, extname } from "node:path";
import { fileURLToPath } from "node:url";

const root = fileURLToPath(new URL("./build/", import.meta.url));

const sessions = new Map();
const types = {
  ".html": "text/html; charset=utf-8",
  ".js": "text/javascript",
  ".css": "text/css",
  ".png": "image/png",
  ".json": "application/json",
};
export async function atender(req, res) {
    const reply = (status, message) => {
      res.writeHead(status, {
        "Content-Type": "application/json",
        "Cache-Control": "no-store",
      });
      res.end(JSON.stringify(typeof message === "string" ? { message } : message));
    };
    try {
      const port = req.socket.localPort;
      const localHosts = ["127.0.0.1:" + port, "localhost:" + port];
      if (!localHosts.includes(req.headers.host))
        return reply(403, "Host desconocido.");
      const url = new URL(req.url, "http://" + req.headers.host);
      for (const [key, session] of sessions)
        if (session.expiry <= Date.now()) sessions.delete(key);
      const token = (req.headers.cookie || "").match(/(?:^|; *)tienda_sesion=([\w-]+)/)?.[1];
      const session = sessions.get(token);
      if (url.pathname === "/api/sesion") {
        if (req.method === "GET")
          return session ? reply(200, { rol: session.rol }) : reply(401, "Inicia sesión.");
        if (req.headers.origin !== url.origin)
          return reply(403, "Origen desconocido.");
        const cookie = (value) =>
          "tienda_sesion=" +
          value +
          "; HttpOnly; SameSite=Strict; Path=/api; Max-Age=" +
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
        const cuenta = await ingresar(data?.usuario, data?.clave);
        if (!cuenta)
          return reply(401, "Usuario o contraseña incorrectos.");
        const id = randomBytes(32).toString("base64url");
        sessions.delete(token);
        sessions.set(id, { ...cuenta, expiry: Date.now() + 3600000 });
        res.setHeader("Set-Cookie", cookie(id));
        return reply(200, { rol: cuenta.rol });
      }
      if (url.pathname.startsWith("/api/")) {
        const path = url.pathname;
        if (req.method !== "GET" && req.headers.origin !== url.origin)
          return reply(403, "Origen desconocido.");
        if (path === "/api/admin/productos" || path === "/api/compra") {
          if (!session) return reply(401, "Inicia sesión para continuar.");
          if (path === "/api/admin/productos" && session.rol !== "admin")
            return reply(403, "Se requiere una cuenta administrativa.");
        }
        await preparar();
        async function body() {
          const chunks = [];
          let size = 0;
          for await (const chunk of req) {
            size += chunk.length;
            if (size > 2900000) falla("Foto demasiado grande.", 413);
            chunks.push(chunk);
          }
          return JSON.parse(Buffer.concat(chunks).toString());
        }
        let result;
        if (path === "/api/productos" && req.method === "GET")
          result = await listar();
        else if (path === "/api/foto" && req.method === "GET") {
          const data = await foto(url.searchParams.get("serial") || "");
          res.writeHead(200, {
            "Content-Type": data.split(";")[0].slice(5),
            "Cache-Control": "no-store",
            "X-Content-Type-Options": "nosniff",
          });
          return res.end(Buffer.from(data.split(",")[1], "base64"));
        } else if (
          path === "/api/admin/productos" &&
          ["POST", "PUT"].includes(req.method)
        ) {
          await guardar(await body(), req.method === "PUT");
          result = { message: "Producto guardado." };
        } else if (path === "/api/compra" && req.method === "POST")
          result = await comprar(await body(), session.usuario);
        else return reply(404, "Ruta desconocida.");
        res.writeHead(200, {
          "Content-Type": "application/json",
          "Cache-Control": "no-store",
        });
        return res.end(JSON.stringify(result));
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
      if (error.status) return reply(error.status, error.message);
      if (req.url.startsWith("/api/") && !(error instanceof SyntaxError)) {
        console.error("Error de inventario:", error.code || error.name);
        return reply(
          503,
          "No se pudo acceder a la base de datos. Revisa MySQL en XAMPP y config.properties.",
        );
      }
      reply(error.code === "ENOENT" ? 404 : 400, "Solicitud no disponible.");
    }
}
