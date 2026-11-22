package Logica_Conexion;

import Helpers.HelperAuditoria;
import Logica_Negocio.Persona;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio unificado para gestionar clientes en MySQL (local) o Firestore (nube).
 */
public class ServicioPersona {

    private static final String COLECCION = "Persona";
    private final DAOInterfacePersona daoLocal;
    private final boolean usarNube;

    public ServicioPersona() {
        this(Conexion.modoNube, new PersonaDAO());
    }

    public ServicioPersona(boolean usarNube, DAOInterfacePersona daoLocal) {
        this.usarNube = usarNube;
        this.daoLocal = daoLocal;
    }

    public List<Persona> listar() throws Exception {
        if (!usarNube) {
            return daoLocal.getPersona();
        }
        List<Persona> lista = new ArrayList<>();
        for (DocumentSnapshot d : coleccion().get().get().getDocuments()) {
            lista.add(PersonaProvider.aPersona(d));
        }
        return lista;
    }

    public Persona buscar(String uid) throws Exception {
        if (uid == null || uid.isBlank()) {
            return null;
        }
        if (!usarNube) {
            return daoLocal.getPersona(uid.trim());
        }
        DocumentSnapshot directo = coleccion().document(uid.trim()).get().get();
        if (directo.exists()) {
            return PersonaProvider.aPersona(directo);
        }
        for (DocumentSnapshot d : coleccion().whereEqualTo("Uid", uid.trim()).get().get().getDocuments()) {
            return PersonaProvider.aPersona(d);
        }
        return null;
    }

    public void guardar(Persona p, boolean nuevo) throws Exception {
        if (!usarNube) {
            if (nuevo) {
                if (daoLocal.getPersona(p.getUid()) != null) {
                    throw new IllegalStateException("Ya existe un cliente con el UID " + p.getUid());
                }
                daoLocal.add(p);
            } else {
                daoLocal.update(p);
            }
        } else {
            if (nuevo && buscar(p.getUid()) != null) {
                throw new IllegalStateException("Ya existe un cliente con el UID " + p.getUid());
            }
            coleccion().document(p.getUid()).set(aMapa(p)).get();
        }
        String origen = usarNube ? "nube" : "local";
        HelperAuditoria.Registrar(nuevo ? "CREAR_CLIENTE" : "ACTUALIZAR_CLIENTE",
                "Uid=" + p.getUid() + " base=" + origen);
    }

    public boolean eliminar(String uid) throws Exception {
        boolean eliminado;
        if (!usarNube) {
            eliminado = daoLocal.delete(uid) > 0;
        } else {
            eliminado = false;
            DocumentSnapshot doc = coleccion().document(uid).get().get();
            if (doc.exists()) {
                doc.getReference().delete().get();
                eliminado = true;
            } else {
                for (DocumentSnapshot d : coleccion().whereEqualTo("Uid", uid).get().get().getDocuments()) {
                    d.getReference().delete().get();
                    eliminado = true;
                }
            }
        }
        if (eliminado) {
            HelperAuditoria.Registrar("ELIMINAR_CLIENTE",
                    "Uid=" + uid + " base=" + (usarNube ? "nube" : "local"));
        }
        return eliminado;
    }

    private CollectionReference coleccion() {
        if (Conexion.db == null) {
            throw new IllegalStateException("No hay conexión activa con Firebase");
        }
        return Conexion.db.collection(COLECCION);
    }

    private static Map<String, Object> aMapa(Persona p) {
        Map<String, Object> datos = new HashMap<>();
        datos.put("Uid", p.getUid());
        datos.put("Nombre", p.getNombre());
        datos.put("Apellido", p.getApellido());
        datos.put("Cedula", p.getCedula());
        datos.put("Direccion", p.getDireccion());
        datos.put("Correo", p.getCorreo());
        datos.put("Telefono", p.getTelefono());
        datos.put("Productos", p.getProducto());
        datos.put("Producto", p.getProducto());
        datos.put("Nom_img", p.getNom_img());
        return datos;
    }
}
