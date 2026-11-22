package Helpers;

import static org.junit.jupiter.api.Assertions.*;

import Logica_Negocio.Persona;
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
        assertNull(HelperValidacion.ValidarProducto(new Producto("NET-001", "Router AX", "TP-Link 3M", "Redes", 150_000, 3)));
        assertEquals(0, HelperValidacion.ValidarTodoContraseña("Clave.Segura-2026/"));
        assertTrue(HelperValidacion.ValidarVacio("   ") > 0);
    }

    @Test
    void elMensajeNombraElCampoConError() {
        assertTrue(HelperValidacion.ValidarProducto(new Producto("L", "X", "Lenovo", "Portátiles", 1, 1)).contains("serial"));
        assertTrue(HelperValidacion.ValidarProducto(new Producto("LAP-1", "X", "Len@vo", "Portátiles", 1, 1)).contains("marca"));
        assertTrue(HelperValidacion.ValidarProducto(new Producto("LAP-1", "X", "Lenovo", "Portátiles", 0, 1)).contains("precio"));
        assertTrue(HelperValidacion.ValidarProducto(new Producto("LAP-1", "X", "Lenovo", "Portátiles", 1, -1)).contains("stock"));
    }

    @Test
    void personaValidaYErroresPorCampo() {
        Persona valida = new Persona("10", "Ana", "Ruiz", "Calle 5 # 3-20", "1061234567", "", "image1", "ana@correo.co", "3001234567");
        assertNull(HelperValidacion.ValidarPersona(valida));
        assertTrue(HelperValidacion.ValidarPersona(new Persona("ab", "Ana", "Ruiz", "Calle 5", "123", "", "img")).contains("UID"));
        assertTrue(HelperValidacion.ValidarPersona(new Persona("10", "Ana2", "Ruiz", "Calle 5", "123", "", "img")).contains("nombre"));
        assertTrue(HelperValidacion.ValidarPersona(new Persona("10", "Ana", "Ruiz", "Calle $", "123", "", "img")).contains("dirección"));
        assertTrue(HelperValidacion.ValidarPersona(new Persona("10", "Ana", "Ruiz", "Calle 5", "abc", "", "img")).contains("cédula"));
        assertTrue(HelperValidacion.ValidarPersona(new Persona("10", "Ana", "Ruiz", "Calle 5", "123", "", "img", "mal@", "")).contains("correo"));
        assertTrue(HelperValidacion.ValidarPersona(new Persona("10", "Ana", "Ruiz", "Calle 5", "123", "", "img", "", "abc")).contains("teléfono"));
    }
}
