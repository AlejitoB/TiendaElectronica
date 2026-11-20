<script>
  import { goto } from "$app/navigation";
  import { lineas, total, moneda, confirmar } from "#lib/tienda.js";
  import { onMount } from "svelte";
  import { sesion } from "#lib/sesion.js";
  import { validarPago, tarjetasDemo } from "#lib/pago-demo.js";
  let autorizado = $state(false), numero = $state(""), vencimiento = $state("");
  let fechaRevision = $state("");
  const ingreso = "/ingresar/?volver=pago";
  onMount(() => {
    sesion().then(rol => {
      if (!rol) return goto(ingreso, { replaceState: true });
      autorizado = true;
    }).catch(e => aviso = e.message);
  });
  let paso = $state(1),
    nombre = $state(""),
    direccion = $state(""),
    tarjeta = $state(null),
    ocupado = $state(false),
    aviso = $state("");
  async function avanzar(e) {
    e.preventDefault();
    aviso = "";
    if (paso === 2) {
      try { tarjeta = validarPago(numero, vencimiento); }
      catch (e) { aviso = e.message; return; }
      fechaRevision = new Date().toLocaleDateString("es-CO", { dateStyle: "long", timeZone: "America/Bogota" });
    }
    if (paso < 3) {
      paso++;
      return;
    }
    if (ocupado) return;
    ocupado = true;
    try {
      const espera = new Promise((resolve) => setTimeout(resolve, 450));
      await Promise.all([confirmar({ nombre, direccion, numero: tarjeta.numero, vencimiento }), espera]);
      await goto("/comprobante/");
    } catch (e) {
      if (e.status === 401) await goto(ingreso);
      else aviso = e.message;
    } finally {
      ocupado = false;
    }
  }
</script>

<svelte:head><title>Finalizar compra · Circuito</title></svelte:head>
<a class="back" href="/carrito/">← Volver al carrito</a>
<h1>Finalizar compra</h1>
{#if !autorizado}<p role="status">{aviso || "Comprobando sesión…"}</p>
{:else if ocupado}<section class="panel narrow formulario" aria-busy="true">
    <h2 role="status">Procesando tu compra…</h2>
    <div class="skeleton h-6 w-2/3"></div>
    <div class="skeleton h-4 w-full"></div>
    <div class="skeleton h-4 w-1/2"></div>
  </section>
{:else if $lineas.length}<div class="split">
    <form class="panel formulario" onsubmit={avanzar}>
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
        <label>Número de tarjeta de prueba
          <input class="input" required inputmode="numeric" autocomplete="off" maxlength="19" bind:value={numero} />
        </label>
        <ul class="demo-numeros">
          {#each tarjetasDemo as t}<li>{t.marca} · {t.aprobada ? "aprobación" : "rechazo"} · <code>{t.numero.match(/.{4}/g).join(" ")}</code></li>{/each}
        </ul>
        <label>Vencimiento<input class="input" required type="month" bind:value={vencimiento} /></label>
      {:else}<h2>Verifica que tus datos sean correctos</h2>
        <p>Fecha de hoy · {fechaRevision}</p>
        <p><strong>Entrega</strong><br />{nombre}<br />{direccion}</p>
        <p>{tarjeta.marca} ···· {tarjeta.numero.slice(-4)} · Vence {vencimiento}</p>
        <p>Total a confirmar · <strong>{moneda($total)}</strong></p>{/if}
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
