/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Helpers;

import Logica_Negocio.Persona;
import Logica_Negocio.Producto;
import java.util.ArrayList;

/**
 *
 * @author 1058932609
 */
public class HelperImpresion {

    public static void ImprimirInfoPersonaNube(ArrayList<Persona> lspersonasnube) {
        String[] parts = null,
                parts2 = null;

        Producto objprodu;
        ArrayList<Producto> lsnube = new ArrayList<>();

        for (int i = 0; i < lspersonasnube.size(); i++) {
            System.out.println("Persona" + "\t" + (i + 1) + "\n"
                    + "El id de la persona es:" + lspersonasnube.get(i).getUid() + "\n"
                    + "El nombre de la persona es:" + "\t" + lspersonasnube.get(i).getNombre() + "\n"
                    + "El Apellido de la persona es:" + "\t" + lspersonasnube.get(i).getApellido() + "\n"
                    + "La Cedula de la persona es:" + "\t" + lspersonasnube.get(i).getCedula() + "\n"
                    + "La direccion de la persona es:" + "\t" + lspersonasnube.get(i).getDireccion() + "\n"
            );
            parts = lspersonasnube.get(i).getProducto().split(";");

            for (int j = 0; j < parts.length; j++) {

                parts2 = parts[j].split(",");
                objprodu = new Producto(parts2[0], parts[1], parts2[2]);
                lsnube.add(objprodu);

            }

            for (int k = 0; k < lsnube.size(); k++) {
                System.out.println("Producto" + "\t" + (k + 1));
                System.out.println("Nombre" + "\t" + lsnube.get(k).getNombre());
                System.out.println("Marca" + "\t" + lsnube.get(k).getNombre());
                System.out.println("Serial" + "\t" + lsnube.get(k).getSerial() + "\n");

            }

            lsnube.clear();
            parts = null;
            parts2 = null;

        }
         
    }
    public static void BuscarPersonaNube(ArrayList<Persona> lspersonasnube, String codigo) {
        String[] partes = null,
                partes2 = null;

        int bandera = 0;
        Producto objprodu;
        ArrayList<Producto> lsnube = new ArrayList<>();

        for (int i = 0; i < lspersonasnube.size(); i++) {
            if (codigo.equals(lspersonasnube.get(i).getUid())) {
                bandera = 1;
                System.out.println("Persona" + "\t" + (i + 1) + "\n"
                        + "El id de la persona es:" + lspersonasnube.get(i).getUid() + "\n"
                        + "El nombre de la persona es:" + "\t" + lspersonasnube.get(i).getNombre() + "\n"
                        + "El apellido de la persona es:" + "\t" + lspersonasnube.get(i).getApellido() + "\n"
                        + "La cedula de la persona es:" + "\t" + lspersonasnube.get(i).getCedula() + "\n"
                        + "La direccion de la persona es:" + "\t" + lspersonasnube.get(i).getDireccion() + "\n"
                );
                partes = lspersonasnube.get(i).getProducto().split(";");

                for (int j = 0; j < partes.length; j++) {
                    partes2 = partes[j].split(",");
                    objprodu = new Producto(partes2[0], partes2[1], partes2[2]);
                    lsnube.add(objprodu);

                }

                for (int k = 0; k < lsnube.size(); k++) {
                    System.out.println("Producto:" + "\t" + (k + 1));
                    System.out.println("Nombre" + "t" + lsnube.get(k).getNombre());
                    System.out.println("Marca" + "t" + lsnube.get(k).getMarca());
                    System.out.println("Serial" + "t" + lsnube.get(k).getSerial());
                }

                lsnube.clear();
                partes = null;
                partes2 = null;
            }

        }

        if (bandera == 0) {
            System.out.println("El uid buscado no se encuentra en la lista");
        }
    }
    
    public static String ImprimirInfoInterfaz(ArrayList<Persona> lspersonasnube) {
        String info = "";
        String[] parts = null,
                parts2 = null;
        Producto objprodu;
        ArrayList<Producto> lsnube = new ArrayList<>();

        for (int i = 0; i < lspersonasnube.size(); i++) {

            info += "Persona" + "\t" + (i + 1) + "\n"
                    + "El id de la persona es:" + lspersonasnube.get(i).getUid() + "\n"
                    + "El nombre de la persona es" + "\t" + lspersonasnube.get(i).getNombre() + "\n"
                    + "El Apellido de la persona es" + "\t" + lspersonasnube.get(i).getApellido() + "\n"
                    + "La cedula de la persona es" + "\t" + lspersonasnube.get(i).getCedula() + "\n"
                    + "La direccion de la persona es" + "\t" + lspersonasnube.get(i).getDireccion() + "\n"
                    + "\n";

            parts = lspersonasnube.get(i).getProducto().split(";");

            for (int j = 0; j < parts.length; j++) {

                parts2 = parts[j].split(",");
                objprodu = new Producto(parts2[0], parts2[1], parts2[2]);
                lsnube.add(objprodu);
            }

            for (int k = 0; k < lsnube.size(); k++) {
                info += "Producto:" + "\t" + (k + 1) + "\n"
                        + "Nombre" + "\t" + lsnube.get(k).getNombre() + "\n"
                        + "Marca" + "\t" + lsnube.get(k).getMarca() + "\n"
                        + "Serial" + "\t" + lsnube.get(k).getSerial() + "\n"
                        + "\n";
            }

            lsnube.clear();
            parts = null;
            parts2 = null;

        }

        return info;
    }
    
    public static String ImprimirInfoInterfazNube(ArrayList<Persona> lspersonasnube, String codigo) {
        String info = "";
        String[] parts = null,
                parts2 = null;
        Producto objprodu;
        ArrayList<Producto> lsnube = new ArrayList<>();
        int bandera = 0;

        for (int i = 0; i < lspersonasnube.size(); i++) {
            if (codigo.equals(lspersonasnube.get(i).getUid())) {
                bandera = 1;
                info += "Persona" + "\t" + (i + 1) + "\n"
                        + "El id de la persona es:" + lspersonasnube.get(i).getUid() + "\n"
                        + "El nombre de la persona es" + "\t" + lspersonasnube.get(i).getNombre() + "\n"
                        + "El Apellido de la persona es" + "\t" + lspersonasnube.get(i).getApellido() + "\n"
                        + "La cedula de la persona es" + "\t" + lspersonasnube.get(i).getCedula() + "\n"
                        + "La direccion de la persona es" + "\t" + lspersonasnube.get(i).getDireccion() + "\n"
                        + "\n";

                parts = lspersonasnube.get(i).getProducto().split(";");

                for (int j = 0; j < parts.length; j++) {

                    parts2 = parts[j].split(",");
                    objprodu = new Producto(parts2[0], parts2[1], parts2[2]);
                    lsnube.add(objprodu);
                }

                for (int k = 0; k < lsnube.size(); k++) {
                    info += "Producto:" + "\t" + (k + 1) + "\n"
                            + "Nombre" + "\t" + lsnube.get(k).getNombre() + "\n"
                            + "Marca" + "\t" + lsnube.get(k).getMarca() + "\n"
                            + "Serial" + "\t" + lsnube.get(k).getSerial() + "\n"
                            + "\n";
                }

                lsnube.clear();
                parts = null;
                parts2 = null;

            }

        }
        if (bandera == 0) {
            System.out.println("El uid buscado no se encuenttra en la lista");
        }
        return info;
    }
      public static String ImprimirInfoInterfazLocal(Persona per) {
        String info = "";
        String[] parts = null,
                parts2 = null;
        Producto objprodu;
        ArrayList<Producto> lsnube = new ArrayList<>();
        int bandera = 0;

                info += "Persona"  + "\n"
                        + "El id de la persona es:" + per.getUid() + "\n"
                        + "El nombre de la persona es" + "\t" + per.getNombre() + "\n"
                        + "El Apellido de la persona es" + "\t" + per.getApellido() + "\n"
                        + "La cedula de la persona es" + "\t" + per.getCedula() + "\n"
                        + "La direccion de la persona es" + "\t" + per.getDireccion() + "\n"
                        + "\n";

                parts = per.getProducto().split(";");

                for (int j = 0; j < parts.length; j++) {

                    parts2 = parts[j].split(",");
                    objprodu = new Producto(parts2[0], parts2[1], parts2[2]);
                    lsnube.add(objprodu);
                }

                for (int k = 0; k < lsnube.size(); k++) {
                    info += "Producto:" + "\t" + (k + 1) + "\n"
                            + "Nombre" + "\t" + lsnube.get(k).getNombre() + "\n"
                            + "Marca" + "\t" + lsnube.get(k).getMarca() + "\n"
                            + "Serial" + "\t" + lsnube.get(k).getSerial() + "\n"
                            + "\n";
                }

                lsnube.clear();
                parts = null;
                parts2 = null;
                
         return info;

            }


}
