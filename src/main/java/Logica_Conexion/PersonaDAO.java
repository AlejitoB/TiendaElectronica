package Logica_Conexion;

import Logica_Negocio.Persona;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * DAO de clientes en MySQL local. Cada operación usa su propia conexión cerrada
 * automáticamente al finalizar para evitar errores de concurrencia.
 */
public class PersonaDAO implements DAOInterfacePersona {

    private static final Set<String> URLS_INICIALIZADAS = new HashSet<>();

    private static Connection conexionActiva() throws SQLException {
        Connection con = Conexion.nuevaConexion();
        asegurarTabla(con);
        return con;
    }

    private static synchronized void asegurarTabla(Connection con) throws SQLException {
        String url = con.getMetaData().getURL();
        if (URLS_INICIALIZADAS.contains(url)) {
            return;
        }
        try (Statement st = con.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS persona (Uid VARCHAR(40) PRIMARY KEY, Nombre VARCHAR(60),"
                    + " Apellido VARCHAR(60), Cedula VARCHAR(30), Direccion VARCHAR(100), Producto VARCHAR(150),"
                    + " Nom_img VARCHAR(100), Correo VARCHAR(100) NOT NULL DEFAULT '', Telefono VARCHAR(30) NOT NULL DEFAULT '')");
        }
        if (!tieneColumna(con, "persona", "Correo")) {
            try (Statement st = con.createStatement()) {
                st.executeUpdate("ALTER TABLE persona ADD COLUMN Correo VARCHAR(100) NOT NULL DEFAULT ''");
            }
        }
        if (!tieneColumna(con, "persona", "Telefono")) {
            try (Statement st = con.createStatement()) {
                st.executeUpdate("ALTER TABLE persona ADD COLUMN Telefono VARCHAR(30) NOT NULL DEFAULT ''");
            }
        }
        URLS_INICIALIZADAS.add(url);
    }

    private static boolean tieneColumna(Connection con, String tabla, String columna) throws SQLException {
        DatabaseMetaData meta = con.getMetaData();
        try (ResultSet rs = meta.getColumns(con.getCatalog(), null, tabla, columna)) {
            if (rs.next()) {
                return true;
            }
        }
        try (ResultSet rs = meta.getColumns(con.getCatalog(), null, tabla.toUpperCase(), columna.toUpperCase())) {
            return rs.next();
        }
    }

    private static Persona aPersona(ResultSet rs) throws SQLException {
        return new Persona(
                rs.getString("Uid"),
                rs.getString("Nombre"),
                rs.getString("Apellido"),
                rs.getString("Direccion"),
                rs.getString("Cedula"),
                rs.getString("Producto"),
                rs.getString("Nom_img"),
                rs.getString("Correo"),
                rs.getString("Telefono"));
    }

    @Override
    public int add(Persona per) throws SQLException {
        String query = "INSERT INTO persona(Uid, Nombre, Apellido, Cedula, Direccion, Producto, Nom_img, Correo, Telefono) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = conexionActiva();
                PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, per.getUid());
            ps.setString(2, per.getNombre());
            ps.setString(3, per.getApellido());
            ps.setString(4, per.getCedula());
            ps.setString(5, per.getDireccion());
            ps.setString(6, per.getProducto());
            ps.setString(7, per.getNom_img());
            ps.setString(8, per.getCorreo());
            ps.setString(9, per.getTelefono());
            return ps.executeUpdate();
        }
    }

    @Override
    public int delete(String id) throws SQLException {
        String query = "DELETE FROM persona WHERE Uid = ?";
        try (Connection con = conexionActiva();
                PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, id);
            return ps.executeUpdate();
        }
    }

    @Override
    public Persona getPersona(String id) throws SQLException {
        String query = "SELECT * FROM persona WHERE Uid = ?";
        try (Connection con = conexionActiva();
                PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? aPersona(rs) : null;
            }
        }
    }

    @Override
    public ArrayList<Persona> getPersona() throws SQLException {
        String query = "SELECT * FROM persona";
        ArrayList<Persona> ls = new ArrayList<>();
        try (Connection con = conexionActiva();
                PreparedStatement ps = con.prepareStatement(query);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ls.add(aPersona(rs));
            }
        }
        return ls;
    }

    @Override
    public void update(Persona per) throws SQLException {
        String query = "UPDATE persona SET Nombre=?, Apellido=?, Direccion=?, Cedula=?, Producto=?, Nom_img=?, Correo=?, Telefono=? "
                + "WHERE Uid=?";
        try (Connection con = conexionActiva();
                PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, per.getNombre());
            ps.setString(2, per.getApellido());
            ps.setString(3, per.getDireccion());
            ps.setString(4, per.getCedula());
            ps.setString(5, per.getProducto());
            ps.setString(6, per.getNom_img());
            ps.setString(7, per.getCorreo());
            ps.setString(8, per.getTelefono());
            ps.setString(9, per.getUid());
            ps.executeUpdate();
        }
    }

    @Override
    public void close() throws SQLException {
        // Las conexiones se cierran automáticamente en cada método.
    }
}
