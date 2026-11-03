package Logica_Conexion;

import Logica_Negocio.Producto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Inventario en MySQL local (tabla producto). La tabla se crea si no existe.
 */
public class ProductoDAO implements DAOInterfaceProducto {

    private static boolean tablasListas = false;

    static Connection conexion() throws SQLException {
        Connection con = Conexion.getConnection();
        if (con == null) {
            throw new SQLException("No hay conexion con la base de datos local (MySQL)");
        }
        if (!tablasListas) {
            try (Statement st = con.createStatement()) {
                st.executeUpdate("CREATE TABLE IF NOT EXISTS producto ("
                        + "Serial VARCHAR(40) PRIMARY KEY, Nombre VARCHAR(120) NOT NULL, Marca VARCHAR(60) NOT NULL,"
                        + "Categoria VARCHAR(60) NOT NULL, Precio BIGINT NOT NULL, Stock INT NOT NULL)");
                st.executeUpdate("CREATE TABLE IF NOT EXISTS pedido ("
                        + "Id VARCHAR(40) PRIMARY KEY, Fecha VARCHAR(30) NOT NULL, Cliente VARCHAR(60) NOT NULL,"
                        + "Total BIGINT NOT NULL, Tarjeta VARCHAR(30) NOT NULL, Detalle TEXT NOT NULL)");
            }
            tablasListas = true;
        }
        return con;
    }

    private static Producto aProducto(ResultSet rs) throws SQLException {
        return new Producto(rs.getString("Serial"), rs.getString("Nombre"), rs.getString("Marca"),
                rs.getString("Categoria"), rs.getLong("Precio"), rs.getInt("Stock"));
    }

    @Override
    public ArrayList<Producto> listar() throws Exception {
        ArrayList<Producto> ls = new ArrayList<>();
        try (PreparedStatement ps = conexion().prepareStatement("SELECT * FROM producto ORDER BY Categoria, Nombre");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ls.add(aProducto(rs));
            }
        }
        return ls;
    }

    @Override
    public Producto buscar(String serial) throws Exception {
        try (PreparedStatement ps = conexion().prepareStatement("SELECT * FROM producto WHERE Serial = ?")) {
            ps.setString(1, serial);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? aProducto(rs) : null;
            }
        }
    }

    @Override
    public void guardar(Producto p) throws Exception {
        String sql = "INSERT INTO producto (Serial, Nombre, Marca, Categoria, Precio, Stock) VALUES (?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE Nombre = VALUES(Nombre), Marca = VALUES(Marca), Categoria = VALUES(Categoria), "
                + "Precio = VALUES(Precio), Stock = VALUES(Stock)";
        try (PreparedStatement ps = conexion().prepareStatement(sql)) {
            ps.setString(1, p.getSerial());
            ps.setString(2, p.getNombre());
            ps.setString(3, p.getMarca());
            ps.setString(4, p.getCategoria());
            ps.setLong(5, p.getPrecio());
            ps.setInt(6, p.getStock());
            ps.executeUpdate();
        }
    }

    @Override
    public boolean eliminar(String serial) throws Exception {
        try (PreparedStatement ps = conexion().prepareStatement("DELETE FROM producto WHERE Serial = ?")) {
            ps.setString(1, serial);
            return ps.executeUpdate() > 0;
        }
    }
}
