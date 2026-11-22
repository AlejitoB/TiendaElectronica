package Logica_Conexion;

import static org.junit.jupiter.api.Assertions.*;

import Logica_Negocio.Carrito;
import Logica_Negocio.Pedido;
import Logica_Negocio.Producto;
import Logica_Negocio.ResumenPedido;
import java.sql.Statement;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de reserva atómica, liberación de stock e historial de pedidos en ProductoDAO (H2).
 */
class ProductoDAOTest {

    @BeforeAll
    static void baseEnMemoria() {
        System.setProperty("tienda.mysql.url", "jdbc:h2:mem:tienda;MODE=MySQL;DB_CLOSE_DELAY=-1");
        System.setProperty("tienda.mysql.usuario", "sa");
        System.setProperty("tienda.mysql.clave", "");
    }

    @BeforeEach
    void limpiar() throws Exception {
        ProductoDAO dao = new ProductoDAO();
        dao.listar();
        try (Statement st = Conexion.nuevaConexion().createStatement()) {
            st.executeUpdate("DELETE FROM pedido");
            st.executeUpdate("DELETE FROM producto");
        }
        dao.guardar(new Producto("LAP-1", "Portátil", "Lenovo", "Portátiles", 2_000_000, 3));
        dao.guardar(new Producto("MOU-1", "Mouse", "Logitech", "Periféricos", 50_000, 1));
    }

    @Test
    void reservaAtomicaYLiberacionAnteFalloDePago() throws Exception {
        ProductoDAO dao = new ProductoDAO();
        Producto lap = dao.buscar("LAP-1");
        Producto mou = dao.buscar("MOU-1");

        Carrito c = new Carrito();
        c.Agregar(lap, 2);
        c.Agregar(mou, 1);

        dao.reservarStock(c.Items());
        assertEquals(1, dao.buscar("LAP-1").getStock());
        assertEquals(0, dao.buscar("MOU-1").getStock());

        // Si el cobro es rechazado, liberarStock restaura todas las unidades.
        dao.liberarStock(c.Items());
        assertEquals(3, dao.buscar("LAP-1").getStock());
        assertEquals(1, dao.buscar("MOU-1").getStock());
    }

    @Test
    void reservaFallaSinDejarCambiosAMedias() throws Exception {
        ProductoDAO dao = new ProductoDAO();
        Producto lap = dao.buscar("LAP-1");
        Producto mouFalso = new Producto("MOU-1", "Mouse", "Logitech", "Periféricos", 50_000, 5);

        Carrito c = new Carrito();
        c.Agregar(lap, 2);
        c.Agregar(mouFalso, 2); // en BD solo hay 1

        assertThrows(IllegalStateException.class, () -> dao.reservarStock(c.Items()));
        assertEquals(3, dao.buscar("LAP-1").getStock());
    }

    @Test
    void guardaYConsultaPedidosPorCliente() throws Exception {
        ProductoDAO dao = new ProductoDAO();
        Producto lap = dao.buscar("LAP-1");
        Carrito c = new Carrito();
        c.Agregar(lap, 1);

        Pedido p1 = new Pedido("Ana Ruiz", "50", c, "VISA **** 4242", "111222");
        dao.registrarVenta(p1);

        List<ResumenPedido> todos = dao.listarPedidos();
        assertEquals(1, todos.size());
        assertEquals("50", todos.get(0).clienteUid());

        assertEquals(1, dao.listarPedidosPorCliente("50").size());
        assertEquals(1, dao.listarPedidosPorCliente("Ana Ruiz").size());
        assertEquals(0, dao.listarPedidosPorCliente("otro").size());
    }
}
