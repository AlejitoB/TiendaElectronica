package Helpers;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.plaf.UIResource;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Tema visual unico de la tienda (FlatLaf) y utilidades de presentacion compartidas.
 */
public class HelperUI {

    public static final Color ACENTO = new Color(0x2563EB);
    public static final Color EXITO = new Color(0x16A34A);
    public static final Color ALERTA = new Color(0xDC2626);
    public static final String TITULO_APP = "Tienda Electrónica";

    private static final NumberFormat MONEDA = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"));

    static {
        MONEDA.setMaximumFractionDigits(0);
    }

    public static void AplicarTema() {
        FlatLaf.setGlobalExtraDefaults(Map.of("@accentColor", "#2563EB"));
        FlatLightLaf.setup();
        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("Button.arc", 10);
        UIManager.put("Component.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("Table.rowHeight", 28);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("ScrollBar.thumbArc", 999);

        // Las pantallas hechas en NetBeans traen colores y fuentes fijos; se unifican al abrir cada ventana.
        Toolkit.getDefaultToolkit().addAWTEventListener(e -> {
            if (e.getID() == WindowEvent.WINDOW_OPENED && e.getSource() instanceof JFrame) {
                JFrame frame = (JFrame) e.getSource();
                if (frame.getTitle() == null || frame.getTitle().isEmpty()) {
                    frame.setTitle(TITULO_APP);
                }
                Unificar(frame.getContentPane());
                frame.setLocationRelativeTo(null);
            }
        }, AWTEvent.WINDOW_EVENT_MASK);
    }

    private static void Unificar(Container cont) {
        for (Component c : cont.getComponents()) {
            if (c instanceof JButton && !(c.getBackground() instanceof UIResource)) {
                c.setBackground(ACENTO);
                c.setForeground(Color.WHITE);
                ((JButton) c).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            Font f = c.getFont();
            // "Roboto Black" no viene instalada en Windows/Linux y Java la reemplaza por una fuente generica.
            if (f != null && (f.getName().startsWith("Roboto") || f.getName().equals("Segoe UI Black"))) {
                c.setFont(new Font("Segoe UI", Font.BOLD, f.getSize()));
            }
            if (c instanceof Container) {
                Unificar((Container) c);
            }
        }
    }

    public static String Moneda(long valor) {
        return MONEDA.format(valor);
    }

    public static JButton BotonPrimario(String texto) {
        JButton b = new JButton(texto);
        b.setBackground(ACENTO);
        b.setForeground(Color.WHITE);
        b.setFont(b.getFont().deriveFont(Font.BOLD));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JLabel Titulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Segoe UI", Font.BOLD, 22));
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        return l;
    }

    public static JLabel Subtitulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setForeground(UIManager.getColor("Label.disabledForeground"));
        return l;
    }

    /** Muestra valores Long de una columna como pesos colombianos, alineados a la derecha. */
    public static void ColumnaMoneda(JTable tabla, int columna) {
        tabla.getColumnModel().getColumn(columna).setCellRenderer(new DefaultTableCellRenderer() {
            {
                setHorizontalAlignment(RIGHT);
            }

            @Override
            protected void setValue(Object value) {
                setText(value instanceof Number ? Moneda(((Number) value).longValue()) : String.valueOf(value));
            }
        });
    }
}
