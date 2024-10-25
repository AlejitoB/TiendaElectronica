/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package GUI_Admin;

import Helpers.HelperRegistro;
import Helpers.HelperValidacion;
import Logica_Conexion.Conexion;
import Logica_Negocio.Persona;
import Logica_Negocio.Producto;
import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.border.LineBorder;

/**
 *
 * @author Santiago Lopez
 */
public class RegistrarPersonaAdministrador extends javax.swing.JFrame {

    /**
     * Creates new form RegistrarPersona
     */
    public ArrayList<Producto> lsproductos = new ArrayList<>();
    public ArrayList<Persona> lspersona = new ArrayList<>();
    Persona objper;
    String producto = "";
    int numglobal = 0;
    int band = 0;
      public String pathc;
     public String s;
     
      public String pathc1;
   
    public String s1;
    
    public String osName = System.getProperty("os.name").toLowerCase();

    public RegistrarPersonaAdministrador() {
        initComponents();
        this.setSize(500,500);
        

        jTextField5.setVisible(false);
        jTextField6.setVisible(false);
        jTextField7.setVisible(false);
        jTextField8.setVisible(false);
        jTextField9.setVisible(false);
        jButton4.setVisible(false);
        jLabel12.setVisible(false);
        
      Path currentRelativePath = Paths.get("");
        s = currentRelativePath.toAbsolutePath().toString();
        // Ajuste de ruta basado en el sistema operativo
        if (osName.equals("linux")) {
            s = currentRelativePath.toAbsolutePath().toString();
           
            pathc1 = s + "//Images//" + "Background" + ".jpg";
            establecerImagen();
        } else if (osName.equals("windows 11") || osName.equals("windows 10")) {
            s1 = currentRelativePath.toAbsolutePath().toString();
            pathc1 = s1 + "\\Images\\" + "Background" + ".jpg";
            establecerImagen();
        }

    }

    public int RegistrarNumeroProductos() {
        String num_pro = jTextField4.getText();
        int res1= HelperValidacion.ValidarVacio(num_pro);
        int numero = 0;
        int band=0;
        
        if(res1==0){
        try {
            numero = Integer.parseInt(num_pro);
        } catch (NumberFormatException e) {
            jTextField4.setBorder(new LineBorder(Color.RED, 2));
            System.out.println("Digite un numero valido" + e.getMessage());
            band = 1;
        }
        }else
        {
         JOptionPane.showMessageDialog(null, "Campo Vacio!");
         jTextField4.setBorder(new LineBorder(Color.RED, 2));
          band=1;
        }
        
        if(band==0){
        int res = HelperValidacion.ValidarCantidadRango(numero);
        

        if (res == 1 && band == 0) {
            numglobal = numero;
            numero = 0;
             jTextField4.setBorder(new LineBorder(Color.BLACK, 1));
            return 1;
        } else {
            jTextField4.setBorder(new LineBorder(Color.RED, 2));
            JOptionPane.showMessageDialog(null, "El numero no se encuentra en el rango");
            return 0;
        }
        }
    return 0;
    }

    public void RegistarProducto() {

        String nombre = jTextField1.getText();
        String marca = jTextField2.getText();
        String serial = jTextField3.getText();

        int res, res1, res2;

        res = Helpers.HelperValidacion.ValidarTodo(nombre);
        res1 = Helpers.HelperValidacion.ValidarTodo(marca);
        res2 = Helpers.HelperValidacion.ValidarTodoSerial(serial);

        if (res == 0 && res1 == 0 && res2 == 0) {

            jTextField1.setBorder(new LineBorder(Color.BLACK, 1));
            jTextField2.setBorder(new LineBorder(Color.BLACK, 1));
            jTextField3.setBorder(new LineBorder(Color.BLACK, 1));
            
            Producto objproducto = new Producto(nombre, marca, serial);
            lsproductos.add(objproducto);
            band++;
            
            JOptionPane.showMessageDialog(null, "Registrando producto"+"\t"+band+"de"+numglobal);

            if (band == numglobal) {

                System.out.println("Alcanzo el Limite de registro");

                jTextField5.setVisible(true);
                jTextField6.setVisible(true);
                jTextField7.setVisible(true);
                jTextField8.setVisible(true);
                jTextField9.setVisible(true);
                jTextField1.setVisible(false);
                jTextField2.setVisible(false);
                jTextField3.setVisible(false);
                jButton2.setVisible(false);

            }
        } else {
            if (res >= 1) {
                jTextField1.setBorder(new LineBorder(Color.RED, 2));
                JOptionPane.showMessageDialog(null, "Revise el campo nombre");
            }
            if (res1 >= 1) {
                jTextField2.setBorder(new LineBorder(Color.RED, 2));
                JOptionPane.showMessageDialog(null, "Revise el campo marca ");
            }
            if (res2 >= 1) {
                jTextField3.setBorder(new LineBorder(Color.RED, 2));
                JOptionPane.showMessageDialog(null, "Revise el campos serial");
            } 
        }

    }

    public void RegistrarPersona() {

        String nombre = jTextField5.getText();
        String apellido = jTextField6.getText();
         String cedula = jTextField7.getText();
        String direccion = jTextField8.getText();
        String nom_img = jTextField9.getText();
        
              int res, res1, res2, res3, res4;

        res = Helpers.HelperValidacion.ValidarTodo(nombre);
        res1 = Helpers.HelperValidacion.ValidarTodo(apellido);
        res2 = Helpers.HelperValidacion.ValidarTodoLetra(cedula);
        res3 =  Helpers.HelperValidacion.ValidarTodoDireccion(direccion);
        res4 = Helpers.HelperValidacion.ValidarTodoSerial(nom_img);
        
        if(res==0 && res1==0 && res2==0 && res3==0 && res4==0){

        for (int i = 0; i < lsproductos.size(); i++) {
            producto += lsproductos.get(i).getNombre() + "," + lsproductos.get(i).getMarca() + "," + lsproductos.get(i).getSerial() + ";";

        }

        int id = (int) (Math.random() * 100000);

        objper = new Persona(String.valueOf(id), nombre, apellido, direccion, cedula, producto, nom_img);
        lspersona.add(objper);
        objper.setProductos(lsproductos);
        HelperRegistro.RegistrarPersonaNubeI(objper, id, producto);
        producto = "";

        jTextField5.setVisible(false);
        jTextField6.setVisible(false);
        jTextField7.setVisible(false);
        jTextField8.setVisible(false);
        jTextField9.setVisible(false);
        jButton1.setVisible(false);
        jButton4.setVisible(true);
        jLabel12.setVisible(true);

        jLabel12.setText("Registro exitoso, El id del cliente es:" + "\t" + id);
        }else
        {
            if (res >= 1) {
                jTextField5.setBorder(new LineBorder(Color.RED, 2));
                JOptionPane.showMessageDialog(null, "Revise el campo nombre");
            }
            if (res1 >= 1) {
                jTextField6.setBorder(new LineBorder(Color.RED, 2));
                JOptionPane.showMessageDialog(null, "Revise el campo apellido ");
            }
            if (res2 >= 1) {
                jTextField7.setBorder(new LineBorder(Color.RED, 2));
                JOptionPane.showMessageDialog(null, "Revise el campos cedula");
            } 
             if (res3 >= 1) {
                jTextField8.setBorder(new LineBorder(Color.RED, 2));
                JOptionPane.showMessageDialog(null, "Revise el campos direccion");
            }
              if (res4 >= 1) {
                jTextField9.setBorder(new LineBorder(Color.RED, 2));
                JOptionPane.showMessageDialog(null, "Revise el campos nombre imagen");
            } 
        }

    }

     public void establecerImagen() {
        
        Image img = null;
        try {
            File file = new File(pathc1);
           img = ImageIO.read(new File(pathc1));
            //5. Setear la imagen al JLabel
            jLabel13.setIcon(new ImageIcon(img));
        } catch (IOException ioexception) {
            System.err.println(ioexception);
        }
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jTextField5 = new javax.swing.JTextField();
        jTextField6 = new javax.swing.JTextField();
        jTextField7 = new javax.swing.JTextField();
        jTextField8 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        jTextField9 = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Registrar Persona Administrador");
        getContentPane().setLayout(null);

        jLabel1.setFont(new java.awt.Font("Roboto Black", 0, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Registrar Producto");
        getContentPane().add(jLabel1);
        jLabel1.setBounds(10, 10, 220, 30);

        jLabel2.setFont(new java.awt.Font("Roboto Black", 1, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Nombre");
        getContentPane().add(jLabel2);
        jLabel2.setBounds(10, 120, 100, 20);

        jLabel3.setFont(new java.awt.Font("Roboto Black", 1, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Marca");
        getContentPane().add(jLabel3);
        jLabel3.setBounds(10, 160, 80, 17);

        jLabel4.setFont(new java.awt.Font("Roboto Black", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Serial");
        getContentPane().add(jLabel4);
        jLabel4.setBounds(10, 190, 70, 17);

        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });
        getContentPane().add(jTextField1);
        jTextField1.setBounds(190, 110, 180, 30);

        jTextField2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField2ActionPerformed(evt);
            }
        });
        getContentPane().add(jTextField2);
        jTextField2.setBounds(190, 150, 180, 30);

        jTextField3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField3ActionPerformed(evt);
            }
        });
        getContentPane().add(jTextField3);
        jTextField3.setBounds(190, 190, 180, 30);

        jSeparator1.setBackground(new java.awt.Color(0, 204, 204));
        jSeparator1.setForeground(new java.awt.Color(0, 204, 204));
        getContentPane().add(jSeparator1);
        jSeparator1.setBounds(0, 250, 490, 20);

        jLabel5.setFont(new java.awt.Font("Roboto Black", 1, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Cantidad Productos");
        getContentPane().add(jLabel5);
        jLabel5.setBounds(10, 90, 150, 20);

        jLabel6.setFont(new java.awt.Font("Roboto Black", 1, 14)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Nombre Imagen");
        getContentPane().add(jLabel6);
        jLabel6.setBounds(20, 420, 110, 17);

        jTextField5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField5ActionPerformed(evt);
            }
        });
        getContentPane().add(jTextField5);
        jTextField5.setBounds(260, 290, 187, 30);

        jTextField6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField6ActionPerformed(evt);
            }
        });
        getContentPane().add(jTextField6);
        jTextField6.setBounds(260, 320, 187, 30);

        jTextField7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField7ActionPerformed(evt);
            }
        });
        getContentPane().add(jTextField7);
        jTextField7.setBounds(260, 350, 187, 30);

        jTextField8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField8ActionPerformed(evt);
            }
        });
        getContentPane().add(jTextField8);
        jTextField8.setBounds(260, 380, 187, 30);

        jTextField4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField4ActionPerformed(evt);
            }
        });
        getContentPane().add(jTextField4);
        jTextField4.setBounds(190, 70, 180, 30);

        jButton1.setBackground(new java.awt.Color(204, 204, 204));
        jButton1.setFont(new java.awt.Font("Roboto Black", 0, 14)); // NOI18N
        jButton1.setText("Registrar Persona");
        jButton1.setActionCommand("Registrar");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton1);
        jButton1.setBounds(270, 440, 190, 20);

        jButton2.setBackground(new java.awt.Color(204, 204, 204));
        jButton2.setFont(new java.awt.Font("Roboto Black", 0, 14)); // NOI18N
        jButton2.setText("Registrar Productos");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton2);
        jButton2.setBounds(310, 230, 170, 20);

        jButton4.setBackground(new java.awt.Color(204, 204, 204));
        jButton4.setFont(new java.awt.Font("Roboto Black", 0, 14)); // NOI18N
        jButton4.setText("Atras");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton4);
        jButton4.setBounds(20, 50, 80, 24);

        jButton3.setBackground(new java.awt.Color(204, 204, 204));
        jButton3.setFont(new java.awt.Font("Roboto Black", 0, 14)); // NOI18N
        jButton3.setText("Aceptar");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton3);
        jButton3.setBounds(380, 40, 90, 24);

        jLabel10.setFont(new java.awt.Font("Roboto Black", 0, 24)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 255, 255));
        jLabel10.setText("Registrar Persona");
        getContentPane().add(jLabel10);
        jLabel10.setBounds(10, 270, 210, 20);

        jTextField9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField9ActionPerformed(evt);
            }
        });
        getContentPane().add(jTextField9);
        jTextField9.setBounds(260, 410, 187, 30);

        jLabel14.setFont(new java.awt.Font("Roboto Black", 1, 14)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(255, 255, 255));
        jLabel14.setText("Nombre");
        getContentPane().add(jLabel14);
        jLabel14.setBounds(20, 300, 80, 17);

        jLabel15.setFont(new java.awt.Font("Roboto Black", 1, 14)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(255, 255, 255));
        jLabel15.setText("Apellido");
        getContentPane().add(jLabel15);
        jLabel15.setBounds(20, 330, 80, 17);

        jLabel12.setBackground(new java.awt.Color(255, 255, 255));
        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        getContentPane().add(jLabel12);
        jLabel12.setBounds(110, 440, 300, 30);

        jLabel16.setFont(new java.awt.Font("Roboto Black", 1, 14)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(255, 255, 255));
        jLabel16.setText("Cedula");
        getContentPane().add(jLabel16);
        jLabel16.setBounds(20, 360, 80, 17);

        jLabel17.setFont(new java.awt.Font("Roboto Black", 1, 14)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(255, 255, 255));
        jLabel17.setText("Direccion");
        getContentPane().add(jLabel17);
        jLabel17.setBounds(20, 390, 80, 20);
        getContentPane().add(jLabel13);
        jLabel13.setBounds(-10, -10, 570, 530);
        getContentPane().add(jSeparator2);
        jSeparator2.setBounds(330, 330, 80, 30);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField5ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField5ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        RegistarProducto();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        RegistrarPersona();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        int retorno = RegistrarNumeroProductos();
        if (retorno == 1) {
            jTextField4.setVisible(false);
            jButton3.setVisible(false);
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jTextField4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField4ActionPerformed

    private void jTextField9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField9ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField9ActionPerformed

    private void jTextField7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField7ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField7ActionPerformed

    private void jTextField6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField6ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField6ActionPerformed

    private void jTextField8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField8ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField8ActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jTextField3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField3ActionPerformed

    private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField2ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        MenuAdministrador menuadmin = new MenuAdministrador();
        menuadmin.setVisible(true);
        dispose();
    }//GEN-LAST:event_jButton4ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(RegistrarPersonaAdministrador.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(RegistrarPersonaAdministrador.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(RegistrarPersonaAdministrador.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(RegistrarPersonaAdministrador.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new RegistrarPersonaAdministrador().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextField8;
    private javax.swing.JTextField jTextField9;
    // End of variables declaration//GEN-END:variables
}
