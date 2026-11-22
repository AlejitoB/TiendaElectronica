package Logica_Negocio;

import Helpers.HelperCifrado;

/**
 * Usuario autenticado en la aplicación (Administrador o Cliente).
 */
public abstract class Usuario {

    public static Usuario sesion;

    private String usu;
    // Hash PBKDF2 o SHA-256 de la contraseña; la clave en texto plano nunca se guarda.
    private String contra;

    public Usuario(String usu, String contra) {
        this.usu = usu == null ? "" : usu;
        this.contra = contra == null ? "" : contra;
    }

    public String getUsu() {
        return usu;
    }

    public void setUsu(String usu) {
        this.usu = usu == null ? "" : usu;
    }

    public String getContra() {
        return contra;
    }

    public void setContra(String contra) {
        this.contra = contra == null ? "" : contra;
    }

    /** Verifica usuario y contraseña en texto plano contra el hash almacenado (PBKDF2 o SHA-256). */
    public boolean Autenticar(String usuarioPlano, String clavePlano) {
        if (usuarioPlano == null || clavePlano == null) {
            return false;
        }
        return HelperCifrado.IgualConstante(usu, usuarioPlano)
                && HelperCifrado.VerificarClave(clavePlano, contra);
    }

    /** Compatibilidad con pruebas y flujos que comparan hashes SHA-256 directamente. */
    public boolean LogOn(String usuarioSha, String contraseñaSha) {
        return HelperCifrado.IgualConstante(HelperCifrado.CifrarSHA256(usu), usuarioSha)
                && HelperCifrado.IgualConstante(contra.toLowerCase(),
                        contraseñaSha == null ? "" : contraseñaSha.toLowerCase());
    }
}
