package Logica_Conexion;

import Logica_Negocio.Producto;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Inventario en Firestore (coleccion "Producto", un documento por serial).
 */
public class ProductoProvider implements DAOInterfaceProducto {

    static final String COLECCION = "Producto";

    private CollectionReference coleccion() {
        return Conexion.db.collection(COLECCION);
    }

    static Producto aProducto(DocumentSnapshot d) {
        Long precio = d.getLong("Precio");
        Long stock = d.getLong("Stock");
        return new Producto(d.getString("Serial"), d.getString("Nombre"), d.getString("Marca"),
                d.getString("Categoria"), precio == null ? 0 : precio, stock == null ? 0 : stock.intValue());
    }

    static Map<String, Object> aMapa(Producto p) {
        Map<String, Object> datos = new HashMap<>();
        datos.put("Serial", p.getSerial());
        datos.put("Nombre", p.getNombre());
        datos.put("Marca", p.getMarca());
        datos.put("Categoria", p.getCategoria());
        datos.put("Precio", p.getPrecio());
        datos.put("Stock", p.getStock());
        return datos;
    }

    @Override
    public ArrayList<Producto> listar() throws Exception {
        ArrayList<Producto> ls = new ArrayList<>();
        for (DocumentSnapshot d : coleccion().get().get().getDocuments()) {
            ls.add(aProducto(d));
        }
        return ls;
    }

    @Override
    public Producto buscar(String serial) throws Exception {
        DocumentSnapshot d = coleccion().document(serial).get().get();
        return d.exists() ? aProducto(d) : null;
    }

    @Override
    public void guardar(Producto p) throws Exception {
        // .get() espera la confirmacion de Firestore: si falla, la pantalla se entera.
        coleccion().document(p.getSerial()).set(aMapa(p)).get();
    }

    @Override
    public boolean eliminar(String serial) throws Exception {
        if (buscar(serial) == null) {
            return false;
        }
        coleccion().document(serial).delete().get();
        return true;
    }
}
