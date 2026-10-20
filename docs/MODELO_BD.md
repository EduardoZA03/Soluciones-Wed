# Modelo de base de datos — CalSoft / La Crianza Arequipeña

Modelo que soporta las historias de usuario HU-01 a HU-09. Reemplaza a la
Figura 3 del documento (que solo tenía `mesas`, `reservas`, `pedidos`,
`usuarios` e `inventario`).

El script completo está en [`src/main/resources/schema.sql`](../src/main/resources/schema.sql).

```mermaid
erDiagram
    usuarios {
        int id PK
        varchar usuario UK
        varchar clave "hash BCrypt"
        varchar rol "Administrador, Almacén, Cocina, Mesero, Empleado"
        timestamptz creado_en
    }
    proveedores {
        int id PK
        varchar nombre
        varchar ruc
        varchar telefono
        varchar email
        varchar api_url "vacío = proveedor simulado"
        varchar api_key
    }
    inventario {
        int id PK
        varchar producto
        varchar categoria
        numeric stock
        varchar unidad
        numeric stock_minimo
        numeric precio
        date fecha_vencimiento
        int proveedor_id FK
    }
    platos {
        int id PK
        varchar nombre UK
        numeric precio
        boolean activo
    }
    recetas {
        int id PK
        int plato_id FK
        int insumo_id FK
        numeric cantidad "por porción"
    }
    pedidos {
        int id PK
        varchar cliente
        varchar mesero
        varchar producto
        int cantidad
        int mesa
        varchar estado
        int plato_id FK
        boolean stock_descontado "evita procesar dos veces"
    }
    movimientos {
        int id PK
        int insumo_id FK
        varchar tipo "ENTRADA, SALIDA, MERMA, AJUSTE"
        numeric cantidad
        numeric stock_resultante
        varchar motivo
        varchar referencia "PED-n, código de OC"
        varchar usuario
        timestamptz creado_en
    }
    alertas {
        int id PK
        int insumo_id FK
        varchar tipo "STOCK_MINIMO, VENCIMIENTO"
        varchar mensaje
        varchar estado "Pendiente, Atendida"
        timestamptz atendida_en
    }
    ordenes_compra {
        int id PK
        varchar codigo UK "va en el QR"
        int proveedor_id FK
        varchar estado
        int alerta_id FK
        date fecha_entrega
        varchar confirmacion_proveedor
    }
    orden_items {
        int id PK
        int orden_id FK
        int insumo_id FK
        numeric cantidad
        numeric precio_unitario
        boolean disponible
        numeric cantidad_recibida
    }
    orden_eventos {
        int id PK
        int orden_id FK
        varchar estado
        text detalle
        varchar usuario
        timestamptz creado_en
    }
    reservas {
        int id PK
        varchar cliente
        date fecha
        time hora
        int personas
        int mesa
        varchar estado
    }

    proveedores ||--o{ inventario : "abastece"
    proveedores ||--o{ ordenes_compra : "recibe"
    platos ||--o{ recetas : "se prepara con"
    inventario ||--o{ recetas : "se usa en"
    platos ||--o{ pedidos : "se vende en"
    inventario ||--o{ movimientos : "kardex"
    inventario ||--o{ alertas : "genera"
    alertas |o--o{ ordenes_compra : "origina"
    ordenes_compra ||--|{ orden_items : "contiene"
    inventario ||--o{ orden_items : "se pide en"
    ordenes_compra ||--o{ orden_eventos : "trazabilidad"
```

> GitHub dibuja el diagrama automáticamente. Para tu documento puedes hacer una
> captura, o pegar el bloque en https://mermaid.live y exportarlo como imagen.

## Tablas nuevas y su historia de usuario

| Tabla | Para qué | HU |
|-------|----------|----|
| `proveedores` | Datos del proveedor y conexión a su API | HU-06, HU-07 |
| `platos`, `recetas` | Carta y cantidad de cada insumo por porción | HU-03 |
| `movimientos` | Kardex: cada cambio de stock con motivo y referencia; también es el histórico que usa la IA | HU-02, HU-03, HU-05 |
| `alertas` | Alertas automáticas de stock mínimo y vencimiento | HU-04 |
| `ordenes_compra`, `orden_items` | Orden con proveedor, insumos, cantidades, total y respuesta del proveedor | HU-06, HU-07, HU-08 |
| `orden_eventos` | Línea de tiempo: creada, aprobada, enviada, confirmada, recibida, errores de API | HU-09 |

Columnas nuevas: `inventario.fecha_vencimiento`, `inventario.proveedor_id`,
`pedidos.plato_id`, `pedidos.stock_descontado`; además el stock pasó a `NUMERIC`
para admitir cantidades como 0.25 kg.

> Nota: en tu figura había una tabla `mesas`. En el sistema la mesa se guarda
> como número dentro de `pedidos` y `reservas`. Si la quieres como tabla aparte,
> hay que agregarla; el resto del documento no depende de ella.
