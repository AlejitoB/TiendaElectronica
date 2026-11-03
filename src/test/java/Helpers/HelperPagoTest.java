package Helpers;

import static org.junit.jupiter.api.Assertions.*;

import java.time.YearMonth;
import org.junit.jupiter.api.Test;

class HelperPagoTest {

    private static final YearMonth HOY = YearMonth.of(2026, 11);

    @Test
    void luhnAceptaTarjetasDePruebaYRechazaErroresDeDigitacion() {
        assertTrue(HelperPago.Luhn("4111111111111111"));
        assertTrue(HelperPago.Luhn("4000000000000002"));
        assertFalse(HelperPago.Luhn("4111111111111112"));
    }

    @Test
    void datosValidosPasan() {
        assertNull(HelperPago.Validar("Andres Garces", "4111 1111 1111 1111", "12/28", "123", HOY));
        assertNull(HelperPago.Validar("Ana", "3782 822463 10005", "11/26", "1234", HOY)); // Amex: CVV de 4
    }

    @Test
    void cadaErrorNombraSuCampo() {
        assertTrue(HelperPago.Validar("A1", "4111111111111111", "12/28", "123", HOY).contains("titular"));
        assertTrue(HelperPago.Validar("Ana", "4111111111111112", "12/28", "123", HOY).contains("numero"));
        assertTrue(HelperPago.Validar("Ana", "4111111111111111", "13/28", "123", HOY).contains("vencimiento"));
        assertTrue(HelperPago.Validar("Ana", "4111111111111111", "10/26", "123", HOY).contains("vencida"));
        assertTrue(HelperPago.Validar("Ana", "4111111111111111", "12/28", "12", HOY).contains("CVV"));
        assertTrue(HelperPago.Validar("Ana", "378282246310005", "12/28", "123", HOY).contains("CVV"));
    }

    @Test
    void franquiciaYEnmascarado() {
        assertEquals("VISA", HelperPago.Franquicia("4111"));
        assertEquals("Mastercard", HelperPago.Franquicia("5555 5555 5555 4444"));
        assertEquals("VISA **** 1111", HelperPago.Enmascarar("4111 1111 1111 1111"));
    }

    @Test
    void laTarjetaTerminadaEn0002SimulaRechazo() {
        assertNull(HelperPago.Autorizar(HelperPago.TARJETA_RECHAZADA));
        assertTrue(HelperPago.Autorizar(HelperPago.TARJETA_APROBADA).matches("\\d{6}"));
    }
}
