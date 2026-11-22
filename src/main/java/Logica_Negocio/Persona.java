package Logica_Negocio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Cliente registrado en el sistema.
 */
public class Persona {

    private String uid = "";
    private String nombre = "";
    private String apellido = "";
    private String direccion = "";
    private String cedula = "";
    private String producto = "";
    private String nomImg = "";
    private String correo = "";
    private String telefono = "";
    private List<Producto> productos = new ArrayList<>();

    public Persona(String uid, String nombre, String apellido, String direccion,
            String cedula, String producto, String nomImg) {
        this(uid, nombre, apellido, direccion, cedula, producto, nomImg, "", "");
    }

    public Persona(String uid, String nombre, String apellido, String direccion,
            String cedula, String producto, String nomImg, String correo, String telefono) {
        setUid(uid);
        setNombre(nombre);
        setApellido(apellido);
        setDireccion(direccion);
        setCedula(cedula);
        setProducto(producto);
        setNom_img(nomImg);
        setCorreo(correo);
        setTelefono(telefono);
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = nvl(uid);
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nvl(nombre);
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = nvl(apellido);
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = nvl(direccion);
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = nvl(cedula);
    }

    public String getProducto() {
        return producto;
    }

    public void setProducto(String producto) {
        this.producto = nvl(producto);
    }

    public String getNom_img() {
        return nomImg;
    }

    public void setNom_img(String nomImg) {
        this.nomImg = nvl(nomImg);
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = nvl(correo);
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = nvl(telefono);
    }

    public List<Producto> getProductos() {
        return Collections.unmodifiableList(productos);
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos == null ? new ArrayList<>() : new ArrayList<>(productos);
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }
}
