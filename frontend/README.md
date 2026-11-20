# Tienda electrónica local

Con MySQL de XAMPP encendido y Node.js 24, abre una terminal en frontend y ejecuta:

    npm ci
    npm run dev

Abre la dirección Local que imprime Vite. Tanto el catálogo como la API funcionan en esa dirección. Para comprobar el inventario, abre /api/productos en el mismo origen. Una lista JSON y HTTP 200 indican que el servidor pudo consultar MySQL. Si devuelve 503, consulta el código de error en la terminal y revisa mysql.url, mysql.usuario y mysql.clave en config.properties junto al pom.xml.

Para usar el servidor de la versión compilada, ejecuta npm run build y luego npm start. Abre http://127.0.0.1:4173. El usuario administrativo es admin y la clave temporal aparece en la terminal salvo que definas TIENDA_ADMIN_CLAVE.
