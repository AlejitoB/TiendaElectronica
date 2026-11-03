package Helpers;

import java.time.YearMonth;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Reglas de la pasarela de pagos. Las tarjetas de prueba son las mismas del Sandbox de Wompi,
 * asi funcionan igual con Wompi configurado o con la simulacion local.
 */
public class HelperPago {

    public static final String TARJETA_APROBADA = "4242 4242 4242 4242";
    public static final String TARJETA_RECHAZADA = "4111 1111 1111 1111";

    public static String SoloDigitos(String numero) {
        return numero == null ? "" : numero.replaceAll("[\\s-]", "");
    }

    public static String Franquicia(String numero) {
        String n = SoloDigitos(numero);
        if (n.startsWith("4")) {
            return "VISA";
        }
        if (n.matches("^(5[1-5]|2[2-7]).*")) {
            return "Mastercard";
        }
        if (n.matches("^3[47].*")) {
            return "American Express";
        }
        return "";
    }

    /** Algoritmo de Luhn: detecta errores de digitacion en el numero de tarjeta. */
    public static boolean Luhn(String digitos) {
        int suma = 0;
        boolean doble = false;
        for (int i = digitos.length() - 1; i >= 0; i--) {
            int d = digitos.charAt(i) - '0';
            if (doble) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            suma += d;
            doble = !doble;
        }
        return suma % 10 == 0;
    }

    /**
     * Valida los datos de la tarjeta. Devuelve null si son validos o un mensaje
     * que nombra el campo y el tipo de error.
     */
    public static String Validar(String titular, String correo, String numero, String vencimiento, String cvv, YearMonth hoy) {
        if (titular == null || !titular.trim().matches("[\\p{L} ]{3,60}")) {
            return "Campo titular: escriba el nombre como aparece en la tarjeta (solo letras)";
        }
        if (correo == null || !correo.trim().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            return "Campo correo: escriba un correo valido para enviar el comprobante";
        }
        String n = SoloDigitos(numero);
        if (!n.matches("\\d{13,19}") || !Luhn(n)) {
            return "Campo numero de tarjeta: el numero no es valido";
        }
        if (Franquicia(n).isEmpty()) {
            return "Campo numero de tarjeta: solo se aceptan VISA, Mastercard o American Express";
        }
        if (vencimiento == null || !vencimiento.trim().matches("(0[1-9]|1[0-2])/\\d{2}")) {
            return "Campo vencimiento: use el formato MM/AA";
        }
        String[] mmaa = vencimiento.trim().split("/");
        YearMonth vence = YearMonth.of(2000 + Integer.parseInt(mmaa[1]), Integer.parseInt(mmaa[0]));
        if (vence.isBefore(hoy)) {
            return "Campo vencimiento: la tarjeta esta vencida";
        }
        int largoCvv = Franquicia(n).equals("American Express") ? 4 : 3;
        if (cvv == null || !cvv.matches("\\d{" + largoCvv + "}")) {
            return "Campo CVV: debe tener " + largoCvv + " digitos";
        }
        return null;
    }

    /** Ej: "VISA **** 1111". */
    public static String Enmascarar(String numero) {
        String n = SoloDigitos(numero);
        return Franquicia(n) + " **** " + n.substring(n.length() - 4);
    }

    /**
     * Simula la respuesta del banco cuando Wompi no esta configurado.
     * Devuelve el codigo de autorizacion o null si es rechazada.
     */
    public static String Autorizar(String numero) {
        if (SoloDigitos(numero).equals(SoloDigitos(TARJETA_RECHAZADA))) {
            return null;
        }
        return String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
    }
}
