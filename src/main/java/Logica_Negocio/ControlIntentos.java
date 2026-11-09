package Logica_Negocio;

/**
 * Cuenta los intentos fallidos de inicio de sesion y bloquea al tercero (criterio de seguridad).
 */
public class ControlIntentos {

    public static final int MAX_INTENTOS = 3;
    private int fallidos = 0;

    /** Registra un fallo y devuelve true si con este la cuenta queda bloqueada. */
    public boolean RegistrarFallo() {
        fallidos++;
        return EstaBloqueado();
    }

    public boolean EstaBloqueado() {
        return fallidos >= MAX_INTENTOS;
    }

    public int IntentosRestantes() {
        return Math.max(0, MAX_INTENTOS - fallidos);
    }

    public void Reiniciar() {
        fallidos = 0;
    }
}
