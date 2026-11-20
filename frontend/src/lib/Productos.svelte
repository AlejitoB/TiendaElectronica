<script>
  import { catalogo, actualizar, problema } from "./tienda.js";
  const nuevo = () => ({
    serial: "",
    nombre: "",
    marca: "",
    categoria: "",
    precio: 1,
    stock: 1,
  });
  let p = $state(nuevo()),
    seleccion = $state(""),
    imagen = $state(null),
    vista = $state(""),
    aviso = $state(""),
    ocupado = $state(false),
    archivo = $state();
  function cargar() {
    const producto = $catalogo.find((item) => item.serial === seleccion);
    p = producto ? { ...producto } : nuevo();
    imagen = null;
    vista = producto?.tieneFoto
      ? "/api/foto?serial=" + encodeURIComponent(p.serial)
      : "";
    aviso = "";
    if (archivo) archivo.value = "";
  }
  async function leer(e) {
    const f = e.target.files[0];
    if (!f) return;
    if (!["image/jpeg", "image/png"].includes(f.type) || f.size > 2097152) {
      aviso = "Elige un PNG o JPEG de hasta 2 MB.";
      return;
    }
    ocupado = true;
    try {
      imagen = await new Promise((ok, fail) => {
        const r = new FileReader();
        r.onload = () => ok(r.result);
        r.onerror = fail;
        r.readAsDataURL(f);
      });
      vista = imagen;
      aviso = "";
    } catch {
      aviso = "No se pudo leer la foto.";
    } finally {
      ocupado = false;
    }
  }
  async function guardar(e) {
    e.preventDefault();
    ocupado = true;
    try {
      const r = await fetch("/api/admin/productos", {
        method: seleccion ? "PUT" : "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ ...p, foto: imagen }),
      });
      const data = await r.json();
      if (!r.ok) throw Error(data.message);
      await actualizar();
      seleccion = "";
      cargar();
      aviso = "Producto guardado en el catálogo.";
    } catch (e) {
      aviso = e.message;
    } finally {
      ocupado = false;
    }
  }
</script>

<form class="panel stack editor" onsubmit={guardar}>
  <h2>Productos</h2>
  <label>
    Producto<select
      class="select"
      bind:value={seleccion}
      onchange={cargar}
      disabled={ocupado}
    >
      <option value="">Nuevo producto</option>{#each $catalogo as item}<option
          value={item.serial}
        >
          {item.nombre} · {item.serial}
        </option>{/each}
    </select>
  </label>
  <div class="formgrid">
    {#each [["serial", "Serial", 40], ["nombre", "Nombre", 120], ["marca", "Marca", 60], ["categoria", "Categoría", 60]] as campo}<label>
        {campo[1]}<input
          class="input"
          required
          maxlength={campo[2]}
          disabled={campo[0] === "serial" && !!seleccion}
          bind:value={p[campo[0]]}
        />
      </label>{/each}
    <label>
      Precio en COP<input
        class="input"
        type="number"
        min="1"
        max="10000000000"
        required
        bind:value={p.precio}
      />
    </label><label>
      Stock<input
        class="input"
        type="number"
        min="0"
        max="1000000"
        required
        bind:value={p.stock}
      />
    </label>
  </div>
  <label>
    Insertar foto <small>PNG o JPEG · máximo 2 MB</small><input
      bind:this={archivo}
      type="file"
      accept="image/png,image/jpeg"
      onchange={leer}
      disabled={ocupado}
    />
  </label>
  {#if vista}<img
      class="preview"
      src={vista}
      alt="Vista previa del producto"
    /><button
      type="button"
      class="link"
      onclick={() => {
        imagen = "";
        vista = "";
        archivo.value = "";
      }}
    >
      Quitar foto
    </button>{/if}
  <button class="btn buy" disabled={ocupado}>Guardar producto</button>
  <p role="status">{aviso || $problema}</p>
</form>
