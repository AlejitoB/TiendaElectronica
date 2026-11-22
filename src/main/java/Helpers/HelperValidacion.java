package Helpers;

import java.util.Set;

/**
 * Validaciones de formularios y entidades del dominio.
 */
public class HelperValidacion {

    private static final Set<Character> ESPECIALES = Set.of(
            '@', '~', '/', ';', ':', '"', '!', 'º', '¬', '|', '?', '¿', '&', '%',
            '$', '_', '*', '<', '>', '€', 'Ç', '+', '(', ')', '»', '¥', '«', '¶',
            '[', ']', '{', '}', '÷', '=', ',');

    public static int RetornarValor(String texto) {
        if (texto == null) {
            return 0;
        }
        int numeros = 0;
        for (int j = 0; j < texto.length(); j++) {
            if (Character.isDigit(texto.charAt(j))) {
                numeros++;
            }
        }
        return numeros;
    }

    public static int RetornarCEV2(String texto) {
        if (texto == null) {
            return 0;
        }
        int ce = 0;
        for (int j = 0; j < texto.length(); j++) {
            char ch = texto.charAt(j);
            if (!Character.isLetter(ch) && ESPECIALES.contains(ch)) {
                ce++;
            }
        }
        return ce;
    }

    public static int ValidarVacio(String cadena) {
        return (cadena == null || cadena.isBlank()) ? 1 : 0;
    }

    public static int RetornarLetra(String texto) {
        if (texto == null) {
            return 0;
        }
        int noDigitos = 0;
        for (int j = 0; j < texto.length(); j++) {
            if (!Character.isDigit(texto.charAt(j))) {
                noDigitos++;
            }
        }
        return noDigitos;
    }

    public static int ValidarCantidadRango(int cantidad) {
        return (cantidad > 0 && cantidad < 1000) ? 1 : 0;
    }

    public static int RetornarCEDireccionV2(String texto) {
        if (texto == null) {
            return 0;
        }
        int ce = 0;
        for (int j = 0; j < texto.length(); j++) {
            if (texto.charAt(j) == '$') {
                ce++;
            }
        }
        return ce;
    }

    public static int RetornarCEV2Contraseña(String contraseña) {
        // Las contraseñas seguras pueden incluir símbolos como ., - o /; solo se rechazan espacios en blanco vacíos.
        return 0;
    }

    public static int ValidarTodo(String cadena) {
        return ValidarVacio(cadena) + RetornarValor(cadena) + RetornarCEV2(cadena);
    }

    /** Verifica que la cadena contenga solo dígitos numéricos. */
    public static int ValidarSoloDigitos(String cadena) {
        return ValidarVacio(cadena) + RetornarLetra(cadena) + RetornarCEV2(cadena);
    }

    /** @deprecated Usar {@link #ValidarSoloDigitos(String)} para mayor claridad. */
    @Deprecated
    public static int ValidarTodoLetra(String cadena) {
        return ValidarSoloDigitos(cadena);
    }

    public static int ValidarTodoDireccion(String cadena) {
        return ValidarVacio(cadena) + RetornarCEDireccionV2(cadena);
    }

    public static int ValidarTodoSerial(String cadena) {
        return ValidarVacio(cadena) + RetornarCEV2(cadena);
    }

    public static int ValidarTodoContraseña(String cadena) {
        return ValidarVacio(cadena) + RetornarCEV2Contraseña(cadena);
    }

    /**
     * Valida un producto del inventario. Devuelve null si es válido o un mensaje
     * que nombra el campo y el tipo de error.
     */
    public static String ValidarProducto(Logica_Negocio.Producto p) {
        if (p == null) {
            return "Datos de producto requeridos";
        }
        if (p.getSerial() == null || !p.getSerial().matches("[A-Za-z0-9-]{3,20}")) {
            return "Campo serial: use de 3 a 20 letras, números o guiones";
        }
        if (p.getNombre() == null || p.getNombre().isBlank() || p.getNombre().length() > 120) {
            return "Campo nombre: es obligatorio (máximo 120 caracteres)";
        }
        if (p.getMarca() == null || p.getMarca().isBlank() || RetornarCEV2(p.getMarca()) > 0) {
            return "Campo marca: es obligatorio y no admite caracteres especiales";
        }
        if (p.getCategoria() == null || p.getCategoria().isBlank()) {
            return "Campo categoría: es obligatorio";
        }
        if (p.getPrecio() <= 0) {
            return "Campo precio: debe ser un número mayor que cero";
        }
        if (p.getStock() < 0 || p.getStock() > 99999) {
            return "Campo stock: debe estar entre 0 y 99999";
        }
        return null;
    }

    /**
     * Valida los datos de un cliente. Devuelve null si es válido o un mensaje
     * que nombra el campo y el tipo de error.
     */
    public static String ValidarPersona(Logica_Negocio.Persona p) {
        if (p == null) {
            return "Datos de cliente requeridos";
        }
        if (ValidarSoloDigitos(p.getUid().trim()) > 0) {
            return "Campo UID: es obligatorio y debe contener solo números";
        }
        if (ValidarTodo(p.getNombre().trim()) > 0) {
            return "Campo nombre: es obligatorio y solo admite letras";
        }
        if (ValidarTodo(p.getApellido().trim()) > 0) {
            return "Campo apellido: es obligatorio y solo admite letras";
        }
        if (ValidarSoloDigitos(p.getCedula().trim()) > 0) {
            return "Campo cédula: es obligatorio y debe contener solo números";
        }
        if (ValidarTodoDireccion(p.getDireccion().trim()) > 0) {
            return "Campo dirección: es obligatorio y no admite el símbolo $";
        }
        if (!p.getCorreo().isBlank() && !p.getCorreo().trim().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            return "Campo correo: escriba un correo electrónico válido";
        }
        if (!p.getTelefono().isBlank() && !p.getTelefono().trim().matches("[0-9+()\\- ]{7,20}")) {
            return "Campo teléfono: use solo dígitos (7 a 20 caracteres)";
        }
        if (ValidarTodoSerial(p.getNom_img().trim()) > 0) {
            return "Campo imagen: es obligatorio y no admite caracteres especiales";
        }
        return null;
    }
}
