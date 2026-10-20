# CalSoft — Gestión de inventario con IA e interoperabilidad

Sistema web para el restaurante **La Crianza Arequipeña**: inventario de
insumos con **inteligencia artificial** (predicción de reposición) e
**interoperabilidad con proveedores** (órdenes de compra por API), además de
POS, cocina, reservas y reportes.

Curso **Soluciones Web y Aplicaciones Distribuidas** — Universidad Privada del Norte.

> 📘 **Instalación paso a paso:** [INSTALACION.md](INSTALACION.md)
> 📗 **Historias de usuario (HU-01 a HU-09):** [docs/HISTORIAS_USUARIO.md](docs/HISTORIAS_USUARIO.md)
> 🔌 **API REST (todos los endpoints):** [docs/API.md](docs/API.md)
> 🗂️ **Modelo de base de datos:** [docs/MODELO_BD.md](docs/MODELO_BD.md)

---

## Arquitectura

```
┌─────────────────────────┐   HTTP + JSON    ┌──────────────────────────────┐   JDBC   ┌────────────┐
│ Frontend  Angular 20    │ ───────────────▶ │ Backend  Spring Boot 3.5     │ ───────▶ │ PostgreSQL │
│ Bootstrap 5 · puerto    │  token JWT en    │ API REST · JPA · Security    │          │ restaurante│
│ 4200                    │  cada petición   │ puerto 8081                  │          └────────────┘
└─────────────────────────┘                  │        │ HTTP POST JSON (X-Api-Key)
                                             │        ▼
                                             │  API del proveedor (simulado incluido)
                                             └──────────────────────────────┘
```

| Capa | Tecnología |
|------|------------|
| Frontend | **Angular 20** (componentes standalone, signals, formularios reactivos, lazy loading), **Bootstrap 5.3**, Bootstrap Icons, html5-qrcode |
| Backend | **Spring Boot 3.5** (Java 17): Spring Web (API REST), **Spring Data JPA** (Hibernate), Bean Validation, **Spring Security + JWT** |
| Base de datos | PostgreSQL 14+ (el backend crea la base, aplica el esquema y siembra datos demo) |
| IA | Pronóstico de consumo: suavizado exponencial con estacionalidad semanal |
| Interoperabilidad | Cliente REST (`RestClient`) que envía órdenes JSON al proveedor; proveedor simulado incluido |
| QR | Generación con ZXing (backend), lectura con la cámara (html5-qrcode) o lectora USB |

## Estructura del repositorio

```
CalSoft-Java/
├── backend/                         ← se abre en NetBeans (proyecto Maven)
│   ├── pom.xml
│   ├── application-local.properties.example   → copia como application-local.properties
│   └── src/main/
│       ├── java/com/calsoft/
│       │   ├── CalSoftApplication.java    → arranque
│       │   ├── config/        crea la BD, aplica schema.sql y datos demo
│       │   ├── modelo/        entidades JPA (Categoria, Proveedor, Insumo, Plato, Pedido, OrdenCompra…)
│       │   ├── repositorio/   repositorios Spring Data JPA (CRUD automático)
│       │   ├── servicio/      lógica: inventario, alertas, IA, órdenes, reportes
│       │   ├── controlador/   controladores REST (/api/...)
│       │   ├── integracion/   cliente de la API del proveedor
│       │   ├── seguridad/     JWT, roles y permisos
│       │   ├── dto/           datos de entrada/salida con validaciones
│       │   └── error/         respuestas de error {"mensaje": "..."}
│       └── resources/
│           ├── application.properties
│           └── schema.sql
├── frontend/                        ← Angular (VS Code o NetBeans)
│   ├── proxy.conf.json              → /api → http://localhost:8081
│   └── src/app/
│       ├── core/          servicios HTTP, sesión, interceptor JWT, guardas, modelos
│       ├── layout/        menú lateral filtrado por rol
│       ├── compartido/    componentes reutilizables (tarjeta, modal)
│       └── paginas/       una carpeta por pantalla (.ts lógica + .html vista)
└── docs/
```

## Tablas maestras (CRUD completos)

| Tabla maestra | Pantalla | API | Operaciones |
|---------------|----------|-----|-------------|
| **Categorías** | `/categorias` | `/api/categorias` | Listar, buscar, crear, editar, eliminar (no deja borrar si algún insumo la usa) |
| **Proveedores** | `/proveedores` | `/api/proveedores` | Listar, buscar, crear, editar, eliminar; RUC de 11 dígitos, email, URL y API key |
| **Insumos** | `/insumos` | `/api/insumos` | Listar con búsqueda y filtros (categoría, estado), crear, editar, eliminar, movimientos de stock |

Cada CRUD tiene: entidad JPA → repositorio Spring Data → servicio con reglas →
controlador REST con `@Valid` → servicio Angular → página con formulario
reactivo y validaciones en pantalla.

## Módulos

| Módulo | Ruta | HU |
|--------|------|----|
| Inicio | `/inicio` | — |
| Pedidos (POS) — descuenta insumos por receta | `/pedidos` | HU-03 |
| Cocina | `/cocina` | — |
| Reservas | `/reservas` | — |
| Insumos · Categorías · Proveedores (maestras) | `/insumos` `/categorias` `/proveedores` | HU-02, HU-07 |
| Carta y recetas | `/carta` | HU-03 |
| Alertas de stock mínimo y vencimiento | `/alertas` | HU-04, HU-06 |
| Predicción IA | `/prediccion` | HU-05 |
| Kardex | `/kardex` | HU-02 |
| Órdenes de compra (API + trazabilidad + QR) | `/compras` | HU-06, HU-07, HU-09 |
| Recepción por QR | `/recepcion` | HU-08 |
| Usuarios y roles | `/usuarios` | HU-01 |
| Reportes | `/reportes` | — |

### Roles (HU-01)

| Rol | Módulos |
|-----|---------|
| Administrador | Todos. Único que aprueba órdenes de compra y gestiona proveedores. |
| Almacén | Insumos, Categorías, Alertas, Kardex, Órdenes de compra, Recepción |
| Cocina | Cocina, Insumos (solo lectura), Carta y recetas |
| Mesero / Empleado | Pedidos (POS), Reservas |

El backend aplica los permisos en [`SecurityConfig.java`](backend/src/main/java/com/calsoft/seguridad/SecurityConfig.java);
el frontend oculta del menú lo que el rol no puede usar.

## Puesta en marcha (resumen)

1. **Backend** — en NetBeans: **File → Open Project** → subcarpeta **`backend`**
   (no la raíz) → **F6** una sola vez → esperar `CalSoft: base de datos lista`.
   Queda en http://localhost:8081. Para reiniciarlo: **Stop** y luego F6.
2. **Frontend** — en otra terminal, dentro de `frontend`:
   ```bat
   cd /d D:\ruta\del\repo\frontend
   npm install
   npm start
   ```
   (`cd /d` es para CMD; en PowerShell basta `cd`). `npm install` solo la primera vez.
3. Abre **http://localhost:4200** y entra con `admin` / `1234`.

Los dos (backend y frontend) tienen que quedar encendidos mientras uses la app.

El detalle (requisitos, clave de PostgreSQL, problemas comunes) está en [INSTALACION.md](INSTALACION.md).

## Seguridad

- Contraseñas con **BCrypt**; sesión **sin estado** con **JWT** (8 horas).
- Cada ruta de la API exige el rol adecuado (Spring Security).
- Validación de datos en el backend (`@Valid`) y en el frontend (formularios reactivos).
- Consultas parametrizadas (JPA / JdbcTemplate): sin inyección SQL.
- La API key de los proveedores nunca se devuelve en el JSON.
- CORS restringido al origen del frontend.
