# CalSoft — Sistema POS (versión Java)

**CalSoft** es un sistema administrativo / punto de venta para restaurantes.
Proyecto del curso **Calidad de Software**.

Esta es la versión reescrita en **Java web** (Servlets + JSP + JSTL) con
**PostgreSQL**, lista para abrir en **Apache NetBeans**. La versión original
estaba en JavaScript (Node.js + Express en el backend y React en el frontend).

> 📘 **¿Primera vez?** Sigue la guía paso a paso
> **[INSTALACION.md](INSTALACION.md)**: programas necesarios, base de datos,
> NetBeans, Tomcat, tareas comunes con PostgreSQL y solución de problemas.

---

## Stack

| Capa       | Tecnología                                         |
|------------|----------------------------------------------------|
| Vistas     | JSP + JSTL, CSS propio, íconos lucide              |
| Lógica     | Servlets (Jakarta EE 10), patrón MVC + DAO         |
| Datos      | PostgreSQL vía JDBC (consultas parametrizadas)     |
| Seguridad  | Contraseñas con **BCrypt**, sesión HTTP + filtro, token CSRF |
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
    │   ├── modelo/      Usuario, Pedido, Reserva, Producto, Resumen, Conteo
    │   ├── dao/         UsuarioDAO, PedidoDAO, ReservaDAO, InventarioDAO, ReporteDAO
    │   ├── controlador/ Servlets: Login, Logout, Dashboard, Pedido, Reserva,
    │   │                Inventario, Usuario, Cocina, Reporte (+ ServletBase)
    │   ├── filtro/      AutenticacionFiltro.java → protege páginas y valida CSRF
    │   └── util/        Formulario.java (validación), ValidacionException
    ├── resources/
    │   ├── db.properties.example → plantilla; cópiala como db.properties  ← AJUSTAR
    │   └── schema.sql     → esquema de la base de datos
    └── webapp/
        ├── WEB-INF/web.xml
        ├── WEB-INF/vistas/   → páginas JSP (no accesibles directamente)
        ├── css/estilos.css
        ├── js/app.js
        └── img/logo.png
```

| Proyecto original (JS)               | Equivalente en Java                         |
|--------------------------------------|---------------------------------------------|
| `backend/src/routes` + `controllers` | `controlador/*Servlet.java`                 |
| consultas en los controllers         | `dao/*DAO.java`                             |
| `middleware/auth.js` (JWT)           | `filtro/AutenticacionFiltro.java` (sesión)  |
| `.env` / `.env.example`              | `resources/db.properties` / `.example`      |
| `npm run seed`                       | `config/InicializadorBD.java` (automático)  |
| `frontend/src/pages/*.jsx`           | `webapp/WEB-INF/vistas/*.jsp`               |
| Tailwind CSS                         | `webapp/css/estilos.css`                    |

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
`restaurante`** si no existe, aplica `schema.sql` y siembra el usuario admin
y los datos demo (solo si las tablas están vacías).

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

| Módulo     | Ruta          | Descripción                                                       |
|------------|---------------|-------------------------------------------------------------------|
| Inicio     | `/dashboard`  | Resumen, pedidos recientes y próximas reservas.                   |
| Pedidos    | `/pedidos`    | Registro, cambio de estado, filtro por mesero y resumen.          |
| Reservas   | `/reservas`   | Reservas con fecha/hora, personas y estado.                       |
| Inventario | `/inventario` | Productos, stock, precio y alerta de bajo stock.                  |
| Cocina     | `/cocina`     | Pedidos activos; se recarga cada 10 s.                            |
| Usuarios   | `/usuarios`   | Alta (clave hasheada), cambio de rol, reseteo de clave.           |
| Reportes   | `/reportes`   | Agregados de pedidos, reservas, inventario y usuarios.            |

## Seguridad

- Contraseñas guardadas con **BCrypt** (nunca en texto plano).
- Todas las páginas, salvo el login, exigen sesión iniciada (`AutenticacionFiltro`).
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
