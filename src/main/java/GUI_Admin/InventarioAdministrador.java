package GUI_Admin;

import GUI_Local.MenuLocal;
import Helpers.HelperUI;
import Helpers.HelperValidacion;
import Logica_Conexion.Conexion;
import Logica_Conexion.DAOInterfaceProducto;
import Logica_Negocio.CatalogoInicial;
import Logica_Negocio.Producto;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.TreeSet;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 * Inventario de articulos de computador: listar, buscar, crear, editar y eliminar productos.
 */
public class InventarioAdministrador extends JFrame {

    static final int STOCK_BAJO = 3;

    private final DAOInterfaceProducto dao;
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"Serial", "Nombre", "Marca", "Categoría", "Precio", "Stock"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return c == 4 ? Long.class : c == 5 ? Integer.class : String.class;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modelo);
    private final JTextField txtBuscar = new JTextField(18);
    private final JTextField txtSerial = new JTextField(14);
    private final JTextField txtNombre = new JTextField(14);
    private final JTextField txtMarca = new JTextField(14);
    private final JComboBox<String> cbCategoria = new JComboBox<>();
    private final JTextField txtPrecio = new JTextField(14);
    private final JSpinner spStock = new JSpinner(new SpinnerNumberModel(0, 0, 99999, 1));
    private final JLabel lblResumen = new JLabel(" ");
    private final JButton btnGuardar = HelperUI.BotonPrimario("Guardar");
    private final JButton btnEliminar = new JButton("Eliminar");

    public InventarioAdministrador() {
        this(DAOInterfaceProducto.Actual());
    }

    public InventarioAdministrador(DAOInterfaceProducto dao) {
        this.dao = dao;
        setTitle(HelperUI.TITULO_APP + " - Inventario");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 640));

        JPanel raiz = new JPanel(new BorderLayout(16, 12));
        raiz.setBorder(BorderFactory.createEmptyBorder(18, 20, 14, 20));
        setContentPane(raiz);

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.add(HelperUI.Titulo("Inventario"), BorderLayout.NORTH);
        cabecera.add(HelperUI.Subtitulo("Base de datos: " + (Conexion.modoNube ? "Nube (Firebase)" : "Local (MySQL)")
                + "   ·   En rojo los productos con " + STOCK_BAJO + " unidades o menos"), BorderLayout.SOUTH);
        raiz.add(cabecera, BorderLayout.NORTH);

        raiz.add(panelTabla(), BorderLayout.CENTER);
        raiz.add(panelFormulario(), BorderLayout.EAST);

        JPanel pie = new JPanel(new BorderLayout());
        pie.add(lblResumen, BorderLayout.CENTER);
        JButton btnVolver = new JButton("Volver al menú");
        btnVolver.addActionListener(e -> volver());
        pie.add(btnVolver, BorderLayout.EAST);
        raiz.add(pie, BorderLayout.SOUTH);

        pack();
        cargar();
    }

    private JPanel panelTabla() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        JPanel barra = new JPanel(new BorderLayout());
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar por serial, nombre, marca o categoría");
        txtBuscar.putClientProperty("JTextField.showClearButton", true);
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filtrar(); }
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            public void changedUpdate(DocumentEvent e) { filtrar(); }
        });
        barra.add(txtBuscar);
        panel.add(barra, BorderLayout.NORTH);

        tabla.setRowSorter(sorter);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] anchos = {80, 320, 90, 115, 110, 60};
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        HelperUI.ColumnaMoneda(tabla, 4);
        tabla.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setHorizontalAlignment(CENTER);
                if (!sel) {
                    comp.setForeground(v instanceof Integer && (Integer) v <= STOCK_BAJO ? HelperUI.ALERTA : t.getForeground());
                }
                return comp;
            }
        });
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() >= 0) {
                mostrar(tabla.convertRowIndexToModel(tabla.getSelectedRow()));
            }
        });
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return panel;
    }

    private JPanel panelFormulario() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Producto"), BorderFactory.createEmptyBorder(6, 8, 8, 8)));
        cbCategoria.setEditable(true);
        txtPrecio.putClientProperty("JTextField.placeholderText", "Ej: 2499000");

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 0, 4, 0);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0;
        Object[][] campos = {{"Serial", txtSerial}, {"Nombre", txtNombre}, {"Marca", txtMarca},
            {"Categoría", cbCategoria}, {"Precio (COP)", txtPrecio}, {"Stock (unidades)", spStock}};
        for (Object[] campo : campos) {
            form.add(new JLabel((String) campo[0]), g);
            form.add((Component) campo[1], g);
        }

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        JButton btnNuevo = new JButton("Nuevo");
        btnNuevo.addActionListener(e -> limpiar());
        btnGuardar.addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> eliminar());
        btnEliminar.setForeground(HelperUI.ALERTA);
        botones.add(btnNuevo);
        botones.add(btnEliminar);
        botones.add(btnGuardar);
        g.insets = new Insets(14, 0, 0, 0);
        form.add(botones, g);
        g.weighty = 1;
        form.add(new JLabel(), g);

        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.add(form, BorderLayout.NORTH);
        contenedor.setPreferredSize(new Dimension(300, 0));
        return contenedor;
    }

    private void cargar() {
        lblResumen.setText("Cargando inventario...");
        new SwingWorker<List<Producto>, Void>() {
            long ms;

            @Override
            protected List<Producto> doInBackground() throws Exception {
                long inicio = System.currentTimeMillis();
                CatalogoInicial.SembrarSiVacio(dao);
                List<Producto> ls = dao.listar();
                ms = System.currentTimeMillis() - inicio;
                return ls;
            }

            @Override
            protected void done() {
                try {
                    llenar(get(), ms);
                } catch (Exception ex) {
                    lblResumen.setText("No se pudo cargar el inventario");
                    JOptionPane.showMessageDialog(InventarioAdministrador.this,
                            "No se pudo cargar el inventario: " + causa(ex), "Inventario", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void llenar(List<Producto> productos, long ms) {
        modelo.setRowCount(0);
        TreeSet<String> categorias = new TreeSet<>();
        long unidades = 0, valor = 0;
        int bajos = 0;
        for (Producto p : productos) {
            modelo.addRow(new Object[]{p.getSerial(), p.getNombre(), p.getMarca(), p.getCategoria(), p.getPrecio(), p.getStock()});
            categorias.add(p.getCategoria());
            unidades += p.getStock();
            valor += p.getPrecio() * p.getStock();
            if (p.getStock() <= STOCK_BAJO) {
                bajos++;
            }
        }
        Object seleccion = cbCategoria.getSelectedItem();
        cbCategoria.removeAllItems();
        categorias.forEach(cbCategoria::addItem);
        cbCategoria.setSelectedItem(seleccion);
        lblResumen.setText(productos.size() + " productos  ·  " + unidades + " unidades  ·  valor "
                + HelperUI.Moneda(valor) + "  ·  " + bajos + " con stock bajo  ·  cargado en " + ms + " ms");
    }

    private void filtrar() {
        String texto = txtBuscar.getText().trim();
        sorter.setRowFilter(texto.isEmpty() ? null : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(texto), 0, 1, 2, 3));
    }

    private void mostrar(int fila) {
        txtSerial.setText((String) modelo.getValueAt(fila, 0));
        txtSerial.setEditable(false);
        txtNombre.setText((String) modelo.getValueAt(fila, 1));
        txtMarca.setText((String) modelo.getValueAt(fila, 2));
        cbCategoria.setSelectedItem(modelo.getValueAt(fila, 3));
        txtPrecio.setText(String.valueOf(modelo.getValueAt(fila, 4)));
        spStock.setValue(modelo.getValueAt(fila, 5));
    }

    private void limpiar() {
        tabla.clearSelection();
        txtSerial.setEditable(true);
        for (JTextField t : new JTextField[]{txtSerial, txtNombre, txtMarca, txtPrecio}) {
            t.setText("");
        }
        cbCategoria.setSelectedItem("");
        spStock.setValue(0);
        txtSerial.requestFocus();
    }

    private void guardar() {
        long precio;
        try {
            precio = Long.parseLong(txtPrecio.getText().trim().replace(".", "").replace(",", ""));
        } catch (NumberFormatException ex) {
            aviso("Campo precio: escriba solo números (sin $ ni espacios)");
            return;
        }
        Object categoria = cbCategoria.getEditor().getItem();
        Producto p = new Producto(txtSerial.getText().trim().toUpperCase(), txtNombre.getText().trim(), txtMarca.getText().trim(),
                categoria == null ? "" : categoria.toString().trim(), precio, (Integer) spStock.getValue());
        String error = HelperValidacion.ValidarProducto(p);
        if (error != null) {
            aviso(error);
            return;
        }
        boolean nuevo = txtSerial.isEditable();
        ejecutar(() -> {
            // Fiabilidad: no se sobrescribe un producto existente al crear uno nuevo con el mismo serial.
            if (nuevo && dao.buscar(p.getSerial()) != null) {
                throw new IllegalStateException("Ya existe un producto con el serial " + p.getSerial());
            }
            dao.guardar(p);
        }, nuevo ? "Producto creado" : "Producto actualizado");
    }

    private void eliminar() {
        String serial = txtSerial.getText().trim();
        if (txtSerial.isEditable() || serial.isEmpty()) {
            aviso("Seleccione en la tabla el producto que desea eliminar");
            return;
        }
        int op = JOptionPane.showConfirmDialog(this, "¿Eliminar definitivamente el producto " + serial + " - "
                + txtNombre.getText() + "?\nEsta acción no se puede deshacer.", "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (op == JOptionPane.YES_OPTION) {
            ejecutar(() -> {
                if (!dao.eliminar(serial)) {
                    throw new IllegalStateException("El producto " + serial + " ya no existe");
                }
            }, "Producto eliminado");
        }
    }

    interface Operacion {
        void correr() throws Exception;
    }

    /** Ejecuta la operacion fuera del hilo de la interfaz y recarga la tabla al terminar. */
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
                    JOptionPane.showMessageDialog(InventarioAdministrador.this, exito);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(InventarioAdministrador.this, causa(ex), "Inventario", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    static String causa(Exception ex) {
        Throwable t = ex;
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t.getMessage();
    }

    private void aviso(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Revise el formulario", JOptionPane.WARNING_MESSAGE);
    }

    private void volver() {
        (Conexion.modoNube ? new MenuAdministrador() : new MenuLocal()).setVisible(true);
        dispose();
    }
}
