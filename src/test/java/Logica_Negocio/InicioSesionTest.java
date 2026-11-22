package Logica_Negocio;

import static org.junit.jupiter.api.Assertions.*;

import Helpers.HelperCifrado;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class InicioSesionTest {

    private final Usuario admin = new Administrador("Admin", HelperCifrado.CifrarSHA256("12345"));
    private final Usuario adminPbkdf2 = new Administrador("Admin", HelperCifrado.HashearClave("Clave.Segura-2026/"));

    @Test
    void logOnConCredencialesCorrectas() {
        assertTrue(admin.LogOn(HelperCifrado.CifrarSHA256("Admin"), HelperCifrado.CifrarSHA256("12345")));
        assertTrue(admin.Autenticar("Admin", "12345"));
        assertTrue(adminPbkdf2.Autenticar("Admin", "Clave.Segura-2026/"));
        assertFalse(adminPbkdf2.Autenticar("Admin", "otra"));
    }

    @Test
    void cifradoConCaracteresEspecialesYUt8Consistente() {
        String hash = HelperCifrado.HashearClave("Ñandú-2026!");
        assertTrue(HelperCifrado.VerificarClave("Ñandú-2026!", hash));
        assertFalse(HelperCifrado.VerificarClave("Nandu-2026!", hash));
    }

    @Test
    void logOnRechazaUsuarioOContrasenaIncorrectos() {
        assertFalse(admin.LogOn(HelperCifrado.CifrarSHA256("Admin"), HelperCifrado.CifrarSHA256("1234")));
        assertFalse(admin.LogOn(HelperCifrado.CifrarSHA256("admin"), HelperCifrado.CifrarSHA256("12345")));
    }

    @Test
    void bloqueaAlTercerIntentoFallido() {
        ControlIntentos c = new ControlIntentos();
        assertFalse(c.RegistrarFallo());
        assertFalse(c.RegistrarFallo());
        assertEquals(1, c.IntentosRestantes());
        assertTrue(c.RegistrarFallo());
        assertTrue(c.EstaBloqueado());
        c.Reiniciar();
        assertFalse(c.EstaBloqueado());
    }

    @Test
    void bloqueoPersisteAlReabrir(@TempDir Path dir) {
        Path estado = dir.resolve("intentos.properties");
        ControlIntentos primero = new ControlIntentos(estado);
        assertFalse(primero.RegistrarFallo());
        assertFalse(primero.RegistrarFallo());
        assertTrue(primero.RegistrarFallo());

        // Al crear una nueva instancia sobre el mismo archivo el bloqueo continúa activo.
        ControlIntentos reiniciado = new ControlIntentos(estado);
        assertTrue(reiniciado.EstaBloqueado());
        assertTrue(reiniciado.SegundosRestantesBloqueo() > 0);
        reiniciado.Reiniciar();
        assertFalse(new ControlIntentos(estado).EstaBloqueado());
    }
}
