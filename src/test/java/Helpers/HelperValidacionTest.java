package Helpers;

import static org.junit.jupiter.api.Assertions.*;

import Logica_Negocio.Producto;
import org.junit.jupiter.api.Test;

class HelperValidacionTest {

    @Test
    void nombreSoloLetrasEsValido() {
        assertEquals(0, HelperValidacion.ValidarTodo("Juan"));
    }

    @Test
    void nombreConNumerosVacioOEspecialesEsInvalido() {
        assertTrue(HelperValidacion.ValidarTodo("Juan2") > 0);
        assertTrue(HelperValidacion.ValidarTodo("") > 0);
        assertTrue(HelperValidacion.ValidarTodo("Ju@n") > 0);
    }

    @Test
    void productoValidoNoTieneError() {
        assertNull(HelperValidacion.ValidarProducto(new Producto("LAP-009", "Portátil 14\"", "Lenovo", "Portátiles", 1_000_000, 5)));
    }

    @Test
    void elMensajeNombraElCampoConError() {
        assertTrue(HelperValidacion.ValidarProducto(new Producto("L", "X", "Lenovo", "Portátiles", 1, 1)).contains("serial"));
        assertTrue(HelperValidacion.ValidarProducto(new Producto("LAP-1", "X", "Len@vo", "Portátiles", 1, 1)).contains("marca"));
        assertTrue(HelperValidacion.ValidarProducto(new Producto("LAP-1", "X", "Lenovo", "Portátiles", 0, 1)).contains("precio"));
        assertTrue(HelperValidacion.ValidarProducto(new Producto("LAP-1", "X", "Lenovo", "Portátiles", 1, -1)).contains("stock"));
    }
}
