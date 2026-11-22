package Logica_Conexion;

import static org.junit.jupiter.api.Assertions.*;

import Helpers.HelperAuditoria;
import Logica_Negocio.Persona;
import java.nio.file.Path;
import java.sql.Statement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Pruebas del servicio unificado de clientes en modo local (H2 en memoria).
 */
class ServicioPersonaTest {

    private String archivoOriginal;

    @BeforeAll
    static void baseEnMemoria() {
        System.setProperty("tienda.mysql.url", "jdbc:h2:mem:tienda;MODE=MySQL;DB_CLOSE_DELAY=-1");
        System.setProperty("tienda.mysql.usuario", "sa");
        System.setProperty("tienda.mysql.clave", "");
    }

    @BeforeEach
    void limpiar(@TempDir Path dir) throws Exception {
        archivoOriginal = HelperAuditoria.archivo;
        HelperAuditoria.archivo = dir.resolve("auditoria.log").toString();
        try (Statement st = Conexion.getConnection().createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS persona (Uid VARCHAR(40) PRIMARY KEY, Nombre VARCHAR(60),"
                    + " Apellido VARCHAR(60), Cedula VARCHAR(30), Direccion VARCHAR(100), Producto VARCHAR(150), Nom_img VARCHAR(100))");
            st.executeUpdate("DELETE FROM persona");
        }
    }

    @AfterEach
    void restaurar() {
        HelperAuditoria.archivo = archivoOriginal;
    }

    @Test
    void crudCompletoYProteccionDeDuplicados() throws Exception {
        ServicioPersona servicio = new ServicioPersona(false, new PersonaDAO());
        Persona ana = new Persona("50", "Ana", "Ruiz", "Calle 5", "106123", "", "image1", "ana@correo.co", "3001112233");

        servicio.guardar(ana, true);
        assertEquals(1, servicio.listar().size());
        Persona leida = servicio.buscar("50");
        assertEquals("Ana", leida.getNombre());
        assertEquals("ana@correo.co", leida.getCorreo());
        assertEquals("3001112233", leida.getTelefono());
        assertThrows(IllegalStateException.class, () -> servicio.guardar(ana, true));

        ana.setDireccion("Carrera 9");
        ana.setCorreo("ana.nueva@correo.co");
        servicio.guardar(ana, false);
        assertEquals("Carrera 9", servicio.buscar("50").getDireccion());
        assertEquals("ana.nueva@correo.co", servicio.buscar("50").getCorreo());

        assertTrue(servicio.eliminar("50"));
        assertFalse(servicio.eliminar("50"));
        assertNull(servicio.buscar("50"));
    }
}
