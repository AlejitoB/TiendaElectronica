<script>
  import {
    lineas,
    total,
    moneda,
    cantidad,
    disponibles,
    tienda,
  } from "#lib/tienda.js";
</script>

<svelte:head><title>Carrito · Circuito</title></svelte:head>
<a class="back" href="/">← Volver al catálogo</a>
<h1>Tu carrito</h1>
{#if !$lineas.length}<section class="panel">
    <h2>El carrito está vacío</h2>
    <a class="btn buy" href="/">Explorar productos</a>
  </section>
{:else}<div class="split">
    <section class="panel">
      {#each $lineas as p (p.serial)}<article class="row">
          <div>
            <p class="brand">{p.marca}</p>
            <h2>{p.nombre}</h2>
            <p>{moneda(p.precio)} por unidad</p>
          </div>
          <div class="controls">
            <button
              class="btn"
              disabled={p.cantidad <= 1}
              aria-label={"Restar una unidad de " + p.nombre}
              onclick={() => cantidad(p, p.cantidad - 1)}
            >
              −
            </button><b>{p.cantidad}</b><button
              class="btn"
              disabled={p.cantidad >= disponibles(p, $tienda)}
              aria-label={"Sumar una unidad de " + p.nombre}
              onclick={() => cantidad(p, p.cantidad + 1)}
            >
              +
            </button><button
              class="link"
              aria-label={"Quitar " + p.nombre}
              onclick={() => cantidad(p, 0)}
            >
              Quitar
            </button>
          </div>
          {#if p.cantidad >= disponibles(p, $tienda)}<small>
              Alcanzaste el stock disponible.
            </small>{/if}<strong>{moneda(p.precio * p.cantidad)}</strong>
        </article>{/each}
    </section>
    <section class="panel summary">
      <h2>Resumen de compra</h2>
      <p>Total en COP</p>
      <strong class="price">{moneda($total)}</strong><a
        class="btn buy"
        href="/pago/"
      >
        Continuar al pago
      </a>
    </section>
  </div>{/if}
