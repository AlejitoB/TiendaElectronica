package GUI_Admin;

import Helpers.ComponentesBento;
import Helpers.ComponentesBento.BotonPildora;
import Helpers.ComponentesBento.EtiquetaPildora;
import Helpers.ComponentesBento.TarjetaBento;
import Helpers.HelperUI;
import Helpers.HelperValidacion;
import Helpers.TemaBento;
import Logica_Conexion.Conexion;
import Logica_Conexion.ServicioPersona;
import Logica_Negocio.Persona;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 * Módulo unificado de clientes para administración en estética Nordic Bento POS.
 * Funciona sobre MySQL local o Firebase nube e incluye correo y teléfono de contacto.
 */
public class PanelClientes extends JPanel {

    private final ServicioPersona servicio;

    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"UID", "Nombre", "Apellido", "Cédula", "Correo", "Teléfono", "Dirección"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modelo);
    private final JTextField txtBuscar = ComponentesBento.CampoBusqueda(
            "Buscar por UID, nombre, cédula o correo...", 280, 34, this::filtrar);
    private final JLabel lblResumen = HelperUI.Subtitulo(" ");

    private List<Persona> cachePersonas = new ArrayList<>();

    private final JLabel lblFoto = new JLabel();
    private final JLabel lblNombreFicha = new JLabel("Nuevo cliente");
    private final EtiquetaPildora pillOrigen = new EtiquetaPildora(
            Conexion.modoNube ? "Firebase Nube" : "MySQL Local",
            Conexion.modoNube ? TemaBento.NUBE_SUAVE : TemaBento.TEAL_SUAVE,
            Conexion.modoNube ? TemaBento.NUBE_AZUL : TemaBento.TEAL);

    private final JTextField txtUid = new JTextField();
    private final JTextField txtNombre = new JTextField();
    private final JTextField txtApellido = new JTextField();
    private final JTextField txtCedula = new JTextField();
    private final JTextField txtCorreo = new JTextField();
    private final JTextField txtTelefono = new JTextField();
    private final JTextField txtDireccion = new JTextField();
    private final JComboBox<String> cbImagen = new JComboBox<>();

    private final BotonPildora btnGuardar = ComponentesBento.BotonTeal("Guardar");
    private final BotonPildora btnEliminar = ComponentesBento.BotonPeligroTexto("Eliminar");

    public PanelClientes() {
        this.servicio = new ServicioPersona();
        setOpaque(false);
        setLayout(new BorderLayout(14, 0));

        add(panelListado(), BorderLayout.CENTER);
        add(panelFicha(), BorderLayout.EAST);

        cargarImagenesDisponibles();
        cargar();
    }

    public void refrescar() {
        cargar();
    }

    private JPanel panelListado() {
        TarjetaBento card = new TarjetaBento();
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 14, 20));

        JPanel cabecera = new JPanel(new BorderLayout(12, 0));
        cabecera.setOpaque(false);
        JLabel tit = new JLabel("Clientes Registrados");
        tit.setFont(TemaBento.Fuente(Font.BOLD, 17));
        tit.setForeground(TemaBento.TEXTO);
        cabecera.add(tit, BorderLayout.WEST);

        JPanel accionesTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        accionesTop.setOpaque(false);
        BotonPildora btnRecargar = ComponentesBento.BotonSuave("Actualizar");
        btnRecargar.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
        btnRecargar.addActionListener(e -> cargar());
        accionesTop.add(txtBuscar);
        accionesTop.add(btnRecargar);
        cabecera.add(accionesTop, BorderLayout.EAST);
        card.add(cabecera, BorderLayout.NORTH);

        tabla.setRowSorter(sorter);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setShowVerticalLines(false);
        tabla.setFillsViewportHeight(true);
        int[] anchos = {65, 110, 110, 100, 160, 105, 150};
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() >= 0) {
                mostrar(tabla.convertRowIndexToModel(tabla.getSelectedRow()));
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(TemaBento.TARJETA);
        card.add(scroll, BorderLayout.CENTER);

        JPanel pie = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pie.setOpaque(false);
        pie.add(lblResumen);
        card.add(pie, BorderLayout.SOUTH);
        return card;
    }

    private JPanel panelFicha() {
        TarjetaBento card = new TarjetaBento();
        card.setPreferredSize(new Dimension(380, 0));
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setOpaque(false);
        lblFoto.setPreferredSize(new Dimension(64, 64));
        lblFoto.setHorizontalAlignment(SwingConstants.CENTER);
        lblFoto.setIcon(ComponentesBento.CargarFotoCliente("", "TS", 64, 64));
        top.add(lblFoto, BorderLayout.WEST);

        JPanel infoTit = new JPanel(new GridBagLayout());
        infoTit.setOpaque(false);
        GridBagConstraints gt = new GridBagConstraints();
        gt.gridx = 0; gt.gridy = 0; gt.anchor = GridBagConstraints.WEST; gt.weightx = 1;
        lblNombreFicha.setFont(TemaBento.Fuente(Font.BOLD, 16));
        lblNombreFicha.setForeground(TemaBento.TEXTO);
        infoTit.add(lblNombreFicha, gt);
        gt.gridy = 1; gt.insets = new Insets(4, 0, 0, 0);
        infoTit.add(pillOrigen, gt);
        top.add(infoTit, BorderLayout.CENTER);
        card.add(top, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        cbImagen.setEditable(true);
        cbImagen.addActionListener(e -> actualizarFotoPreview());

        txtUid.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ej: 101");
        txtCorreo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "cliente@correo.com");
        txtTelefono.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "3001234567");

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 0, 5, 8);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        Object[][] campos = {
            {"UID", txtUid},
            {"Nombre", txtNombre},
            {"Apellido", txtApellido},
            {"Cédula", txtCedula},
            {"Correo", txtCorreo},
            {"Teléfono", txtTelefono},
            {"Dirección", txtDireccion},
            {"Imagen", cbImagen}
        };
        for (int i = 0; i < campos.length; i++) {
            g.gridy = i;
            g.gridx = 0;
            g.weightx = 0.30;
            JLabel lbl = new JLabel((String) campos[i][0]);
            lbl.setFont(TemaBento.Fuente(Font.PLAIN, 13));
            lbl.setForeground(TemaBento.TEXTO_SECUNDARIO);
            form.add(lbl, g);

            g.gridx = 1;
            g.weightx = 0.70;
            Component c = (Component) campos[i][1];
            c.setPreferredSize(new Dimension(200, 34));
            form.add(c, g);
        }
        g.gridy = campos.length;
        g.gridx = 0;
        g.gridwidth = 2;
        g.weighty = 1;
        form.add(new JLabel(), g);
        card.add(form, BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        botones.setOpaque(false);
        BotonPildora btnLimpiar = ComponentesBento.BotonSuave("Limpiar");
        btnLimpiar.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        btnLimpiar.addActionListener(e -> limpiar());
        btnEliminar.addActionListener(e -> eliminar());
        btnGuardar.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        btnGuardar.addActionListener(e -> guardar());
        botones.add(btnLimpiar);
        botones.add(btnEliminar);
        botones.add(btnGuardar);
        card.add(botones, BorderLayout.SOUTH);
        return card;
    }

    private void cargarImagenesDisponibles() {
        cbImagen.removeAllItems();
        Path dir = Path.of("Images");
        if (Files.isDirectory(dir)) {
            try (Stream<Path> files = Files.list(dir)) {
                files.filter(Files::isRegularFile)
                        .map(p -> p.getFileName().toString())
                        .filter(n -> !n.toLowerCase().startsWith("background"))
                        .map(n -> n.contains(".") ? n.substring(0, n.lastIndexOf('.')) : n)
                        .sorted()
                        .forEach(cbImagen::addItem);
            } catch (Exception ignored) {
            }
        }
        if (cbImagen.getItemCount() == 0) {
            cbImagen.addItem("image1");
        }
        cbImagen.setSelectedIndex(0);
    }

    private void cargar() {
        lblResumen.setText("Cargando clientes...");
        new SwingWorker<List<Persona>, Void>() {
            long ms;

            @Override
            protected List<Persona> doInBackground() throws Exception {
                long inicio = System.currentTimeMillis();
                List<Persona> lista = servicio.listar();
                ms = System.currentTimeMillis() - inicio;
                return lista;
            }

            @Override
            protected void done() {
                try {
                    cachePersonas = get();
                    modelo.setRowCount(0);
                    for (Persona p : cachePersonas) {
                        modelo.addRow(new Object[]{
                            p.getUid(), p.getNombre(), p.getApellido(),
                            p.getCedula(), p.getCorreo(), p.getTelefono(), p.getDireccion()
                        });
                    }
                    lblResumen.setText(cachePersonas.size() + " cliente(s) registrado(s) · cargado en " + ms + " ms");
                } catch (Exception ex) {
                    lblResumen.setText("No se pudieron cargar los clientes");
                    JOptionPane.showMessageDialog(PanelClientes.this,
                            "Error al consultar clientes: " + causa(ex),
                            "Clientes", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void filtrar() {
        String q = txtBuscar.getText().trim();
        sorter.setRowFilter(q.isEmpty() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(q), 0, 1, 2, 3, 4));
    }

    private void mostrar(int filaModelo) {
        if (filaModelo < 0 || filaModelo >= cachePersonas.size()) {
            return;
        }
        Persona p = cachePersonas.get(filaModelo);
        txtUid.setText(p.getUid());
        txtUid.setEditable(false);
        txtNombre.setText(p.getNombre());
        txtApellido.setText(p.getApellido());
        txtCedula.setText(p.getCedula());
        txtCorreo.setText(p.getCorreo());
        txtTelefono.setText(p.getTelefono());
        txtDireccion.setText(p.getDireccion());
        cbImagen.setSelectedItem(p.getNom_img());

        String iniciales = iniciales(p.getNombre(), p.getApellido());
        lblNombreFicha.setText((p.getNombre() + " " + p.getApellido()).trim());
        lblFoto.setIcon(ComponentesBento.CargarFotoCliente(p.getNom_img(), iniciales, 64, 64));
    }

    private void actualizarFotoPreview() {
        Object sel = cbImagen.getEditor().getItem();
        String img = sel == null ? "" : sel.toString().trim();
        String ini = iniciales(txtNombre.getText(), txtApellido.getText());
        lblFoto.setIcon(ComponentesBento.CargarFotoCliente(img, ini, 64, 64));
    }

    private void limpiar() {
        tabla.clearSelection();
        txtUid.setEditable(true);
        for (JTextField t : new JTextField[]{txtUid, txtNombre, txtApellido, txtCedula, txtCorreo, txtTelefono, txtDireccion}) {
            t.setText("");
        }
        if (cbImagen.getItemCount() > 0) {
            cbImagen.setSelectedIndex(0);
        }
        lblNombreFicha.setText("Nuevo cliente");
        lblFoto.setIcon(ComponentesBento.CargarFotoCliente("", "TS", 64, 64));
        txtUid.requestFocus();
    }

    private void guardar() {
        Object imgSel = cbImagen.getEditor().getItem();
        Persona p = new Persona(
                txtUid.getText().trim(),
                txtNombre.getText().trim(),
                txtApellido.getText().trim(),
                txtDireccion.getText().trim(),
                txtCedula.getText().trim(),
                "",
                imgSel == null ? "" : imgSel.toString().trim(),
                txtCorreo.getText().trim(),
                txtTelefono.getText().trim());
        String error = HelperValidacion.ValidarPersona(p);
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Revise el formulario", JOptionPane.WARNING_MESSAGE);
            return;
        }
        boolean nuevo = txtUid.isEditable();
        ejecutar(() -> servicio.guardar(p, nuevo), nuevo ? "Cliente registrado" : "Cliente actualizado");
    }

    private void eliminar() {
        String uid = txtUid.getText().trim();
        if (txtUid.isEditable() || uid.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione en la tabla el cliente que desea eliminar",
                    "Clientes", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int op = JOptionPane.showConfirmDialog(this,
                "¿Eliminar definitivamente al cliente " + uid + " (" + txtNombre.getText() + " " + txtApellido.getText() + ")?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (op == JOptionPane.YES_OPTION) {
            ejecutar(() -> {
                if (!servicio.eliminar(uid)) {
                    throw new IllegalStateException("No se encontró el cliente con UID " + uid);
                }
            }, "Cliente eliminado");
        }
    }

    private interface Operacion {
        void correr() throws Exception;
    }

    private void ejecutar(Operacion op, String exito) {
        btnGuardar.setEnabled(false);
        btnEliminar.setEnabled(false);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                op.correr();
                return null;
            }

            @Override
            protected void done() {
                btnGuardar.setEnabled(true);
                btnEliminar.setEnabled(true);
                try {
                    get();
                    limpiar();
                    cargar();
                    JOptionPane.showMessageDialog(PanelClientes.this, exito);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PanelClientes.this, causa(ex),
                            "Clientes", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private static String iniciales(String nombre, String apellido) {
        String n = nombre == null || nombre.isBlank() ? "" : nombre.trim().substring(0, 1);
        String a = apellido == null || apellido.isBlank() ? "" : apellido.trim().substring(0, 1);
        String res = (n + a).toUpperCase();
        return res.isEmpty() ? "TS" : res;
    }

    private static String causa(Exception ex) {
        Throwable t = ex;
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t.getMessage();
    }
}
