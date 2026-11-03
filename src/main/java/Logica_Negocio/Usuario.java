/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Logica_Negocio;

import Helpers.HelperCifrado;

/**
 *
 * @author 1058932609
 */
public abstract class Usuario {
    
    private String usu;
    // Hash SHA-256 de la contrasena; la clave en texto plano nunca se guarda.
    private String contra;

    public Usuario(String usu, String contra) {
        this.usu = usu;
        this.contra = contra;
    }

    public String getUsu() {
        return usu;
    }

    public void setUsu(String usu) {
        this.usu = usu;
    }

    public String getContra() {
        return contra;
    }

    public void setContra(String contra) {
        this.contra = contra;
    }
    
    // Metodo plantilla: el LogOn es el mismo para Administrador y Cliente (recibe hashes SHA-256).
    public boolean LogOn(String usuario, String contraseña) {
        return HelperCifrado.CifrarSHA256(usu).equals(usuario) && contra.equalsIgnoreCase(contraseña);
    }
    
}
