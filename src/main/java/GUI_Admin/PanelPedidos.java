package GUI_Admin;

import Helpers.ComponentesBento;
import Helpers.ComponentesBento.BotonPildora;
import Helpers.ComponentesBento.EtiquetaPildora;
import Helpers.ComponentesBento.TarjetaBento;
import Helpers.HelperUI;
import Helpers.TemaBento;
import Logica_Conexion.DAOInterfaceProducto;
import Logica_Negocio.ResumenPedido;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 * Visor de pedidos y ventas realizadas en estética Nordic Bento POS.
 * En modo administrador muestra todas las ventas con métricas; en modo cliente
 * muestra únicamente las compras del usuario autenticado.
 */
public class PanelPedidos extends JPanel {

    private final DAOInterfaceProducto dao;
    private final String filtroCliente;

    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"Pedido", "Fecha", "Cliente", "Tarjeta", "Autorización", "Total COP"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return c == 5 ? Long.class : String.class;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modelo);
    private final JTextField txtBuscar = ComponentesBento.CampoBusqueda(
            "Buscar por pedido, cliente, tarjeta o autorización...", 290, 34, this::filtrar);

    private final JLabel lblKpiCantidad = new JLabel("0 pedidos");
    private final JLabel lblKpiIngresos = new JLabel("$ 0 COP");
    private final JLabel lblKpiPromedio = new JLabel("$ 0 COP");
    private final JLabel lblResumen = HelperUI.Subtitulo(" ");

    private final JLabel lblDetalleTitulo = new JLabel("Seleccione un pedido");
    private final EtiquetaPildora pillEstado = new EtiquetaPildora(
            "Aprobado", TemaBento.EXITO_SUAVE, TemaBento.EXITO);
    private final JTextArea txtDetalle = new JTextArea();
    private List<ResumenPedido> cachePedidos = new ArrayList<>();

    public PanelPedidos(String filtroCliente) {
        this(DAOInterfaceProducto.Actual(), filtroCliente);
    }

    public PanelPedidos(DAOInterfaceProducto dao, String filtroCliente) {
        this.dao = dao;
        this.filtroCliente = filtroCliente;
        setOpaque(false);
        setLayout(new BorderLayout(0, 14));

        add(panelKPIs(), BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(new BorderLayout(14, 0));
        cuerpo.setOpaque(false);
        cuerpo.add(panelTabla(), BorderLayout.CENTER);
        cuerpo.add(panelDetalle(), BorderLayout.EAST);
        add(cuerpo, BorderLayout.CENTER);

        refrescar();
    }

    public void refrescar() {
        lblResumen.setText("Cargando pedidos...");
        new SwingWorker<List<ResumenPedido>, Void>() {
            @Override
            protected List<ResumenPedido> doInBackground() throws Exception {
                return filtroCliente == null
                        ? dao.listarPedidos()
                        : dao.listarPedidosPorCliente(filtroCliente);
            }

            @Override
            protected void done() {
                try {
                    llenar(get());
                } catch (Exception ex) {
                    lblResumen.setText("No se pudieron cargar los pedidos");
                    JOptionPane.showMessageDialog(PanelPedidos.this,
                            "Error al consultar pedidos: " + causa(ex),
                            "Pedidos", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private JPanel panelKPIs() {
        JPanel fila = new JPanel(new GridLayout(1, 3, 14, 0));
        fila.setOpaque(false);

        lblKpiCantidad.setFont(TemaBento.Fuente(Font.BOLD, 18));
        lblKpiCantidad.setForeground(TemaBento.TEXTO);
        lblKpiIngresos.setFont(TemaBento.Fuente(Font.BOLD, 20));
        lblKpiIngresos.setForeground(TemaBento.TEXTO);
        lblKpiPromedio.setFont(TemaBento.Fuente(Font.BOLD, 18));
        lblKpiPromedio.setForeground(TemaBento.TEAL);

        String titCant = filtroCliente == null ? "Pedidos Totales" : "Mis Compras";
        String titTotal = filtroCliente == null ? "Ingresos Acumulados" : "Total Invertido";
        fila.add(ComponentesBento.TarjetaKPI(titCant, lblKpiCantidad));
        fila.add(ComponentesBento.TarjetaKPI(titTotal, lblKpiIngresos));
        fila.add(ComponentesBento.TarjetaKPI("Ticket Promedio", lblKpiPromedio));
        return fila;
    }

    private JPanel panelTabla() {
        TarjetaBento card = new TarjetaBento();
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 14, 20));

        JPanel cabecera = new JPanel(new BorderLayout(12, 0));
        cabecera.setOpaque(false);
        JLabel tit = new JLabel(filtroCliente == null ? "Historial de Ventas" : "Mis Pedidos");
        tit.setFont(TemaBento.Fuente(Font.BOLD, 17));
        tit.setForeground(TemaBento.TEXTO);
        cabecera.add(tit, BorderLayout.WEST);

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controles.setOpaque(false);
        BotonPildora btnActualizar = ComponentesBento.BotonSuave("Actualizar");
        btnActualizar.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
        btnActualizar.addActionListener(e -> refrescar());
        controles.add(txtBuscar);
        controles.add(btnActualizar);
        cabecera.add(controles, BorderLayout.EAST);
        card.add(cabecera, BorderLayout.NORTH);

        tabla.setRowSorter(sorter);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setShowVerticalLines(false);
        tabla.setFillsViewportHeight(true);
        int[] anchos = {105, 145, 115, 130, 115, 120};
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        HelperUI.ColumnaMoneda(tabla, 5);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() >= 0) {
                mostrarDetalle(tabla.convertRowIndexToModel(tabla.getSelectedRow()));
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

    private JPanel panelDetalle() {
        TarjetaBento card = new TarjetaBento();
        card.setPreferredSize(new Dimension(360, 0));
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JPanel head = new JPanel(new BorderLayout(8, 0));
        head.setOpaque(false);
        lblDetalleTitulo.setFont(TemaBento.Fuente(Font.BOLD, 16));
        lblDetalleTitulo.setForeground(TemaBento.TEXTO);
        head.add(lblDetalleTitulo, BorderLayout.WEST);
        head.add(pillEstado, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);

        txtDetalle.setEditable(false);
        txtDetalle.setOpaque(false);
        txtDetalle.setLineWrap(true);
        txtDetalle.setWrapStyleWord(true);
        txtDetalle.setFont(TemaBento.Fuente(Font.PLAIN, 13));
        txtDetalle.setForeground(TemaBento.TEXTO);
        txtDetalle.setText("Haz clic en un pedido de la tabla para ver el comprobante y el detalle de artículos.");

        JScrollPane scroll = new JScrollPane(txtDetalle);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        card.add(scroll, BorderLayout.CENTER);

        JSeparator sep = new JSeparator();
        sep.setForeground(TemaBento.BORDE);
        JPanel pie = new JPanel(new BorderLayout(0, 6));
        pie.setOpaque(false);
        pie.add(sep, BorderLayout.NORTH);
        pie.add(HelperUI.Subtitulo("Comprobante sincronizado con la base de datos."), BorderLayout.SOUTH);
        card.add(pie, BorderLayout.SOUTH);
        return card;
    }

    private void llenar(List<ResumenPedido> pedidos) {
        cachePedidos = pedidos;
        modelo.setRowCount(0);
        long acumulado = 0;
        for (ResumenPedido r : pedidos) {
            String etiquetaCliente = r.clienteUid().isBlank() ? r.cliente() : r.cliente() + " (" + r.clienteUid() + ")";
            modelo.addRow(new Object[]{r.id(), r.fecha(), etiquetaCliente, r.tarjeta(), r.autorizacion(), r.total()});
            acumulado += r.total();
        }
        int n = pedidos.size();
        lblKpiCantidad.setText(n + (n == 1 ? " pedido" : " pedidos"));
        lblKpiIngresos.setText(HelperUI.Moneda(acumulado) + " COP");
        lblKpiPromedio.setText(HelperUI.Moneda(n == 0 ? 0 : acumulado / n) + " COP");
        lblResumen.setText(n + " pedido(s) encontrado(s)");
        if (!pedidos.isEmpty()) {
            tabla.setRowSelectionInterval(0, 0);
        }
    }

    private void mostrarDetalle(int fila) {
        if (fila < 0 || fila >= cachePedidos.size()) {
            return;
        }
        ResumenPedido r = cachePedidos.get(fila);
        lblDetalleTitulo.setText(r.id());
        StringBuilder sb = new StringBuilder();
        sb.append("Fecha: ").append(r.fecha()).append('\n');
        sb.append("Cliente: ").append(r.cliente());
        if (!r.clienteUid().isBlank()) {
            sb.append(" (UID ").append(r.clienteUid()).append(')');
        }
        sb.append("\nTarjeta: ").append(r.tarjeta()).append('\n');
        sb.append("Autorización: ").append(r.autorizacion()).append("\n\n");
        sb.append("Artículos:\n").append(r.detalle().isBlank() ? "(Sin detalle registrado)" : r.detalle());
        sb.append("\n\nTotal pagado: ").append(HelperUI.Moneda(r.total())).append(" COP");
        txtDetalle.setText(sb.toString());
        txtDetalle.setCaretPosition(0);
    }

    private void filtrar() {
        String q = txtBuscar.getText().trim();
        sorter.setRowFilter(q.isEmpty() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(q)));
    }

    private static String causa(Exception ex) {
        Throwable t = ex;
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t.getMessage();
    }
}
