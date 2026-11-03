package Logica_Conexion;

import Helpers.HelperCifrado;
import Helpers.HelperConfig;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Cobro con tarjeta en el ambiente Sandbox de Wompi (Bancolombia): no mueve dinero real.
 * Flujo: tokens de aceptacion -> token de la tarjeta -> transaccion -> consulta hasta estado final.
 */
public class PasarelaWompi {

    public static final String URL = "https://sandbox.wompi.co/v1";

    private static final Gson GSON = new Gson();
    // Se crea al primer cobro: si Wompi no esta configurado no se abre ningun recurso de red.
    private static HttpClient http;

    /** Wompi se usa solo si config.properties trae las llaves; si no, la app usa la simulacion local. */
    public static boolean Configurada() {
        return !HelperConfig.Obtener("wompi.llave.publica").isBlank() && !HelperConfig.Obtener("wompi.secreto.integridad").isBlank();
    }

    /** Firma de integridad: SHA-256 de referencia + monto en centavos + moneda + secreto. */
    public static String Firma(String referencia, long centavos, String moneda, String secreto) {
        return HelperCifrado.CifrarSHA256(referencia + centavos + moneda + secreto);
    }

    /**
     * Cobra el total y devuelve el id de la transaccion aprobada.
     * Lanza IllegalStateException con el motivo si Wompi la rechaza o falla.
     */
    public static String Cobrar(String titular, String numero, String mes, String anio, String cvc,
            String correo, String referencia, long totalPesos) throws Exception {
        String llave = HelperConfig.Obtener("wompi.llave.publica");
        long centavos = totalPesos * 100;

        JsonObject comercio = pedir(HttpRequest.newBuilder(URI.create(URL + "/merchants/info"))
                .header("x-merchant-public-key", llave).GET()).getAsJsonObject("data");

        // shortcut: la tarjeta se envia sin cifrar (modo simple de Wompi); en produccion usar el payload JWE.
        String token = pedir(post("/tokens/cards", llave, Map.of("number", numero, "cvc", cvc,
                "exp_month", mes, "exp_year", anio, "card_holder", titular)))
                .getAsJsonObject("data").get("id").getAsString();

        Map<String, Object> cuerpo = Map.of(
                "acceptance_token", comercio.getAsJsonObject("presigned_acceptance").get("acceptance_token").getAsString(),
                "accept_personal_auth", comercio.getAsJsonObject("presigned_personal_data_auth").get("acceptance_token").getAsString(),
                "amount_in_cents", centavos,
                "currency", "COP",
                "signature", Firma(referencia, centavos, "COP", HelperConfig.Obtener("wompi.secreto.integridad")),
                "customer_email", correo,
                "reference", referencia,
                "payment_method", Map.of("type", "CARD", "installments", 1, "token", token));
        String id = pedir(post("/transactions", llave, cuerpo)).getAsJsonObject("data").get("id").getAsString();

        // La transaccion nace PENDING; se consulta hasta un estado final (maximo ~30 s).
        for (int i = 0; i < 30; i++) {
            JsonObject tx = pedir(HttpRequest.newBuilder(URI.create(URL + "/transactions/" + id)).GET()).getAsJsonObject("data");
            String estado = tx.get("status").getAsString();
            if (estado.equals("APPROVED")) {
                return id;
            }
            if (!estado.equals("PENDING")) {
                String motivo = tx.has("status_message") && !tx.get("status_message").isJsonNull()
                        ? tx.get("status_message").getAsString() : estado;
                throw new IllegalStateException("Wompi rechazó la transacción: " + motivo);
            }
            Thread.sleep(1000);
        }
        throw new IllegalStateException("Wompi no confirmó el pago a tiempo (transacción " + id + "). No se registró el pedido.");
    }

    private static HttpRequest.Builder post(String ruta, String llave, Object cuerpo) {
        return HttpRequest.newBuilder(URI.create(URL + ruta))
                .header("Authorization", "Bearer " + llave)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(cuerpo)));
    }

    private static JsonObject pedir(HttpRequest.Builder req) throws Exception {
        if (http == null) {
            http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        }
        HttpResponse<String> r = http.send(req.timeout(Duration.ofSeconds(20)).build(), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() / 100 != 2) {
            throw new IllegalStateException("Wompi respondió " + r.statusCode() + ": " + r.body());
        }
        return GSON.fromJson(r.body(), JsonObject.class);
    }
}
