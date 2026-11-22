# Tienda Electrónica — Calidad de Software (Grupo 1)

[![Pruebas](https://github.com/AlejitoB/TiendaElectronica/actions/workflows/pruebas.yml/badge.svg)](https://github.com/AlejitoB/TiendaElectronica/actions/workflows/pruebas.yml)

Aplicación de escritorio en Java 17 (Swing + FlatLaf) para la gestión de clientes, inventario de artículos de computador y venta con pasarela de pagos simulada. Usa persistencia dual: **Firebase (nube)** o **MySQL (local)**, elegida al iniciar.

## Requisitos

- JDK 17 y Maven 3.9+
- Para modo nube: el archivo de cuenta de servicio de Firebase (`tienda-electronica.json`), que **no** se sube al repositorio
- Para modo local: MySQL/XAMPP con una base `prueba` (las tablas `producto` y `pedido` se crean solas; `persona` debe existir como antes)

## Configuración

1. Copie `config.example.properties` como `config.properties` (está en `.gitignore`).
2. Ajuste la ruta de Firebase, la conexión MySQL y los usuarios. Las contraseñas se guardan como hash SHA-256:
   ```bash
   python -c "import hashlib;print(hashlib.sha256(b'MiClave').hexdigest())"
   ```
3. Cada clave también puede venir de una variable de entorno: `TIENDA_MYSQL_URL`, `TIENDA_ADMIN_CLAVE_SHA256`, etc.

Si no existe `config.properties` se usan los valores de ejemplo (`Admin` / `12345` y `Cliente` / `12345`).

## Ejecutar y probar

```bash
mvn compile exec:java
```

```bash
mvn test
```

Las pruebas no necesitan XAMPP ni internet: los DAO se prueban contra una base **H2 en memoria** (modo MySQL). Al terminar, el reporte de cobertura queda en `target/site/jacoco/index.html`.

**Integración continua:** GitHub Actions corre `mvn test` en cada push a `master` y en cada Pull Request (pestaña *Actions*). El resumen de cobertura por paquete aparece en cada ejecución y el reporte completo se descarga como artefacto `reporte-cobertura-jacoco`.

Flujo: **Acceso unificado (Base Nube / Local + Inicio de sesión) → Ventana principal por rol**. El administrador accede a **Inventario**, **Pedidos**, **Clientes** y **Auditoría**; el cliente accede a **Tienda** y **Mis Pedidos**.

| Rol | Qué puede hacer |
|-----|-----------------|
| Administrador | **Inventario** (crear, editar, eliminar, buscar, stock bajo), **Pedidos** (historial de ventas y métricas), **Clientes** (CRUD unificado en Firebase o MySQL) y **Auditoría** |
| Cliente | **Tienda**: catálogo por tarjetas, carrito, reserva atómica de stock y pago con Wompi / simulación; **Mis Pedidos** |

### Pasarela de pagos (Wompi Sandbox o simulada)

Con `wompi.llave.publica` y `wompi.secreto.integridad` en `config.properties`, el pago se procesa en el **ambiente Sandbox de Wompi** (Bancolombia): tokeniza la tarjeta, crea la transacción con firma de integridad y espera su estado final. Sin esas llaves, la app usa una simulación local con las mismas reglas. En ningún caso se cobra dinero real.

Las llaves se obtienen gratis en [comercios.wompi.co](https://comercios.wompi.co) → Desarrolladores, en modo **Sandbox** (`pub_test_...` y `test_integrity_...`).

Antes de cobrar se valida titular, correo, número (algoritmo de Luhn), vencimiento `MM/AA`, CVV y el stock disponible.

| Tarjeta de prueba (Sandbox de Wompi) | Resultado |
|-------------------|-----------|
| `4242 4242 4242 4242` | Aprobada |
| `4111 1111 1111 1111` | Rechazada |

Al aprobarse, el stock se descuenta y el pedido se guarda **en una sola transacción**: si un artículo ya no alcanza, no se descuenta nada. De la tarjeta solo se guarda la franquicia y los últimos 4 dígitos.

## Estructura

| Paquete | Capa | Contenido |
|---------|------|-----------|
| `GUI_Admin`, `GUI_Cliente`, `GUI_Local` | Presentación | Interfaz unificada Nordic Bento POS en código puro (`VentanaAcceso`, `VentanaPrincipal`, `InventarioAdministrador`, `PanelPedidos`, `PanelClientes`, `PanelAuditoria`, `TiendaCliente`, `PasarelaPago`) |
| `Logica_Negocio` | Negocio | `Persona`, `Producto`, `Usuario`, `Carrito`, `Pedido`, `ResumenPedido`, `ControlIntentos`, `CatalogoInicial` |
| `Helpers` | Negocio | Validación, cifrado (`PBKDF2` + `SHA-256`), pagos, auditoría (`HelperAuditoria`, `LectorAuditoria`), configuración, sistema visual (`TemaBento`, `ComponentesBento`, `HelperUI`) |
| `Logica_Conexion` | Datos | `Conexion` (conexiones independientes por operación), `ServicioPersona` / `PersonaDAO` / `PersonaProvider`, `DAOInterfaceProducto` con `ProductoProvider` (Firestore) y `ProductoDAO` (MySQL) |

## Brechas de ISO/IEC 25010 atendidas (Tabla 7 del avance)

| Característica | Acción |
|----------------|--------|
| Adecuación funcional | Historial de auditoría en `auditoria.log` para actualizar, eliminar y vender |
| Eficiencia de desempeño | Carga del inventario medida y mostrada en pantalla; operaciones en segundo plano |
| Compatibilidad | Inventario y ventas funcionan igual en Firebase y MySQL (mismo `DAOInterfaceProducto`) |
| Usabilidad | Tema unificado; los mensajes nombran el campo y el tipo de error |
| Fiabilidad | Venta transaccional (0 registros a medias); si la nube cae se ofrece la base local |
| Seguridad | Credenciales fuera del código; bloqueo al 3.er intento con mensaje genérico; confirmación al eliminar |
| Mantenibilidad | 31 pruebas unitarias (JUnit 5) sobre validación, pagos, reserva de stock, carrito, login persistente, clientes y auditoría |
| Portabilidad | `firebase-admin` 9.11.0 y `mysql-connector-j` 9.4.0; clase principal corregida |

> **Importante:** la llave de Firebase anterior estuvo publicada en el historial de git y Google la desactivó. El proyecto usa ahora un proyecto de Firebase nuevo; su llave (`tienda-electronica.json`) se comparte por privado y nunca se sube al repositorio. Se genera en la consola de Firebase → Configuración del proyecto → Cuentas de servicio → *Generar nueva clave privada*.
