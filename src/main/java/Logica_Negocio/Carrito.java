package Logica_Negocio;

import java.util.ArrayList;
import java.util.List;

/**
 * Carrito de compras del cliente. No deja agregar mas unidades de las que hay en inventario.
 */
public class Carrito {

    public static class Item {

        public final Producto producto;
        public int cantidad;

        public Item(Producto producto, int cantidad) {
            this.producto = producto;
            this.cantidad = cantidad;
        }

        public long Subtotal() {
            return producto.getPrecio() * cantidad;
        }
    }

    private final List<Item> items = new ArrayList<>();

    /** Agrega unidades del producto. Devuelve null si se pudo o el motivo por el que no. */
    public String Agregar(Producto p, int cantidad) {
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
        return items;
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
