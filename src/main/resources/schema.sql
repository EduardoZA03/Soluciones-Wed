-- =====================================================
--  Sistema POS - CalSoft
--  Esquema de base de datos (PostgreSQL)
-- =====================================================
--  Es idempotente: se puede ejecutar varias veces sin error.
--  La aplicación lo ejecuta sola al arrancar (InicializadorBD),
--  y después siembra el usuario admin con la clave hasheada.
-- =====================================================

-- Tabla de usuarios del sistema
CREATE TABLE IF NOT EXISTS usuarios (
    id        SERIAL PRIMARY KEY,
    usuario   VARCHAR(50)  NOT NULL UNIQUE,
    clave     VARCHAR(255) NOT NULL,            -- siempre hash bcrypt, nunca texto plano
    rol       VARCHAR(30)  NOT NULL DEFAULT 'Empleado',
    creado_en TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Tabla de pedidos (caso de uso principal del POS)
CREATE TABLE IF NOT EXISTS pedidos (
    id        SERIAL PRIMARY KEY,
    cliente   VARCHAR(100) NOT NULL,
    mesero    VARCHAR(100) NOT NULL,
    producto  VARCHAR(100) NOT NULL,
    cantidad  INTEGER      NOT NULL CHECK (cantidad > 0),
    mesa      INTEGER      NOT NULL CHECK (mesa > 0),
    estado    VARCHAR(20)  NOT NULL DEFAULT 'Pendiente'
              CHECK (estado IN ('Pendiente', 'En cocina', 'Entregado')),
    creado_en TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_pedidos_estado ON pedidos (estado);

-- Tabla de reservas
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

-- Tabla de inventario (productos y stock)
CREATE TABLE IF NOT EXISTS inventario (
    id           SERIAL PRIMARY KEY,
    producto     VARCHAR(100)  NOT NULL,
    categoria    VARCHAR(50)   NOT NULL DEFAULT 'General',
    stock        INTEGER       NOT NULL DEFAULT 0 CHECK (stock >= 0),
    unidad       VARCHAR(20)   NOT NULL DEFAULT 'unidad',
    stock_minimo INTEGER       NOT NULL DEFAULT 0 CHECK (stock_minimo >= 0),
    precio       NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (precio >= 0),
    creado_en    TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- =====================================================
--  Gestión de inventario con IA e interoperabilidad
--  (historias de usuario HU-02 a HU-09)
-- =====================================================

-- Las recetas usan cantidades con decimales (p. ej. 0.25 kg),
-- así que el stock pasa de INTEGER a NUMERIC si aún no lo es.
DO $$
BEGIN
    IF (SELECT data_type FROM information_schema.columns
        WHERE table_name = 'inventario' AND column_name = 'stock') = 'integer' THEN
        ALTER TABLE inventario ALTER COLUMN stock TYPE NUMERIC(12,3);
        ALTER TABLE inventario ALTER COLUMN stock_minimo TYPE NUMERIC(12,3);
    END IF;
END $$;

-- Proveedores (HU-06, HU-07). Si api_url está vacío se usa el
-- proveedor simulado de la propia aplicación (/api/proveedor/ordenes).
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

-- Nuevos datos del insumo: vencimiento (HU-04) y proveedor habitual
ALTER TABLE inventario ADD COLUMN IF NOT EXISTS fecha_vencimiento DATE;
ALTER TABLE inventario ADD COLUMN IF NOT EXISTS proveedor_id INTEGER
    REFERENCES proveedores (id) ON DELETE SET NULL;

-- Carta del restaurante y recetas (HU-03)
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

-- El pedido (venta del POS) apunta a un plato de la carta y
-- recuerda si ya descontó insumos, para no procesarlo dos veces.
ALTER TABLE pedidos ADD COLUMN IF NOT EXISTS plato_id INTEGER
    REFERENCES platos (id) ON DELETE SET NULL;
ALTER TABLE pedidos ADD COLUMN IF NOT EXISTS stock_descontado BOOLEAN NOT NULL DEFAULT false;

-- Kardex: cada cambio de stock queda registrado (HU-02, HU-03)
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

-- Alertas automáticas de stock mínimo y vencimiento (HU-04)
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

-- Órdenes de compra (HU-06, HU-07, HU-08, HU-09)
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

-- Línea de tiempo de cada orden (HU-09)
CREATE TABLE IF NOT EXISTS orden_eventos (
    id        SERIAL PRIMARY KEY,
    orden_id  INTEGER     NOT NULL REFERENCES ordenes_compra (id) ON DELETE CASCADE,
    estado    VARCHAR(30) NOT NULL,
    detalle   TEXT,
    usuario   VARCHAR(50),
    creado_en TIMESTAMPTZ NOT NULL DEFAULT now()
);
