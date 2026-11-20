<script>
  import Productos from "#lib/Productos.svelte";
  import { onMount } from "svelte";
  let usuario = $state(""),
    clave = $state(""),
    dentro = $state(false),
    ocupado = $state(true),
    mensaje = $state("");
  async function sesion(method = "GET") {
    ocupado = true;
    mensaje = "";
    try {
      const r = await fetch("/api/admin", {
        method,
        headers: { "Content-Type": "application/json" },
        body:
          method === "POST" ? JSON.stringify({ usuario, clave }) : undefined,
      });
      if (!r.headers.get("content-type")?.includes("application/json"))
        throw new Error();
      const data = await r.json();
      if (r.ok) {
        dentro = method !== "DELETE";
        clave = "";
      } else if (method !== "GET" || r.status !== 401) mensaje = data.message;
    } catch {
      mensaje = "El acceso requiere el servidor local. Inícialo con npm start.";
    } finally {
      ocupado = false;
    }
  }
  onMount(() => {
    sesion();
  });
</script>

<svelte:head><title>Administración · Circuito</title></svelte:head><a
  class="back"
  href="/"
>
  ← Volver al catálogo
</a>
<h1>Administración</h1>
{#if dentro}<Productos />
  <section class="panel narrow stack">
    <h2>Sesión administrativa activa</h2>
    <p>Has iniciado sesión en la tienda local.</p>
    <button class="btn" disabled={ocupado} onclick={() => sesion("DELETE")}>
      Cerrar sesión
    </button>
  </section>
{:else}<form
    class="panel narrow stack"
    onsubmit={(e) => {
      e.preventDefault();
      sesion("POST");
    }}
  >
    <h2>Iniciar sesión</h2>
    <label>
      Usuario<input
        class="input"
        required
        autocomplete="username"
        bind:value={usuario}
      />
    </label><label>
      Contraseña<input
        class="input"
        required
        type="password"
        autocomplete="current-password"
        bind:value={clave}
      />
    </label><button class="btn buy" disabled={ocupado}>Entrar</button>
  </form>{/if}
{#if mensaje}<p class="error narrow" role="alert">{mensaje}</p>{/if}
