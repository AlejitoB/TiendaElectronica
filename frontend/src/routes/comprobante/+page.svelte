<script>
  import { tienda, moneda } from "#lib/tienda.js";
  let pedido = $derived($tienda.recibo);
</script>

<svelte:head><title>Comprobante · Circuito</title></svelte:head>
<div class="recibo"><a
  class="back"
  href="/"
>
  ← Volver al catálogo
</a>
{#if pedido}<p class="eyebrow">PASO 4 DE 4</p>
  <h1><span class="aprobado" aria-hidden="true">✓</span> Pago aprobado</h1>
  <section class="panel ticket">
    <p class="eyebrow">COMPROBANTE SIMULADO</p>
    {#if pedido.cliente}<p>{pedido.cliente} · {pedido.direccion}</p>{/if}
    <p>Pedido <code>{pedido.id}</code></p>
    <p>{new Date(pedido.fecha).toLocaleString("es-CO", { timeZone: "America/Bogota" })}</p>
    {#if pedido.terminacion}<p>{pedido.tarjeta} ···· {pedido.terminacion}</p>{/if}
    {#each pedido.items as p}<div class="partida">
        <span>{p.cantidad} × {p.nombre}</span><b>
          {moneda(p.precio * p.cantidad)}
        </b>
      </div>{/each}
    <p class="price">Total {moneda(pedido.total)}</p>
  </section>{:else}<h1>Comprobante</h1>
  <p>Aún no has completado una compra.</p>{/if}
</div>
