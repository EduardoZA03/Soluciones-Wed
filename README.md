# CalSoft — Sistema POS (versión Java)

**CalSoft** es un sistema administrativo / punto de venta para restaurantes,
con **gestión de inventario con inteligencia artificial e interoperabilidad
con proveedores** (caso: restaurante *La Crianza Arequipeña*).
Curso **Soluciones Web y Aplicaciones Distribuidas**.

Esta es la versión en **Java web** (Servlets + JSP + JSTL) con **PostgreSQL**,
lista para abrir en **Apache NetBeans**. La versión original estaba en
JavaScript (Node.js + Express en el backend y React en el frontend).

> 📗 **Historias de usuario:** [docs/HISTORIAS_USUARIO.md](docs/HISTORIAS_USUARIO.md)
> explica cómo se cumple cada HU (HU-01 a HU-09) y cómo probarla.
> 🗂️ **Modelo de BD:** [docs/MODELO_BD.md](docs/MODELO_BD.md) (diagrama entidad-relación).

> 📘 **¿Primera vez?** Sigue la guía paso a paso
> **[INSTALACION.md](INSTALACION.md)**: programas necesarios, base de datos,
> NetBeans, Tomcat, tareas comunes con PostgreSQL y solución de problemas.

---

## Stack

| Capa       | Tecnología                                         |
|------------|----------------------------------------------------|
| Vistas     | JSP + JSTL, **Bootstrap 5.3** + Bootstrap Icons (WebJars, sin CDN) |
| Lógica     | Servlets (Jakarta EE 10), patrón MVC + DAO + capa de servicios con transacciones |
| Datos      | PostgreSQL vía JDBC (consultas parametrizadas)     |
| IA         | Pronóstico de consumo: suavizado exponencial con estacionalidad semanal |
| Interoperabilidad | API REST JSON con proveedores (`java.net.http` + **Gson**) |
| QR         | Generación con **ZXing**, lectura con cámara (**html5-qrcode**) o lectora USB |
| Seguridad  | Contraseñas con **BCrypt**, sesión HTTP + filtro, permisos por rol, token CSRF |
| Build      | Maven (proyecto `war`)                             |
| Servidor   | Apache Tomcat 10.1 o superior                      |

## Estructura

```
CalSoft-Java/
├── pom.xml                                  → dependencias Maven
└── src/main/
    ├── java/com/calsoft/
    │   ├── config/      Conexion.java         → conexión JDBC (lee db.properties)
    │   │                InicializadorBD.java  → crea BD, tablas y datos demo al arrancar
    │   │                Transaccion.java      → varias consultas en una sola transacción
    │   ├── modelo/      Usuario, Pedido, Reserva, Producto (insumo), Plato, RecetaItem,
    │   │                Movimiento, Alerta, Proveedor, OrdenCompra, OrdenItem,
    │   │                OrdenEvento, Prediccion, Resumen, Conteo
    │   ├── dao/         Un DAO por tabla: consultas SQL
    │   ├── servicio/    InventarioService (descuento por receta, movimientos),
    │   │                OrdenService (ciclo de la orden), PrediccionService (IA)
    │   ├── integracion/ ProveedorApiCliente + JSON de solicitud y respuesta
    │   ├── api/         ProveedorSimuladoServlet → API REST que imita a un proveedor
    │   ├── controlador/ Un servlet por pantalla (+ ServletBase, QrServlet)
    │   ├── filtro/      AutenticacionFiltro.java → sesión, permisos por rol y CSRF
    │   └── util/        Permisos (rol → módulos), Formulario (validación)
    ├── resources/
    │   ├── db.properties.example → plantilla; cópiala como db.properties  ← AJUSTAR
    │   └── schema.sql     → esquema de la base de datos
    └── webapp/
        ├── WEB-INF/web.xml
        ├── WEB-INF/vistas/   → páginas JSP (no accesibles directamente)
        │   └── plantilla/    → cabecera (menú lateral) y pie comunes
        ├── WEB-INF/tags/     → componentes JSP reutilizables (stat.tag)
        ├── css/estilos.css   → colores de la marca sobre Bootstrap
        ├── js/app.js         → comportamiento de la interfaz (modales, confirmaciones)
        ├── js/recepcion.js   → lector de QR con la cámara
        └── img/logo.png
docs/
├── HISTORIAS_USUARIO.md → HU-01 a HU-09: dónde están y cómo probarlas
└── MODELO_BD.md         → diagrama entidad-relación
```

### Separación por capas

Cada cosa vive en su propia carpeta y no se mezclan:

| Capa | Dónde | Contiene |
|------|-------|----------|
| **Programación (lógica)** | `src/main/java` | Servlets, DAO, modelos, validación, seguridad |
| **Datos** | `src/main/resources` | `schema.sql`, configuración de conexión |
| **Vistas (estructura HTML)** | `src/main/webapp/WEB-INF/vistas` | Páginas JSP, sin SQL ni lógica de negocio |
| **Diseño** | `src/main/webapp/css` + Bootstrap | Estilos; Bootstrap llega por Maven (`pom.xml`) |
| **Comportamiento** | `src/main/webapp/js` | JavaScript de la interfaz (no accede a datos) |
| **Imágenes** | `src/main/webapp/img` | Logo e imágenes |

Las JSP no tienen bloques `<style>` ni `<script>` propios: los estilos van en
`estilos.css` y el JS en `app.js` / `recepcion.js`. La única excepción es el
ancho de las barras de Reportes, que Bootstrap define con `style="width: …%"`.

| Proyecto original (JS)               | Equivalente en Java                         |
|--------------------------------------|---------------------------------------------|
| `backend/src/routes` + `controllers` | `controlador/*Servlet.java`                 |
| consultas en los controllers         | `dao/*DAO.java`                             |
| `middleware/auth.js` (JWT)           | `filtro/AutenticacionFiltro.java` (sesión)  |
| `.env` / `.env.example`              | `resources/db.properties` / `.example`      |
| `npm run seed`                       | `config/InicializadorBD.java` (automático)  |
| `frontend/src/pages/*.jsx`           | `webapp/WEB-INF/vistas/*.jsp`               |
| Tailwind CSS                         | Bootstrap 5 + `webapp/css/estilos.css`      |
| `components/ui/StatCard.jsx`         | `WEB-INF/tags/stat.tag`                     |

---

## Requisitos

1. **JDK 17 o superior** (por ejemplo, Eclipse Temurin 21).
2. **Apache NetBeans** 20 o superior.
3. **Apache Tomcat 10.1** (NetBeans puede registrarlo; ver paso 3).
4. **PostgreSQL** 14 o superior.

## Puesta en marcha en NetBeans (resumen)

Versión corta. El paso a paso completo, con los menús exactos y los errores
frecuentes, está en [INSTALACION.md](INSTALACION.md).

### 1. Configurar la base de datos

En `src/main/resources/`, copia `db.properties.example` como `db.properties`
y pon el usuario y la clave de tu PostgreSQL (`db.properties` está en
`.gitignore`, así tu clave no se sube al repositorio):

```properties
db.user=postgres
db.password=TU_CLAVE
```

No hace falta crear nada a mano: al arrancar, la aplicación **crea la base
`restaurante`** si no existe, aplica `schema.sql` (también actualiza bases de
la versión anterior sin perder datos) y siembra el usuario admin y los datos
demo: insumos, proveedores, carta con recetas y 8 semanas de consumo histórico
para la IA (solo si las tablas están vacías).

### 2. Abrir el proyecto

`File → Open Project…` y selecciona la carpeta `CalSoft-Java`. NetBeans lo
reconoce como proyecto Maven (ícono de taza) y descarga las dependencias.

### 3. Registrar Tomcat (solo la primera vez)

1. Descarga Tomcat 10.1 (zip) desde https://tomcat.apache.org y descomprímelo.
2. En NetBeans: `Tools → Servers → Add Server… → Apache Tomcat or TomEE`.
3. Elige la carpeta de Tomcat y crea un usuario/clave de administrador.

### 4. Ejecutar

Clic derecho en el proyecto → **Run** (o `F6`). Si pregunta el servidor,
elige el Tomcat registrado. Se abrirá:

**http://localhost:8080/CalSoft**

| Usuario | Clave  |
|---------|--------|
| `admin` | `1234` |

---

## Módulos

| Módulo | Ruta | Descripción | HU |
|--------|------|-------------|----|
| Inicio | `/dashboard` | Resumen, alertas, órdenes en curso, pedidos y reservas. | — |
| Pedidos (POS) | `/pedidos` | Venta de platos de la carta; descuenta insumos por receta. | HU-03 |
| Cocina | `/cocina` | Pedidos activos; se recarga cada 10 s. | — |
| Reservas | `/reservas` | Reservas con fecha/hora, personas y estado. | — |
| Insumos | `/inventario` | Stock, vencimiento, proveedor; búsqueda, filtros y movimientos. | HU-02 |
| Carta y recetas | `/carta` | Platos y cantidad de cada insumo por porción. | HU-03 |
| Alertas | `/alertas` | Stock mínimo y vencimiento; marcar atendida o generar orden. | HU-04, HU-06 |
| Predicción IA | `/prediccion` | Consumo estimado, cantidad sugerida, aceptar/ajustar. | HU-05 |
| Kardex | `/movimientos` | Historial de entradas, salidas, mermas y ajustes. | HU-02 |
| Órdenes de compra | `/compras` | Borrador → aprobación → envío por API → confirmación; línea de tiempo y QR. | HU-06, HU-07, HU-09 |
| Recepción | `/recepcion` | Lectura del QR / código de barras y validación de cantidades. | HU-08 |
| Proveedores | `/proveedores` | Contacto, URL y API key de cada proveedor. | HU-07 |
| Usuarios | `/usuarios` | Alta (clave hasheada), cambio de rol, reseteo de clave. | HU-01 |
| Reportes | `/reportes` | Pedidos, reservas, inventario, consumo, mermas y proyección de compras. | — |

### Roles (HU-01)

| Rol | Módulos |
|-----|---------|
| Administrador | Todos. Es el único que aprueba órdenes de compra. |
| Almacén | Insumos, Alertas, Kardex, Órdenes de compra (borradores), Recepción |
| Cocina | Cocina, Insumos, Carta y recetas |
| Mesero / Empleado | Pedidos (POS), Reservas |

Los permisos están en un solo lugar: [`util/Permisos.java`](src/main/java/com/calsoft/util/Permisos.java).

### API del proveedor simulado

`POST /CalSoft/api/proveedor/ordenes` con la cabecera `X-Api-Key` (las claves
de demostración están en la tabla `proveedores`). El formato JSON y las reglas
están en [docs/HISTORIAS_USUARIO.md](docs/HISTORIAS_USUARIO.md#proveedor-simulado-api).

## Seguridad

- Contraseñas guardadas con **BCrypt** (nunca en texto plano).
- Todas las páginas, salvo el login, exigen sesión iniciada (`AutenticacionFiltro`).
- Cada rol solo entra a sus módulos; el menú oculta el resto.
- La API del proveedor se autentica con API key (no usa la sesión).
- Al iniciar sesión se crea una sesión nueva (evita fijación de sesión).
- Cada formulario lleva un **token CSRF** que el filtro verifica.
- Consultas SQL siempre con `PreparedStatement` (sin inyección SQL).
- Toda la información del usuario se escapa en las JSP (`fn:escapeXml`) contra XSS.
- Un usuario no puede eliminar su propia cuenta.

## Cómo agregar un módulo nuevo

1. **BD**: agrega la tabla en `src/main/resources/schema.sql`.
2. **Modelo**: crea la clase en `modelo/`.
3. **DAO**: crea `dao/<Modulo>DAO.java` con sus consultas.
4. **Servlet**: crea `controlador/<Modulo>Servlet.java` con `@WebServlet("/<modulo>")`
   extendiendo `ServletBase`.
5. **Vista**: crea `WEB-INF/vistas/<modulo>.jsp` incluyendo la cabecera y el pie,
   y agrega el enlace en `WEB-INF/vistas/plantilla/cabecera.jspf`.
