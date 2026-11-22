package GUI_Local;

import GUI_Admin.InventarioAdministrador;
import GUI_Admin.PanelAuditoria;
import GUI_Admin.PanelClientes;
import GUI_Admin.PanelPedidos;
import GUI_Cliente.TiendaCliente;
import Helpers.ComponentesBento;
import Helpers.ComponentesBento.BotonPildora;
import Helpers.ComponentesBento.EtiquetaPildora;
import Helpers.ComponentesBento.SelectorSegmentado;
import Helpers.ComponentesBento.TarjetaBento;
import Helpers.HelperUI;
import Helpers.TemaBento;
import Logica_Conexion.Conexion;
import Logica_Negocio.Administrador;
import Logica_Negocio.Usuario;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Contenedor principal unificado en estilo Nordic Bento POS.
 * Separa claramente los módulos de Administrador (Inventario, Pedidos, Clientes,
 * Auditoría) y Cliente comprador (Tienda, Mis Pedidos).
 */
public class VentanaPrincipal extends JFrame {

    private final Usuario usuario;
    private final CardLayout tarjetas = new CardLayout();
    private final JPanel cuerpo = new JPanel(tarjetas);

    private TiendaCliente panelTienda;
    private InventarioAdministrador panelInventario;
    private PanelPedidos panelPedidos;
    private PanelClientes panelClientes;
    private PanelAuditoria panelAuditoria;

    public VentanaPrincipal(Usuario usuario) {
        this.usuario = usuario;
        boolean esAdmin = usuario instanceof Administrador;

        setTitle(HelperUI.TITULO_APP + " — " + (esAdmin ? "Administración" : "Tienda Electrónica"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 700));

        JPanel raiz = new JPanel(new BorderLayout(0, 14));
        raiz.setBackground(TemaBento.FONDO_PIEDRA);
        raiz.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        setContentPane(raiz);

        cuerpo.setOpaque(false);
        String[] opciones = construirModulos(esAdmin);

        raiz.add(crearBarraSuperior(esAdmin, opciones), BorderLayout.NORTH);
        raiz.add(cuerpo, BorderLayout.CENTER);

        pack();
        setSize(new Dimension(1260, 740));
        setLocationRelativeTo(null);
    }

    private String[] construirModulos(boolean esAdmin) {
        if (esAdmin) {
            panelInventario = new InventarioAdministrador();
            panelPedidos = new PanelPedidos(null);
            panelClientes = new PanelClientes();
            panelAuditoria = new PanelAuditoria();
            cuerpo.add(panelInventario, "Inventario");
            cuerpo.add(panelPedidos, "Pedidos");
            cuerpo.add(panelClientes, "Clientes");
            cuerpo.add(panelAuditoria, "Auditoría");
            return new String[]{"Inventario", "Pedidos", "Clientes", "Auditoría"};
        }
        String nombreCliente = usuario == null ? "cliente" : usuario.getUsu();
        panelTienda = new TiendaCliente();
        panelPedidos = new PanelPedidos(nombreCliente);
        cuerpo.add(panelTienda, "Tienda");
        cuerpo.add(panelPedidos, "Mis Pedidos");
        return new String[]{"Tienda", "Mis Pedidos"};
    }

    private JPanel crearBarraSuperior(boolean esAdmin, String[] opciones) {
        TarjetaBento barra = new TarjetaBento();
        barra.setLayout(new BorderLayout(16, 0));
        barra.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JPanel marca = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        marca.setOpaque(false);
        JLabel icono = new JLabel(ComponentesBento.IconoMarca(34, TemaBento.TEAL, Color.WHITE));
        JLabel nombre = new JLabel(HelperUI.TITULO_APP);
        nombre.setFont(TemaBento.Fuente(Font.BOLD, 18));
        nombre.setForeground(TemaBento.TEXTO);
        JLabel rol = new JLabel(esAdmin ? "Administración" : "Tienda Electrónica");
        rol.setFont(TemaBento.Fuente(Font.PLAIN, 13));
        rol.setForeground(TemaBento.TEXTO_SECUNDARIO);
        marca.add(icono);
        marca.add(nombre);
        marca.add(rol);
        barra.add(marca, BorderLayout.WEST);

        JPanel centro = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centro.setOpaque(false);
        SelectorSegmentado nav = new SelectorSegmentado(opciones, this::cambiarModulo);
        centro.add(nav);
        barra.add(centro, BorderLayout.CENTER);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        derecha.setOpaque(false);

        EtiquetaPildora pillBD = new EtiquetaPildora(
                Conexion.modoNube ? "Nube · Firebase" : "Local · MySQL",
                Conexion.modoNube ? TemaBento.NUBE_SUAVE : TemaBento.TEAL_SUAVE,
                Conexion.modoNube ? TemaBento.NUBE_AZUL : TemaBento.TEAL);
        pillBD.setIcon(ComponentesBento.PuntoEstado(
                Conexion.modoNube ? TemaBento.NUBE_AZUL : TemaBento.EXITO, 8));
        pillBD.setIconTextGap(6);
        pillBD.setFont(TemaBento.Fuente(Font.BOLD, 12));
        pillBD.setBorder(BorderFactory.createEmptyBorder(7, 12, 7, 12));

        String nomUsu = usuario == null ? "Usuario" : usuario.getUsu();
        BotonPildora btnSalir = ComponentesBento.BotonSuave(nomUsu + " · Salir");
        btnSalir.setFont(TemaBento.Fuente(Font.BOLD, 12));
        btnSalir.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
        btnSalir.addActionListener(e -> cerrarSesion());

        derecha.add(pillBD);
        derecha.add(btnSalir);
        barra.add(derecha, BorderLayout.EAST);
        return barra;
    }

    private void cambiarModulo(String nombre) {
        tarjetas.show(cuerpo, nombre);
        if ("Inventario".equals(nombre) && panelInventario != null) {
            panelInventario.refrescar();
        } else if (("Pedidos".equals(nombre) || "Mis Pedidos".equals(nombre)) && panelPedidos != null) {
            panelPedidos.refrescar();
        } else if ("Clientes".equals(nombre) && panelClientes != null) {
            panelClientes.refrescar();
        } else if ("Auditoría".equals(nombre) && panelAuditoria != null) {
            panelAuditoria.refrescar();
        } else if ("Tienda".equals(nombre) && panelTienda != null) {
            panelTienda.refrescar();
        }
    }

    private void cerrarSesion() {
        int op = JOptionPane.showConfirmDialog(this,
                "¿Cerrar la sesión actual y volver al acceso?",
                "Cerrar sesión", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (op == JOptionPane.YES_OPTION) {
            Usuario.sesion = null;
            new VentanaAcceso().setVisible(true);
            dispose();
        }
    }
}
