<script>
  import { goto } from "$app/navigation";
  import { lineas, total, moneda, confirmar } from "#lib/tienda.js";
  let paso = $state(1),
    nombre = $state(""),
    direccion = $state(""),
    tarjeta = $state("Visa"),
    ocupado = $state(false),
    aviso = $state("");
  async function avanzar(e) {
    e.preventDefault();
    aviso = "";
    if (paso < 3) {
      paso++;
      return;
    }
    if (ocupado) return;
    ocupado = true;
    try {
      const espera = new Promise((resolve) => setTimeout(resolve, 450));
      await Promise.all([confirmar({ nombre, direccion, tarjeta }), espera]);
      await goto("/comprobante/");
    } catch (e) {
      aviso = e.message;
    } finally {
      ocupado = false;
    }
  }
</script>

<svelte:head><title>Finalizar compra · Circuito</title></svelte:head>
<a class="back" href="/carrito/">← Volver al carrito</a>
<h1>Finalizar compra</h1>
{#if ocupado}<section class="panel narrow stack" aria-busy="true">
    <h2 role="status">Procesando tu compra…</h2>
    <div class="skeleton h-6 w-2/3"></div>
    <div class="skeleton h-4 w-full"></div>
    <div class="skeleton h-4 w-1/2"></div>
  </section>
{:else if $lineas.length}<div class="split">
    <form class="panel stack" onsubmit={avanzar}>
      <p class="eyebrow">Paso {paso} de 4</p>
      {#if paso === 1}<h2>Datos de entrega</h2>
        <label>
          Nombre completo<input
            class="input"
            required
            maxlength="60"
            autocomplete="name"
            bind:value={nombre}
          />
        </label><label>
          Dirección<input
            class="input"
            required
            maxlength="200"
            autocomplete="street-address"
            bind:value={direccion}
          />
        </label>
      {:else if paso === 2}<h2>Forma de pago</h2>
        <p>Compra de prueba. No se realizará ningún cobro.</p>
        {#each ["Visa", "Mastercard", "Rechazo"] as marca}<label
            class="cardchoice"
          >
            <input type="radio" bind:group={tarjeta} value={marca} /><strong>
              {marca === "Rechazo" ? "Tarjeta de prueba rechazada" : marca}
            </strong>
          </label>{/each}
      {:else}<h2>Casi está listo</h2>
        <p>{nombre}<br />{direccion}</p>
        <p>Tarjeta de prueba · {tarjeta}</p>{/if}
      {#if aviso}<p class="error" role="alert">{aviso}</p>{/if}
      <button class="btn buy">
        {paso === 3 ? "Confirmar compra" : "Continuar"}
      </button>{#if paso > 1}<button
          class="link"
          type="button"
          onclick={() => {
            paso--;
            aviso = "";
          }}
        >
          Volver al paso anterior
        </button>{/if}
    </form>
    <aside class="panel summary">
      <h2>Resumen</h2>
      {#each $lineas as item}<p>
          {item.cantidad} × {item.nombre}
        </p>{/each}<strong class="price">{moneda($total)}</strong>
    </aside>
  </div>
{:else}<p>Agrega un producto para continuar.</p>
  <a class="link" href="/">Ir al catálogo</a>{/if}
