package Helpers;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HelperAuditoriaTest {

    @Test
    void agregaUnaLineaPorAccion(@TempDir Path dir) throws Exception {
        String original = HelperAuditoria.archivo;
        HelperAuditoria.archivo = dir.resolve("auditoria.log").toString();
        try {
            HelperAuditoria.Registrar("ELIMINAR_CLIENTE", "Uid=7");
            HelperAuditoria.Registrar("VENTA", "PED-1\n2 x LAP-001");
            List<String> lineas = Files.readAllLines(Path.of(HelperAuditoria.archivo));
            assertEquals(2, lineas.size());
            assertTrue(lineas.get(0).endsWith("| ELIMINAR_CLIENTE | Uid=7"));
            assertTrue(lineas.get(1).contains("PED-1 2 x LAP-001"));
        } finally {
            HelperAuditoria.archivo = original;
        }
    }
}
