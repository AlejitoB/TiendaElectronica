package Logica_Negocio;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CarritoTest {

    private final Producto mouse = new Producto("MOU-001", "Mouse", "Logitech", "Periféricos", 50_000, 3);
    private final Producto ssd = new Producto("ALM-001", "SSD", "Kingston", "Almacenamiento", 300_000, 10);

    @Test
    void sumaCantidadesYTotal() {
        Carrito c = new Carrito();
        assertNull(c.Agregar(mouse, 2));
        assertNull(c.Agregar(mouse, 1));
        assertNull(c.Agregar(ssd, 1));
        assertEquals(2, c.Items().size());
        assertEquals(4, c.Unidades());
        assertEquals(3 * 50_000 + 300_000, c.Total());
    }

    @Test
    void noPermiteSuperarElStock() {
        Carrito c = new Carrito();
        assertNull(c.Agregar(mouse, 2));
        assertNotNull(c.Agregar(mouse, 2));
        assertEquals(2, c.Buscar("MOU-001").cantidad);
        assertNotNull(c.Agregar(ssd, 0));
    }

    @Test
    void quitarYVaciar() {
        Carrito c = new Carrito();
        c.Agregar(mouse, 1);
        c.Agregar(ssd, 1);
        c.Quitar("MOU-001");
        assertNull(c.Buscar("MOU-001"));
        c.Vaciar();
        assertTrue(c.EstaVacio());
        assertEquals(0, c.Total());
    }

    @Test
    void elPedidoCopiaLosItemsDelCarrito() {
        Carrito c = new Carrito();
        c.Agregar(ssd, 2);
        Pedido p = new Pedido("Cliente", c, "VISA **** 1111", "123456");
        c.Vaciar();
        assertEquals(1, p.items.size());
        assertEquals(600_000, p.total);
        assertEquals("2 x ALM-001 SSD = 600000", p.Detalle());
    }
}
