import adapter from "@sveltejs/adapter-static";
import { defineConfig } from "vite";
import { sveltekit } from "@sveltejs/kit/vite";
import tailwind from "@tailwindcss/vite";
export default defineConfig({
  plugins: [
    {
      name: "api-local",
      async configureServer(server) {
        const { atender } = await import(new URL("./tienda-http.js", import.meta.url).href);
        server.middlewares.use((req, res, next) => {
          if (req.url?.startsWith("/api/")) return atender(req, res);
          next();
        });
      },
    },
    tailwind(),
    sveltekit({
      adapter: adapter(),
      prerender: {
        entries: ["/", "/carrito/", "/pago/", "/comprobante/", "/admin/", "/ingresar/"],
      },
    }),
  ],
});
