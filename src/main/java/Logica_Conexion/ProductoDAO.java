package Logica_Conexion;

import Logica_Negocio.Carrito;
import Logica_Negocio.Pedido;
import Logica_Negocio.Producto;
import Logica_Negocio.ResumenPedido;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Inventario y pedidos en MySQL local. Usa una conexión por operación para
 * garantizar transacciones seguras en ejecuciones concurrentes.
 */
public class ProductoDAO implements DAOInterfaceProducto {

    private static final Set<String> URLS_INICIALIZADAS = new HashSet<>();

    static Connection conexion() throws SQLException {
        Connection con = Conexion.nuevaConexion();
        asegurarTablas(con);
        return con;
    }

    private static synchronized void asegurarTablas(Connection con) throws SQLException {
        String url = con.getMetaData().getURL();
        if (URLS_INICIALIZADAS.contains(url)) {
            return;
        }
        try (Statement st = con.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS producto ("
                    + "Serial VARCHAR(40) PRIMARY KEY, Nombre VARCHAR(120) NOT NULL, Marca VARCHAR(60) NOT NULL,"
                    + "Categoria VARCHAR(60) NOT NULL, Precio BIGINT NOT NULL, Stock INT NOT NULL)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS pedido ("
                    + "Id VARCHAR(40) PRIMARY KEY, Fecha VARCHAR(30) NOT NULL, Cliente VARCHAR(60) NOT NULL,"
                    + "ClienteUid VARCHAR(40) NOT NULL DEFAULT '', Total BIGINT NOT NULL,"
                    + "Tarjeta VARCHAR(30) NOT NULL, Autorizacion VARCHAR(40) NOT NULL, Detalle TEXT NOT NULL)");
        }
        // Si la tabla pedido ya existía de una versión previa, agrega ClienteUid de forma segura.
        if (!tieneColumna(con, "pedido", "ClienteUid")) {
            try (Statement st = con.createStatement()) {
                st.executeUpdate("ALTER TABLE pedido ADD COLUMN ClienteUid VARCHAR(40) NOT NULL DEFAULT ''");
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

    private static Producto aProducto(ResultSet rs) throws SQLException {
        return new Producto(rs.getString("Serial"), rs.getString("Nombre"), rs.getString("Marca"),
                rs.getString("Categoria"), rs.getLong("Precio"), rs.getInt("Stock"));
    }

    private static ResumenPedido aResumen(ResultSet rs) throws SQLException {
        String uid = "";
        try {
            uid = rs.getString("ClienteUid");
        } catch (SQLException ignored) {
        }
        return new ResumenPedido(
                rs.getString("Id"),
                rs.getString("Fecha"),
                rs.getString("Cliente"),
                uid == null ? "" : uid,
                rs.getLong("Total"),
                rs.getString("Tarjeta"),
                rs.getString("Autorizacion"),
                rs.getString("Detalle"));
    }

    @Override
    public ArrayList<Producto> listar() throws Exception {
        ArrayList<Producto> ls = new ArrayList<>();
        try (Connection con = conexion();
                PreparedStatement ps = con.prepareStatement("SELECT * FROM producto ORDER BY Categoria, Nombre");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ls.add(aProducto(rs));
            }
        }
        return ls;
    }

    @Override
    public Producto buscar(String serial) throws Exception {
        try (Connection con = conexion();
                PreparedStatement ps = con.prepareStatement("SELECT * FROM producto WHERE Serial = ?")) {
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
        try (Connection con = conexion();
                PreparedStatement ps = con.prepareStatement(sql)) {
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
        try (Connection con = conexion();
                PreparedStatement ps = con.prepareStatement("DELETE FROM producto WHERE Serial = ?")) {
            ps.setString(1, serial);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public void reservarStock(List<Carrito.Item> items) throws Exception {
        try (Connection con = conexion()) {
            con.setAutoCommit(false);
            try {
                descontarStockEnTx(con, items);
                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        }
    }

    @Override
    public void liberarStock(List<Carrito.Item> items) throws Exception {
        if (items == null || items.isEmpty()) {
            return;
        }
        try (Connection con = conexion()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE producto SET Stock = Stock + ? WHERE Serial = ?")) {
                for (Carrito.Item i : items) {
                    ps.setInt(1, i.getCantidad());
                    ps.setString(2, i.producto.getSerial());
                    ps.executeUpdate();
                }
                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        }
    }

    @Override
    public void guardarPedido(Pedido pedido) throws Exception {
        try (Connection con = conexion()) {
            insertarPedidoEnTx(con, pedido);
        }
    }

    @Override
    public List<ResumenPedido> listarPedidos() throws Exception {
        List<ResumenPedido> ls = new ArrayList<>();
        try (Connection con = conexion();
                PreparedStatement ps = con.prepareStatement("SELECT * FROM pedido ORDER BY Fecha DESC");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ls.add(aResumen(rs));
            }
        }
        return ls;
    }

    @Override
    public List<ResumenPedido> listarPedidosPorCliente(String cliente) throws Exception {
        List<ResumenPedido> ls = new ArrayList<>();
        try (Connection con = conexion();
                PreparedStatement ps = con.prepareStatement(
                        "SELECT * FROM pedido WHERE Cliente = ? OR ClienteUid = ? ORDER BY Fecha DESC")) {
            ps.setString(1, cliente);
            ps.setString(2, cliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ls.add(aResumen(rs));
                }
            }
        }
        return ls;
    }

    @Override
    public void registrarVenta(Pedido pedido) throws Exception {
        try (Connection con = conexion()) {
            con.setAutoCommit(false);
            try {
                descontarStockEnTx(con, pedido.items);
                insertarPedidoEnTx(con, pedido);
                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        }
    }

    private static void descontarStockEnTx(Connection con, List<Carrito.Item> items) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE producto SET Stock = Stock - ? WHERE Serial = ? AND Stock >= ?")) {
            for (Carrito.Item i : items) {
                ps.setInt(1, i.getCantidad());
                ps.setString(2, i.producto.getSerial());
                ps.setInt(3, i.getCantidad());
                if (ps.executeUpdate() != 1) {
                    throw new IllegalStateException("Stock insuficiente para " + i.producto.getNombre());
                }
            }
        }
    }

    private static void insertarPedidoEnTx(Connection con, Pedido pedido) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO pedido (Id, Fecha, Cliente, ClienteUid, Total, Tarjeta, Autorizacion, Detalle) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, pedido.id);
            ps.setString(2, pedido.fecha);
            ps.setString(3, pedido.cliente);
            ps.setString(4, pedido.clienteUid);
            ps.setLong(5, pedido.total);
            ps.setString(6, pedido.tarjeta);
            ps.setString(7, pedido.autorizacion);
            ps.setString(8, pedido.Detalle());
            ps.executeUpdate();
        }
    }
}
