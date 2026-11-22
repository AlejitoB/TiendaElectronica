package Logica_Conexion;

import Logica_Negocio.Carrito;
import Logica_Negocio.Pedido;
import Logica_Negocio.Producto;
import Logica_Negocio.ResumenPedido;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Inventario en memoria para pruebas: mismo contrato que Firebase y MySQL, sin red ni base de datos.
 */
public class DAOProductoMemoria implements DAOInterfaceProducto {

    public final Map<String, Producto> productos = new LinkedHashMap<>();
    public final List<Pedido> pedidos = new ArrayList<>();

    @Override
    public ArrayList<Producto> listar() {
        ArrayList<Producto> ls = new ArrayList<>();
        productos.values().forEach(p -> ls.add(copia(p)));
        return ls;
    }

    @Override
    public Producto buscar(String serial) {
        Producto p = productos.get(serial);
        return p == null ? null : copia(p);
    }

    @Override
    public void guardar(Producto p) {
        productos.put(p.getSerial(), copia(p));
    }

    @Override
    public boolean eliminar(String serial) {
        return productos.remove(serial) != null;
    }

    @Override
    public void reservarStock(List<Carrito.Item> items) {
        for (Carrito.Item i : items) {
            Producto p = productos.get(i.producto.getSerial());
            if (p == null || p.getStock() < i.getCantidad()) {
                throw new IllegalStateException("Stock insuficiente para " + i.producto.getNombre());
            }
        }
        items.forEach(i -> {
            Producto p = productos.get(i.producto.getSerial());
            p.setStock(p.getStock() - i.getCantidad());
        });
    }

    @Override
    public void liberarStock(List<Carrito.Item> items) {
        for (Carrito.Item i : items) {
            Producto p = productos.get(i.producto.getSerial());
            if (p != null) {
                p.setStock(p.getStock() + i.getCantidad());
            }
        }
    }

    @Override
    public void guardarPedido(Pedido pedido) {
        pedidos.add(pedido);
    }

    @Override
    public List<ResumenPedido> listarPedidos() {
        List<ResumenPedido> ls = new ArrayList<>();
        for (int i = pedidos.size() - 1; i >= 0; i--) {
            ls.add(pedidos.get(i).aResumen());
        }
        return ls;
    }

    @Override
    public List<ResumenPedido> listarPedidosPorCliente(String cliente) {
        List<ResumenPedido> ls = new ArrayList<>();
        for (ResumenPedido r : listarPedidos()) {
            if (cliente != null && (cliente.equals(r.cliente()) || cliente.equals(r.clienteUid()))) {
                ls.add(r);
            }
        }
        return ls;
    }

    @Override
    public void registrarVenta(Pedido pedido) {
        reservarStock(pedido.items);
        guardarPedido(pedido);
    }

    static Producto copia(Producto p) {
        return new Producto(p.getSerial(), p.getNombre(), p.getMarca(), p.getCategoria(), p.getPrecio(), p.getStock());
    }
}
