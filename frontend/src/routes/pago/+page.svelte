<script>
  import { total, moneda, confirmar, unidades } from "#lib/tienda.js";
  import { goto } from "$app/navigation";
  let resultado = $state("aprobar"),
    error = $state("");
  function pagar(e) {
    e.preventDefault();
    if (resultado === "rechazar") {
      error =
        "Pago rechazado por el simulador. Tu carrito está guardado. Puedes cambiar el resultado y reintentar.";
      return;
    }
    if (confirmar()) goto("/comprobante/");
  }
</script>

<svelte:head><title>Pago simulado · Circuito</title></svelte:head>
<a class="back" href="/carrito/">← Volver al carrito</a>
<h1>Pago simulado</h1>
{#if $unidades}<form class="panel narrow stack" onsubmit={pagar}>
    <p>
      Esta compra de prueba utiliza una pasarela local. No se realiza ningún
      cobro.
    </p>
    <label>
      Resultado de prueba<select
        class="select"
        bind:value={resultado}
        onchange={() => (error = "")}
      >
        <option value="aprobar">Aprobar operación</option><option
          value="rechazar"
        >
          Rechazar operación
        </option>
      </select>
    </label><strong class="price">{moneda($total)}</strong>{#if error}<p
        role="alert"
        class="error"
      >
        {error}
      </p>{/if}<button class="btn buy">Confirmar compra simulada</button>
  </form>{:else}<div class="panel">
    <p>Agrega un producto para iniciar el pago.</p>
    <a class="link" href="/">Ir al catálogo</a>
  </div>{/if}
