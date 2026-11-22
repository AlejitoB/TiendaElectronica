package Logica_Conexion;

import Logica_Negocio.Carrito;
import Logica_Negocio.Pedido;
import Logica_Negocio.Producto;
import Logica_Negocio.ResumenPedido;
import java.util.ArrayList;
import java.util.List;

/**
 * Contrato del inventario y pedidos. Lo implementan ProductoProvider (Firebase)
 * y ProductoDAO (MySQL) para mantener la misma lógica en ambos motores.
 */
public interface DAOInterfaceProducto {

    ArrayList<Producto> listar() throws Exception;

    Producto buscar(String serial) throws Exception;

    /** Crea el producto o lo actualiza si el serial ya existe. */
    void guardar(Producto p) throws Exception;

    boolean eliminar(String serial) throws Exception;

    /** Descuenta de forma atómica el stock antes de cobrar en pasarela. */
    void reservarStock(List<Carrito.Item> items) throws Exception;

    /** Devuelve al inventario las unidades reservadas si el pago se rechaza o falla. */
    void liberarStock(List<Carrito.Item> items) throws Exception;

    /** Persiste el pedido confirmado tras aprobarse el pago. */
    void guardarPedido(Pedido pedido) throws Exception;

    List<ResumenPedido> listarPedidos() throws Exception;

    List<ResumenPedido> listarPedidosPorCliente(String cliente) throws Exception;

    /**
     * Descuenta el stock de cada artículo y guarda el pedido en una sola transacción:
     * si algún artículo no alcanza, no se descuenta nada (0 registros a medias).
     */
    void registrarVenta(Pedido pedido) throws Exception;

    /** Implementación según la base elegida en el acceso. */
    static DAOInterfaceProducto Actual() {
        return Conexion.modoNube ? new ProductoProvider() : new ProductoDAO();
    }
}
