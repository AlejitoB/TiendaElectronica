package GUI_Cliente;

import Helpers.HelperPago;
import Helpers.HelperUI;
import Logica_Conexion.DAOInterfaceProducto;
import Logica_Negocio.Carrito;
import Logica_Negocio.Pedido;
import Logica_Negocio.Usuario;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.YearMonth;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Pasarela de pagos simulada: resume el pedido, pide los datos de la tarjeta,
 * simula la autorizacion del banco y registra la venta descontando el inventario.
 */
public class PasarelaPago extends JDialog {

    private final Carrito carrito;
    private final DAOInterfaceProducto dao;
    private Pedido pedido;

    private final JTextField txtTitular = new JTextField(22);
    private final JTextField txtNumero = new JTextField(22);
    private final JTextField txtVence = new JTextField(6);
    private final JPasswordField txtCvv = new JPasswordField(4);
    private final JLabel lblFranquicia = new JLabel(" ");
    private final JProgressBar progreso = new JProgressBar();
    private final JButton btnPagar;
    private final JButton btnCancelar = new JButton("Cancelar");

    /** Abre la pasarela y devuelve el pedido pagado, o null si el cliente cancelo. */
    public static Pedido Mostrar(JFrame padre, Carrito carrito, DAOInterfaceProducto dao) {
        PasarelaPago p = new PasarelaPago(padre, carrito, dao);
        p.setVisible(true);
        return p.pedido;
    }

    PasarelaPago(JFrame padre, Carrito carrito, DAOInterfaceProducto dao) {
        super(padre, "Pasarela de pagos", true);
        this.carrito = carrito;
        this.dao = dao;
        btnPagar = HelperUI.BotonPrimario("Pagar " + HelperUI.Moneda(carrito.Total()));

        JPanel raiz = new JPanel(new BorderLayout(20, 14));
        raiz.setBorder(BorderFactory.createEmptyBorder(18, 20, 16, 20));
        setContentPane(raiz);

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.add(HelperUI.Titulo("Pago seguro con tarjeta"), BorderLayout.NORTH);
        cabecera.add(HelperUI.Subtitulo("Pasarela simulada: no se realiza ningún cobro real."), BorderLayout.SOUTH);
        raiz.add(cabecera, BorderLayout.NORTH);
        raiz.add(resumen(), BorderLayout.WEST);
        raiz.add(formulario(), BorderLayout.CENTER);

        JPanel pie = new JPanel(new BorderLayout(10, 0));
        progreso.setVisible(false);
        progreso.setStringPainted(true);
        pie.add(progreso, BorderLayout.CENTER);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnCancelar.addActionListener(e -> dispose());
        btnPagar.addActionListener(e -> pagar());
        botones.add(btnCancelar);
        botones.add(btnPagar);
        pie.add(botones, BorderLayout.EAST);
        raiz.add(pie, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnPagar);
        pack();
        setLocationRelativeTo(padre);
    }

    private JPanel resumen() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Resumen del pedido"), BorderFactory.createEmptyBorder(6, 8, 8, 8)));
        StringBuilder sb = new StringBuilder();
        for (Carrito.Item i : carrito.Items()) {
            sb.append(i.cantidad).append(" × ").append(i.producto.getNombre()).append('\n')
                    .append("      ").append(HelperUI.Moneda(i.Subtotal())).append("\n\n");
        }
        JTextArea detalle = new JTextArea(sb.toString().trim());
        detalle.setEditable(false);
        detalle.setOpaque(false);
        JScrollPane scroll = new JScrollPane(detalle);
        scroll.setBorder(null);
        scroll.setPreferredSize(new Dimension(320, 220));
        panel.add(scroll, BorderLayout.CENTER);
        JLabel total = new JLabel("Total a pagar: " + HelperUI.Moneda(carrito.Total()));
        total.setFont(new Font("Segoe UI", Font.BOLD, 18));
        panel.add(total, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel formulario() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Datos de la tarjeta"), BorderFactory.createEmptyBorder(6, 10, 8, 10)));
        txtTitular.putClientProperty("JTextField.placeholderText", "Como aparece en la tarjeta");
        txtNumero.putClientProperty("JTextField.placeholderText", "0000 0000 0000 0000");
        txtVence.putClientProperty("JTextField.placeholderText", "MM/AA");
        txtCvv.putClientProperty("JTextField.placeholderText", "123");
        lblFranquicia.setFont(lblFranquicia.getFont().deriveFont(Font.BOLD));
        lblFranquicia.setForeground(HelperUI.ACENTO);
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
        poner(form, g, 0, 0, 2, new JLabel("Titular"));
        poner(form, g, 0, 1, 2, txtTitular);
        poner(form, g, 0, 2, 1, new JLabel("Número de tarjeta"));
        lblFranquicia.setHorizontalAlignment(JLabel.RIGHT);
        poner(form, g, 1, 2, 1, lblFranquicia);
        poner(form, g, 0, 3, 2, txtNumero);
        poner(form, g, 0, 4, 1, new JLabel("Vencimiento (MM/AA)"));
        poner(form, g, 1, 4, 1, new JLabel("CVV"));
        poner(form, g, 0, 5, 1, txtVence);
        poner(form, g, 1, 5, 1, txtCvv);
        g.insets = new Insets(14, 0, 0, 8);
        poner(form, g, 0, 6, 2, HelperUI.Subtitulo("Tarjetas de prueba: " + HelperPago.TARJETA_APROBADA + " (aprobada)"));
        g.insets = new Insets(2, 0, 0, 8);
        poner(form, g, 0, 7, 2, HelperUI.Subtitulo("y " + HelperPago.TARJETA_RECHAZADA + " (rechazada)."));
        g.weighty = 1;
        g.anchor = GridBagConstraints.NORTHWEST;
        poner(form, g, 0, 8, 2, HelperUI.Subtitulo("Solo se guarda la franquicia y los últimos 4 dígitos."));
        form.setPreferredSize(new Dimension(360, 0));
        return form;
    }

    private void poner(JPanel form, GridBagConstraints g, int x, int y, int ancho, Component c) {
        g.gridx = x;
        g.gridy = y;
        g.gridwidth = ancho;
        form.add(c, g);
    }

    private void franquicia() {
        lblFranquicia.setText(HelperPago.Franquicia(txtNumero.getText()));
    }

    private void pagar() {
        String numero = txtNumero.getText();
        String error = HelperPago.Validar(txtTitular.getText(), numero, txtVence.getText(),
                String.valueOf(txtCvv.getPassword()), YearMonth.now());
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Revise los datos de pago", JOptionPane.WARNING_MESSAGE);
            return;
        }
        procesando(true);
        String cliente = Usuario.sesion == null ? "cliente" : Usuario.sesion.getUsu();
        new SwingWorker<Pedido, Void>() {
            @Override
            protected Pedido doInBackground() throws Exception {
                Thread.sleep(1500); // simula la espera de respuesta del banco
                String autorizacion = HelperPago.Autorizar(numero);
                if (autorizacion == null) {
                    throw new IllegalStateException("Transacción rechazada por el banco: fondos insuficientes.");
                }
                Pedido p = new Pedido(cliente, carrito, HelperPago.Enmascarar(numero), autorizacion);
                try {
                    dao.registrarVenta(p);
                } catch (Exception ex) {
                    throw new IllegalStateException("El pago fue reversado porque no se pudo registrar el pedido: "
                            + causa(ex), ex);
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
                    // get() envuelve en ExecutionException el error lanzado en doInBackground.
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
        for (JTextField t : new JTextField[]{txtTitular, txtNumero, txtVence, txtCvv}) {
            t.setEnabled(!si);
        }
        revalidate();
    }

    private void recibo() {
        StringBuilder sb = new StringBuilder();
        for (Carrito.Item i : pedido.items) {
            sb.append(i.cantidad).append(" × ").append(i.producto.getNombre()).append("   ")
                    .append(HelperUI.Moneda(i.Subtotal())).append('\n');
        }
        JTextArea texto = new JTextArea("Pedido: " + pedido.id + "\nFecha: " + pedido.fecha + "\nCliente: " + pedido.cliente
                + "\nTarjeta: " + pedido.tarjeta + "\nAutorización: " + pedido.autorizacion + "\n\n"
                + sb + "\nTotal pagado: " + HelperUI.Moneda(pedido.total));
        texto.setEditable(false);
        texto.setOpaque(false);
        JOptionPane.showMessageDialog(this, texto, "¡Pago aprobado!", JOptionPane.INFORMATION_MESSAGE);
    }

    private static String causa(Throwable t) {
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t.getMessage();
    }
}
