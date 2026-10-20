-- =====================================================
--  CalSoft - La Crianza Arequipeña
--  Esquema de base de datos (PostgreSQL)
-- =====================================================
--  Es idempotente: se puede ejecutar varias veces sin error.
--  El backend lo ejecuta solo al arrancar (EsquemaYDatosIniciales)
--  y también actualiza bases de versiones anteriores sin perder datos.
-- =====================================================

-- ---------- Seguridad ----------
CREATE TABLE IF NOT EXISTS usuarios (
    id        SERIAL PRIMARY KEY,
    usuario   VARCHAR(50)  NOT NULL UNIQUE,
    clave     VARCHAR(255) NOT NULL,            -- siempre hash BCrypt, nunca texto plano
    rol       VARCHAR(30)  NOT NULL DEFAULT 'Empleado',
    creado_en TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ---------- Tablas maestras ----------
CREATE TABLE IF NOT EXISTS categorias (
    id          SERIAL PRIMARY KEY,
    nombre      VARCHAR(50)  NOT NULL UNIQUE,
    descripcion VARCHAR(200),
    creado_en   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Si api_url está vacío se usa el proveedor simulado del propio backend.
CREATE TABLE IF NOT EXISTS proveedores (
    id        SERIAL PRIMARY KEY,
    nombre    VARCHAR(100) NOT NULL,
    ruc       VARCHAR(11),
    telefono  VARCHAR(30),
    email     VARCHAR(100),
    api_url   VARCHAR(255),
    api_key   VARCHAR(100),
    creado_en TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Insumos (tabla "inventario")
CREATE TABLE IF NOT EXISTS inventario (
    id                SERIAL PRIMARY KEY,
    producto          VARCHAR(100)  NOT NULL,
    categoria_id      INTEGER       REFERENCES categorias (id),
    stock             NUMERIC(12,3) NOT NULL DEFAULT 0 CHECK (stock >= 0),
    unidad            VARCHAR(20)   NOT NULL DEFAULT 'unidad',
    stock_minimo      NUMERIC(12,3) NOT NULL DEFAULT 0 CHECK (stock_minimo >= 0),
    precio            NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (precio >= 0),
    fecha_vencimiento DATE,
    proveedor_id      INTEGER       REFERENCES proveedores (id) ON DELETE SET NULL,
    creado_en         TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- Migración de bases anteriores: stock entero → decimal, columnas nuevas
DO $$
BEGIN
    IF (SELECT data_type FROM information_schema.columns
        WHERE table_name = 'inventario' AND column_name = 'stock') = 'integer' THEN
        ALTER TABLE inventario ALTER COLUMN stock TYPE NUMERIC(12,3);
        ALTER TABLE inventario ALTER COLUMN stock_minimo TYPE NUMERIC(12,3);
    END IF;
END $$;

ALTER TABLE inventario ADD COLUMN IF NOT EXISTS categoria_id INTEGER REFERENCES categorias (id);
ALTER TABLE inventario ADD COLUMN IF NOT EXISTS fecha_vencimiento DATE;
ALTER TABLE inventario ADD COLUMN IF NOT EXISTS proveedor_id INTEGER
    REFERENCES proveedores (id) ON DELETE SET NULL;

-- Migración: la categoría era un texto dentro del insumo; ahora es una tabla maestra.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name = 'inventario' AND column_name = 'categoria') THEN
        INSERT INTO categorias (nombre)
            SELECT DISTINCT categoria FROM inventario WHERE categoria IS NOT NULL
            ON CONFLICT (nombre) DO NOTHING;
        UPDATE inventario i SET categoria_id = c.id
            FROM categorias c WHERE c.nombre = i.categoria AND i.categoria_id IS NULL;
        ALTER TABLE inventario DROP COLUMN categoria;
    END IF;
END $$;

-- Carta del restaurante y recetas (insumos por porción)
CREATE TABLE IF NOT EXISTS platos (
    id        SERIAL PRIMARY KEY,
    nombre    VARCHAR(100)  NOT NULL UNIQUE,
    precio    NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (precio >= 0),
    activo    BOOLEAN       NOT NULL DEFAULT true,
    creado_en TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS recetas (
    id        SERIAL PRIMARY KEY,
    plato_id  INTEGER       NOT NULL REFERENCES platos (id) ON DELETE CASCADE,
    insumo_id INTEGER       NOT NULL REFERENCES inventario (id) ON DELETE CASCADE,
    cantidad  NUMERIC(12,3) NOT NULL CHECK (cantidad > 0),
    UNIQUE (plato_id, insumo_id)
);

-- ---------- Operación ----------
CREATE TABLE IF NOT EXISTS pedidos (
    id               SERIAL PRIMARY KEY,
    cliente          VARCHAR(100) NOT NULL,
    mesero           VARCHAR(100) NOT NULL,
    producto         VARCHAR(100) NOT NULL,
    cantidad         INTEGER      NOT NULL CHECK (cantidad > 0),
    mesa             INTEGER      NOT NULL CHECK (mesa > 0),
    estado           VARCHAR(20)  NOT NULL DEFAULT 'Pendiente'
                     CHECK (estado IN ('Pendiente', 'En cocina', 'Entregado')),
    plato_id         INTEGER      REFERENCES platos (id) ON DELETE SET NULL,
    stock_descontado BOOLEAN      NOT NULL DEFAULT false,   -- evita descontar dos veces
    creado_en        TIMESTAMPTZ  NOT NULL DEFAULT now()
);

ALTER TABLE pedidos ADD COLUMN IF NOT EXISTS plato_id INTEGER REFERENCES platos (id) ON DELETE SET NULL;
ALTER TABLE pedidos ADD COLUMN IF NOT EXISTS stock_descontado BOOLEAN NOT NULL DEFAULT false;
CREATE INDEX IF NOT EXISTS idx_pedidos_estado ON pedidos (estado);

CREATE TABLE IF NOT EXISTS reservas (
    id        SERIAL PRIMARY KEY,
    cliente   VARCHAR(100) NOT NULL,
    telefono  VARCHAR(30),
    fecha     DATE         NOT NULL,
    hora      TIME         NOT NULL,
    personas  INTEGER      NOT NULL CHECK (personas > 0),
    mesa      INTEGER      NOT NULL CHECK (mesa > 0),
    estado    VARCHAR(20)  NOT NULL DEFAULT 'Pendiente'
              CHECK (estado IN ('Pendiente', 'Confirmada', 'Cancelada')),
    creado_en TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_reservas_fecha ON reservas (fecha);

-- ---------- Inventario con IA e interoperabilidad ----------
-- Kardex: cada cambio de stock queda registrado (también es el histórico de la IA)
CREATE TABLE IF NOT EXISTS movimientos (
    id               SERIAL PRIMARY KEY,
    insumo_id        INTEGER       NOT NULL REFERENCES inventario (id) ON DELETE CASCADE,
    tipo             VARCHAR(10)   NOT NULL CHECK (tipo IN ('ENTRADA', 'SALIDA', 'MERMA', 'AJUSTE')),
    cantidad         NUMERIC(12,3) NOT NULL,
    stock_resultante NUMERIC(12,3),
    motivo           VARCHAR(200),
    referencia       VARCHAR(50),
    usuario          VARCHAR(50),
    creado_en        TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_movimientos_insumo ON movimientos (insumo_id, creado_en);

-- Alertas automáticas de stock mínimo y vencimiento
CREATE TABLE IF NOT EXISTS alertas (
    id           SERIAL PRIMARY KEY,
    insumo_id    INTEGER      NOT NULL REFERENCES inventario (id) ON DELETE CASCADE,
    tipo         VARCHAR(20)  NOT NULL CHECK (tipo IN ('STOCK_MINIMO', 'VENCIMIENTO')),
    mensaje      VARCHAR(255) NOT NULL,
    estado       VARCHAR(12)  NOT NULL DEFAULT 'Pendiente' CHECK (estado IN ('Pendiente', 'Atendida')),
    creado_en    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    atendida_en  TIMESTAMPTZ,
    atendida_por VARCHAR(50)
);

-- Solo una alerta pendiente por insumo y tipo
CREATE UNIQUE INDEX IF NOT EXISTS ux_alertas_pendiente
    ON alertas (insumo_id, tipo) WHERE estado = 'Pendiente';

-- Órdenes de compra a proveedores
CREATE TABLE IF NOT EXISTS ordenes_compra (
    id                     SERIAL PRIMARY KEY,
    codigo                 VARCHAR(40) NOT NULL UNIQUE,   -- va en el QR
    proveedor_id           INTEGER     NOT NULL REFERENCES proveedores (id),
    estado                 VARCHAR(30) NOT NULL DEFAULT 'Borrador'
                           CHECK (estado IN ('Borrador', 'Aprobada', 'Enviada', 'Confirmada', 'Rechazada',
                                             'Recibida', 'Recibida con incidencia', 'Cancelada')),
    alerta_id              INTEGER     REFERENCES alertas (id) ON DELETE SET NULL,
    fecha_entrega          DATE,
    confirmacion_proveedor VARCHAR(60),
    creado_por             VARCHAR(50),
    creado_en              TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS orden_items (
    id                SERIAL PRIMARY KEY,
    orden_id          INTEGER       NOT NULL REFERENCES ordenes_compra (id) ON DELETE CASCADE,
    insumo_id         INTEGER       NOT NULL REFERENCES inventario (id),
    cantidad          NUMERIC(12,3) NOT NULL CHECK (cantidad > 0),
    precio_unitario   NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (precio_unitario >= 0),
    disponible        BOOLEAN,
    cantidad_recibida NUMERIC(12,3),
    UNIQUE (orden_id, insumo_id)
);

-- Línea de tiempo de cada orden (trazabilidad)
CREATE TABLE IF NOT EXISTS orden_eventos (
    id        SERIAL PRIMARY KEY,
    orden_id  INTEGER     NOT NULL REFERENCES ordenes_compra (id) ON DELETE CASCADE,
    estado    VARCHAR(30) NOT NULL,
    detalle   TEXT,
    usuario   VARCHAR(50),
    creado_en TIMESTAMPTZ NOT NULL DEFAULT now()
);
