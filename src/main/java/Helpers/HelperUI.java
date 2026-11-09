package Helpers;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
    public static final String TITULO_APP = "Tech Shop";

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
                String titulo = TITULOS.get(frame.getClass().getSimpleName());
                if (titulo != null) {
                    frame.setTitle(TITULO_APP + " - " + titulo);
                } else if (frame.getTitle() == null || frame.getTitle().isEmpty()) {
                    frame.setTitle(TITULO_APP);
                }
                frame.setIconImage(ICONO);
                Unificar(frame.getContentPane());
                frame.setLocationRelativeTo(null);
                ConfirmarCierre(frame);
            }
        }, AWTEvent.WINDOW_EVENT_MASK);
    }

    // Titulos de las pantallas de NetBeans: varios estaban vacios o copiados de otra pantalla.
    private static final Map<String, String> TITULOS = Map.ofEntries(
            Map.entry("Portada", "Bienvenido"),
            Map.entry("EscogerBD", "Elegir base de datos"),
            Map.entry("InicioSesionAdministrador", "Iniciar sesión"),
            Map.entry("MenuAdministrador", "Menú administrador"),
            Map.entry("MenuCliente", "Menú cliente"),
            Map.entry("MenuLocal", "Menú local"),
            Map.entry("RegistrarPersonaAdministrador", "Registrar persona"),
            Map.entry("VisualizarPersonasAdministrador", "Ver personas"),
            Map.entry("BuscarPersonaAdministrador", "Buscar persona"),
            Map.entry("EliminarPersonaAdministrador", "Eliminar persona"),
            Map.entry("BuscarPersonaCliente", "Buscar persona"),
            Map.entry("RegistarBDLocal", "Registrar persona (local)"),
            Map.entry("MostrarPersonaLocal", "Ver personas (local)"),
            Map.entry("BuscarPersonaLocal", "Buscar persona (local)"),
            Map.entry("EditarPersona", "Editar persona (local)"),
            Map.entry("EliminarPersona", "Eliminar persona (local)"));

    /** Icono de la barra de tareas: cuadro azul redondeado con "TS" (Tech Shop). */
    private static final Image ICONO = CrearIcono();

    private static Image CrearIcono() {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(ACENTO);
        g.fillRoundRect(0, 0, 64, 64, 18, 18);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Segoe UI", Font.BOLD, 30));
        FontMetrics fm = g.getFontMetrics();
        g.drawString("TS", (64 - fm.stringWidth("TS")) / 2, (64 - fm.getHeight()) / 2 + fm.getAscent());
        g.dispose();
        return img;
    }

    /** La X ya no cierra la aplicacion de golpe: pregunta antes, para no perder lo que se estaba haciendo. */
    private static void ConfirmarCierre(JFrame frame) {
        if (frame.getDefaultCloseOperation() != JFrame.EXIT_ON_CLOSE) {
            return;
        }
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (JOptionPane.showConfirmDialog(frame, "¿Desea salir de " + TITULO_APP + "?", "Salir",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
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
