package GUI_Admin;

import Helpers.ComponentesBento;
import Helpers.ComponentesBento.BotonPildora;
import Helpers.ComponentesBento.EtiquetaPildora;
import Helpers.ComponentesBento.TarjetaBento;
import Helpers.HelperUI;
import Helpers.LectorAuditoria;
import Helpers.TemaBento;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 * Visor de registro de auditoría (auditoria.log) en estética Nordic Bento POS.
 */
public class PanelAuditoria extends JPanel {

    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"Fecha y hora", "Usuario", "Acción", "Detalle"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modelo);
    private final JTextField txtBuscar = ComponentesBento.CampoBusqueda(
            "Filtrar por usuario, acción o detalle...", 280, 34, this::filtrar);
    private final JLabel lblResumen = HelperUI.Subtitulo(" ");

    public PanelAuditoria() {
        setOpaque(false);
        setLayout(new BorderLayout());

        TarjetaBento card = new TarjetaBento();
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 14, 20));

        JPanel cabecera = new JPanel(new BorderLayout(12, 0));
        cabecera.setOpaque(false);
        JPanel titulos = new JPanel(new BorderLayout(0, 2));
        titulos.setOpaque(false);
        JLabel tit = new JLabel("Registro de Auditoría");
        tit.setFont(TemaBento.Fuente(Font.BOLD, 18));
        tit.setForeground(TemaBento.TEXTO);
        titulos.add(tit, BorderLayout.NORTH);
        titulos.add(HelperUI.Subtitulo("Historial de ventas, cambios de inventario y gestión de clientes"), BorderLayout.SOUTH);
        cabecera.add(titulos, BorderLayout.WEST);

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
        tabla.setShowVerticalLines(false);
        tabla.setFillsViewportHeight(true);
        int[] anchos = {155, 110, 185, 520};
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        tabla.getColumnModel().getColumn(2).setCellRenderer(new RenderizadorAccion());

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(TemaBento.TARJETA);
        card.add(scroll, BorderLayout.CENTER);

        JPanel pie = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pie.setOpaque(false);
        pie.add(lblResumen);
        card.add(pie, BorderLayout.SOUTH);

        add(card, BorderLayout.CENTER);
        refrescar();
    }

    public void refrescar() {
        modelo.setRowCount(0);
        List<LectorAuditoria.Registro> registros = LectorAuditoria.LeerRecientes();
        for (LectorAuditoria.Registro r : registros) {
            modelo.addRow(new Object[]{r.fecha(), r.usuario(), r.accion(), r.detalle()});
        }
        lblResumen.setText(registros.size() + " evento(s) registrado(s) en auditoria.log");
    }

    private void filtrar() {
        String q = txtBuscar.getText().trim();
        sorter.setRowFilter(q.isEmpty() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(q)));
    }

    private static class RenderizadorAccion extends DefaultTableCellRenderer {

        private final EtiquetaPildora pildora = new EtiquetaPildora("", TemaBento.SUPERFICIE_SUAVE, TemaBento.TEXTO);
        private final JPanel contenedor = new JPanel(new GridBagLayout());

        RenderizadorAccion() {
            pildora.setFont(TemaBento.Fuente(Font.BOLD, 11));
            contenedor.add(pildora);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            contenedor.setBackground(sel ? t.getSelectionBackground() : t.getBackground());
            String accion = v == null ? "" : v.toString();
            pildora.setText(accion);
            if (accion.startsWith("VENTA_SIN")) {
                pildora.setColores(TemaBento.TERRACOTA_SUAVE, TemaBento.TERRACOTA);
            } else if (accion.startsWith("VENTA")) {
                pildora.setColores(TemaBento.EXITO_SUAVE, TemaBento.EXITO);
            } else if (accion.startsWith("ELIMINAR")) {
                pildora.setColores(TemaBento.PELIGRO_SUAVE, TemaBento.PELIGRO);
            } else if (accion.startsWith("CREAR")) {
                pildora.setColores(TemaBento.TEAL_SUAVE, TemaBento.TEAL);
            } else {
                pildora.setColores(TemaBento.SUPERFICIE_SUAVE, TemaBento.TEXTO);
            }
            return contenedor;
        }
    }
}
