/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Logica_Conexion;

import Logica_Negocio.Persona;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Santiago Lopez
 */
public class PersonaDAO implements DAOInterfacePersona {

    public static Connection con = Conexion.getConnection();

    @Override
    public int add(Persona per) throws SQLException {
        String query
                = "INSERT INTO persona(Uid,Nombre,Apellido, Direccion,Cedula,Producto,Nom_img)"
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, per.getUid());
        ps.setString(2, per.getNombre());
        ps.setString(3, per.getApellido());
        ps.setString(4, per.getDireccion());
        ps.setString(5, per.getCedula());
        ps.setString(6, per.getProducto());
        ps.setString(7, per.getNom_img());

        int n = ps.executeUpdate();
        return n;
    }

    @Override
    public void delete(String id) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Persona getPersona(String id) throws SQLException {
        
        String query
            = "select * from persona where Uid= ?";
        PreparedStatement ps
            = con.prepareStatement(query);
 
        ps.setString(1, id);
        Persona per=null;
        ResultSet rs = ps.executeQuery();
        boolean check = false;
 
        while (rs.next()) {
            check = true;
            per = new Persona(rs.getString("Uid"),
                    rs.getString("Nombre"),
            rs.getString("Apellido"),
            rs.getString("Direccion"),
                    rs.getString("Cedula"),
                    rs.getString("Producto"),
                    rs.getString("Nom_img"));
        }
 
        if (check == true) {
            return per;
        }
        else
            return null;
    }

    @Override
    public ArrayList<Persona> getPersona() throws SQLException {
        String query = "select * from persona";
        PreparedStatement ps
            = con.prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        ArrayList<Persona> ls = new ArrayList<>();
 
        while (rs.next()) {
            Persona per = new Persona(rs.getString("Uid"),
                    rs.getString("Nombre"),
            rs.getString("Apellido"),
            rs.getString("Direccion"),
                    rs.getString("Cedula"),
                    rs.getString("Producto"),
                    rs.getString("Nom_img"));
          
            ls.add(per);
        }
        return ls;
    }

    @Override
    public void update(Persona per) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void close() throws SQLException {
        con.close();
    }

}
