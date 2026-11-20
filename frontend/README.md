# Circuito · Tienda electrónica local

Para correrlo deben entrar directamente a la carpeta frontend y correr `npm install`

## Abrir la tienda

Con Node.js 24 instalado, abrir una terminal y ejecutar `npm start`. Abrir http://127.0.0.1:4173 en el navegador. La versión compilada funciona sin instalar dependencias y sin conexión a Internet. Detener con Ctrl+C.

El enlace Administración abre el formulario local. El usuario es `admin`. La consola del servidor muestra una clave aleatoria para esa ejecución. Para conservar una clave propia, definir la variable de entorno `TIENDA_ADMIN_CLAVE` antes del arranque. La sesión administrativa dura una hora y se invalida al cerrar sesión o reiniciar el servidor.

## Alcance del prototipo

Las fichas reproducen los 15 artículos de CatalogoInicial.java. Los gráficos son ilustraciones de categoría. La tienda conserva carrito, existencias simuladas y último comprobante en sessionStorage, dentro de la pestaña. Se puede recargar durante el recorrido. Una sesión nueva e independiente permite repetir la demostración con stock inicial.

El pago simulado permite elegir aprobación o rechazo sin introducir datos bancarios. La sesión administrativa se comprueba en el servidor local. La integración con los DAO, los usuarios Java y el inventario compartido corresponde a la siguiente fase. El servidor local de Node permite ejecutar y evaluar la maqueta de forma independiente.

## Desarrollo

Con Node.js 24, ejecutar `npm ci` y `npm run build` para regenerar la carpeta compilada. Usar `npm start` para probar todas las rutas, incluido el acceso administrativo.
