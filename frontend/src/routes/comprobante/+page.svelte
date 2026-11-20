<script>
  import { tienda, moneda } from "#lib/tienda.js";
  let pedido = $derived($tienda.recibo);
</script>

<svelte:head><title>Comprobante · Circuito</title></svelte:head><a
  class="back"
  href="/"
>
  ← Volver al catálogo
</a>
{#if pedido}<p class="eyebrow">PASO 4 DE 4</p>
  <h1><span class="aprobado" aria-hidden="true">✓</span> Pago aprobado</h1>
  <section class="panel narrow">
    <p class="eyebrow">COMPROBANTE SIMULADO</p>
    {#if pedido.cliente}<p>{pedido.cliente} · {pedido.direccion}</p>{/if}
    <h2>Pedido {pedido.id}</h2>
    <p>{new Date(pedido.fecha).toLocaleString("es-CO")}</p>
    {#each pedido.items as p}<div class="row">
        <span>{p.cantidad} × {p.nombre}</span><b>
          {moneda(p.precio * p.cantidad)}
        </b>
      </div>{/each}
    <p class="price">Total {moneda(pedido.total)}</p>
  </section>{:else}<h1>Comprobante</h1>
  <p>Aún no has completado una compra.</p>{/if}
