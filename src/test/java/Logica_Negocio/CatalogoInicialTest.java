package Logica_Negocio;

import static org.junit.jupiter.api.Assertions.*;

import Helpers.HelperValidacion;
import Logica_Conexion.DAOProductoMemoria;
import org.junit.jupiter.api.Test;

class CatalogoInicialTest {

    @Test
    void siembraSoloSiElInventarioEstaVacio() throws Exception {
        DAOProductoMemoria dao = new DAOProductoMemoria();
        assertEquals(CatalogoInicial.PRODUCTOS.size(), CatalogoInicial.SembrarSiVacio(dao));
        assertEquals(0, CatalogoInicial.SembrarSiVacio(dao));
        assertEquals(CatalogoInicial.PRODUCTOS.size(), dao.listar().size());
    }

    @Test
    void todosLosArticulosDelCatalogoSonValidos() {
        for (Producto p : CatalogoInicial.PRODUCTOS) {
            assertNull(HelperValidacion.ValidarProducto(p), p.getSerial());
        }
    }
}
