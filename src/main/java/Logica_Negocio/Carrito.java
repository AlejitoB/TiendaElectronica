package Logica_Negocio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Carrito de compras del cliente. No deja agregar más unidades de las que hay en inventario.
 */
public class Carrito {

    public static class Item {

        public final Producto producto;
        // Paquete-privado para que pruebas en Logica_Negocio lean directo sin exponer mutación externa.
        public int cantidad;

        public Item(Producto producto, int cantidad) {
            this.producto = producto;
            this.cantidad = cantidad;
        }

        public int getCantidad() {
            return cantidad;
        }

        public long Subtotal() {
            return producto.getPrecio() * cantidad;
        }
    }

    private final List<Item> items = new ArrayList<>();

    /** Agrega unidades del producto. Devuelve null si se pudo o el motivo por el que no. */
    public String Agregar(Producto p, int cantidad) {
        if (p == null) {
            return "Seleccione un producto válido";
        }
        if (cantidad <= 0) {
            return "La cantidad debe ser mayor que cero";
        }
        Item item = Buscar(p.getSerial());
        int enCarrito = item == null ? 0 : item.cantidad;
        if (enCarrito + cantidad > p.getStock()) {
            return "Solo hay " + p.getStock() + " unidades de " + p.getNombre()
                    + (enCarrito > 0 ? " (ya tiene " + enCarrito + " en el carrito)" : "");
        }
        if (item == null) {
            items.add(new Item(p, cantidad));
        } else {
            item.cantidad += cantidad;
        }
        return null;
    }

    /** Actualiza la cantidad exacta de un artículo; si es <= 0, lo elimina del carrito. */
    public String ActualizarCantidad(String serial, int nuevaCantidad) {
        Item item = Buscar(serial);
        if (item == null) {
            return "El producto no está en el carrito";
        }
        if (nuevaCantidad <= 0) {
            Quitar(serial);
            return null;
        }
        if (nuevaCantidad > item.producto.getStock()) {
            return "Solo hay " + item.producto.getStock() + " unidades disponibles de " + item.producto.getNombre();
        }
        item.cantidad = nuevaCantidad;
        return null;
    }

    public void Quitar(String serial) {
        items.removeIf(i -> i.producto.getSerial().equals(serial));
    }

    public Item Buscar(String serial) {
        for (Item i : items) {
            if (i.producto.getSerial().equals(serial)) {
                return i;
            }
        }
        return null;
    }

    public List<Item> Items() {
        return Collections.unmodifiableList(items);
    }

    public long Total() {
        long total = 0;
        for (Item i : items) {
            total += i.Subtotal();
        }
        return total;
    }

    public int Unidades() {
        int n = 0;
        for (Item i : items) {
            n += i.cantidad;
        }
        return n;
    }

    public boolean EstaVacio() {
        return items.isEmpty();
    }

    public void Vaciar() {
        items.clear();
    }
}
