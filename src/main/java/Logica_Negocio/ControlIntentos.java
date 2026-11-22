package Logica_Negocio;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Cuenta los intentos fallidos de inicio de sesión y bloquea al tercero durante 60 segundos.
 * Soporta persistencia en disco para que reiniciar la aplicación no evada el bloqueo.
 */
public class ControlIntentos {

    public static final int MAX_INTENTOS = 3;
    public static final long DURACION_BLOQUEO_MS = 60_000L;

    private final Path archivoEstado;
    private int fallidos = 0;
    private long bloqueadoHastaMs = 0L;

    /** Instancia en memoria (ideal para pruebas unitarias). */
    public ControlIntentos() {
        this(null);
    }

    /** Instancia con respaldo persistente en el archivo indicado. */
    public ControlIntentos(Path archivoEstado) {
        this.archivoEstado = archivoEstado;
        cargar();
    }

    public static ControlIntentos Persistente() {
        Path ruta = Path.of(System.getProperty("user.home"), ".local", "share", "techshop", "intentos.properties");
        return new ControlIntentos(ruta);
    }

    /** Registra un fallo y devuelve true si con este la cuenta queda bloqueada. */
    public synchronized boolean RegistrarFallo() {
        limpiarSiExpiro();
        fallidos++;
        if (fallidos >= MAX_INTENTOS) {
            bloqueadoHastaMs = System.currentTimeMillis() + DURACION_BLOQUEO_MS;
        }
        guardar();
        return EstaBloqueado();
    }

    public synchronized boolean EstaBloqueado() {
        limpiarSiExpiro();
        return fallidos >= MAX_INTENTOS;
    }

    public synchronized int IntentosRestantes() {
        limpiarSiExpiro();
        return Math.max(0, MAX_INTENTOS - fallidos);
    }

    public synchronized long SegundosRestantesBloqueo() {
        limpiarSiExpiro();
        if (bloqueadoHastaMs <= 0) {
            return 0;
        }
        long restanteMs = bloqueadoHastaMs - System.currentTimeMillis();
        return restanteMs <= 0 ? 0 : (restanteMs + 999) / 1000;
    }

    public synchronized void Reiniciar() {
        fallidos = 0;
        bloqueadoHastaMs = 0L;
        guardar();
    }

    private void limpiarSiExpiro() {
        if (bloqueadoHastaMs > 0 && System.currentTimeMillis() >= bloqueadoHastaMs) {
            fallidos = 0;
            bloqueadoHastaMs = 0L;
            guardar();
        }
    }

    private void cargar() {
        if (archivoEstado == null || !Files.isRegularFile(archivoEstado)) {
            return;
        }
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(archivoEstado)) {
            props.load(in);
            fallidos = Integer.parseInt(props.getProperty("fallidos", "0"));
            bloqueadoHastaMs = Long.parseLong(props.getProperty("bloqueadoHastaMs", "0"));
            limpiarSiExpiro();
        } catch (Exception ignored) {
            fallidos = 0;
            bloqueadoHastaMs = 0L;
        }
    }

    private void guardar() {
        if (archivoEstado == null) {
            return;
        }
        try {
            Path padre = archivoEstado.getParent();
            if (padre != null) {
                Files.createDirectories(padre);
            }
            Properties props = new Properties();
            props.setProperty("fallidos", String.valueOf(fallidos));
            props.setProperty("bloqueadoHastaMs", String.valueOf(bloqueadoHastaMs));
            try (OutputStream out = Files.newOutputStream(archivoEstado)) {
                props.store(out, "Estado de bloqueo de acceso");
            }
        } catch (Exception ignored) {
            // Si no se puede escribir en disco, continúa en memoria sin tumbar el login.
        }
    }
}
