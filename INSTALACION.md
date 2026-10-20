# Guía de instalación — CalSoft (Angular + Spring Boot)

Pasos para dejar CalSoft funcionando en Windows desde cero.

---

## 1. Programas necesarios

| Programa | Versión | Para qué |
|----------|---------|----------|
| **JDK** (Eclipse Temurin) | 17 o superior (recomendado 21) | Backend Spring Boot |
| **Apache NetBeans** | 20 o superior | Abrir y ejecutar el backend (trae Maven) |
| **Node.js** | 20.19+ / 22 / 24 (LTS) | Frontend Angular |
| **PostgreSQL** | 14 o superior | Base de datos (incluye pgAdmin 4) |
| **Git** | cualquiera | Descargar el repositorio |
| VS Code (opcional) | — | Editar el frontend Angular |

Instalación rápida con winget (PowerShell):

```powershell
winget install --id EclipseAdoptium.Temurin.21.JDK -e
winget install --id FriendsOfApacheNetBeans.NetBeans -e
winget install --id OpenJS.NodeJS.LTS -e
winget install --id PostgreSQL.PostgreSQL.17 -e
winget install --id Git.Git -e
```

> Al instalar PostgreSQL **anota la contraseña del usuario `postgres`**.
> Ya **no hace falta Tomcat**: Spring Boot trae su propio servidor.

## 2. Descargar el proyecto

```bash
git clone https://github.com/EduardoZA03/Soluciones-Wed.git
```

## 3. Base de datos

No hay que crear nada a mano. Al arrancar, el backend:

1. Crea la base **`restaurante`** si no existe.
2. Aplica `backend/src/main/resources/schema.sql` (idempotente; también
   actualiza bases de versiones anteriores sin perder datos).
3. Crea el usuario **`admin` / `1234`**.
4. Si las tablas están vacías, siembra datos demo: categorías, proveedores,
   insumos, carta con recetas, reservas, pedidos y 8 semanas de consumo
   histórico para la IA.

Solo necesitas que PostgreSQL esté encendido (`Get-Service postgresql*` debe
decir `Running`) y la clave correcta (paso 4).

## 4. Configurar la clave de PostgreSQL

Si tu clave de `postgres` **no** es `postgres`:

1. En la carpeta `backend`, copia `application-local.properties.example` como
   **`application-local.properties`**.
2. Pon tu clave:
   ```properties
   db.password=TU_CLAVE
   ```

Ese archivo no se sube a GitHub. También sirven las variables de entorno
`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`.

## 5. Ejecutar el backend (NetBeans)

1. **File → Open Project…** → selecciona la carpeta **`backend`** (ícono de taza ☕).
2. La primera vez descarga las dependencias (1–3 minutos).
3. Clic derecho en el proyecto → **Run** (o `F6`). Si pregunta la clase
   principal, elige `com.calsoft.CalSoftApplication`.
4. En la pestaña **Output** debe aparecer:
   ```
   Tomcat started on port 8081
   CalSoft: base de datos lista
   ```

Sin NetBeans, desde una terminal en `backend`:

```bash
mvn spring-boot:run
```

> El backend usa el puerto **8081** para no chocar con un Tomcat que use el 8080.
> Para cambiarlo: variable de entorno `PORT` o `server.port` en `application.properties`.

## 6. Ejecutar el frontend (Angular)

En una terminal (PowerShell, CMD o la de VS Code):

```bash
cd frontend
npm install
npm start
```

`npm install` solo hace falta la primera vez. Cuando diga
`Local: http://localhost:4200/`, abre esa dirección y entra con **`admin` / `1234`**.

El frontend envía las llamadas `/api/...` al backend gracias a
`frontend/proxy.conf.json`, así que **los dos tienen que estar encendidos**.

---

## 7. Probar la API sin el frontend

Con el backend encendido:

```bash
curl -X POST http://localhost:8081/api/auth/login -H "Content-Type: application/json" -d "{\"usuario\":\"admin\",\"clave\":\"1234\"}"
```

Copia el `token` de la respuesta y úsalo en las demás llamadas:

```bash
curl http://localhost:8081/api/categorias -H "Authorization: Bearer TOKEN"
```

Todos los endpoints están en [docs/API.md](docs/API.md). También puedes usar
Postman o Thunder Client (VS Code).

---

## 8. Tareas comunes con la base de datos

Con psql (`"C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -h localhost -d restaurante`)
o con pgAdmin → Query Tool:

```sql
-- Ver los datos
SELECT * FROM categorias;
SELECT id, producto, stock, stock_minimo, fecha_vencimiento FROM inventario;

-- Recuperar la clave del admin (vuelve a 1234 al reiniciar el backend)
DELETE FROM usuarios WHERE usuario = 'admin';
```

Empezar de cero (detén el backend antes):

```powershell
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -h localhost -c "DROP DATABASE restaurante;"
```

## 9. Solución de problemas

| Síntoma | Solución |
|---------|----------|
| `Port 8081 was already in use` | Otro backend sigue abierto: ciérralo, o cambia el puerto con la variable `PORT` (y en `frontend/proxy.conf.json`). |
| `la autentificación password falló para el usuario «postgres»` | Pon tu clave en `backend/application-local.properties` (paso 4). |
| `Connection refused` a PostgreSQL | `Start-Service postgresql-x64-17` |
| En el login: "No se pudo conectar con el servidor" | El backend no está encendido (paso 5). |
| `ng` / `npm` no se reconoce | Instala Node.js y abre una terminal nueva. |
| Tras actualizar el proyecto falla Angular | Borra `frontend/node_modules` y vuelve a ejecutar `npm install`. |
| "Tu rol no tiene permiso para esta acción" | Entra con un usuario Administrador o cambia el rol en Usuarios. |

### Olvidé la contraseña de `postgres`

1. Bloc de notas **como administrador** → `C:\Program Files\PostgreSQL\17\data\pg_hba.conf`
   (haz una copia antes) → en las líneas `127.0.0.1/32` y `::1/128` cambia `scram-sha-256` por `trust`.
2. PowerShell como administrador:
   ```powershell
   Restart-Service postgresql-x64-17
   & "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -h localhost -c "ALTER USER postgres PASSWORD 'NuevaClave';"
   ```
3. **Vuelve a poner `scram-sha-256`** en `pg_hba.conf` y reinicia el servicio.
