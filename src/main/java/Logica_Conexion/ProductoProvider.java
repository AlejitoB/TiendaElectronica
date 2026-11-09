package Logica_Conexion;

import Logica_Negocio.Carrito;
import Logica_Negocio.Pedido;
import Logica_Negocio.Producto;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    @Override
    public void registrarVenta(Pedido pedido) throws Exception {
        Firestore db = Conexion.db;
        db.runTransaction(tx -> {
            // Firestore exige hacer todas las lecturas antes de las escrituras.
            List<DocumentSnapshot> docs = new ArrayList<>();
            for (Carrito.Item i : pedido.items) {
                DocumentSnapshot d = tx.get(coleccion().document(i.producto.getSerial())).get();
                Long stock = d.exists() ? d.getLong("Stock") : null;
                if (stock == null || stock < i.cantidad) {
                    throw new IllegalStateException("Stock insuficiente para " + i.producto.getNombre()
                            + ": quedan " + (stock == null ? 0 : stock));
                }
                docs.add(d);
            }
            List<Map<String, Object>> articulos = new ArrayList<>();
            for (int k = 0; k < docs.size(); k++) {
                Carrito.Item i = pedido.items.get(k);
                tx.update(docs.get(k).getReference(), "Stock", docs.get(k).getLong("Stock") - i.cantidad);
                articulos.add(Map.of("Serial", i.producto.getSerial(), "Nombre", i.producto.getNombre(),
                        "Cantidad", i.cantidad, "PrecioUnitario", i.producto.getPrecio()));
            }
            Map<String, Object> datos = new HashMap<>();
            datos.put("Id", pedido.id);
            datos.put("Fecha", pedido.fecha);
            datos.put("Cliente", pedido.cliente);
            datos.put("Total", pedido.total);
            datos.put("Tarjeta", pedido.tarjeta);
            datos.put("Autorizacion", pedido.autorizacion);
            datos.put("Articulos", articulos);
            tx.set(db.collection("Pedido").document(pedido.id), datos);
            return null;
        }).get();
    }
}
