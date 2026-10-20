# Guía de instalación — CalSoft (Java)

Pasos para dejar CalSoft funcionando en una PC con Windows desde cero:
programas necesarios, base de datos, NetBeans, Tomcat y solución de problemas.

> Tiempo aproximado: 20–30 minutos la primera vez (la mayor parte son descargas).

---

## Índice

1. [Programas necesarios](#1-programas-necesarios)
2. [Descargar el proyecto](#2-descargar-el-proyecto)
3. [Base de datos PostgreSQL](#3-base-de-datos-postgresql)
4. [Configurar la conexión (db.properties)](#4-configurar-la-conexión-dbproperties)
5. [Abrir el proyecto en NetBeans](#5-abrir-el-proyecto-en-netbeans)
6. [Registrar Tomcat en NetBeans](#6-registrar-tomcat-en-netbeans)
7. [Ejecutar la aplicación](#7-ejecutar-la-aplicación)
8. [Tareas comunes con la base de datos](#8-tareas-comunes-con-la-base-de-datos)
9. [Solución de problemas](#9-solución-de-problemas)

---

## 1. Programas necesarios

| Programa | Versión | Para qué sirve |
|----------|---------|----------------|
| **JDK** (Eclipse Temurin) | 17 o superior (recomendado 21) | Compilar y ejecutar Java |
| **Apache NetBeans** | 20 o superior | IDE (trae Maven incluido) |
| **Apache Tomcat** | 10.1.x | Servidor web donde corre la app |
| **PostgreSQL** | 14 o superior | Base de datos (incluye pgAdmin 4) |
| **Git** | cualquiera | Descargar el repositorio (opcional) |

### Opción A: instalar con winget (rápido)

En una PowerShell:

```powershell
winget install --id EclipseAdoptium.Temurin.21.JDK -e
winget install --id FriendsOfApacheNetBeans.NetBeans -e
winget install --id PostgreSQL.PostgreSQL.17 -e
winget install --id Git.Git -e
```

Tomcat no está en winget; descárgalo a mano (ver abajo).

### Opción B: descargas manuales

- JDK: https://adoptium.net (elige *Temurin 21 LTS*, instalador `.msi`).
  Durante la instalación marca **"Set JAVA_HOME variable"**.
- NetBeans: https://netbeans.apache.org/front/main/download/
- PostgreSQL: https://www.postgresql.org/download/windows/
- Tomcat 10.1: https://tomcat.apache.org/download-10.cgi → *Core* → **64-bit Windows zip**.
  Descomprímelo en una carpeta sin espacios raros, por ejemplo
  `C:\Users\<tu-usuario>\Tomcat\apache-tomcat-10.1.xx`.

> ⚠️ Usa **Tomcat 10.1**, no Tomcat 9. El proyecto usa `jakarta.servlet`
> (Jakarta EE 10), que Tomcat 9 no soporta.

### Al instalar PostgreSQL

- El instalador pide una **contraseña para el usuario `postgres`**. **Anótala**:
  la necesitarás en el paso 4.
- Deja el puerto por defecto **5432**.
- Deja marcado **pgAdmin 4** (herramienta gráfica para ver la base de datos).

---

## 2. Descargar el proyecto

```bash
git clone https://github.com/EduardoZA03/Soluciones-Wed.git
```

O en GitHub: botón **Code → Download ZIP** y descomprímelo.

---

## 3. Base de datos PostgreSQL

### Lo que hace la aplicación sola

**No necesitas crear tablas a mano.** Cada vez que Tomcat arranca la aplicación,
la clase `InicializadorBD` hace lo siguiente:

1. Si la base **`restaurante`** no existe, la crea.
2. Ejecuta `src/main/resources/schema.sql` (crea las tablas si no existen;
   es seguro ejecutarlo muchas veces).
3. Crea el usuario **`admin`** con clave **`1234`** si no existe.
4. Si las tablas están vacías, inserta datos de ejemplo
   (6 productos, 3 reservas y 3 pedidos).

Lo único que necesitas es que PostgreSQL esté **encendido** y que
`db.properties` tenga la **clave correcta** (paso 4).

### Comprobar que PostgreSQL está encendido

En PowerShell:

```powershell
Get-Service postgresql*
```

Debe decir `Running`. Si dice `Stopped`:

```powershell
Start-Service postgresql-x64-17
```

(o `Win + R` → `services.msc` → busca *postgresql* → **Iniciar**).

### Tablas que se crean

| Tabla | Contenido |
|-------|-----------|
| `usuarios` | usuario, clave (hash BCrypt), rol, fecha de creación |
| `pedidos` | cliente, mesero, producto, cantidad, mesa, estado |
| `reservas` | cliente, teléfono, fecha, hora, personas, mesa, estado |
| `inventario` | producto, categoría, stock, unidad, stock mínimo, precio |

Estados válidos:

- Pedidos: `Pendiente`, `En cocina`, `Entregado`
- Reservas: `Pendiente`, `Confirmada`, `Cancelada`
- Roles: `Administrador`, `Mesero`, `Cocina`, `Empleado`

### (Opcional) Crear la base a mano

Solo si prefieres hacerlo tú o tu usuario de PostgreSQL no tiene permiso para
crear bases.

**Con pgAdmin 4:**

1. Abre pgAdmin 4 → **Servers → PostgreSQL 17** (pide la clave de `postgres`).
2. Clic derecho en **Databases → Create → Database…**
3. Nombre: `restaurante` → **Save**.
4. Clic derecho en `restaurante` → **Query Tool**.
5. Abre el archivo `src/main/resources/schema.sql` (ícono de carpeta) y
   ejecútalo con **F5**.

**Con la consola (psql):**

```powershell
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -h localhost -c "CREATE DATABASE restaurante;"
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -h localhost -d restaurante -f src\main\resources\schema.sql
```

> El usuario `admin` **no** se crea en el SQL porque su clave debe guardarse
> hasheada con BCrypt. Lo crea la aplicación al arrancar.

---

## 4. Configurar la conexión (db.properties)

El archivo con la clave real **no se sube a GitHub**. En el repositorio hay una
plantilla.

1. Ve a la carpeta `src/main/resources/`.
2. Copia `db.properties.example` y renombra la copia a **`db.properties`**.
3. Ábrelo y pon tu clave de PostgreSQL:

```properties
db.host=localhost
db.port=5432
db.name=restaurante
db.user=postgres
db.password=LA_CLAVE_QUE_PUSISTE_AL_INSTALAR_POSTGRESQL
```

En PowerShell, desde la carpeta del proyecto:

```powershell
Copy-Item src\main\resources\db.properties.example src\main\resources\db.properties
notepad src\main\resources\db.properties
```

> Si `db.properties` no existe, la app usa `postgres` / `postgres` por defecto.

También se pueden usar variables de entorno, que tienen prioridad sobre el
archivo: `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD`.

---

## 5. Abrir el proyecto en NetBeans

1. Abre NetBeans → **File → Open Project…**
2. Selecciona la carpeta del proyecto (la que contiene `pom.xml`).
   Debe aparecer con el ícono de taza ☕ (proyecto Maven).
3. **Open Project**. La primera vez NetBeans descarga las dependencias
   (necesita internet; tarda 1–2 minutos).
4. Para comprobar que compila: clic derecho en el proyecto → **Clean and Build**.
   Debe terminar en **BUILD SUCCESS**.

---

## 6. Registrar Tomcat en NetBeans

Solo la primera vez en cada PC.

1. **Tools → Servers → Add Server…**
2. Elige **Apache Tomcat or TomEE** → **Next**.
3. **Server Location**: la carpeta de Tomcat, por ejemplo
   `C:\Users\<tu-usuario>\Tomcat\apache-tomcat-10.1.xx`.
4. **Username / Password**: inventa unos (ej. `admin` / `admin`) y marca
   **Create user if it does not exist** → **Finish**.

Luego asigna el servidor al proyecto:

1. Clic derecho en el proyecto → **Properties → Run**.
2. **Server**: *Apache Tomcat or TomEE*.
3. **Context Path**: `/CalSoft` → **OK**.

---

## 7. Ejecutar la aplicación

1. Pulsa **F6** (o clic derecho → **Run**).
2. NetBeans compila, arranca Tomcat y abre el navegador en:

   **http://localhost:8080/CalSoft**

3. Inicia sesión:

   | Usuario | Clave |
   |---------|-------|
   | `admin` | `1234` |

Para comprobar que la base se inicializó, en la pestaña **Output → Apache Tomcat**
debe aparecer:

```
CalSoft: base de datos lista
```

Para detener: pestaña **Services → Servers → Apache Tomcat** → clic derecho → **Stop**.

---

## 8. Tareas comunes con la base de datos

Todos los comandos se pueden ejecutar en pgAdmin (**Query Tool** sobre la base
`restaurante`) o con psql:

```powershell
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -h localhost -d restaurante
```

### Ver los datos

```sql
SELECT id, usuario, rol, creado_en FROM usuarios;
SELECT * FROM pedidos ORDER BY id;
SELECT * FROM reservas ORDER BY fecha, hora;
SELECT * FROM inventario ORDER BY producto;
```

### Recuperar la clave del admin (vuelve a `1234`)

```sql
DELETE FROM usuarios WHERE usuario = 'admin';
```

Luego reinicia la aplicación (vuelve a ejecutar con F6). Se crea de nuevo
`admin` / `1234`.

### Volver a cargar los datos de ejemplo

Los datos demo solo se insertan si la tabla está vacía:

```sql
TRUNCATE pedidos, reservas, inventario RESTART IDENTITY;
```

Reinicia la aplicación.

### Empezar de cero (borra TODO)

Primero detén Tomcat (para que no haya conexiones abiertas) y luego:

```powershell
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -h localhost -c "DROP DATABASE restaurante;"
```

Al volver a ejecutar la app se crea todo de nuevo.

### Copia de seguridad y restauración

```powershell
# Guardar
& "C:\Program Files\PostgreSQL\17\bin\pg_dump.exe" -U postgres -h localhost -d restaurante -f respaldo.sql

# Restaurar (en una base vacía)
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -h localhost -d restaurante -f respaldo.sql
```

### Usar una base de datos en la nube

Para que todo el equipo comparta los mismos datos se puede usar un PostgreSQL
en la nube (Supabase, Neon, Render…). Solo cambia `db.properties`:

```properties
db.host=el-host-que-te-da-el-proveedor
db.port=5432
db.name=nombre_de_la_base
db.user=usuario_del_proveedor
db.password=clave_del_proveedor
```

> Si el proveedor no permite crear bases, crea primero la base desde su panel.
> Algunos exigen SSL: en ese caso hay que añadir `?sslmode=require` a la URL
> en `Conexion.java`.

---

## 9. Solución de problemas

| Síntoma | Causa | Solución |
|---------|-------|----------|
| Login: *"No se pudo conectar con la base de datos"* | PostgreSQL apagado o clave incorrecta | Revisa el paso 3 (servicio encendido) y la clave en `db.properties`. Mira el detalle en **Output → Apache Tomcat**. |
| Log: `la autentificación password falló para el usuario «postgres»` | Clave de `db.properties` incorrecta | Pon la misma clave con la que entras a pgAdmin. Después **Clean and Build** y F6. |
| Log: `Connection refused` | PostgreSQL no está corriendo o usa otro puerto | `Start-Service postgresql-x64-17` y revisa `db.port`. |
| `No suitable Deployment Server is defined` | Tomcat no está registrado o asignado | Paso 6. |
| `Address already in use: bind` / puerto 8080 ocupado | Otro programa usa el 8080 | Cierra el otro programa o cambia el puerto: **Tools → Servers → Apache Tomcat → pestaña Connection → Server Port**. |
| Error `jakarta.servlet... ClassNotFoundException` | Se usa Tomcat 9 | Usa **Tomcat 10.1**. |
| `JAVA_HOME is not defined` al iniciar Tomcat | JDK sin `JAVA_HOME` | Reinstala el JDK marcando *Set JAVA_HOME*, o en NetBeans: **Tools → Servers → Apache Tomcat → pestaña Platform** y elige el JDK. |
| Tildes o ñ se ven como `Ã±` | Archivo guardado sin UTF-8 | Guarda los archivos en UTF-8 (NetBeans lo hace por defecto). |
| *"Token CSRF inválido"* (error 403) | La sesión expiró o se reinició Tomcat | Recarga la página e inicia sesión de nuevo. |
| Olvidé la clave de `postgres` | — | Ver abajo. |

### Olvidé la contraseña del usuario `postgres`

1. Abre el Bloc de notas **como administrador** y abre
   `C:\Program Files\PostgreSQL\17\data\pg_hba.conf` (haz una copia antes).
2. Al final, en las líneas de `127.0.0.1/32` y `::1/128`, cambia
   `scram-sha-256` por `trust` y guarda.
3. En PowerShell **como administrador**:

   ```powershell
   Restart-Service postgresql-x64-17
   & "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -h localhost -c "ALTER USER postgres PASSWORD 'NuevaClave';"
   ```

4. **Importante:** vuelve a poner `scram-sha-256` en `pg_hba.conf`, guarda y
   reinicia otra vez el servicio.
5. Pon la nueva clave en `db.properties`.
