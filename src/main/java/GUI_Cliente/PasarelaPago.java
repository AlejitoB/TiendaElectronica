package GUI_Cliente;

import Helpers.ComponentesBento;
import Helpers.ComponentesBento.BotonPildora;
import Helpers.ComponentesBento.EtiquetaPildora;
import Helpers.ComponentesBento.TarjetaBento;
import Helpers.HelperAuditoria;
import Helpers.HelperPago;
import Helpers.HelperUI;
import Helpers.TemaBento;
import Logica_Conexion.DAOInterfaceProducto;
import Logica_Conexion.PasarelaWompi;
import Logica_Conexion.ServicioPersona;
import Logica_Negocio.Carrito;
import Logica_Negocio.Pedido;
import Logica_Negocio.Persona;
import Logica_Negocio.Usuario;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.YearMonth;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Pasarela de pagos en estética Nordic Bento POS: reserva el stock atómicamente
 * antes de cobrar vía Wompi Sandbox o simulación local, y libera las unidades
 * si la transacción es rechazada.
 */
public class PasarelaPago extends JDialog {

    private record OpcionCliente(String etiqueta, Persona persona) {
        @Override
        public String toString() {
            return etiqueta;
        }
    }

    private final Carrito carrito;
    private final DAOInterfaceProducto dao;
    private Pedido pedido;

    private final JComboBox<OpcionCliente> cbComprador = new JComboBox<>();
    private final JTextField txtTitular = new JTextField(22);
    private final JTextField txtCorreo = new JTextField(22);
    private final JTextField txtNumero = new JTextField(22);
    private final JTextField txtVence = new JTextField(6);
    private final JPasswordField txtCvv = new JPasswordField(4);
    private final EtiquetaPildora pillFranquicia = new EtiquetaPildora(
            "Tarjeta", TemaBento.SUPERFICIE_SUAVE, TemaBento.TEXTO_SECUNDARIO);
    private final JProgressBar progreso = new JProgressBar();
    private final BotonPildora btnPagar;
    private final BotonPildora btnCancelar = ComponentesBento.BotonSuave("Cancelar");

    /** Abre la pasarela y devuelve el pedido pagado, o null si el cliente canceló. */
    public static Pedido Mostrar(JFrame padre, Carrito carrito, DAOInterfaceProducto dao) {
        PasarelaPago p = new PasarelaPago(padre, carrito, dao);
        p.setVisible(true);
        return p.pedido;
    }

    PasarelaPago(JFrame padre, Carrito carrito, DAOInterfaceProducto dao) {
        super(padre, "Tech Shop — Pasarela de pagos", true);
        this.carrito = carrito;
        this.dao = dao;
        this.btnPagar = ComponentesBento.BotonCarbon("Pagar " + HelperUI.Moneda(carrito.Total()));
        this.btnPagar.setIcon(ComponentesBento.IconoFlechaDerecha(14, Color.WHITE));
        this.btnPagar.setHorizontalTextPosition(SwingConstants.LEFT);
        this.btnPagar.setIconTextGap(8);

        JPanel raiz = new JPanel(new BorderLayout(16, 14));
        raiz.setBackground(TemaBento.FONDO_PIEDRA);
        raiz.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        setContentPane(raiz);

        TarjetaBento cabecera = new TarjetaBento();
        cabecera.setLayout(new BorderLayout(10, 4));
        cabecera.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        JPanel titulos = new JPanel(new BorderLayout(0, 2));
        titulos.setOpaque(false);
        titulos.add(HelperUI.Titulo("Pago seguro con tarjeta"), BorderLayout.NORTH);
        titulos.add(HelperUI.Subtitulo(PasarelaWompi.Configurada()
                ? "Procesado por Wompi (ambiente Sandbox): reserva de stock antes de cobrar."
                : "Pasarela simulada local: reserva de stock antes de autorizar."), BorderLayout.SOUTH);
        cabecera.add(titulos, BorderLayout.CENTER);
        cabecera.add(new EtiquetaPildora(
                PasarelaWompi.Configurada() ? "Wompi Sandbox" : "Simulación Local",
                TemaBento.TEAL_SUAVE, TemaBento.TEAL), BorderLayout.EAST);
        raiz.add(cabecera, BorderLayout.NORTH);

        raiz.add(resumen(), BorderLayout.WEST);
        raiz.add(formulario(), BorderLayout.CENTER);

        JPanel pie = new JPanel(new BorderLayout(12, 0));
        pie.setOpaque(false);
        progreso.setVisible(false);
        progreso.setStringPainted(true);
        progreso.setForeground(TemaBento.TEAL);
        pie.add(progreso, BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setOpaque(false);
        btnCancelar.addActionListener(e -> dispose());
        btnPagar.addActionListener(e -> pagar());
        botones.add(btnCancelar);
        botones.add(btnPagar);
        pie.add(botones, BorderLayout.EAST);
        raiz.add(pie, BorderLayout.SOUTH);

        cargarCompradores();
        getRootPane().setDefaultButton(btnPagar);
        pack();
        setMinimumSize(new Dimension(840, 540));
        setLocationRelativeTo(padre);
    }

    private JPanel resumen() {
        TarjetaBento card = new TarjetaBento();
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        card.setPreferredSize(new Dimension(320, 0));

        JLabel tit = new JLabel("Resumen del pedido");
        tit.setFont(TemaBento.Fuente(Font.BOLD, 16));
        tit.setForeground(TemaBento.TEXTO);
        card.add(tit, BorderLayout.NORTH);

        StringBuilder sb = new StringBuilder();
        for (Carrito.Item i : carrito.Items()) {
            sb.append(i.getCantidad()).append(" × ").append(i.producto.getNombre()).append('\n')
                    .append("   ").append(HelperUI.Moneda(i.Subtotal())).append("\n\n");
        }
        JTextArea detalle = new JTextArea(sb.toString().trim());
        detalle.setEditable(false);
        detalle.setOpaque(false);
        detalle.setFont(TemaBento.Fuente(Font.PLAIN, 13));
        detalle.setForeground(TemaBento.TEXTO);
        detalle.setLineWrap(true);
        detalle.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(detalle);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        card.add(scroll, BorderLayout.CENTER);

        JPanel pie = new JPanel(new BorderLayout(0, 8));
        pie.setOpaque(false);
        JSeparator sep = new JSeparator();
        sep.setForeground(TemaBento.BORDE);
        pie.add(sep, BorderLayout.NORTH);
        JLabel total = new JLabel("Total: " + HelperUI.Moneda(carrito.Total()));
        total.setFont(TemaBento.Fuente(Font.BOLD, 18));
        total.setForeground(TemaBento.TEXTO);
        pie.add(total, BorderLayout.SOUTH);
        card.add(pie, BorderLayout.SOUTH);
        return card;
    }

    private JPanel formulario() {
        TarjetaBento form = new TarjetaBento();
        form.setLayout(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        form.setPreferredSize(new Dimension(440, 0));

        String usu = Usuario.sesion == null ? "cliente" : Usuario.sesion.getUsu();
        cbComprador.addItem(new OpcionCliente("Compra directa (" + usu + ")", null));
        cbComprador.setPreferredSize(new Dimension(0, 36));
        cbComprador.addActionListener(e -> {
            OpcionCliente sel = (OpcionCliente) cbComprador.getSelectedItem();
            if (sel != null && sel.persona() != null) {
                Persona per = sel.persona();
                txtTitular.setText((per.getNombre() + " " + per.getApellido()).trim());
                if (!per.getCorreo().isBlank()) {
                    txtCorreo.setText(per.getCorreo());
                }
            }
        });

        txtTitular.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Como aparece en la tarjeta");
        txtCorreo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Para el comprobante de pago");
        txtNumero.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "0000 0000 0000 0000");
        txtVence.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "MM/AA");
        txtCvv.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "123");

        for (JTextField f : new JTextField[]{txtTitular, txtCorreo, txtNumero, txtVence, txtCvv}) {
            f.setPreferredSize(new Dimension(f.getPreferredSize().width, 36));
        }

        txtNumero.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { franquicia(); }
            public void removeUpdate(DocumentEvent e) { franquicia(); }
            public void changedUpdate(DocumentEvent e) { franquicia(); }
        });

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 0, 4, 8);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        g.weightx = 1;

        JLabel tit = new JLabel("Datos de la tarjeta");
        tit.setFont(TemaBento.Fuente(Font.BOLD, 16));
        tit.setForeground(TemaBento.TEXTO);
        g.insets = new Insets(0, 0, 8, 8);
        poner(form, g, 0, 0, 2, tit);

        g.insets = new Insets(3, 0, 2, 8);
        poner(form, g, 0, 1, 2, etiqueta("Comprador registrado (opcional)"));
        poner(form, g, 0, 2, 2, cbComprador);
        poner(form, g, 0, 3, 2, etiqueta("Titular"));
        poner(form, g, 0, 4, 2, txtTitular);
        poner(form, g, 0, 5, 2, etiqueta("Correo electrónico"));
        poner(form, g, 0, 6, 2, txtCorreo);
        poner(form, g, 0, 7, 1, etiqueta("Número de tarjeta"));
        JPanel pFran = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pFran.setOpaque(false);
        pFran.add(pillFranquicia);
        poner(form, g, 1, 7, 1, pFran);
        poner(form, g, 0, 8, 2, txtNumero);
        poner(form, g, 0, 9, 1, etiqueta("Vencimiento (MM/AA)"));
        poner(form, g, 1, 9, 1, etiqueta("CVV"));
        poner(form, g, 0, 10, 1, txtVence);
        poner(form, g, 1, 10, 1, txtCvv);

        g.insets = new Insets(10, 0, 0, 8);
        poner(form, g, 0, 11, 2, HelperUI.Subtitulo("Tarjetas prueba: " + HelperPago.TARJETA_APROBADA + " (aprobada)"));
        g.insets = new Insets(2, 0, 0, 8);
        poner(form, g, 0, 12, 2, HelperUI.Subtitulo("y " + HelperPago.TARJETA_RECHAZADA + " (rechazada). Solo se guardan últimos 4 dígitos."));
        g.weighty = 1;
        poner(form, g, 0, 13, 2, new JLabel());
        return form;
    }

    private void cargarCompradores() {
        new SwingWorker<List<Persona>, Void>() {
            @Override
            protected List<Persona> doInBackground() throws Exception {
                return new ServicioPersona().listar();
            }

            @Override
            protected void done() {
                try {
                    for (Persona p : get()) {
                        cbComprador.addItem(new OpcionCliente(
                                p.getNombre() + " " + p.getApellido() + " (UID " + p.getUid() + ")", p));
                    }
                } catch (Exception ignored) {
                    // Si falla o está vacío, permanece la opción de compra directa.
                }
            }
        }.execute();
    }

    private static JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(TemaBento.Fuente(Font.BOLD, 12));
        l.setForeground(TemaBento.TEXTO);
        return l;
    }

    private void poner(JPanel form, GridBagConstraints g, int x, int y, int ancho, Component c) {
        g.gridx = x;
        g.gridy = y;
        g.gridwidth = ancho;
        form.add(c, g);
    }

    private void franquicia() {
        String fr = HelperPago.Franquicia(txtNumero.getText());
        if (fr.isEmpty()) {
            pillFranquicia.setText("Tarjeta");
            pillFranquicia.setColores(TemaBento.SUPERFICIE_SUAVE, TemaBento.TEXTO_SECUNDARIO);
        } else {
            pillFranquicia.setText(fr);
            pillFranquicia.setColores(TemaBento.TEAL_SUAVE, TemaBento.TEAL);
        }
    }

    private void pagar() {
        String numero = HelperPago.SoloDigitos(txtNumero.getText());
        String titular = txtTitular.getText().trim();
        String correo = txtCorreo.getText().trim();
        String vence = txtVence.getText().trim();
        String cvv = String.valueOf(txtCvv.getPassword());
        String error = HelperPago.Validar(titular, correo, numero, vence, cvv, YearMonth.now());
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Revise los datos de pago", JOptionPane.WARNING_MESSAGE);
            return;
        }
        procesando(true);
        OpcionCliente sel = (OpcionCliente) cbComprador.getSelectedItem();
        String usuarioSesion = Usuario.sesion == null ? "cliente" : Usuario.sesion.getUsu();
        String clienteNombre = (sel != null && sel.persona() != null)
                ? (sel.persona().getNombre() + " " + sel.persona().getApellido()).trim()
                : usuarioSesion;
        String clienteUid = (sel != null && sel.persona() != null) ? sel.persona().getUid() : "";

        new SwingWorker<Pedido, Void>() {
            @Override
            protected Pedido doInBackground() throws Exception {
                // Reserva atómica antes de cobrar: evita cobrar si otro comprador agotó el stock.
                dao.reservarStock(carrito.Items());
                Pedido p = new Pedido(clienteNombre, clienteUid, carrito, HelperPago.Enmascarar(numero), null);
                try {
                    if (PasarelaWompi.Configurada()) {
                        String[] mmaa = vence.split("/");
                        p.autorizacion = PasarelaWompi.Cobrar(titular, numero, mmaa[0], mmaa[1], cvv, correo, p.id, p.total);
                    } else {
                        Thread.sleep(1500);
                        p.autorizacion = HelperPago.Autorizar(numero);
                        if (p.autorizacion == null) {
                            throw new IllegalStateException("Transacción rechazada por el banco: fondos insuficientes.");
                        }
                    }
                } catch (Exception exCobro) {
                    try {
                        dao.liberarStock(carrito.Items());
                    } catch (Exception exLib) {
                        HelperAuditoria.Registrar("STOCK_RESERVADO_PENDIENTE",
                                p.id + " errorLiberar=" + causa(exLib));
                    }
                    throw exCobro;
                }
                try {
                    dao.guardarPedido(p);
                    HelperAuditoria.Registrar("VENTA", p.id + " total=" + p.total + " tarjeta=" + p.tarjeta
                            + " autorizacion=" + p.autorizacion + " " + p.Detalle());
                } catch (Exception ex) {
                    HelperAuditoria.Registrar("VENTA_SIN_REGISTRAR", p.id + " autorizacion=" + p.autorizacion + " " + causa(ex));
                    throw new IllegalStateException("El pago " + p.autorizacion + " fue aprobado pero no se pudo registrar el pedido ("
                            + causa(ex) + "). Quedó en la auditoría para que el administrador lo revise.", ex);
                }
                return p;
            }

            @Override
            protected void done() {
                procesando(false);
                try {
                    pedido = get();
                    recibo();
                    dispose();
                } catch (Exception ex) {
                    Throwable t = ex instanceof ExecutionException ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(PasarelaPago.this, t.getMessage(), "Pago no aprobado", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void procesando(boolean si) {
        progreso.setVisible(si);
        progreso.setIndeterminate(si);
        progreso.setString(si ? "Procesando pago..." : "");
        btnPagar.setEnabled(!si);
        btnCancelar.setEnabled(!si);
        cbComprador.setEnabled(!si);
        for (JTextField t : new JTextField[]{txtTitular, txtCorreo, txtNumero, txtVence, txtCvv}) {
            t.setEnabled(!si);
        }
        revalidate();
    }

    private void recibo() {
        StringBuilder sb = new StringBuilder();
        for (Carrito.Item i : pedido.items) {
            sb.append(i.getCantidad()).append(" × ").append(i.producto.getNombre()).append("   ")
                    .append(HelperUI.Moneda(i.Subtotal())).append('\n');
        }
        JTextArea texto = new JTextArea("Pedido: " + pedido.id + "\nFecha: " + pedido.fecha + "\nCliente: " + pedido.cliente
                + "\nTarjeta: " + pedido.tarjeta + "\nAutorización: " + pedido.autorizacion + "\n\n"
                + sb + "\nTotal pagado: " + HelperUI.Moneda(pedido.total));
        texto.setEditable(false);
        texto.setOpaque(false);
        texto.setFont(TemaBento.Fuente(Font.PLAIN, 13));
        JOptionPane.showMessageDialog(this, texto, "¡Pago aprobado!", JOptionPane.INFORMATION_MESSAGE);
    }

    private static String causa(Throwable t) {
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t.getMessage();
    }
}
