package Logica_Negocio;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Compra confirmada. De la tarjeta solo se guarda la franquicia y los ultimos 4 digitos.
 */
public class Pedido {

    public final String id;
    public final String fecha;
    public final String cliente;
    public final List<Carrito.Item> items;
    public final long total;
    public final String tarjeta;
    // La asigna la pasarela al aprobar; el id del pedido es la referencia que se envia a Wompi.
    public String autorizacion;

    public Pedido(String cliente, Carrito carrito, String tarjeta, String autorizacion) {
        this.id = "PED-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.cliente = cliente;
        this.items = new ArrayList<>(carrito.Items());
        this.total = carrito.Total();
        this.tarjeta = tarjeta;
        this.autorizacion = autorizacion;
    }

    /** Resumen de una linea por articulo, para guardar y para el recibo. */
    public String Detalle() {
        StringBuilder sb = new StringBuilder();
        for (Carrito.Item i : items) {
            sb.append(i.cantidad).append(" x ").append(i.producto.getSerial()).append(' ')
                    .append(i.producto.getNombre()).append(" = ").append(i.Subtotal()).append('\n');
        }
        return sb.toString().trim();
    }
}
