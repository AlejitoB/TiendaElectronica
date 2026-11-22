package Helpers;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
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
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Tema visual único de la tienda (Nordic Bento POS sobre FlatLaf) y utilidades compartidas.
 */
public class HelperUI {

    public static final Color ACENTO = TemaBento.TEAL;
    public static final Color EXITO = TemaBento.EXITO;
    public static final Color ALERTA = TemaBento.TERRACOTA;
    public static final String TITULO_APP = "Tech Shop";

    private static final NumberFormat MONEDA = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"));
    private static boolean listenerInstalado = false;

    static {
        MONEDA.setMaximumFractionDigits(0);
    }

    public static void AplicarTema() {
        TemaBento.ConfigurarFlatLaf();
        if (listenerInstalado) {
            return;
        }
        listenerInstalado = true;
        Toolkit.getDefaultToolkit().addAWTEventListener(e -> {
            if (e.getID() == WindowEvent.WINDOW_OPENED && e.getSource() instanceof JFrame frame) {
                if (frame.getTitle() == null || frame.getTitle().isEmpty()) {
                    frame.setTitle(TITULO_APP + " — Tienda Electrónica");
                }
                frame.setIconImage(ICONO);
                ConfirmarCierre(frame);
            }
        }, AWTEvent.WINDOW_EVENT_MASK);
    }

    /** Icono de la barra de tareas: cuadro verde petróleo con "TS" (Tech Shop). */
    public static final Image ICONO = CrearIcono();

    private static Image CrearIcono() {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(TemaBento.TEAL);
        g.fillRoundRect(0, 0, 64, 64, 18, 18);
        g.setColor(Color.WHITE);
        g.setFont(TemaBento.Fuente(Font.BOLD, 28));
        FontMetrics fm = g.getFontMetrics();
        g.drawString("TS", (64 - fm.stringWidth("TS")) / 2, (64 - fm.getHeight()) / 2 + fm.getAscent());
        g.dispose();
        return img;
    }

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

    public static String Moneda(long valor) {
        return MONEDA.format(valor);
    }

    public static JButton BotonPrimario(String texto) {
        return ComponentesBento.BotonTeal(texto);
    }

    public static JLabel Titulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(TemaBento.Fuente(Font.BOLD, 20));
        l.setForeground(TemaBento.TEXTO);
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
        return l;
    }

    public static JLabel Subtitulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(TemaBento.Fuente(Font.PLAIN, 12));
        l.setForeground(TemaBento.TEXTO_SECUNDARIO);
        return l;
    }

    /** Muestra valores Long de una columna como pesos colombianos, alineados a la derecha. */
    public static void ColumnaMoneda(JTable tabla, int columna) {
        tabla.getColumnModel().getColumn(columna).setCellRenderer(new DefaultTableCellRenderer() {
            {
                setHorizontalAlignment(RIGHT);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 12));
            }

            @Override
            protected void setValue(Object value) {
                setText(value instanceof Number ? Moneda(((Number) value).longValue()) : String.valueOf(value));
                setFont(TemaBento.Fuente(Font.BOLD, 13));
            }
        });
    }

    /** Renderizador tipo píldora centrada para columnas categóricas o de estado. */
    public static void ColumnaPildoraCategoria(JTable tabla, int columna) {
        tabla.getColumnModel().getColumn(columna).setCellRenderer(new DefaultTableCellRenderer() {
            private final ComponentesBento.EtiquetaPildora pildora = new ComponentesBento.EtiquetaPildora(
                    "", TemaBento.SUPERFICIE_SUAVE, TemaBento.TEXTO);
            private final javax.swing.JPanel contenedor = new javax.swing.JPanel(new java.awt.GridBagLayout());

            {
                pildora.setFont(TemaBento.Fuente(Font.BOLD, 11));
                contenedor.add(pildora);
            }

            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                contenedor.setBackground(sel ? t.getSelectionBackground() : t.getBackground());
                pildora.setText(v == null ? "" : v.toString());
                pildora.setColores(sel ? Color.WHITE : TemaBento.SUPERFICIE_SUAVE, TemaBento.TEXTO);
                return contenedor;
            }
        });
    }
}
