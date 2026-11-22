package GUI_Admin;

import Helpers.ComponentesBento;
import Helpers.ComponentesBento.BotonPildora;
import Helpers.ComponentesBento.EtiquetaPildora;
import Helpers.ComponentesBento.TarjetaBento;
import Helpers.HelperAuditoria;
import Helpers.HelperUI;
import Helpers.HelperValidacion;
import Helpers.TemaBento;
import Logica_Conexion.DAOInterfaceProducto;
import Logica_Negocio.CatalogoInicial;
import Logica_Negocio.Producto;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 * Inventario administrativo en estética Nordic Bento POS: tarjetas KPI, tabla
 * con píldoras de categoría/stock y ficha lateral para crear, editar o eliminar.
 */
public class InventarioAdministrador extends JPanel {

    static final int STOCK_BAJO = 3;

    private final DAOInterfaceProducto dao;
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"Serial", "Producto", "Marca", "Categoría", "Precio COP", "Stock"}, 0) {
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
    private final JTextField txtBuscar = ComponentesBento.CampoBusqueda(
            "Buscar por nombre, serial o marca...", 280, 34, this::filtrar);

    private final JLabel lblKpiCatalogo = new JLabel("0 artículos · 0 unidades");
    private final JLabel lblKpiValor = new JLabel("$ 0 COP");
    private final EtiquetaPildora pillKpiAlerta = new EtiquetaPildora(
            "0 productos (<= 3 uds)", TemaBento.TERRACOTA, Color.WHITE);
    private final JLabel lblTiempo = HelperUI.Subtitulo(" ");

    private final JTextField txtSerial = new JTextField();
    private final JTextField txtNombre = new JTextField();
    private final JTextField txtMarca = new JTextField();
    private final JComboBox<String> cbCategoria = new JComboBox<>();
    private final JTextField txtPrecio = new JTextField();
    private final JSpinner spStock = new JSpinner(new SpinnerNumberModel(0, 0, 99999, 1));

    private final BotonPildora btnGuardar = ComponentesBento.BotonTeal("Guardar");
    private final BotonPildora btnEliminar = ComponentesBento.BotonPeligroTexto("Eliminar");

    public InventarioAdministrador() {
        this(DAOInterfaceProducto.Actual());
    }

    public InventarioAdministrador(DAOInterfaceProducto dao) {
        this.dao = dao;
        setOpaque(false);
        setLayout(new BorderLayout(0, 14));

        add(panelKPIs(), BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(new BorderLayout(14, 0));
        cuerpo.setOpaque(false);
        cuerpo.add(panelTabla(), BorderLayout.CENTER);
        cuerpo.add(panelFormulario(), BorderLayout.EAST);
        add(cuerpo, BorderLayout.CENTER);

        cargar();
    }

    public void refrescar() {
        cargar();
    }

    private JPanel panelKPIs() {
        JPanel fila = new JPanel(new GridLayout(1, 3, 14, 0));
        fila.setOpaque(false);

        lblKpiCatalogo.setFont(TemaBento.Fuente(Font.BOLD, 18));
        lblKpiCatalogo.setForeground(TemaBento.TEXTO);

        lblKpiValor.setFont(TemaBento.Fuente(Font.BOLD, 20));
        lblKpiValor.setForeground(TemaBento.TEXTO);

        pillKpiAlerta.setFont(TemaBento.Fuente(Font.BOLD, 14));
        pillKpiAlerta.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));

        fila.add(ComponentesBento.TarjetaKPI("Catálogo Activo", lblKpiCatalogo));
        fila.add(ComponentesBento.TarjetaKPI("Valor en Bodega", lblKpiValor));
        fila.add(ComponentesBento.TarjetaKPI("Reposición Urgente", pillKpiAlerta));
        return fila;
    }

    private JPanel panelTabla() {
        TarjetaBento card = new TarjetaBento();
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 14, 20));

        JPanel cabecera = new JPanel(new BorderLayout(14, 0));
        cabecera.setOpaque(false);
        JLabel tit = new JLabel("Inventario de Productos");
        tit.setFont(TemaBento.Fuente(Font.BOLD, 17));
        tit.setForeground(TemaBento.TEXTO);
        cabecera.add(tit, BorderLayout.WEST);
        cabecera.add(txtBuscar, BorderLayout.EAST);
        card.add(cabecera, BorderLayout.NORTH);

        tabla.setRowSorter(sorter);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setShowVerticalLines(false);
        tabla.setFillsViewportHeight(true);
        int[] anchos = {88, 260, 95, 115, 115, 86};
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        HelperUI.ColumnaPildoraCategoria(tabla, 3);
        HelperUI.ColumnaMoneda(tabla, 4);
        tabla.getColumnModel().getColumn(5).setCellRenderer(new RenderizadorStock());

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
        pie.add(lblTiempo);
        card.add(pie, BorderLayout.SOUTH);
        return card;
    }

    private JPanel panelFormulario() {
        TarjetaBento card = new TarjetaBento();
        card.setPreferredSize(new Dimension(370, 0));
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel tit = new JLabel("Ficha de Producto");
        tit.setFont(TemaBento.Fuente(Font.BOLD, 17));
        tit.setForeground(TemaBento.TEXTO);
        card.add(tit, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        cbCategoria.setEditable(true);
        txtPrecio.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ej: 2499000");
        txtSerial.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ej: LAP-004");

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 0, 6, 8);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        Object[][] campos = {
            {"Serial", txtSerial},
            {"Nombre", txtNombre},
            {"Marca", txtMarca},
            {"Categoría", cbCategoria},
            {"Precio COP", txtPrecio},
            {"Stock", spStock}
        };
        for (int i = 0; i < campos.length; i++) {
            g.gridy = i;
            g.gridx = 0;
            g.weightx = 0.34;
            JLabel lbl = new JLabel((String) campos[i][0]);
            lbl.setFont(TemaBento.Fuente(Font.PLAIN, 13));
            lbl.setForeground(TemaBento.TEXTO_SECUNDARIO);
            form.add(lbl, g);

            g.gridx = 1;
            g.weightx = 0.66;
            Component comp = (Component) campos[i][1];
            comp.setPreferredSize(new Dimension(180, 34));
            form.add(comp, g);
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

    private void cargar() {
        lblTiempo.setText("Cargando inventario...");
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
                    lblTiempo.setText("No se pudo cargar el inventario");
                    JOptionPane.showMessageDialog(InventarioAdministrador.this,
                            "No se pudo cargar el inventario: " + causa(ex), "Inventario", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void llenar(List<Producto> productos, long ms) {
        modelo.setRowCount(0);
        TreeSet<String> categorias = new TreeSet<>();
        long unidades = 0;
        long valor = 0;
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

        lblKpiCatalogo.setText(productos.size() + " artículos · " + unidades + " unidades");
        lblKpiValor.setText(HelperUI.Moneda(valor) + " COP");
        if (bajos == 0) {
            pillKpiAlerta.setText("Stock saludable");
            pillKpiAlerta.setColores(TemaBento.EXITO, Color.WHITE);
        } else {
            pillKpiAlerta.setText(bajos + (bajos == 1 ? " producto (<= " : " productos (<= ") + STOCK_BAJO + " uds)");
            pillKpiAlerta.setColores(TemaBento.TERRACOTA, Color.WHITE);
        }
        lblTiempo.setText("Sincronizado en " + ms + " ms · Doble clic o selección para editar");
    }

    private void filtrar() {
        String texto = txtBuscar.getText().trim();
        sorter.setRowFilter(texto.isEmpty() ? null
                : RowFilter.regexFilter("(?i)" + Pattern.quote(texto), 0, 1, 2, 3));
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
            precio = Long.parseLong(txtPrecio.getText().trim().replace(".", "").replace(",", "").replace("$", "").trim());
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
            if (nuevo && dao.buscar(p.getSerial()) != null) {
                throw new IllegalStateException("Ya existe un producto con el serial " + p.getSerial());
            }
            dao.guardar(p);
            HelperAuditoria.Registrar(nuevo ? "CREAR_PRODUCTO" : "ACTUALIZAR_PRODUCTO",
                    "Serial=" + p.getSerial() + " precio=" + p.getPrecio() + " stock=" + p.getStock());
        }, nuevo ? "Producto creado" : "Producto actualizado");
    }

    private void eliminar() {
        String serial = txtSerial.getText().trim();
        if (txtSerial.isEditable() || serial.isEmpty()) {
            aviso("Seleccione en la tabla el producto que desea eliminar");
            return;
        }
        int op = JOptionPane.showConfirmDialog(this,
                "¿Eliminar definitivamente el producto " + serial + " - " + txtNombre.getText()
                        + "?\nEsta acción no se puede deshacer.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (op == JOptionPane.YES_OPTION) {
            ejecutar(() -> {
                if (!dao.eliminar(serial)) {
                    throw new IllegalStateException("El producto " + serial + " ya no existe");
                }
                HelperAuditoria.Registrar("ELIMINAR_PRODUCTO", "Serial=" + serial);
            }, "Producto eliminado");
        }
    }

    interface Operacion {
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
                    JOptionPane.showMessageDialog(InventarioAdministrador.this, exito);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(InventarioAdministrador.this, causa(ex),
                            "Inventario", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private static String causa(Exception ex) {
        Throwable t = ex;
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t.getMessage();
    }

    private void aviso(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Revise el formulario", JOptionPane.WARNING_MESSAGE);
    }

    private static class RenderizadorStock extends DefaultTableCellRenderer {

        private final EtiquetaPildora pildora = new EtiquetaPildora("", TemaBento.EXITO, Color.WHITE);
        private final JPanel contenedor = new JPanel(new GridBagLayout());

        RenderizadorStock() {
            pildora.setFont(TemaBento.Fuente(Font.BOLD, 11));
            contenedor.add(pildora);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            contenedor.setBackground(sel ? t.getSelectionBackground() : t.getBackground());
            int stock = v instanceof Number ? ((Number) v).intValue() : 0;
            pildora.setText(stock + " uds");
            if (stock == 0) {
                pildora.setColores(TemaBento.PELIGRO, Color.WHITE);
            } else if (stock <= STOCK_BAJO) {
                pildora.setColores(TemaBento.TERRACOTA, Color.WHITE);
            } else {
                pildora.setColores(TemaBento.EXITO, Color.WHITE);
            }
            return contenedor;
        }
    }
}
