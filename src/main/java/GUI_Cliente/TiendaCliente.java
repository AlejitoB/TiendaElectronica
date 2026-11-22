package GUI_Cliente;

import Helpers.ComponentesBento;
import Helpers.ComponentesBento.BotonPildora;
import Helpers.ComponentesBento.EtiquetaPildora;
import Helpers.ComponentesBento.TarjetaBento;
import Helpers.HelperUI;
import Helpers.TemaBento;
import Logica_Conexion.DAOInterfaceProducto;
import Logica_Negocio.Carrito;
import Logica_Negocio.CatalogoInicial;
import Logica_Negocio.Pedido;
import Logica_Negocio.Producto;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * Vista de tienda para el cliente en estilo Nordic Bento POS: búsqueda, filtros
 * por categoría, tarjetas de producto y ticket interactivo de carrito.
 */
public class TiendaCliente extends JPanel {

    private static final String TODAS = "Todo";

    private final DAOInterfaceProducto dao;
    private final Carrito carrito = new Carrito();
    private List<Producto> productos = new ArrayList<>();
    private String categoriaActiva = TODAS;

    private final JTextField txtBuscar = ComponentesBento.CampoBusqueda(
            "Buscar producto por nombre, marca o serial...", 0, 40, this::renderizarProductos);
    private final JPanel panelCategorias = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
    private final JPanel gridProductos = new JPanel(new GridLayout(0, 2, 12, 12));
    private final JLabel lblEstadoCatalogo = HelperUI.Subtitulo("Cargando catálogo...");

    private final JPanel listaCarrito = new JPanel();
    private final EtiquetaPildora pillArticulos = new EtiquetaPildora(
            "0 artículos", TemaBento.SUPERFICIE_SUAVE, TemaBento.TEXTO_SECUNDARIO);
    private final JButton btnVaciar = ComponentesBento.BotonPeligroTexto("Vaciar");
    private final JLabel lblSubtotal = new JLabel("$ 0");
    private final JLabel lblTotal = new JLabel("$ 0");
    private final BotonPildora btnPagar = ComponentesBento.BotonCarbon("Finalizar y Pagar");

    public TiendaCliente() {
        this(DAOInterfaceProducto.Actual());
    }

    public TiendaCliente(DAOInterfaceProducto dao) {
        this.dao = dao;
        setOpaque(false);
        setLayout(new BorderLayout(16, 0));

        add(panelCatalogo(), BorderLayout.CENTER);
        add(panelCarrito(), BorderLayout.EAST);

        refrescarCarrito();
        cargar();
    }

    public void refrescar() {
        cargar();
    }

    private JPanel panelCatalogo() {
        JPanel cont = new JPanel(new BorderLayout(0, 12));
        cont.setOpaque(false);

        // Barra superior de búsqueda y filtros por categoría
        JPanel barra = new JPanel(new BorderLayout(0, 8));
        barra.setOpaque(false);

        txtBuscar.setBackground(TemaBento.TARJETA);
        barra.add(txtBuscar, BorderLayout.NORTH);

        panelCategorias.setOpaque(false);
        barra.add(panelCategorias, BorderLayout.CENTER);
        cont.add(barra, BorderLayout.NORTH);

        // Grilla de tarjetas dentro de scroll limpio
        gridProductos.setOpaque(false);
        JPanel envoltura = new JPanel(new BorderLayout());
        envoltura.setOpaque(false);
        envoltura.add(gridProductos, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(envoltura);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        cont.add(scroll, BorderLayout.CENTER);

        JPanel pie = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pie.setOpaque(false);
        pie.add(lblEstadoCatalogo);
        cont.add(pie, BorderLayout.SOUTH);

        return cont;
    }

    private JPanel panelCarrito() {
        TarjetaBento card = new TarjetaBento();
        card.setPreferredSize(new Dimension(360, 0));
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        // Cabecera tipo ticket
        JPanel head = new JPanel(new BorderLayout(8, 0));
        head.setOpaque(false);
        JPanel titulos = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titulos.setOpaque(false);
        JLabel lblTit = new JLabel("Resumen de Compra");
        lblTit.setFont(TemaBento.Fuente(Font.BOLD, 17));
        lblTit.setForeground(TemaBento.TEXTO);
        titulos.add(lblTit);
        titulos.add(pillArticulos);
        head.add(titulos, BorderLayout.WEST);

        btnVaciar.setFont(TemaBento.Fuente(Font.BOLD, 11));
        btnVaciar.addActionListener(e -> {
            carrito.Vaciar();
            refrescarCarrito();
        });
        head.add(btnVaciar, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);

        // Lista dinámica de ítems del carrito
        listaCarrito.setOpaque(false);
        listaCarrito.setLayout(new BoxLayout(listaCarrito, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(listaCarrito);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        card.add(scroll, BorderLayout.CENTER);

        // Totales + CTA
        JPanel abajo = new JPanel(new BorderLayout(0, 12));
        abajo.setOpaque(false);

        JPanel resumenMontos = new JPanel(new GridBagLayout());
        resumenMontos.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 0, 4, 0);

        JSeparator sep1 = new JSeparator();
        sep1.setForeground(TemaBento.BORDE);
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2; g.weightx = 1;
        resumenMontos.add(sep1, g);

        agregarFilaMonto(resumenMontos, g, 1, "Subtotal", lblSubtotal, false);
        JLabel lblIva = new JLabel("$ 0");
        agregarFilaMonto(resumenMontos, g, 2, "IVA (0%)", lblIva, false);

        JSeparator sep2 = new JSeparator();
        sep2.setForeground(TemaBento.BORDE);
        g.gridx = 0; g.gridy = 3; g.gridwidth = 2;
        resumenMontos.add(sep2, g);

        agregarFilaMonto(resumenMontos, g, 4, "Total", lblTotal, true);
        abajo.add(resumenMontos, BorderLayout.CENTER);

        btnPagar.setPreferredSize(new Dimension(0, 44));
        btnPagar.setFont(TemaBento.Fuente(Font.BOLD, 14));
        btnPagar.setIcon(ComponentesBento.IconoFlechaDerecha(15, Color.WHITE));
        btnPagar.setHorizontalTextPosition(SwingConstants.LEFT);
        btnPagar.setIconTextGap(8);
        btnPagar.addActionListener(e -> pagar());
        abajo.add(btnPagar, BorderLayout.SOUTH);

        card.add(abajo, BorderLayout.SOUTH);
        return card;
    }

    private void agregarFilaMonto(JPanel p, GridBagConstraints g, int fila, String etiqueta, JLabel valor, boolean destacado) {
        g.gridwidth = 1;
        g.gridy = fila;
        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(TemaBento.Fuente(destacado ? Font.BOLD : Font.PLAIN, destacado ? 16 : 13));
        lbl.setForeground(destacado ? TemaBento.TEXTO : TemaBento.TEXTO_SECUNDARIO);
        g.gridx = 0; g.weightx = 0.5;
        p.add(lbl, g);

        valor.setHorizontalAlignment(SwingConstants.RIGHT);
        valor.setFont(TemaBento.Fuente(Font.BOLD, destacado ? 20 : 13));
        valor.setForeground(TemaBento.TEXTO);
        g.gridx = 1; g.weightx = 0.5;
        p.add(valor, g);
    }

    private void cargar() {
        lblEstadoCatalogo.setText("Cargando catálogo...");
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
                    reconstruirCategorias();
                    renderizarProductos();
                } catch (Exception ex) {
                    lblEstadoCatalogo.setText("Error al cargar el catálogo");
                    JOptionPane.showMessageDialog(TiendaCliente.this,
                            "No se pudo cargar el catálogo: " + ex.getMessage(),
                            "Tienda", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void reconstruirCategorias() {
        TreeSet<String> categorias = new TreeSet<>();
        for (Producto p : productos) {
            if (p.getCategoria() != null && !p.getCategoria().isBlank()) {
                categorias.add(p.getCategoria());
            }
        }
        if (!TODAS.equals(categoriaActiva) && !categorias.contains(categoriaActiva)) {
            categoriaActiva = TODAS;
        }
        panelCategorias.removeAll();
        panelCategorias.add(crearChipCategoria(TODAS));
        for (String cat : categorias) {
            panelCategorias.add(crearChipCategoria(cat));
        }
        panelCategorias.revalidate();
        panelCategorias.repaint();
    }

    private BotonPildora crearChipCategoria(String categoria) {
        boolean activo = categoria.equals(categoriaActiva);
        BotonPildora chip = new BotonPildora(
                categoria,
                activo ? TemaBento.TEAL : TemaBento.TARJETA,
                activo ? TemaBento.TEAL_HOVER : TemaBento.SUPERFICIE_SUAVE,
                activo ? Color.WHITE : TemaBento.TEXTO,
                activo ? null : TemaBento.BORDE,
                999);
        chip.setFont(TemaBento.Fuente(Font.BOLD, 12));
        chip.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        chip.addActionListener(e -> {
            categoriaActiva = categoria;
            reconstruirCategorias();
            renderizarProductos();
        });
        return chip;
    }

    private void renderizarProductos() {
        String filtro = txtBuscar.getText().trim().toLowerCase();
        gridProductos.removeAll();
        int visibles = 0;
        for (Producto p : productos) {
            boolean coincideCat = TODAS.equals(categoriaActiva) || categoriaActiva.equals(p.getCategoria());
            boolean coincideTxt = filtro.isEmpty()
                    || p.getNombre().toLowerCase().contains(filtro)
                    || p.getMarca().toLowerCase().contains(filtro)
                    || p.getSerial().toLowerCase().contains(filtro);
            if (coincideCat && coincideTxt) {
                gridProductos.add(crearTarjetaProducto(p));
                visibles++;
            }
        }
        if (visibles == 0) {
            JPanel vacio = new JPanel(new FlowLayout(FlowLayout.CENTER));
            vacio.setOpaque(false);
            vacio.setBorder(BorderFactory.createEmptyBorder(40, 0, 40, 0));
            vacio.add(HelperUI.Subtitulo("No se encontraron productos con ese filtro."));
            gridProductos.add(vacio);
        }
        lblEstadoCatalogo.setText(visibles + " de " + productos.size() + " productos disponibles");
        gridProductos.revalidate();
        gridProductos.repaint();
    }

    private TarjetaBento crearTarjetaProducto(Producto p) {
        TarjetaBento card = new TarjetaBento();
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        // Fila superior: píldora categoría + píldora stock
        JPanel top = new JPanel(new BorderLayout(8, 0));
        top.setOpaque(false);
        EtiquetaPildora pillCat = new EtiquetaPildora(
                p.getCategoria(), TemaBento.SUPERFICIE_SUAVE, TemaBento.TEXTO);
        top.add(pillCat, BorderLayout.WEST);

        String txtStock;
        Color bgStock;
        Color fgStock;
        if (p.getStock() <= 0) {
            txtStock = "Agotado";
            bgStock = TemaBento.PELIGRO_SUAVE;
            fgStock = TemaBento.PELIGRO;
        } else if (p.getStock() <= 3) {
            txtStock = "Quedan " + p.getStock();
            bgStock = TemaBento.TERRACOTA;
            fgStock = Color.WHITE;
        } else {
            txtStock = p.getStock() + " uds";
            bgStock = TemaBento.SUPERFICIE_SUAVE;
            fgStock = TemaBento.TEXTO_SECUNDARIO;
        }
        EtiquetaPildora pillStock = new EtiquetaPildora(txtStock, bgStock, fgStock);
        top.add(pillStock, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        // Centro: nombre y marca
        JPanel info = new JPanel(new BorderLayout(0, 3));
        info.setOpaque(false);
        JLabel lblNombre = new JLabel("<html><body style='width:240px'>" + escaparHtml(p.getNombre()) + "</body></html>");
        lblNombre.setFont(TemaBento.Fuente(Font.BOLD, 14));
        lblNombre.setForeground(TemaBento.TEXTO);
        JLabel lblMarca = new JLabel(p.getMarca() + " · " + p.getSerial());
        lblMarca.setFont(TemaBento.Fuente(Font.PLAIN, 12));
        lblMarca.setForeground(TemaBento.TEXTO_SECUNDARIO);
        info.add(lblNombre, BorderLayout.CENTER);
        info.add(lblMarca, BorderLayout.SOUTH);
        card.add(info, BorderLayout.CENTER);

        // Pie: precio COP y botón + Añadir
        JPanel pie = new JPanel(new BorderLayout(8, 0));
        pie.setOpaque(false);
        JLabel lblPrecio = new JLabel(HelperUI.Moneda(p.getPrecio()));
        lblPrecio.setFont(TemaBento.Fuente(Font.BOLD, 17));
        lblPrecio.setForeground(TemaBento.TEXTO);
        pie.add(lblPrecio, BorderLayout.WEST);

        BotonPildora btnAdd = ComponentesBento.BotonTeal("+ Añadir");
        btnAdd.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
        btnAdd.setEnabled(p.getStock() > 0);
        btnAdd.addActionListener(e -> agregar(p, 1));
        pie.add(btnAdd, BorderLayout.EAST);
        card.add(pie, BorderLayout.SOUTH);

        return card;
    }

    private void agregar(Producto p, int cantidad) {
        String error = carrito.Agregar(p, cantidad);
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Stock insuficiente", JOptionPane.WARNING_MESSAGE);
            return;
        }
        refrescarCarrito();
    }

    private void decrementar(Carrito.Item item) {
        carrito.ActualizarCantidad(item.producto.getSerial(), item.getCantidad() - 1);
        refrescarCarrito();
    }

    private void refrescarCarrito() {
        listaCarrito.removeAll();
        if (carrito.EstaVacio()) {
            JPanel vacio = new JPanel(new GridBagLayout());
            vacio.setOpaque(false);
            JLabel lbl = new JLabel("<html><center>Tu carrito está vacío.<br>Agrega productos del catálogo.</center></html>");
            lbl.setFont(TemaBento.Fuente(Font.PLAIN, 13));
            lbl.setForeground(TemaBento.TEXTO_SECUNDARIO);
            vacio.add(lbl);
            listaCarrito.add(Box.createVerticalGlue());
            listaCarrito.add(vacio);
            listaCarrito.add(Box.createVerticalGlue());
        } else {
            for (int idx = 0; idx < carrito.Items().size(); idx++) {
                Carrito.Item item = carrito.Items().get(idx);
                listaCarrito.add(crearFilaCarrito(item));
                if (idx < carrito.Items().size() - 1) {
                    JSeparator sep = new JSeparator();
                    sep.setForeground(new Color(0xECEAE4));
                    sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                    listaCarrito.add(sep);
                }
            }
            listaCarrito.add(Box.createVerticalGlue());
        }

        int unidades = carrito.Unidades();
        pillArticulos.setText(unidades + (unidades == 1 ? " artículo" : " artículos"));
        String monto = HelperUI.Moneda(carrito.Total());
        lblSubtotal.setText(monto);
        lblTotal.setText(monto);
        btnVaciar.setVisible(!carrito.EstaVacio());
        btnPagar.setEnabled(!carrito.EstaVacio());
        btnPagar.setIcon(ComponentesBento.IconoFlechaDerecha(15,
                carrito.EstaVacio() ? TemaBento.TEXTO_SUAVE : Color.WHITE));

        listaCarrito.revalidate();
        listaCarrito.repaint();
    }

    private JPanel crearFilaCarrito(Carrito.Item item) {
        JPanel row = new JPanel(new BorderLayout(8, 4));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(10, 2, 10, 2));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));

        JLabel lblNombre = new JLabel("<html><body style='width:140px'>"
                + escaparHtml(item.producto.getNombre()) + "</body></html>");
        lblNombre.setFont(TemaBento.Fuente(Font.PLAIN, 13));
        lblNombre.setForeground(TemaBento.TEXTO);
        row.add(lblNombre, BorderLayout.WEST);

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        controles.setOpaque(false);

        BotonPildora btnMenos = crearMiniBoton("−");
        btnMenos.addActionListener(e -> decrementar(item));
        JLabel lblCant = new JLabel(item.getCantidad() + "×", SwingConstants.CENTER);
        lblCant.setFont(TemaBento.Fuente(Font.BOLD, 12));
        lblCant.setPreferredSize(new Dimension(26, 24));
        BotonPildora btnMas = crearMiniBoton("+");
        btnMas.addActionListener(e -> agregar(item.producto, 1));

        JLabel lblSub = new JLabel(HelperUI.Moneda(item.Subtotal()));
        lblSub.setFont(TemaBento.Fuente(Font.BOLD, 13));
        lblSub.setForeground(TemaBento.TEXTO);
        lblSub.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 2));

        controles.add(btnMenos);
        controles.add(lblCant);
        controles.add(btnMas);
        controles.add(lblSub);
        row.add(controles, BorderLayout.EAST);

        return row;
    }

    private BotonPildora crearMiniBoton(String texto) {
        BotonPildora b = new BotonPildora(texto, TemaBento.SUPERFICIE_SUAVE, TemaBento.CAMPO_FONDO,
                TemaBento.TEXTO, TemaBento.BORDE, 8);
        b.setFont(TemaBento.Fuente(Font.BOLD, 12));
        b.setBorder(BorderFactory.createEmptyBorder(2, 7, 2, 7));
        return b;
    }

    private void pagar() {
        Window w = SwingUtilities.getWindowAncestor(this);
        JFrame padre = w instanceof JFrame ? (JFrame) w : null;
        Pedido pedido = PasarelaPago.Mostrar(padre, carrito, dao);
        if (pedido != null) {
            carrito.Vaciar();
            refrescarCarrito();
            cargar();
        }
    }

    private static String escaparHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
