package Helpers;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Utilidades de cifrado y verificación segura de contraseñas.
 * Incluye PBKDF2 con sal para credenciales y SHA-256 (UTF-8) para firmas de integridad (Wompi).
 */
public class HelperCifrado {

    private static final String PREFIJO_PBKDF2 = "pbkdf2$";
    private static final int ITERACIONES = 65_536;
    private static final int LARGO_CLAVE_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String CifrarSHA256(String mensaje) {
        if (mensaje == null) {
            mensaje = "";
        }
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] digest = sha.digest(mensaje.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /** Genera un hash PBKDF2WithHmacSHA256 con sal aleatoria de 16 bytes. */
    public static String HashearClave(String clave) {
        if (clave == null) {
            throw new IllegalArgumentException("La contraseña no puede ser nula");
        }
        byte[] sal = new byte[16];
        RANDOM.nextBytes(sal);
        byte[] hash = pbkdf2(clave.toCharArray(), sal, ITERACIONES);
        return PREFIJO_PBKDF2 + ITERACIONES + "$"
                + Base64.getEncoder().encodeToString(sal) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Verifica una contraseña en texto plano contra un hash PBKDF2 o SHA-256 heredado,
     * utilizando comparación en tiempo constante.
     */
    public static boolean VerificarClave(String clavePlana, String hashGuardado) {
        if (clavePlana == null || hashGuardado == null || hashGuardado.isBlank()) {
            return false;
        }
        if (hashGuardado.startsWith(PREFIJO_PBKDF2)) {
            String[] partes = hashGuardado.split("\\$");
            if (partes.length != 4) {
                return false;
            }
            try {
                int iter = Integer.parseInt(partes[1]);
                byte[] sal = Base64.getDecoder().decode(partes[2]);
                byte[] esperado = Base64.getDecoder().decode(partes[3]);
                byte[] actual = pbkdf2(clavePlana.toCharArray(), sal, iter);
                return MessageDigest.isEqual(esperado, actual);
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        return IgualConstante(CifrarSHA256(clavePlana), hashGuardado.toLowerCase());
    }

    /** Comparación en tiempo constante para evitar ataques de temporización. */
    public static boolean IgualConstante(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8));
    }

    private static byte[] pbkdf2(char[] clave, byte[] sal, int iteraciones) {
        try {
            PBEKeySpec spec = new PBEKeySpec(clave, sal, iteraciones, LARGO_CLAVE_BITS);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("No se pudo derivar la contraseña con PBKDF2", e);
        }
    }
}
