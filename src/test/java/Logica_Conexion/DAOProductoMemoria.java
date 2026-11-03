package Logica_Conexion;

import Logica_Negocio.Producto;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Inventario en memoria para pruebas: mismo contrato que Firebase y MySQL, sin red ni base de datos.
 */
public class DAOProductoMemoria implements DAOInterfaceProducto {

    public final Map<String, Producto> productos = new LinkedHashMap<>();

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

    static Producto copia(Producto p) {
        return new Producto(p.getSerial(), p.getNombre(), p.getMarca(), p.getCategoria(), p.getPrecio(), p.getStock());
    }
}
