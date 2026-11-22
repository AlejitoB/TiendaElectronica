package GUI_Local;

import Helpers.HelperUI;

/**
 * Punto de entrada principal de la aplicación de escritorio Tech Shop.
 */
public class Portada {

    public static void main(String[] args) {
        HelperUI.AplicarTema();
        java.awt.EventQueue.invokeLater(() -> new VentanaAcceso().setVisible(true));
    }
}
