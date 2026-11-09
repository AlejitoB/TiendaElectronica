/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Logica_Negocio;

/**
 *
 * @author 1058932609
 */
public class Producto {

    public String nombre,marca,serial;
    public String categoria = "";
    // Precio en pesos colombianos (sin decimales) y unidades disponibles en inventario.
    public long precio;
    public int stock;

    public Producto(){

    }

    public Producto(String nombre, String marca, String serial) {
        this.nombre = nombre;
        this.marca = marca;
        this.serial = serial;
    }

    public Producto(String serial, String nombre, String marca, String categoria, long precio, int stock) {
        this(nombre, marca, serial);
        this.categoria = categoria;
        this.precio = precio;
        this.stock = stock;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
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
