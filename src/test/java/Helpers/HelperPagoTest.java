package Helpers;

import static org.junit.jupiter.api.Assertions.*;

import Logica_Conexion.PasarelaWompi;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;

class HelperPagoTest {

    private static final YearMonth HOY = YearMonth.of(2026, 11);
    private static final String CORREO = "ana@correo.co";

    @Test
    void luhnAceptaTarjetasDePruebaYRechazaErroresDeDigitacion() {
        assertTrue(HelperPago.Luhn("4242424242424242"));
        assertTrue(HelperPago.Luhn("4111111111111111"));
        assertFalse(HelperPago.Luhn("4111111111111112"));
    }

    @Test
    void datosValidosPasan() {
        assertNull(HelperPago.Validar("Andres Garces", CORREO, "4242 4242 4242 4242", "12/28", "123", HOY));
        assertNull(HelperPago.Validar("Ana", CORREO, "3782 822463 10005", "11/26", "1234", HOY)); // Amex: CVV de 4
    }

    @Test
    void cadaErrorNombraSuCampo() {
        assertTrue(HelperPago.Validar("A1", CORREO, "4242424242424242", "12/28", "123", HOY).contains("titular"));
        assertTrue(HelperPago.Validar("Ana", "ana@", "4242424242424242", "12/28", "123", HOY).contains("correo"));
        assertTrue(HelperPago.Validar("Ana", CORREO, "4242424242424241", "12/28", "123", HOY).contains("numero"));
        assertTrue(HelperPago.Validar("Ana", CORREO, "4242424242424242", "13/28", "123", HOY).contains("vencimiento"));
        assertTrue(HelperPago.Validar("Ana", CORREO, "4242424242424242", "10/26", "123", HOY).contains("vencida"));
        assertTrue(HelperPago.Validar("Ana", CORREO, "4242424242424242", "12/28", "12", HOY).contains("CVV"));
        assertTrue(HelperPago.Validar("Ana", CORREO, "378282246310005", "12/28", "123", HOY).contains("CVV"));
    }

    @Test
    void franquiciaYEnmascarado() {
        assertEquals("VISA", HelperPago.Franquicia("4242"));
        assertEquals("Mastercard", HelperPago.Franquicia("5555 5555 5555 4444"));
        assertEquals("VISA **** 4242", HelperPago.Enmascarar("4242 4242 4242 4242"));
    }

    @Test
    void lasTarjetasDePruebaSonLasDelSandboxDeWompi() {
        assertNull(HelperPago.Autorizar(HelperPago.TARJETA_RECHAZADA));
        assertTrue(HelperPago.Autorizar(HelperPago.TARJETA_APROBADA).matches("\\d{6}"));
    }

    @Test
    void firmaDeIntegridadIgualAlEjemploDeLaDocumentacionDeWompi() {
        assertEquals("37c8407747e595535433ef8f6a811d853cd943046624a0ec04662b17bbf33bf5",
                PasarelaWompi.Firma("sk8-438k4-xmxm392-sn2m", 2490000, "COP", "prod_integrity_Z5mMke9x0k8gpErbDqwrJXMqsI6SFli6"));
    }
}
