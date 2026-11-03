package Logica_Conexion;

import Logica_Negocio.Producto;
import java.util.ArrayList;

/**
 * Contrato del inventario. Lo implementan ProductoProvider (Firebase) y ProductoDAO (MySQL),
 * asi las pantallas no cambian segun la base elegida.
 */
public interface DAOInterfaceProducto {

    public ArrayList<Producto> listar() throws Exception;

    public Producto buscar(String serial) throws Exception;

    /** Crea el producto o lo actualiza si el serial ya existe. */
    public void guardar(Producto p) throws Exception;

    public boolean eliminar(String serial) throws Exception;

    /** Implementacion segun la base elegida en EscogerBD. */
    public static DAOInterfaceProducto Actual() {
        return Conexion.modoNube ? new ProductoProvider() : new ProductoDAO();
    }
}
