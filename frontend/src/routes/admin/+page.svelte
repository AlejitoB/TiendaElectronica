<script>
  import Productos from "#lib/Productos.svelte";
  import { onMount } from "svelte";
  import { goto } from "$app/navigation";
  import { sesion } from "#lib/sesion.js";
  let dentro = $state(false), mensaje = $state("");
  onMount(() => {
    sesion().then(rol => {
      if (rol === "admin") dentro = true;
      else return goto(rol ? "/" : "/ingresar/", { replaceState: true });
    }).catch(e => mensaje = e.message);
  });
  async function salir() {
    try { await sesion("DELETE"); dentro = false; await goto("/ingresar/"); }
    catch (e) { mensaje = e.message; }
  }
</script>
<svelte:head><title>Administración · Circuito</title></svelte:head>
<a class="back" href="/">← Volver al catálogo</a>
{#if dentro}
  <h1>Administración</h1>
  <Productos />
  <button class="btn" onclick={salir}>Cerrar sesión</button>
{:else if !mensaje}<p role="status">Comprobando sesión…</p>{/if}
{#if mensaje}<p class="error" role="alert">{mensaje}</p>{/if}
