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
