package Helpers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import com.formdev.flatlaf.FlatClientProperties;

/**
 * Componentes reutilizables del estilo Nordic Bento POS (tarjetas redondeadas,
 * píldoras, selector segmentado, tarjetas KPI e iconos vectoriales).
 */
public final class ComponentesBento {

    private ComponentesBento() {
    }

    /** Tarjeta blanca redondeada con borde sutil de 1 px. */
    public static class TarjetaBento extends JPanel {

        private Color fondo;
        private Color borde;
        private int radio;

        public TarjetaBento() {
            this(TemaBento.TARJETA, TemaBento.BORDE, TemaBento.RADIO_TARJETA);
        }

        public TarjetaBento(Color fondo, Color borde, int radio) {
            this.fondo = fondo;
            this.borde = borde;
            this.radio = radio;
            setOpaque(false);
        }

        public void setColores(Color fondo, Color borde) {
            this.fondo = fondo;
            this.borde = borde;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            // Sombra sutil inferior para dar profundidad de tarjeta Bento.
            g2.setColor(new Color(0, 0, 0, 12));
            g2.fill(new RoundRectangle2D.Float(1, 2, w - 2f, h - 2f, radio, radio));
            g2.setColor(fondo);
            g2.fill(new RoundRectangle2D.Float(0, 0, w - 1f, h - 2f, radio, radio));
            if (borde != null) {
                g2.setColor(borde);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 2f, h - 3f, radio, radio));
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Botón en forma de píldora con color de fondo, hover y radio completo. */
    public static class BotonPildora extends JButton {

        private Color normal;
        private Color hover;
        private Color borde;
        private int radio;
        private boolean encima;

        public BotonPildora(String texto, Color normal, Color hover, Color textoColor, Color borde, int radio) {
            super(texto);
            this.normal = normal;
            this.hover = hover;
            this.borde = borde;
            this.radio = radio;
            setForeground(textoColor);
            setFont(TemaBento.Fuente(Font.BOLD, 13));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    encima = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    encima = false;
                    repaint();
                }
            });
        }

        public void setEstilo(Color normal, Color hover, Color textoColor, Color borde) {
            this.normal = normal;
            this.hover = hover;
            this.borde = borde;
            setForeground(textoColor);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            int arc = (radio <= 0 || radio >= h) ? h : radio;
            Color bg = !isEnabled() ? TemaBento.CAMPO_FONDO : (encima ? hover : normal);
            g2.setColor(bg);
            g2.fill(new RoundRectangle2D.Float(0, 0, w - 1f, h - 1f, arc, arc));
            if (borde != null) {
                g2.setColor(borde);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 2f, h - 2f, arc, arc));
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static BotonPildora BotonTeal(String texto) {
        return new BotonPildora(texto, TemaBento.TEAL, TemaBento.TEAL_HOVER, Color.WHITE, null, 14);
    }

    public static BotonPildora BotonCarbon(String texto) {
        return new BotonPildora(texto, TemaBento.CARBON, TemaBento.CARBON_HOVER, Color.WHITE, null, 14);
    }

    public static BotonPildora BotonSuave(String texto) {
        return new BotonPildora(texto, TemaBento.SUPERFICIE_SUAVE, TemaBento.CAMPO_FONDO,
                TemaBento.TEXTO, TemaBento.BORDE, 14);
    }

    public static BotonPildora BotonPeligroTexto(String texto) {
        BotonPildora b = new BotonPildora(texto, TemaBento.TARJETA, TemaBento.PELIGRO_SUAVE,
                TemaBento.PELIGRO, null, 14);
        b.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        return b;
    }

    /** Etiqueta redondeada tipo píldora para estados, categorías o alertas. */
    public static class EtiquetaPildora extends JLabel {

        private Color fondo;
        private int radio;

        public EtiquetaPildora(String texto, Color fondo, Color textoColor) {
            this(texto, fondo, textoColor, 999);
        }

        public EtiquetaPildora(String texto, Color fondo, Color textoColor, int radio) {
            super(texto, SwingConstants.CENTER);
            this.fondo = fondo;
            this.radio = radio;
            setForeground(textoColor);
            setFont(TemaBento.Fuente(Font.BOLD, 11));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        }

        public void setColores(Color fondo, Color textoColor) {
            this.fondo = fondo;
            setForeground(textoColor);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int h = getHeight();
            int arc = (radio <= 0 || radio >= h) ? h : radio;
            g2.setColor(fondo);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1f, h - 1f, arc, arc));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Selector segmentado estilo cápsula (ej: Inventario | Clientes | Auditoría). */
    public static class SelectorSegmentado extends JPanel {

        private final Map<String, BotonPildora> botones = new LinkedHashMap<>();
        private String seleccionado;
        private final Color fondoActivo;
        private final Color textoActivo;

        public SelectorSegmentado(String[] opciones, Consumer<String> alCambiar) {
            this(opciones, TemaBento.CARBON, Color.WHITE, alCambiar);
        }

        public SelectorSegmentado(String[] opciones, Color fondoActivo, Color textoActivo, Consumer<String> alCambiar) {
            this.fondoActivo = fondoActivo;
            this.textoActivo = textoActivo;
            setOpaque(false);
            setLayout(new FlowLayout(FlowLayout.CENTER, 4, 4));
            setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
            for (String op : opciones) {
                BotonPildora b = new BotonPildora(op, new Color(0, 0, 0, 0), new Color(0, 0, 0, 14),
                        TemaBento.TEXTO_SECUNDARIO, null, 999);
                b.setFont(TemaBento.Fuente(Font.BOLD, 12));
                b.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
                b.addActionListener(e -> {
                    Seleccionar(op);
                    if (alCambiar != null) {
                        alCambiar.accept(op);
                    }
                });
                botones.put(op, b);
                add(b);
            }
            if (opciones.length > 0) {
                Seleccionar(opciones[0]);
            }
        }

        public void Seleccionar(String opcion) {
            this.seleccionado = opcion;
            botones.forEach((k, b) -> {
                boolean activo = k.equals(opcion);
                b.setEstilo(activo ? fondoActivo : new Color(0, 0, 0, 0),
                        activo ? fondoActivo : new Color(0, 0, 0, 14),
                        activo ? textoActivo : TemaBento.TEXTO_SECUNDARIO, null);
            });
        }

        public String Seleccionado() {
            return seleccionado;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(TemaBento.SUPERFICIE_SUAVE);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1f, getHeight() - 1f, getHeight(), getHeight()));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Tarjeta métrica superior para el panel de administración. */
    public static TarjetaBento TarjetaKPI(String etiqueta, JComponent contenidoPrincipal) {
        TarjetaBento card = new TarjetaBento();
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createEmptyBorder(14, 18, 16, 18));
        JLabel lbl = new JLabel(etiqueta, SwingConstants.CENTER);
        lbl.setFont(TemaBento.Fuente(Font.PLAIN, 12));
        lbl.setForeground(TemaBento.TEXTO_SECUNDARIO);
        card.add(lbl, BorderLayout.NORTH);
        JPanel centro = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centro.setOpaque(false);
        centro.add(contenidoPrincipal);
        card.add(centro, BorderLayout.CENTER);
        return card;
    }

    /** Icono vectorial minimalista de cubo 3D para la marca Tech Shop. */
    public static Icon IconoMarca(int size, Color fondo, Color trazo) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.translate(x, y);
                g2.setColor(fondo);
                g2.fill(new RoundRectangle2D.Float(0, 0, size, size, size * 0.28f, size * 0.28f));

                // Cubo geométrico simple dentro del recuadro.
                g2.setColor(trazo);
                int cx = size / 2;
                int cy = size / 2;
                int r = Math.max(5, size / 4);
                int[] hx = {cx, cx + r, cx + r, cx, cx - r, cx - r};
                int[] hy = {cy - r, cy - r / 2, cy + r / 2, cy + r, cy + r / 2, cy - r / 2};
                g2.drawPolygon(hx, hy, 6);
                g2.drawLine(cx, cy, cx, cy + r);
                g2.drawLine(cx, cy, cx + r, cy - r / 2);
                g2.drawLine(cx, cy, cx - r, cy - r / 2);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    /** Icono vectorial de marca de verificación (evita glifos faltantes en fuentes Linux). */
    public static Icon IconoCheck(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new java.awt.BasicStroke(2f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                g2.drawLine(x + 2, y + size / 2, x + size / 2 - 1, y + size - 3);
                g2.drawLine(x + size / 2 - 1, y + size - 3, x + size - 2, y + 3);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    /** Icono vectorial de flecha hacia la derecha para botones CTA. */
    public static Icon IconoFlechaDerecha(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new java.awt.BasicStroke(1.8f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                int midY = y + size / 2;
                g2.drawLine(x + 2, midY, x + size - 3, midY);
                g2.drawLine(x + size - 7, midY - 4, x + size - 3, midY);
                g2.drawLine(x + size - 7, midY + 4, x + size - 3, midY);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    /** Icono circular pequeño de color (ej. indicador de estado de BD). */
    public static Icon PuntoEstado(Color color, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(x, y, size, size);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    /**
     * Carga una imagen de la carpeta Images de forma multiplataforma y la escala;
     * si no existe, devuelve un avatar circular/cuadrado con las iniciales.
     */
    public static ImageIcon CargarFotoCliente(String nombreImagen, String iniciales, int ancho, int alto) {
        if (nombreImagen != null && !nombreImagen.isBlank()) {
            String limpio = nombreImagen.trim();
            Path base = Path.of("Images");
            Path[] candidatos = {
                base.resolve(limpio),
                base.resolve(limpio + ".jpg"),
                base.resolve(limpio + ".png"),
                base.resolve(limpio + ".jpeg")
            };
            for (Path p : candidatos) {
                if (Files.isRegularFile(p)) {
                    try {
                        Image img = ImageIO.read(p.toFile());
                        if (img != null) {
                            return new ImageIcon(img.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH));
                        }
                    } catch (Exception ignored) {
                        // Continúa al avatar por defecto.
                    }
                }
            }
        }
        return AvatarIniciales(iniciales, ancho, alto);
    }

    private static ImageIcon AvatarIniciales(String iniciales, int ancho, int alto) {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(
                ancho, alto, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(TemaBento.TEAL_SUAVE);
        g2.fill(new RoundRectangle2D.Float(0, 0, ancho, alto, 20, 20));
        g2.setColor(TemaBento.TEAL);
        String txt = (iniciales == null || iniciales.isBlank()) ? "TS" : iniciales.toUpperCase();
        g2.setFont(TemaBento.Fuente(Font.BOLD, Math.max(16, ancho / 3)));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(txt, (ancho - fm.stringWidth(txt)) / 2, (alto - fm.getHeight()) / 2 + fm.getAscent());
        g2.dispose();
        return new ImageIcon(img);
    }

    /** Envuelve un componente con padding interno para usar dentro de contenedores transparentes. */
    public static JPanel Envolver(Component c, int top, int left, int bottom, int right) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(top, left, bottom, right));
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    /** Fuerza un ancho preferido manteniendo la altura natural del componente. */
    public static void FijarAncho(JComponent c, int ancho) {
        Dimension d = c.getPreferredSize();
        c.setPreferredSize(new Dimension(ancho, d.height));
    }

    /** Crea un campo de texto con placeholder, botón limpiar y callback al escribir. */
    public static JTextField CampoBusqueda(String placeholder, int ancho, int alto, Runnable alCambiar) {
        JTextField campo = new JTextField();
        campo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        campo.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        campo.setFont(TemaBento.Fuente(Font.PLAIN, 13));
        campo.setPreferredSize(new Dimension(ancho, alto));
        if (alCambiar != null) {
            campo.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { alCambiar.run(); }
                public void removeUpdate(DocumentEvent e) { alCambiar.run(); }
                public void changedUpdate(DocumentEvent e) { alCambiar.run(); }
            });
        }
        return campo;
    }
}
