/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Helpers;

import java.util.ArrayList;

/**
 *
 * @author 1058932609
 */
public class HelperValidacion {
    
     public static int RetornarValor(String nombre)
    {
        int numero=0;
        int letra=0;

        
        for (int j = 0; j < nombre.length(); j++) {
             boolean flag = Character.isDigit(nombre.charAt(j));
             if(flag) {
                //System.out.println("'"+ nombre.charAt(j)+"' is a number");
                numero++;
             }
             else {
                //System.out.println("'"+ nombre.charAt(j)+"' is a letter");
                letra++;
             }

        }
        return numero;
    }
     
      public static int RetornarCEV2(String nombre)
    {
        int ce=0;
        
        ArrayList<Character> lscaracteres = new ArrayList<>();
        
        lscaracteres.add('@');
        lscaracteres.add('~');
        lscaracteres.add('/');
        lscaracteres.add(';');
        lscaracteres.add(':');
        lscaracteres.add('"');
        lscaracteres.add('!');
        lscaracteres.add('º');
        lscaracteres.add('¬');
        lscaracteres.add('|');
        lscaracteres.add('?');
        lscaracteres.add('¿');
        lscaracteres.add('&');
        lscaracteres.add('%');
        lscaracteres.add('$');
        lscaracteres.add('_');
        lscaracteres.add('*');
        lscaracteres.add('<');
        lscaracteres.add('>');
        lscaracteres.add('€');
        lscaracteres.add('Ç');
        lscaracteres.add('+');
        lscaracteres.add('(');
        lscaracteres.add(')');
        lscaracteres.add('»');
        lscaracteres.add('¥');
        lscaracteres.add('«');
        lscaracteres.add('¶');
        lscaracteres.add('[');
        lscaracteres.add(']');
        lscaracteres.add('{');
        lscaracteres.add('}');
        lscaracteres.add('÷');
        lscaracteres.add('=');
        lscaracteres.add(',');

        for (int j = 0; j < nombre.length(); j++) {
             boolean flag = Character.isLetter(nombre.charAt(j));
             if(!flag) {
                 for (int i = 0; i < lscaracteres.size(); i++) {
                      
                     if(lscaracteres.get(i).compareTo(nombre.charAt(j))==0)
                     {
                        ce++;
                     }
                 }
             }
        }
        return ce;
    }
      
      public static int ValidarVacio(String cadena) {
        if (cadena.equals("")) {
            return 1;
        } else {
            return 0;
        }

    }
      
      
       public static int RetornarLetra(String nombre)
    {
        int numero=0;
        int letra=0;

        
        for (int j = 0; j < nombre.length(); j++) {
             boolean flag = Character.isDigit(nombre.charAt(j));
             if(flag) {
                //System.out.println("'"+ nombre.charAt(j)+"' is a number");
                numero++;
             }
             else {
                //System.out.println("'"+ nombre.charAt(j)+"' is a letter");
                letra++;
             }

        }
        return letra;
    }
       
   public static int ValidarCantidadRango(int cantidad)
   {
       if(cantidad>0 && cantidad<1000){
           return 1;
       }
      else
       {
           return 0;
       }
   }
   
   public static int RetornarCEDireccionV2(String nombre){
       int ce=0;
       ArrayList<Character> lscaracteres=new ArrayList<>();
       lscaracteres.add('$');
       
       for (int j = 0; j < nombre.length(); j++) {
           boolean flag=Character.isLetter(nombre.charAt(j));
           if(!flag){
               for (int i = 0; i < lscaracteres.size(); i++) {
                   if(lscaracteres.get(i).compareTo(nombre.charAt(j))==0)
                   {
                       ce++;
                       
                   }
                   
               }
           }
           
       }
       return ce;
   }
   public static int RetornarCEV2Contraseña(String contraseña)
    {
        int ce=0;
        
        ArrayList<Character> lscaracteres = new ArrayList<>();
        
        lscaracteres.add('.');
        lscaracteres.add('-');
        lscaracteres.add('/');
     

        for (int j = 0; j < contraseña.length(); j++) {
             boolean flag = Character.isLetter(contraseña.charAt(j));
             if(!flag) {
                 for (int i = 0; i < lscaracteres.size(); i++) {
                      
                     if(lscaracteres.get(i).compareTo(contraseña.charAt(j))==0)
                     {
                        ce++;
                     }
                 }
             }
        }
        return ce;
    }
    
    public static int RetornarCE_Address(String direccion) {//no lo se broooo
        int ce = 0;

        ArrayList<Character> lscaracteres = new ArrayList<>();

        lscaracteres.add('~');
        lscaracteres.add('/');
        lscaracteres.add(';');
        lscaracteres.add(':');
        lscaracteres.add('"');
        lscaracteres.add('!');
        lscaracteres.add('º');
        lscaracteres.add('¬');
        lscaracteres.add('|');
        lscaracteres.add('?');
        lscaracteres.add('¿');
        lscaracteres.add('&');
        lscaracteres.add('%');
        lscaracteres.add('$');
        lscaracteres.add('_');
        lscaracteres.add('*');
        lscaracteres.add('<');
        lscaracteres.add('>');
        lscaracteres.add('€');
        lscaracteres.add('Ç');
        lscaracteres.add('+');
        lscaracteres.add('(');
        lscaracteres.add(')');
        lscaracteres.add('»');
        lscaracteres.add('¥');
        lscaracteres.add('«');
        lscaracteres.add('¶');
        lscaracteres.add('[');
        lscaracteres.add(']');
        lscaracteres.add('{');
        lscaracteres.add('}');
        lscaracteres.add('÷');
        lscaracteres.add('=');
        lscaracteres.add(',');
        lscaracteres.add('@');

        for (int j = 0; j < direccion.length(); j++) {
            boolean flag = Character.isLetter(direccion.charAt(j));
            if (!flag) {
                for (int i = 0; i < lscaracteres.size(); i++) {

                    if (lscaracteres.get(i).compareTo(direccion.charAt(j)) == 0) {
                        ce++;
                    }
                }
            }
        }
        return ce;
    }
    
    public static int ValidarTodo(String cadena)
   {
       int conteo=ValidarVacio(cadena)+RetornarValor(cadena)+RetornarCEV2(cadena);
       return conteo;
   }
   
    public static int ValidarTodoLetra(String cadena)
   {
       int conteo=ValidarVacio(cadena)+RetornarLetra(cadena)+RetornarCEV2(cadena);
       return conteo;
   }
    public static int ValidarTodoDireccion(String cadena)
    {
        int conteo=ValidarVacio(cadena)+RetornarCEDireccionV2(cadena);
        return conteo;
    }
    public static int ValidarTodoSerial(String cadena)
    {
        int conteo=ValidarVacio(cadena)+RetornarCEV2(cadena);
        return conteo;  
    }
    public static int ValidarTodoContraseña(String cadena)
    {
        int conteo=ValidarVacio(cadena)+RetornarCEV2Contraseña(cadena);
        return conteo;
    }
    

    /**
     * Valida un producto del inventario. Devuelve null si es valido o un mensaje
     * que nombra el campo y el tipo de error (criterio de usabilidad).
     */
    public static String ValidarProducto(Logica_Negocio.Producto p) {
        if (p.getSerial() == null || !p.getSerial().matches("[A-Za-z0-9-]{3,20}")) {
            return "Campo serial: use de 3 a 20 letras, numeros o guiones";
        }
        if (p.getNombre() == null || p.getNombre().isBlank() || p.getNombre().length() > 120) {
            return "Campo nombre: es obligatorio (maximo 120 caracteres)";
        }
        if (p.getMarca() == null || p.getMarca().isBlank() || RetornarCEV2(p.getMarca()) > 0) {
            return "Campo marca: es obligatorio y no admite caracteres especiales";
        }
        if (p.getCategoria() == null || p.getCategoria().isBlank()) {
            return "Campo categoria: es obligatorio";
        }
        if (p.getPrecio() <= 0) {
            return "Campo precio: debe ser un numero mayor que cero";
        }
        if (p.getStock() < 0 || p.getStock() > 99999) {
            return "Campo stock: debe estar entre 0 y 99999";
        }
        return null;
    }
}
    
    

