package Logica_Negocio;

import Logica_Conexion.DAOInterfaceProducto;
import java.util.List;

/**
 * Articulos de computador con los que arranca la tienda cuando el inventario esta vacio.
 */
public class CatalogoInicial {

    public static final List<Producto> PRODUCTOS = List.of(
            new Producto("LAP-001", "Portátil Lenovo IdeaPad 3 15\" Ryzen 5 16 GB", "Lenovo", "Portátiles", 2_499_000, 8),
            new Producto("LAP-002", "Portátil ASUS VivoBook 14 Core i5 8 GB", "ASUS", "Portátiles", 2_799_000, 6),
            new Producto("LAP-003", "MacBook Air 13\" chip M2 256 GB", "Apple", "Portátiles", 4_999_000, 4),
            new Producto("MON-001", "Monitor LG 24\" IPS Full HD", "LG", "Monitores", 649_000, 12),
            new Producto("MON-002", "Monitor Samsung 27\" curvo 75 Hz", "Samsung", "Monitores", 899_000, 7),
            new Producto("TEC-001", "Teclado mecánico Redragon Kumara", "Redragon", "Periféricos", 189_000, 20),
            new Producto("MOU-001", "Mouse inalámbrico Logitech M170", "Logitech", "Periféricos", 49_900, 35),
            new Producto("MOU-002", "Mouse gamer Logitech G203", "Logitech", "Periféricos", 119_000, 15),
            new Producto("CAM-001", "Cámara web Logitech C920 Full HD", "Logitech", "Periféricos", 289_000, 2),
            new Producto("AUD-001", "Diadema HyperX Cloud Stinger", "HyperX", "Audio", 229_000, 10),
            new Producto("ALM-001", "SSD Kingston NV2 1 TB NVMe", "Kingston", "Almacenamiento", 299_000, 18),
            new Producto("ALM-002", "Disco externo Seagate 2 TB USB 3.0", "Seagate", "Almacenamiento", 329_000, 9),
            new Producto("RAM-001", "Memoria RAM Corsair Vengeance 16 GB DDR4", "Corsair", "Componentes", 199_000, 14),
            new Producto("GPU-001", "Tarjeta gráfica MSI GeForce RTX 4060 8 GB", "MSI", "Componentes", 1_699_000, 3),
            new Producto("RED-001", "Router WiFi TP-Link Archer C6", "TP-Link", "Redes", 179_000, 11)
    );

    /** Carga el catalogo si el inventario no tiene productos. Devuelve cuantos agrego. */
    public static int SembrarSiVacio(DAOInterfaceProducto dao) throws Exception {
        if (!dao.listar().isEmpty()) {
            return 0;
        }
        for (Producto p : PRODUCTOS) {
            // Copia para no compartir (y modificar) las instancias de la lista constante.
            dao.guardar(new Producto(p.getSerial(), p.getNombre(), p.getMarca(), p.getCategoria(), p.getPrecio(), p.getStock()));
        }
        return PRODUCTOS.size();
    }
}
