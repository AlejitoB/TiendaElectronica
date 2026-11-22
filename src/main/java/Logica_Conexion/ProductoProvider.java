package Logica_Conexion;

import Logica_Negocio.Carrito;
import Logica_Negocio.Pedido;
import Logica_Negocio.Producto;
import Logica_Negocio.ResumenPedido;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Inventario y pedidos en Firestore (colección "Producto" y "Pedido").
 */
public class ProductoProvider implements DAOInterfaceProducto {

    static final String COLECCION = "Producto";
    static final String COLECCION_PEDIDO = "Pedido";

    private CollectionReference coleccion() {
        return firestore().collection(COLECCION);
    }

    private static Firestore firestore() {
        if (Conexion.db == null) {
            throw new IllegalStateException("No hay conexión activa con Firebase");
        }
        return Conexion.db;
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
    public void reservarStock(List<Carrito.Item> items) throws Exception {
        Firestore db = firestore();
        db.runTransaction(tx -> {
            List<DocumentSnapshot> docs = new ArrayList<>();
            for (Carrito.Item i : items) {
                DocumentSnapshot d = tx.get(coleccion().document(i.producto.getSerial())).get();
                Long stock = d.exists() ? d.getLong("Stock") : null;
                if (stock == null || stock < i.getCantidad()) {
                    throw new IllegalStateException("Stock insuficiente para " + i.producto.getNombre()
                            + ": quedan " + (stock == null ? 0 : stock));
                }
                docs.add(d);
            }
            for (int k = 0; k < docs.size(); k++) {
                Carrito.Item i = items.get(k);
                tx.update(docs.get(k).getReference(), "Stock", docs.get(k).getLong("Stock") - i.getCantidad());
            }
            return null;
        }).get();
    }

    @Override
    public void liberarStock(List<Carrito.Item> items) throws Exception {
        if (items == null || items.isEmpty()) {
            return;
        }
        Firestore db = firestore();
        db.runTransaction(tx -> {
            List<DocumentSnapshot> docs = new ArrayList<>();
            for (Carrito.Item i : items) {
                docs.add(tx.get(coleccion().document(i.producto.getSerial())).get());
            }
            for (int k = 0; k < docs.size(); k++) {
                DocumentSnapshot d = docs.get(k);
                if (d.exists()) {
                    Long actual = d.getLong("Stock");
                    long nuevo = (actual == null ? 0 : actual) + items.get(k).getCantidad();
                    tx.update(d.getReference(), "Stock", nuevo);
                }
            }
            return null;
        }).get();
    }

    @Override
    public void guardarPedido(Pedido pedido) throws Exception {
        firestore().collection(COLECCION_PEDIDO).document(pedido.id).set(aMapaPedido(pedido)).get();
    }

    @Override
    public List<ResumenPedido> listarPedidos() throws Exception {
        List<ResumenPedido> ls = new ArrayList<>();
        for (DocumentSnapshot d : firestore().collection(COLECCION_PEDIDO)
                .orderBy("Fecha", Query.Direction.DESCENDING).get().get().getDocuments()) {
            ls.add(aResumen(d));
        }
        return ls;
    }

    @Override
    public List<ResumenPedido> listarPedidosPorCliente(String cliente) throws Exception {
        List<ResumenPedido> ls = new ArrayList<>();
        for (ResumenPedido r : listarPedidos()) {
            if (cliente != null && (cliente.equals(r.cliente()) || cliente.equals(r.clienteUid()))) {
                ls.add(r);
            }
        }
        return ls;
    }

    @Override
    public void registrarVenta(Pedido pedido) throws Exception {
        Firestore db = firestore();
        db.runTransaction(tx -> {
            List<DocumentSnapshot> docs = new ArrayList<>();
            for (Carrito.Item i : pedido.items) {
                DocumentSnapshot d = tx.get(coleccion().document(i.producto.getSerial())).get();
                Long stock = d.exists() ? d.getLong("Stock") : null;
                if (stock == null || stock < i.getCantidad()) {
                    throw new IllegalStateException("Stock insuficiente para " + i.producto.getNombre()
                            + ": quedan " + (stock == null ? 0 : stock));
                }
                docs.add(d);
            }
            for (int k = 0; k < docs.size(); k++) {
                Carrito.Item i = pedido.items.get(k);
                tx.update(docs.get(k).getReference(), "Stock", docs.get(k).getLong("Stock") - i.getCantidad());
            }
            tx.set(db.collection(COLECCION_PEDIDO).document(pedido.id), aMapaPedido(pedido));
            return null;
        }).get();
    }

    private static Map<String, Object> aMapaPedido(Pedido pedido) {
        List<Map<String, Object>> articulos = new ArrayList<>();
        for (Carrito.Item i : pedido.items) {
            articulos.add(Map.of("Serial", i.producto.getSerial(), "Nombre", i.producto.getNombre(),
                    "Cantidad", i.getCantidad(), "PrecioUnitario", i.producto.getPrecio()));
        }
        Map<String, Object> datos = new HashMap<>();
        datos.put("Id", pedido.id);
        datos.put("Fecha", pedido.fecha);
        datos.put("Cliente", pedido.cliente);
        datos.put("ClienteUid", pedido.clienteUid);
        datos.put("Total", pedido.total);
        datos.put("Tarjeta", pedido.tarjeta);
        datos.put("Autorizacion", pedido.autorizacion);
        datos.put("Detalle", pedido.Detalle());
        datos.put("Articulos", articulos);
        return datos;
    }

    private static ResumenPedido aResumen(DocumentSnapshot d) {
        Long total = d.getLong("Total");
        String detalle = d.getString("Detalle");
        if (detalle == null) {
            detalle = "";
        }
        return new ResumenPedido(
                nvl(d.getString("Id")),
                nvl(d.getString("Fecha")),
                nvl(d.getString("Cliente")),
                nvl(d.getString("ClienteUid")),
                total == null ? 0 : total,
                nvl(d.getString("Tarjeta")),
                nvl(d.getString("Autorizacion")),
                detalle);
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }
}
