package Logica_Conexion;

import Logica_Negocio.Persona;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Precondition;
import com.google.firebase.cloud.FirestoreClient;
import java.util.ArrayList;
import java.util.Map;

/**
 * Proveedor de clientes en Firestore (capa de datos pura sin acoplamiento a interfaz).
 */
public class PersonaProvider {

    private static final String COLECCION = "Persona";

    private static Firestore firestore() {
        return Conexion.db != null ? Conexion.db : FirestoreClient.getFirestore();
    }

    public static boolean GuardarPersona(String coleccion, String documento, Map<String, Object> data) {
        try {
            DocumentReference docRef = firestore().collection(coleccion).document(documento);
            docRef.set(data).get();
            return true;
        } catch (Exception e) {
            System.out.println("Error Firestore GuardarPersona: " + e.getMessage());
            return false;
        }
    }

    public static ArrayList<Persona> CargarInfoPersona() {
        ArrayList<Persona> lspersona = new ArrayList<>();
        try {
            CollectionReference persona = firestore().collection(COLECCION);
            for (DocumentSnapshot document : persona.get().get().getDocuments()) {
                lspersona.add(aPersona(document));
            }
        } catch (Exception e) {
            System.out.println("Error Firestore CargarInfoPersona: " + e.getMessage());
        }
        return lspersona;
    }

    public static boolean RetornarUid(String uid) {
        return CargarInfoPersonaCodigo(uid) != null;
    }

    public static Persona CargarInfoPersonaCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return null;
        }
        try {
            CollectionReference col = firestore().collection(COLECCION);
            DocumentSnapshot directo = col.document(codigo).get().get();
            if (directo.exists()) {
                return aPersona(directo);
            }
            for (DocumentSnapshot doc : col.whereEqualTo("Uid", codigo).get().get().getDocuments()) {
                return aPersona(doc);
            }
        } catch (Exception e) {
            System.out.println("Error Firestore CargarInfoPersonaCodigo: " + e.getMessage());
        }
        return null;
    }

    public static boolean EliminarPersona(String coleccion, String uid) {
        if (uid == null || uid.isBlank()) {
            return false;
        }
        try {
            CollectionReference col = firestore().collection(coleccion);
            DocumentSnapshot directo = col.document(uid).get().get();
            if (directo.exists()) {
                directo.getReference().delete(Precondition.NONE).get();
                return true;
            }
            for (DocumentSnapshot doc : col.whereEqualTo("Uid", uid).get().get().getDocuments()) {
                doc.getReference().delete(Precondition.NONE).get();
                return true;
            }
        } catch (Exception e) {
            System.out.println("Error Firestore EliminarPersona: " + e.getMessage());
        }
        return false;
    }

    static Persona aPersona(DocumentSnapshot document) {
        String prod = document.getString("Productos");
        if (prod == null) {
            prod = document.getString("Producto");
        }
        return new Persona(
                nvl(document.getString("Uid")),
                nvl(document.getString("Nombre")),
                nvl(document.getString("Apellido")),
                nvl(document.getString("Direccion")),
                nvl(document.getString("Cedula")),
                nvl(prod),
                nvl(document.getString("Nom_img")),
                nvl(document.getString("Correo")),
                nvl(document.getString("Telefono")));
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }
}
