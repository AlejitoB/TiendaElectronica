package GUI_Local;

import Helpers.ComponentesBento;
import Helpers.ComponentesBento.BotonPildora;
import Helpers.ComponentesBento.EtiquetaPildora;
import Helpers.ComponentesBento.SelectorSegmentado;
import Helpers.ComponentesBento.TarjetaBento;
import Helpers.HelperConfig;
import Helpers.HelperUI;
import Helpers.HelperValidacion;
import Helpers.TemaBento;
import Logica_Conexion.Conexion;
import Logica_Negocio.Administrador;
import Logica_Negocio.Cliente;
import Logica_Negocio.ControlIntentos;
import Logica_Negocio.Usuario;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;

/**
 * Pantalla unificada de bienvenida, selección de base de datos e inicio de sesión
 * en estética Nordic Bento POS.
 */
public class VentanaAcceso extends JFrame {

    private static final String BD_LOCAL = "MySQL Local";
    private static final String BD_NUBE = "Firebase Nube";

    private final ControlIntentos intentos = ControlIntentos.Persistente();
    private final SelectorSegmentado selectorBD;
    private final JTextField txtUsuario = new JTextField();
    private final JPasswordField txtClave = new JPasswordField();
    private final BotonPildora btnEntrar = ComponentesBento.BotonCarbon("Entrar a Tech Shop");
    private final JLabel lblError = new JLabel(" ");
    private Timer timerDesbloqueo;

    public VentanaAcceso() {
        setTitle(HelperUI.TITULO_APP + " — Tienda Electrónica");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(920, 560));

        JPanel lienzo = new JPanel(new GridBagLayout());
        lienzo.setBackground(TemaBento.FONDO_PIEDRA);
        lienzo.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));
        setContentPane(lienzo);

        TarjetaBento tarjeta = new TarjetaBento();
        tarjeta.setPreferredSize(new Dimension(820, 460));
        tarjeta.setLayout(new GridLayout(1, 2, 0, 0));

        selectorBD = new SelectorSegmentado(new String[]{BD_LOCAL, BD_NUBE}, sel -> limpiarError());

        tarjeta.add(crearPanelMarca());
        tarjeta.add(crearPanelFormulario());

        lienzo.add(tarjeta);
        getRootPane().setDefaultButton(btnEntrar);
        pack();
        setLocationRelativeTo(null);
        if (intentos.EstaBloqueado()) {
            iniciarCuentaRegresiva();
        }
    }

    private JPanel crearPanelMarca() {
        BufferedImage fotoPortada = ComponentesBento.CargarRecursoImagen("/imagenes/techshop-portada.jpg");
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                int w = getWidth();
                int h = getHeight() - 2;
                float r = TemaBento.RADIO_TARJETA;
                Path2D.Float forma = new Path2D.Float();
                forma.moveTo(r, 0);
                forma.lineTo(w, 0);
                forma.lineTo(w, h);
                forma.lineTo(r, h);
                forma.quadTo(0, h, 0, h - r);
                forma.lineTo(0, r);
                forma.quadTo(0, 0, r, 0);
                forma.closePath();

                if (fotoPortada != null) {
                    g2.setClip(forma);
                    ComponentesBento.DibujarImagenCover(g2, fotoPortada, 0, 0, w, h, 0.82, 0.35);
                    // Degradado Nocturno + Teal: mantiene el neón "Tech Shop" nítido arriba y legibilidad abajo.
                    GradientPaint gp = new GradientPaint(0, 0, new Color(12, 24, 24, 25),
                            0, h, new Color(10, 48, 45, 238));
                    g2.setPaint(gp);
                    g2.fillRect(0, 0, w, h);
                } else {
                    g2.setColor(TemaBento.TEAL);
                    g2.fill(forma);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(26, 32, 24, 28));

        panel.add(Box.createVerticalStrut(128));

        JLabel headline = new JLabel("<html>Tu tienda de<br>tecnología en un solo lugar</html>") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.translate(1, 2);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
                super.paintComponent(g2);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        headline.setFont(TemaBento.Fuente(Font.BOLD, 24));
        headline.setForeground(Color.WHITE);
        headline.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(headline);

        panel.add(Box.createVerticalStrut(18));

        for (String beneficio : new String[]{
            "Inventario en tiempo real",
            "Cobros con Wompi",
            "Auditoría segura"
        }) {
            JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            fila.setOpaque(false);
            fila.setAlignmentX(LEFT_ALIGNMENT);
            EtiquetaPildora p = new EtiquetaPildora(beneficio, new Color(255, 255, 255, 240), TemaBento.TEXTO);
            p.setIcon(ComponentesBento.IconoCheck(12, TemaBento.TEAL));
            p.setIconTextGap(8);
            p.setFont(TemaBento.Fuente(Font.BOLD, 12));
            p.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
            fila.add(p);
            panel.add(fila);
            panel.add(Box.createVerticalStrut(9));
        }
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel crearPanelFormulario() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(32, 34, 28, 34));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.anchor = GridBagConstraints.WEST;

        JLabel tit = new JLabel("Acceder al sistema");
        tit.setFont(TemaBento.Fuente(Font.BOLD, 23));
        tit.setForeground(TemaBento.TEXTO);
        g.gridy = 0; g.insets = new Insets(0, 0, 4, 0);
        form.add(tit, g);

        JLabel sub = HelperUI.Subtitulo("Selecciona la base de datos e ingresa tu cuenta");
        g.gridy = 1; g.insets = new Insets(0, 0, 18, 0);
        form.add(sub, g);

        JPanel contSelector = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        contSelector.setOpaque(false);
        contSelector.add(selectorBD);
        g.gridy = 2; g.insets = new Insets(0, 0, 18, 0);
        form.add(contSelector, g);

        txtUsuario.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Usuario (ej. Admin o Cliente)");
        txtUsuario.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        txtUsuario.setPreferredSize(new Dimension(0, 40));
        txtUsuario.setFont(TemaBento.Fuente(Font.PLAIN, 13));
        g.gridy = 3; g.insets = new Insets(0, 0, 12, 0);
        form.add(txtUsuario, g);

        txtClave.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Contraseña");
        txtClave.putClientProperty(FlatClientProperties.STYLE, "showRevealButton: true");
        txtClave.setPreferredSize(new Dimension(0, 40));
        txtClave.setFont(TemaBento.Fuente(Font.PLAIN, 13));
        g.gridy = 4; g.insets = new Insets(0, 0, 8, 0);
        form.add(txtClave, g);

        lblError.setFont(TemaBento.Fuente(Font.BOLD, 12));
        lblError.setForeground(TemaBento.PELIGRO);
        lblError.setHorizontalAlignment(SwingConstants.CENTER);
        g.gridy = 5; g.insets = new Insets(2, 0, 10, 0);
        form.add(lblError, g);

        btnEntrar.setPreferredSize(new Dimension(0, 44));
        btnEntrar.setFont(TemaBento.Fuente(Font.BOLD, 14));
        btnEntrar.setIcon(ComponentesBento.IconoFlechaDerecha(15, Color.WHITE));
        btnEntrar.setHorizontalTextPosition(SwingConstants.LEFT);
        btnEntrar.setIconTextGap(8);
        btnEntrar.addActionListener(e -> intentarAcceso());
        g.gridy = 6; g.insets = new Insets(0, 0, 12, 0);
        form.add(btnEntrar, g);

        JLabel pie = new JLabel("Sesión de Administrador o Cliente", SwingConstants.CENTER);
        pie.setFont(TemaBento.Fuente(Font.PLAIN, 11));
        pie.setForeground(TemaBento.TEXTO_SECUNDARIO);
        g.gridy = 7; g.insets = new Insets(0, 0, 0, 0);
        form.add(pie, g);

        return form;
    }

    private void intentarAcceso() {
        if (intentos.EstaBloqueado()) {
            iniciarCuentaRegresiva();
            return;
        }
        String usuario = txtUsuario.getText().trim();
        String clave = String.valueOf(txtClave.getPassword());

        int errUsu = HelperValidacion.ValidarTodo(usuario);
        int errClave = HelperValidacion.ValidarTodoContraseña(clave);
        if (errUsu != 0 || errClave != 0) {
            mostrarError(errUsu != 0
                    ? "Usuario inválido: solo letras, sin espacios ni símbolos."
                    : "Contraseña inválida: no puede estar vacía.");
            return;
        }

        Usuario admin = new Administrador(HelperConfig.Obtener("admin.usuario"), HelperConfig.Obtener("admin.clave.sha256"));
        Usuario cliente = new Cliente(HelperConfig.Obtener("cliente.usuario"), HelperConfig.Obtener("cliente.clave.sha256"));

        Usuario autenticado = null;
        if (admin.Autenticar(usuario, clave)) {
            autenticado = admin;
        } else if (cliente.Autenticar(usuario, clave)) {
            autenticado = cliente;
        }

        if (autenticado == null) {
            txtClave.setText("");
            if (intentos.RegistrarFallo()) {
                iniciarCuentaRegresiva();
            } else {
                mostrarError("Usuario o contraseña inválida. Intentos restantes: " + intentos.IntentosRestantes());
            }
            return;
        }

        intentos.Reiniciar();
        if (timerDesbloqueo != null) {
            timerDesbloqueo.stop();
        }
        limpiarError();
        boolean quiereNube = BD_NUBE.equals(selectorBD.Seleccionado());
        conectarYEntrar(autenticado, quiereNube);
    }

    private void iniciarCuentaRegresiva() {
        btnEntrar.setEnabled(false);
        mostrarError("Acceso bloqueado. Espere " + Math.max(1, intentos.SegundosRestantesBloqueo()) + " s.");
        if (timerDesbloqueo != null && timerDesbloqueo.isRunning()) {
            return;
        }
        timerDesbloqueo = new Timer(1000, e -> {
            long segs = intentos.SegundosRestantesBloqueo();
            if (segs <= 0 && !intentos.EstaBloqueado()) {
                timerDesbloqueo.stop();
                btnEntrar.setEnabled(true);
                limpiarError();
            } else {
                mostrarError("Acceso bloqueado. Espere " + Math.max(1, segs) + " s.");
            }
        });
        timerDesbloqueo.start();
    }

    private void conectarYEntrar(Usuario autenticado, boolean quiereNube) {
        if (!quiereNube) {
            Conexion.modoNube = false;
            abrirPrincipal(autenticado);
            return;
        }
        btnEntrar.setEnabled(false);
        btnEntrar.setText("Conectando a Firebase...");
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return Conexion.Conectar();
            }

            @Override
            protected void done() {
                btnEntrar.setEnabled(true);
                btnEntrar.setText("Entrar a Tech Shop");
                btnEntrar.setIcon(ComponentesBento.IconoFlechaDerecha(15, Color.WHITE));
                boolean conectado = false;
                try {
                    conectado = get();
                } catch (Exception ignored) {
                }
                if (conectado) {
                    Conexion.modoNube = true;
                    abrirPrincipal(autenticado);
                } else {
                    int op = JOptionPane.showConfirmDialog(VentanaAcceso.this,
                            "No fue posible conectar con Firebase.\n¿Desea continuar con la base de datos local (MySQL)?",
                            "Nube no disponible", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (op == JOptionPane.YES_OPTION) {
                        Conexion.modoNube = false;
                        selectorBD.Seleccionar(BD_LOCAL);
                        abrirPrincipal(autenticado);
                    }
                }
            }
        }.execute();
    }

    private void abrirPrincipal(Usuario autenticado) {
        Usuario.sesion = autenticado;
        new VentanaPrincipal(autenticado).setVisible(true);
        dispose();
    }

    private void mostrarError(String msg) {
        lblError.setText(msg);
    }

    private void limpiarError() {
        lblError.setText(" ");
    }
}
