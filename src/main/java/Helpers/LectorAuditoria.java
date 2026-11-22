package Helpers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lee las entradas de auditoria.log y las convierte en registros estructurados
 * para mostrarlas en la vista administrativa.
 */
public final class LectorAuditoria {

    private LectorAuditoria() {
    }

    public record Registro(String fecha, String usuario, String accion, String detalle) {
    }

    public static List<Registro> LeerRecientes() {
        return LeerDesde(Path.of(HelperAuditoria.archivo));
    }

    public static List<Registro> LeerDesde(Path ruta) {
        if (!Files.isRegularFile(ruta)) {
            return List.of();
        }
        List<Registro> registros = new ArrayList<>();
        try {
            List<String> lineas = Files.readAllLines(ruta);
            for (String linea : lineas) {
                if (linea == null || linea.isBlank()) {
                    continue;
                }
                String[] partes = linea.split("\\s*\\|\\s*", 4);
                if (partes.length == 4) {
                    registros.add(new Registro(partes[0], partes[1], partes[2], partes[3]));
                } else {
                    registros.add(new Registro("", "", "EVENTO", linea.trim()));
                }
            }
        } catch (Exception e) {
            System.out.println("No se pudo leer la auditoria: " + e.getMessage());
        }
        Collections.reverse(registros);
        return registros;
    }
}
