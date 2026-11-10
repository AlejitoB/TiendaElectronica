package Helpers;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Lee la configuracion externa (credenciales y conexiones) para no dejarla en el codigo.
 * Orden: propiedad -Dtienda.clave (la usan las pruebas) > variable de entorno (TIENDA_MYSQL_URL...)
 * > config.properties > config.example.properties.
 */
public class HelperConfig {

    private static Properties props;

    public static String Obtener(String clave) {
        String prop = System.getProperty("tienda." + clave);
        if (prop != null) {
            return prop;
        }
        String env = System.getenv("TIENDA_" + clave.toUpperCase().replace('.', '_'));
        if (env != null) {
            return env;
        }
        return Cargar().getProperty(clave, "");
    }

    private static synchronized Properties Cargar() {
        if (props == null) {
            props = new Properties();
            for (String archivo : new String[]{"config.example.properties", "config.properties"}) {
                try (InputStream in = new FileInputStream(archivo)) {
                    props.load(in);
                } catch (IOException e) {
                    System.out.println("No se encontro " + archivo);
                }
            }
        }
        return props;
    }
}
