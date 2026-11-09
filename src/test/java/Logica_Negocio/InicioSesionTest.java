package Logica_Negocio;

import static org.junit.jupiter.api.Assertions.*;

import Helpers.HelperCifrado;
import org.junit.jupiter.api.Test;

class InicioSesionTest {

    private final Usuario admin = new Administrador("Admin", HelperCifrado.CifrarSHA256("12345"));

    @Test
    void logOnConCredencialesCorrectas() {
        assertTrue(admin.LogOn(HelperCifrado.CifrarSHA256("Admin"), HelperCifrado.CifrarSHA256("12345")));
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
}
