import adapter from "@sveltejs/adapter-static";
import { defineConfig } from "vite";
import { sveltekit } from "@sveltejs/kit/vite";
import tailwind from "@tailwindcss/vite";
export default defineConfig({
  plugins: [
    tailwind(),
    sveltekit({
      adapter: adapter(),
      prerender: {
        entries: ["/", "/carrito/", "/pago/", "/comprobante/", "/admin/"],
      },
    }),
  ],
});
