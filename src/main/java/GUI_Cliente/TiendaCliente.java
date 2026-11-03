package GUI_Cliente;

import Helpers.HelperUI;
import Logica_Conexion.DAOInterfaceProducto;
import Logica_Negocio.Carrito;
import Logica_Negocio.CatalogoInicial;
import Logica_Negocio.Pedido;
import Logica_Negocio.Producto;
import Logica_Negocio.Usuario;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Pattern;
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
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 * Tienda del cliente: catalogo de articulos, carrito y paso a la pasarela de pagos.
 */
public class TiendaCliente extends JFrame {

    private static final String TODAS = "Todas las categorías";

    private final DAOInterfaceProducto dao;
    private final Carrito carrito = new Carrito();
    private List<Producto> productos = new ArrayList<>();

    private final DefaultTableModel modeloCatalogo = new DefaultTableModel(
            new Object[]{"Producto", "Marca", "Categoría", "Precio", "Disponibles"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return c == 3 ? Long.class : c == 4 ? Integer.class : String.class;
        }
    };
    private final JTable tablaCatalogo = new JTable(modeloCatalogo);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modeloCatalogo);
    private final JTextField txtBuscar = new JTextField();
    private final JComboBox<String> cbCategoria = new JComboBox<>();
    private final JSpinner spCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));

    private final DefaultTableModel modeloCarrito = new DefaultTableModel(new Object[]{"Producto", "Cant.", "Subtotal"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable tablaCarrito = new JTable(modeloCarrito);
    private final JLabel lblTotal = new JLabel();
    private final JLabel lblUnidades = new JLabel();
    private final JButton btnPagar = HelperUI.BotonPrimario("Ir a pagar");

    public TiendaCliente() {
        this(DAOInterfaceProducto.Actual());
    }

    public TiendaCliente(DAOInterfaceProducto dao) {
        this.dao = dao;
        setTitle(HelperUI.TITULO_APP + " - Tienda");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 640));

        JPanel raiz = new JPanel(new BorderLayout(16, 12));
        raiz.setBorder(BorderFactory.createEmptyBorder(18, 20, 14, 20));
        setContentPane(raiz);

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.add(HelperUI.Titulo("Artículos de computador"), BorderLayout.NORTH);
        String usuario = Usuario.sesion == null ? "cliente" : Usuario.sesion.getUsu();
        cabecera.add(HelperUI.Subtitulo("Hola, " + usuario + ". Elija sus productos y agréguelos al carrito (doble clic agrega 1)."),
                BorderLayout.SOUTH);
        raiz.add(cabecera, BorderLayout.NORTH);
        raiz.add(panelCatalogo(), BorderLayout.CENTER);
        raiz.add(panelCarrito(), BorderLayout.EAST);

        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        JButton btnVolver = new JButton("Volver al menú");
        btnVolver.addActionListener(e -> {
            new MenuCliente().setVisible(true);
            dispose();
        });
        pie.add(btnVolver);
        raiz.add(pie, BorderLayout.SOUTH);

        refrescarCarrito();
        pack();
        cargar();
    }

    private JPanel panelCatalogo() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        JPanel filtros = new JPanel(new BorderLayout(8, 0));
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar producto o marca");
        txtBuscar.putClientProperty("JTextField.showClearButton", true);
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filtrar(); }
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            public void changedUpdate(DocumentEvent e) { filtrar(); }
        });
        cbCategoria.setPrototypeDisplayValue(TODAS + "    ");
        cbCategoria.addActionListener(e -> filtrar());
        filtros.add(txtBuscar, BorderLayout.CENTER);
        filtros.add(cbCategoria, BorderLayout.EAST);
        panel.add(filtros, BorderLayout.NORTH);

        tablaCatalogo.setRowSorter(sorter);
        tablaCatalogo.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] anchos = {330, 90, 115, 110, 80};
        for (int i = 0; i < anchos.length; i++) {
            tablaCatalogo.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        HelperUI.ColumnaMoneda(tablaCatalogo, 3);
        tablaCatalogo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    agregar(1);
                }
            }
        });
        panel.add(new JScrollPane(tablaCatalogo), BorderLayout.CENTER);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton btnAgregar = HelperUI.BotonPrimario("Agregar al carrito");
        btnAgregar.addActionListener(e -> agregar((Integer) spCantidad.getValue()));
        acciones.add(new JLabel("Cantidad"));
        acciones.add(spCantidad);
        acciones.add(btnAgregar);
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel panelCarrito() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Su carrito"), BorderFactory.createEmptyBorder(6, 8, 8, 8)));
        panel.setPreferredSize(new Dimension(380, 0));

        tablaCarrito.getColumnModel().getColumn(0).setPreferredWidth(200);
        tablaCarrito.getColumnModel().getColumn(1).setPreferredWidth(40);
        HelperUI.ColumnaMoneda(tablaCarrito, 2);
        panel.add(new JScrollPane(tablaCarrito), BorderLayout.CENTER);

        JPanel abajo = new JPanel(new BorderLayout(0, 10));
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JButton btnQuitar = new JButton("Quitar");
        btnQuitar.addActionListener(e -> quitar());
        JButton btnVaciar = new JButton("Vaciar");
        btnVaciar.addActionListener(e -> {
            carrito.Vaciar();
            refrescarCarrito();
        });
        botones.add(btnQuitar);
        botones.add(btnVaciar);
        abajo.add(botones, BorderLayout.NORTH);

        JPanel totales = new JPanel(new BorderLayout());
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 20));
        totales.add(lblUnidades, BorderLayout.NORTH);
        totales.add(lblTotal, BorderLayout.CENTER);
        abajo.add(totales, BorderLayout.CENTER);

        btnPagar.setPreferredSize(new Dimension(0, 40));
        btnPagar.addActionListener(e -> pagar());
        abajo.add(btnPagar, BorderLayout.SOUTH);
        panel.add(abajo, BorderLayout.SOUTH);
        return panel;
    }

    private void cargar() {
        new SwingWorker<List<Producto>, Void>() {
            @Override
            protected List<Producto> doInBackground() throws Exception {
                CatalogoInicial.SembrarSiVacio(dao);
                return dao.listar();
            }

            @Override
            protected void done() {
                try {
                    productos = get();
                    llenarCatalogo();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(TiendaCliente.this, "No se pudo cargar el catálogo: " + ex.getMessage(),
                            "Tienda", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void llenarCatalogo() {
        modeloCatalogo.setRowCount(0);
        TreeSet<String> categorias = new TreeSet<>();
        for (Producto p : productos) {
            modeloCatalogo.addRow(new Object[]{p.getNombre(), p.getMarca(), p.getCategoria(), p.getPrecio(), p.getStock()});
            categorias.add(p.getCategoria());
        }
        cbCategoria.removeAllItems();
        cbCategoria.addItem(TODAS);
        categorias.forEach(cbCategoria::addItem);
    }

    private void filtrar() {
        List<RowFilter<Object, Object>> filtros = new ArrayList<>();
        String texto = txtBuscar.getText().trim();
        if (!texto.isEmpty()) {
            filtros.add(RowFilter.regexFilter("(?i)" + Pattern.quote(texto), 0, 1));
        }
        Object cat = cbCategoria.getSelectedItem();
        if (cat != null && !TODAS.equals(cat)) {
            filtros.add(RowFilter.regexFilter("^" + Pattern.quote(cat.toString()) + "$", 2));
        }
        sorter.setRowFilter(filtros.isEmpty() ? null : RowFilter.andFilter(filtros));
    }

    private void agregar(int cantidad) {
        int fila = tablaCatalogo.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto del catálogo", "Tienda", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Producto p = productos.get(tablaCatalogo.convertRowIndexToModel(fila));
        String error = carrito.Agregar(p, cantidad);
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Stock insuficiente", JOptionPane.WARNING_MESSAGE);
            return;
        }
        refrescarCarrito();
    }

    private void quitar() {
        int fila = tablaCarrito.getSelectedRow();
        if (fila >= 0) {
            carrito.Quitar(carrito.Items().get(fila).producto.getSerial());
            refrescarCarrito();
        }
    }

    private void refrescarCarrito() {
        modeloCarrito.setRowCount(0);
        for (Carrito.Item i : carrito.Items()) {
            modeloCarrito.addRow(new Object[]{i.producto.getNombre(), i.cantidad, i.Subtotal()});
        }
        lblUnidades.setText(carrito.Unidades() + " artículo(s)");
        lblTotal.setText("Total: " + HelperUI.Moneda(carrito.Total()));
        btnPagar.setEnabled(!carrito.EstaVacio());
    }

    private void pagar() {
        Pedido pedido = PasarelaPago.Mostrar(this, carrito, dao);
        if (pedido != null) {
            carrito.Vaciar();
            refrescarCarrito();
            cargar();
        }
    }
}
