package Logica_Negocio;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Compra confirmada. De la tarjeta solo se guarda la franquicia y los últimos 4 dígitos.
 */
public class Pedido {

    public final String id;
    public final String fecha;
    public final String cliente;
    public final String clienteUid;
    public final List<Carrito.Item> items;
    public final long total;
    public final String tarjeta;
    // La asigna la pasarela al aprobar; el id del pedido es la referencia que se envía a Wompi.
    public String autorizacion;

    public Pedido(String cliente, Carrito carrito, String tarjeta, String autorizacion) {
        this(cliente, "", carrito, tarjeta, autorizacion);
    }

    public Pedido(String cliente, String clienteUid, Carrito carrito, String tarjeta, String autorizacion) {
        this.id = "PED-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.cliente = cliente == null ? "" : cliente;
        this.clienteUid = clienteUid == null ? "" : clienteUid;
        List<Carrito.Item> copia = new ArrayList<>();
        for (Carrito.Item i : carrito.Items()) {
            copia.add(new Carrito.Item(i.producto, i.getCantidad()));
        }
        this.items = Collections.unmodifiableList(copia);
        this.total = carrito.Total();
        this.tarjeta = tarjeta == null ? "" : tarjeta;
        this.autorizacion = autorizacion;
    }

    public ResumenPedido aResumen() {
        return new ResumenPedido(id, fecha, cliente, clienteUid, total, tarjeta, autorizacion, Detalle());
    }

    /** Resumen de una línea por artículo, para guardar y para el recibo. */
    public String Detalle() {
        StringBuilder sb = new StringBuilder();
        for (Carrito.Item i : items) {
            sb.append(i.getCantidad()).append(" x ").append(i.producto.getSerial()).append(' ')
                    .append(i.producto.getNombre()).append(" = ").append(i.Subtotal()).append('\n');
        }
        return sb.toString().trim();
    }
}
