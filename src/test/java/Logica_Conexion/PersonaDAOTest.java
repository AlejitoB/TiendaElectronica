package Logica_Conexion;

import static org.junit.jupiter.api.Assertions.*;

import Logica_Negocio.Persona;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * PersonaDAO contra una base H2 en memoria (modo MySQL): sin XAMPP ni red.
 * Regresion del issue #3: Cedula y Direccion se guardaban en columnas invertidas.
 */
class PersonaDAOTest {

    @BeforeAll
    static void baseEnMemoria() {
        System.setProperty("tienda.mysql.url", "jdbc:h2:mem:tienda;MODE=MySQL;DB_CLOSE_DELAY=-1");
        System.setProperty("tienda.mysql.usuario", "sa");
        System.setProperty("tienda.mysql.clave", "");
    }

    @BeforeEach
    void tablaLimpia() throws Exception {
        try (Statement st = Conexion.getConnection().createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS persona (Uid VARCHAR(20) PRIMARY KEY, Nombre VARCHAR(60),"
                    + " Apellido VARCHAR(60), Cedula VARCHAR(20), Direccion VARCHAR(100), Producto VARCHAR(100), Nom_img VARCHAR(60))");
            st.executeUpdate("DELETE FROM persona");
        }
    }

    private static Persona ana() {
        // Orden del constructor: uid, nombre, apellido, direccion, cedula, producto, imagen
        return new Persona("1", "Ana", "Ruiz", "Calle 5 # 3-20", "1061234567", "Mouse", "ana.jpg");
    }

    @Test
    void addGuardaCadaCampoEnSuColumna() throws Exception {
        assertEquals(1, new PersonaDAO().add(ana()));

        Connection con = Conexion.getConnection();
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT Cedula, Direccion FROM persona WHERE Uid = '1'")) {
            assertTrue(rs.next());
            assertEquals("1061234567", rs.getString("Cedula"));
            assertEquals("Calle 5 # 3-20", rs.getString("Direccion"));
        }
    }

    @Test
    void loQueSeGuardaSeLeeIgual() throws Exception {
        PersonaDAO dao = new PersonaDAO();
        dao.add(ana());
        Persona leida = dao.getPersona("1");
        assertEquals("1061234567", leida.getCedula());
        assertEquals("Calle 5 # 3-20", leida.getDireccion());
        assertEquals("Ana", leida.getNombre());
    }

    @Test
    void actualizarYEliminar() throws Exception {
        PersonaDAO dao = new PersonaDAO();
        dao.add(ana());
        Persona cambio = new Persona("1", "Ana", "Ruiz", "Carrera 9 # 1-10", "1061234567", "Teclado", "ana.jpg");
        dao.update(cambio);
        assertEquals("Carrera 9 # 1-10", dao.getPersona("1").getDireccion());
        assertEquals(1, dao.getPersona().size());
        assertEquals(1, dao.delete("1"));
        assertNull(dao.getPersona("1"));
    }
}
