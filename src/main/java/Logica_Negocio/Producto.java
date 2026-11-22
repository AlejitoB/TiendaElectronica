package Logica_Negocio;

/**
 * Artículo del inventario de la tienda electrónica.
 */
public class Producto {

    private String nombre = "";
    private String marca = "";
    private String serial = "";
    private String categoria = "";
    // Precio en pesos colombianos (sin decimales) y unidades disponibles en inventario.
    private long precio;
    private int stock;

    public Producto() {
    }

    public Producto(String nombre, String marca, String serial) {
        setNombre(nombre);
        setMarca(marca);
        setSerial(serial);
    }

    public Producto(String serial, String nombre, String marca, String categoria, long precio, int stock) {
        this(nombre, marca, serial);
        setCategoria(categoria);
        setPrecio(precio);
        setStock(stock);
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre == null ? "" : nombre;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca == null ? "" : marca;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial == null ? "" : serial;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria == null ? "" : categoria;
    }

    public long getPrecio() {
        return precio;
    }

    public void setPrecio(long precio) {
        this.precio = precio;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }
}
