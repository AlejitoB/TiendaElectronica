package Helpers;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.UIManager;

/**
 * Tokens visuales del sistema de diseño Nordic Bento POS y configuración de FlatLaf.
 */
public final class TemaBento {

    private TemaBento() {
    }

    // Superficies
    public static final Color FONDO_PIEDRA = new Color(0xF5F4F0);
    public static final Color TARJETA = Color.WHITE;
    public static final Color SUPERFICIE_SUAVE = new Color(0xEFECE6);
    public static final Color CAMPO_FONDO = new Color(0xEAE8E2);
    public static final Color BORDE = new Color(0xE2DFD7);
    public static final Color BORDE_INPUT = new Color(0xD6D3CB);

    // Acentos y estados
    public static final Color TEAL = new Color(0x0F766E);
    public static final Color TEAL_HOVER = new Color(0x0D645D);
    public static final Color TEAL_SUAVE = new Color(0xE6F4F1);
    public static final Color CARBON = new Color(0x18181B);
    public static final Color CARBON_HOVER = new Color(0x27272A);

    public static final Color EXITO = new Color(0x15803D);
    public static final Color EXITO_SUAVE = new Color(0xDCFCE7);
    public static final Color TERRACOTA = new Color(0xEA580C);
    public static final Color TERRACOTA_SUAVE = new Color(0xFFEDD5);
    public static final Color PELIGRO = new Color(0xDC2626);
    public static final Color PELIGRO_SUAVE = new Color(0xFEE2E2);
    public static final Color NUBE_AZUL = new Color(0x1D4ED8);
    public static final Color NUBE_SUAVE = new Color(0xDBEAFE);

    // Texto
    public static final Color TEXTO = new Color(0x18181B);
    public static final Color TEXTO_SECUNDARIO = new Color(0x6B7280);
    public static final Color TEXTO_SUAVE = new Color(0x9CA3AF);

    // Radios y espaciado
    public static final int RADIO_TARJETA = 20;
    public static final int RADIO_CONTROL = 12;

    private static final String FAMILIA = ResolverFamiliaFuente();

    private static String ResolverFamiliaFuente() {
        Set<String> disponibles = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String candidata : new String[]{"Inter", "Segoe UI", "Ubuntu", "Noto Sans", "Liberation Sans"}) {
            if (disponibles.contains(candidata)) {
                return candidata;
            }
        }
        return Font.SANS_SERIF;
    }

    public static Font Fuente(int estilo, int tamano) {
        return new Font(FAMILIA, estilo, tamano);
    }

    public static void ConfigurarFlatLaf() {
        FlatLaf.setGlobalExtraDefaults(Map.of(
                "@accentColor", "#0F766E",
                "@background", "#F5F4F0"
        ));
        FlatLightLaf.setup();
        UIManager.put("defaultFont", Fuente(Font.PLAIN, 13));
        UIManager.put("Panel.background", FONDO_PIEDRA);
        UIManager.put("Button.arc", 14);
        UIManager.put("Component.arc", 14);
        UIManager.put("TextComponent.arc", 14);
        UIManager.put("TextField.background", CAMPO_FONDO);
        UIManager.put("PasswordField.background", CAMPO_FONDO);
        UIManager.put("ComboBox.background", CAMPO_FONDO);
        UIManager.put("Spinner.background", CAMPO_FONDO);
        UIManager.put("Component.borderColor", BORDE_INPUT);
        UIManager.put("Component.focusedBorderColor", TEAL);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Table.rowHeight", 38);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.gridColor", new Color(0xEFECE6));
        UIManager.put("Table.selectionBackground", TEAL_SUAVE);
        UIManager.put("Table.selectionForeground", TEXTO);
        UIManager.put("TableHeader.background", TARJETA);
        UIManager.put("TableHeader.font", Fuente(Font.BOLD, 12));
        UIManager.put("TableHeader.separatorColor", BORDE);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.width", 8);
    }
}
