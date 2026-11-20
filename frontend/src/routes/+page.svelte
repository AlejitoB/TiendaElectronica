<script>
  import { page } from "$app/state";
  import { browser } from "$app/env";
  import { goto } from "$app/navigation";
  import {
    catalogo,
    problema,
    actualizar,
    tienda,
    moneda,
    cantidad,
    disponibles,
  } from "#lib/tienda.js";
  let grupos = $derived([...new Set($catalogo.map((p) => p.categoria))]);
  const fotos = {
    LAP: "laptop",
    MON: "desktop-computer",
    TEC: "keyboard",
    MOU: "computer-mouse",
    CAM: "video-camera",
    AUD: "headphone",
  };
  const orden = ["LAP-001", "AUD-001", "MON-001", "TEC-001"];
  let productos = $derived([
    ...orden
      .map((id) => $catalogo.find((p) => p.serial === id))
      .filter(Boolean),
    ...$catalogo.filter((p) => !orden.includes(p.serial)),
  ]);
  let grupo = $state("Todos"),
    ordenar = $state("destacados"),
    filtros = $state(false),
    aviso = $state("");
  const normalizar = (s) =>
    s
      .normalize("NFD")
      .replace(/[\u0300-\u036f]/g, "")
      .toLowerCase();
  let consulta = $derived(browser ? page.url.searchParams.get("q") || "" : "");
  let lista = $derived.by(() => {
    const filas = productos.filter(
      (p) =>
        (grupo === "Todos" || p.categoria === grupo) &&
        normalizar(
          [p.nombre, p.marca, p.serial, p.categoria].join(" "),
        ).includes(normalizar(consulta.trim())),
    );
    if (ordenar !== "destacados")
      filas.sort(
        (a, b) => (a.precio - b.precio) * (ordenar === "menor" ? 1 : -1),
      );
    return filas;
  });
</script>

<svelte:head><title>Catálogo · Circuito</title></svelte:head>
<div class="heading">
  <div>
    <p class="eyebrow">TIENDA DE TECNOLOGÍA</p>
    <h1>Catálogo de productos</h1>
  </div>
</div>
{#if $problema}<p class="error" role="alert">
    {$problema} <button class="link" onclick={actualizar}>Reintentar</button>
  </p>{/if}
<div class="catalog">
  <aside class:open={filtros}>
    <button
      class="mobile-filter btn"
      onclick={() => (filtros = !filtros)}
      aria-expanded={filtros}
    >
      Filtrar por categoría
    </button>
    <div class="facet">
      <h2>Categorías</h2>
      {#each ["Todos", ...grupos] as g}<button
          aria-pressed={g === grupo}
          class:chosen={g === grupo}
          onclick={() => {
            grupo = g;
            filtros = false;
            aviso = "";
          }}
        >
          {g}<span>
            {g === "Todos"
              ? $catalogo.length
              : $catalogo.filter((p) => p.categoria === g).length}
          </span>
        </button>{/each}
    </div>
  </aside>
  <section aria-label="Resultados">
    <div class="toolbar">
      <strong>
        {lista.length}
        {lista.length === 1 ? "producto" : "productos"}{consulta
          ? " · “" + consulta + "”"
          : ""}
      </strong><label>
        Ordenar <select class="select select-sm" bind:value={ordenar}>
          <option value="destacados">Destacados</option><option value="menor">
            Menor precio
          </option><option value="mayor">Mayor precio</option>
        </select>
      </label>
    </div>
    <div class="sr-only" role="status">{aviso}</div>
    <div class="gridprod">
      {#each lista as p (p.serial)}
        {@const prefijo = p.serial.split("-")[0]}
        {@const stock = disponibles(p, $tienda)}
        {@const n = $tienda.carrito[p.serial] || 0}
        <article class="product">
          <div class={"photo tone-" + prefijo}>
            <small>{p.serial}</small>{#if p.tieneFoto}<img
                src={"/api/foto?serial=" + encodeURIComponent(p.serial)}
                alt={p.nombre}
              />{:else if fotos[prefijo]}<img
                src={"/equipos/" + fotos[prefijo] + ".png"}
                alt=""
              />{:else}<span class="type-art">{p.categoria}</span>{/if}
          </div>
          <div class="body">
            <p class="brand">{p.marca}</p>
            <h2>{p.nombre}</h2>
            <strong class="price">{moneda(p.precio)}</strong>
            <p class="stock">
              {stock === 0 ? "Agotado" : stock + " disponibles"}
            </p>
            <button
              class="btn buy"
              disabled={n >= stock}
              onclick={() => {
                cantidad(p, n + 1);
                aviso =
                  p.nombre + " agregado. Unidades en el carrito: " + (n + 1);
              }}
            >
              {stock === 0
                ? "Agotado"
                : n >= stock
                  ? "Límite alcanzado"
                  : n
                    ? "Agregar otro (" + n + ")"
                    : "+ Agregar"}
            </button>
          </div>
        </article>
      {:else}{#if !$problema}<div class="panel">
          <h2>Sin coincidencias</h2>
          <p>Prueba otra marca o categoría.</p>
          <button
            class="btn"
            onclick={() => {
              grupo = "Todos";
              goto("/");
            }}
          >
            Ver todo el catálogo
          </button>
        </div>{/if}{/each}
    </div>
  </section>
</div>
