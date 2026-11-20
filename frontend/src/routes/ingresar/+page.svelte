<script>
  import { onMount } from "svelte";
  import { goto } from "$app/navigation";
  import { page } from "$app/state";
  import { sesion } from "#lib/sesion.js";

  let usuario = $state(""), clave = $state(""), mensaje = $state("");
  let ocupado = $state(true), rol = $state(null);
  let volverPago = $state(false);
  const destino = rol => rol === "admin" ? "/admin/" : volverPago ? "/pago/" : "/";
  onMount(() => {
    volverPago = page.url.searchParams.get("volver") === "pago";
    async function cargar() {
      try {
        rol = await sesion();
        if (rol && volverPago) await goto(destino(rol));
      } catch (e) { mensaje = e.message; }
      finally { ocupado = false; }
    }
    cargar();
  });
  async function entrar(e) {
    e.preventDefault();
    mensaje = "";
    ocupado = true;
    try {
      rol = await sesion("POST", { usuario, clave });
      clave = "";
      await goto(destino(rol));
    } catch (e) { mensaje = e.message; }
    finally { ocupado = false; }
  }
  async function salir() {
    ocupado = true;
    try { await sesion("DELETE"); rol = null; clave = ""; }
    catch (e) { mensaje = e.message; }
    finally { ocupado = false; }
  }
</script>
<svelte:head><title>Iniciar sesión · Circuito</title></svelte:head>
<a class="back" href="/">← Volver al catálogo</a>
<h1>{rol ? "Mi cuenta" : "Iniciar sesión"}</h1>
{#if rol}
  <section class="panel narrow formulario">
    <a class="link" href={rol === "admin" ? "/admin/" : "/carrito/"}>{rol === "admin" ? "Ir al panel" : "Ver carrito"}</a>
    <button class="btn" disabled={ocupado} onclick={salir}>Cerrar sesión</button>
  </section>
{:else}
  <form class="panel narrow formulario" onsubmit={entrar}>
    {#if volverPago}<p>Inicia sesión para continuar con tu carrito.</p>{/if}
    <label>Usuario<input class="input" required autocomplete="username" bind:value={usuario} /></label>
    <label>Contraseña<input class="input" type="password" required autocomplete="current-password" bind:value={clave} /></label>
    <button class="btn buy" disabled={ocupado}>Entrar</button>
  </form>
{/if}
{#if mensaje}<p class="error narrow" role="alert">{mensaje}</p>{/if}
