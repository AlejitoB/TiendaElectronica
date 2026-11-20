<script>
  import "../tienda.css";
  import { onMount } from "svelte";
  import { page } from "$app/state";
  import { goto } from "$app/navigation";
  import { restaurar, unidades } from "#lib/tienda.js";
  let { children } = $props();
  let busqueda = $state("");
  $effect(() => {
    busqueda = page.url.searchParams.get("q") || "";
  });
  let oscuro = $state(false);
  function aplicar(valor) {
    oscuro = valor;
    document.documentElement.dataset.theme = valor ? "dark" : "light";
  }
  onMount(() => {
    aplicar(localStorage.getItem("tema") === "dark");
    return restaurar();
  });
  function cambiar() {
    aplicar(!oscuro);
    localStorage.setItem("tema", oscuro ? "dark" : "light");
  }

  function buscar(e) {
    e.preventDefault();
    goto(busqueda.trim() ? "/?q=" + encodeURIComponent(busqueda.trim()) : "/");
  }
</script>

<a class="skip" href="#contenido">Saltar al contenido</a>
<header>
  <div class="bar">
    <a href="/" class="logo" aria-label="Circuito, inicio">
      <span class="mark" aria-hidden="true">C</span>Circuito<span class="dot">
        .
      </span>
    </a>
    <form class="search" onsubmit={buscar} role="search">
      <input
        aria-label="Buscar productos o marcas"
        bind:value={busqueda}
        placeholder="Busca productos o marcas"
        type="search"
      /><button aria-label="Buscar">
        <svg
          viewBox="0 0 24 24"
          width="21"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
        >
          <circle cx="10" cy="10" r="6" /><path d="m15 15 6 6" />
        </svg>
      </button>
    </form>
    <button
      class="theme-button"
      aria-label="Tema oscuro"
      aria-pressed={oscuro}
      onclick={cambiar}
    >
      {oscuro ? "☾ Oscuro" : "☀ Claro"}
    </button>
    <a class="basket" href="/carrito/">Carrito <b>{$unidades}</b></a>
  </div>
</header>
<main id="contenido" class="wrap">{@render children()}</main>
<footer>
  <div class="wrap">
    <span>Circuito · Tienda electrónica</span><a href="/ingresar/">
      Mi cuenta
    </a>
  </div>
</footer>
