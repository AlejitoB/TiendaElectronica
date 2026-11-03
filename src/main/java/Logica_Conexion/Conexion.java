/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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

/**
 *
 * @author Santiago Lopez
 */
public class Conexion {

    public static Firestore db;
    private static Connection con = null;

    // true = Firebase (nube), false = MySQL (local). Lo decide EscogerBD.
    public static boolean modoNube = true;

    static {
        // Java en Windows abre sockets internos en la carpeta temporal; en algunos equipos AppData\Local los bloquea
        // ("Unable to establish loopback connection") y Firebase/Wompi no arrancan. La carpeta del usuario si funciona.
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

            FirebaseApp.initializeApp(options);
            db = FirestoreClient.getFirestore();
            System.out.println("Conexion Exitosa");
            return true;
        } catch (Exception | ExceptionInInitializerError e) {
            // Firebase lanza un Error (no una Exception) si la red local de Java falla; sin esto la app se cierra.
            System.out.println("Error:" + e);
            return false;
        }
    }

    // Singleton: se reutiliza la misma conexion mientras siga abierta.
    public static synchronized Connection getConnection() {
        try {
            if (con == null || con.isClosed()) {
                con = DriverManager.getConnection(HelperConfig.Obtener("mysql.url"),
                        HelperConfig.Obtener("mysql.usuario"), HelperConfig.Obtener("mysql.clave"));
            }
        } catch (SQLException e) {
            System.out.println("Error MySQL:" + e.getMessage());
        }
        return con;
    }

}
