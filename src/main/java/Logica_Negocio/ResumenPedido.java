package Logica_Negocio;

/**
 * Vista inmutable de un pedido almacenado en MySQL o Firestore para historial y reportes.
 */
public record ResumenPedido(
        String id,
        String fecha,
        String cliente,
        String clienteUid,
        long total,
        String tarjeta,
        String autorizacion,
        String detalle) {
}
