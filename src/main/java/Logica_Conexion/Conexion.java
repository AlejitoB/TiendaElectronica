package Logica_Conexion;

import Helpers.HelperConfig;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Fábrica de conexiones para Firestore (nube) y JDBC/MySQL (local).
 */
public class Conexion {

    public static Firestore db;
    private static Connection con = null;
    private static String urlActiva = null;

    // true = Firebase (nube), false = MySQL (local).
    public static boolean modoNube = true;

    static {
        // Java en Windows abre sockets internos en AppData\Local; user.home evita bloqueos de loopback.
        if (System.getProperty("jdk.net.unixdomain.tmpdir") == null) {
            System.setProperty("jdk.net.unixdomain.tmpdir", System.getProperty("user.home"));
        }
    }

    public static boolean Conectar() {
        if (db != null) {
            return true;
        }
        try (FileInputStream as = new FileInputStream(HelperConfig.Obtener("firebase.credenciales"))) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(as))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            db = FirestoreClient.getFirestore();
            return true;
        } catch (Exception | ExceptionInInitializerError e) {
            System.out.println("Error Firebase: " + e);
            return false;
        }
    }

    /** Abre una conexión JDBC independiente por operación/transacción para evitar condiciones de carrera. */
    public static Connection nuevaConexion() throws SQLException {
        String url = HelperConfig.Obtener("mysql.url");
        if (url == null || url.isBlank()) {
            throw new SQLException("No se configuró mysql.url para la base de datos local");
        }
        return DriverManager.getConnection(url,
                HelperConfig.Obtener("mysql.usuario"),
                HelperConfig.Obtener("mysql.clave"));
    }

    /**
     * Conexión compartida para compatibilidad con utilidades y pruebas heredadas.
     * En los DAO usar {@link #nuevaConexion()} dentro de try-with-resources.
     */
    public static synchronized Connection getConnection() {
        try {
            String url = HelperConfig.Obtener("mysql.url");
            if (con == null || con.isClosed() || !Objects.equals(urlActiva, url)) {
                con = nuevaConexion();
                urlActiva = url;
            }
        } catch (SQLException e) {
            System.out.println("Error MySQL: " + e.getMessage());
        }
        return con;
    }
}
