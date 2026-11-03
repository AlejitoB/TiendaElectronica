package Helpers;

import Logica_Negocio.Usuario;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Historial de auditoria: cada actualizacion, eliminacion y venta queda en auditoria.log
 * con fecha, usuario, accion y detalle.
 */
public class HelperAuditoria {

    public static String archivo = "auditoria.log";

    public static synchronized void Registrar(String accion, String detalle) {
        String usuario = Usuario.sesion == null ? "desconocido" : Usuario.sesion.getUsu();
        String linea = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                + " | " + usuario + " | " + accion + " | " + detalle.replace('\n', ' ');
        try (PrintWriter out = new PrintWriter(new FileWriter(archivo, true))) {
            out.println(linea);
        } catch (IOException e) {
            System.out.println("No se pudo escribir la auditoria: " + e.getMessage());
        }
    }
}
