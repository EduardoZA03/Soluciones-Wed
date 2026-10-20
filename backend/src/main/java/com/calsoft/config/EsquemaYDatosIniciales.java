package com.calsoft.config;

import com.calsoft.servicio.AlertaService;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Al arrancar el backend:
 *  1. Aplica el esquema (schema.sql, idempotente; también migra bases anteriores).
 *  2. Crea el usuario admin (clave 1234) y garantiza que exista un Administrador.
 *  3. Siembra datos de demostración solo en las tablas vacías: categorías,
 *     proveedores, insumos, carta con recetas, reservas, pedidos y 8 semanas
 *     de consumo histórico para que la IA tenga de qué aprender.
 */
@Component
public class EsquemaYDatosIniciales implements ApplicationRunner {

    private static final Logger LOG = Logger.getLogger(EsquemaYDatosIniciales.class.getName());

    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder;
    private final AlertaService alertas;

    public EsquemaYDatosIniciales(JdbcTemplate jdbc, PasswordEncoder encoder, AlertaService alertas) {
        this.jdbc = jdbc;
        this.encoder = encoder;
        this.alertas = alertas;
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        try (InputStream in = new ClassPathResource("schema.sql").getInputStream()) {
            // El driver de PostgreSQL acepta todo el script (con bloques DO $$) en una sola ejecución.
            jdbc.execute(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
        sembrarAdmin();
        sembrarCategorias();
        sembrarProveedores();
        sembrarInsumos();
        sembrarCarta();
        sembrarReservas();
        sembrarPedidos();
        sembrarHistorial();
        alertas.evaluarTodo();
        LOG.info("CalSoft: base de datos lista");
    }

    private boolean vacia(String tabla) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM " + tabla, Integer.class);
        return n == null || n == 0;
    }

    private void sembrarAdmin() {
        int creado = jdbc.update("INSERT INTO usuarios (usuario, clave, rol) VALUES ('admin', ?, 'Administrador') "
                + "ON CONFLICT (usuario) DO NOTHING", encoder.encode("1234"));
        if (creado > 0) {
            LOG.info("CalSoft: usuario 'admin' creado (clave de desarrollo: 1234)");
        }
        // Siempre debe existir un Administrador (p. ej. en bases de versiones anteriores).
        jdbc.update("UPDATE usuarios SET rol = 'Administrador' WHERE usuario = 'admin' "
                + "AND NOT EXISTS (SELECT 1 FROM usuarios WHERE rol = 'Administrador')");
    }

    private void sembrarCategorias() {
        if (!vacia("categorias")) {
            return;
        }
        String[][] categorias = {
            {"Carnes", "Pollo, res, cerdo y menudencias"},
            {"Verduras", "Tubérculos, ajíes y hortalizas"},
            {"Abarrotes", "Arroz, aceite, condimentos y secos"},
            {"Bebidas", "Gaseosas, cervezas y jugos"},
        };
        for (String[] c : categorias) {
            jdbc.update("INSERT INTO categorias (nombre, descripcion) VALUES (?, ?)", c[0], c[1]);
        }
    }

    private void sembrarProveedores() {
        if (!vacia("proveedores")) {
            return;
        }
        // api_url NULL = usa el proveedor simulado incluido en el backend.
        jdbc.update("INSERT INTO proveedores (nombre, ruc, telefono, email, api_key) VALUES (?, ?, ?, ?, ?)",
                "Distribuidora Arequipa SAC", "20512345671", "987111222", "ventas@distarequipa.pe", "demo-arequipa-2026");
        jdbc.update("INSERT INTO proveedores (nombre, ruc, telefono, email, api_key) VALUES (?, ?, ?, ?, ?)",
                "Bebidas del Norte EIRL", "20698765432", "986333444", "pedidos@bebidasnorte.pe", "demo-bebidas-2026");
        // Proveedor habitual de los insumos que ya existían (bases anteriores).
        jdbc.update("UPDATE inventario i SET proveedor_id = (SELECT id FROM proveedores ORDER BY id DESC LIMIT 1) "
                + "FROM categorias c WHERE i.categoria_id = c.id AND c.nombre = 'Bebidas' AND i.proveedor_id IS NULL");
        jdbc.update("UPDATE inventario SET proveedor_id = (SELECT id FROM proveedores ORDER BY id LIMIT 1) "
                + "WHERE proveedor_id IS NULL");
    }

    private void sembrarInsumos() {
        if (!vacia("inventario")) {
            return;
        }
        Object[][] items = {
            // nombre, categoría, stock, unidad, mínimo, precio, días para vencer (null = no vence)
            {"Pollo entero", "Carnes", 40, "unidad", 10, "18.00", 2},
            {"Papas", "Verduras", 120, "kg", 30, "2.50", 20},
            {"Rocoto", "Verduras", 25, "kg", 10, "6.00", 20},
            {"Arroz", "Abarrotes", 80, "kg", 20, "3.20", null},
            {"Inca Kola 1.5L", "Bebidas", 8, "unidad", 12, "7.50", null}, // bajo stock a propósito
            {"Cerveza", "Bebidas", 60, "unidad", 24, "6.00", null},
        };
        for (Object[] i : items) {
            String proveedor = "Bebidas".equals(i[1]) ? "Bebidas del Norte EIRL" : "Distribuidora Arequipa SAC";
            jdbc.update("INSERT INTO inventario (producto, categoria_id, stock, unidad, stock_minimo, precio, "
                    + "fecha_vencimiento, proveedor_id) VALUES (?, (SELECT id FROM categorias WHERE nombre = ?), "
                    + "?, ?, ?, ?, ?, (SELECT id FROM proveedores WHERE nombre = ?))",
                    i[0], i[1], i[2], i[3], i[4], new BigDecimal((String) i[5]),
                    i[6] == null ? null : java.sql.Date.valueOf(LocalDate.now().plusDays((Integer) i[6])), proveedor);
        }
        LOG.info("CalSoft: insumos demo sembrados (6)");
    }

    /** Carta con recetas: cuánto de cada insumo usa una porción. */
    private void sembrarCarta() {
        if (!vacia("platos")) {
            return;
        }
        Object[][] platos = {
            {"Pollo a la brasa", "22.00", new String[][] {{"Pollo entero", "0.25"}, {"Papas", "0.30"}}},
            {"Rocoto relleno", "28.00", new String[][] {{"Rocoto", "0.20"}, {"Papas", "0.25"}}},
            {"Anticuchos", "18.00", new String[][] {{"Papas", "0.20"}}},
            {"Arroz con pollo", "20.00", new String[][] {{"Pollo entero", "0.25"}, {"Arroz", "0.15"}}},
            {"Inca Kola 1.5L", "8.50", new String[][] {{"Inca Kola 1.5L", "1"}}},
            {"Cerveza", "9.00", new String[][] {{"Cerveza", "1"}}},
        };
        for (Object[] p : platos) {
            Integer platoId = jdbc.queryForObject("INSERT INTO platos (nombre, precio) VALUES (?, ?) RETURNING id",
                    Integer.class, p[0], new BigDecimal((String) p[1]));
            for (String[] r : (String[][]) p[2]) {
                jdbc.update("INSERT INTO recetas (plato_id, insumo_id, cantidad) "
                        + "SELECT ?, id, ? FROM inventario WHERE producto = ? ORDER BY id LIMIT 1",
                        platoId, new BigDecimal(r[1]), r[0]);
            }
        }
        jdbc.update("UPDATE pedidos p SET plato_id = pl.id FROM platos pl WHERE p.plato_id IS NULL AND pl.nombre = p.producto");
        LOG.info("CalSoft: carta demo sembrada (6 platos con receta)");
    }

    private void sembrarReservas() {
        if (!vacia("reservas")) {
            return;
        }
        LocalDate manana = LocalDate.now().plusDays(1);
        jdbc.update("INSERT INTO reservas (cliente, telefono, fecha, hora, personas, mesa, estado) VALUES "
                + "('Familia Quispe', '987654321', ?, '13:30', 6, 4, 'Confirmada'), "
                + "('Pedro Ramos', '999111222', ?, '20:00', 2, 7, 'Pendiente'), "
                + "('Empresa XYZ', '988777666', ?, '14:00', 12, 1, 'Pendiente')",
                java.sql.Date.valueOf(manana), java.sql.Date.valueOf(manana), java.sql.Date.valueOf(manana.plusDays(1)));
    }

    private void sembrarPedidos() {
        if (!vacia("pedidos")) {
            return;
        }
        jdbc.update("INSERT INTO pedidos (cliente, mesero, producto, cantidad, mesa, estado, plato_id) "
                + "SELECT v.cliente, v.mesero, v.producto, v.cantidad, v.mesa, v.estado, pl.id FROM (VALUES "
                + "('Ana', 'Carlos', 'Pollo a la brasa', 2, 4, 'Pendiente'), "
                + "('Luis', 'Marta', 'Anticuchos', 1, 7, 'En cocina'), "
                + "('Sofía', 'Carlos', 'Rocoto relleno', 3, 2, 'Entregado')) "
                + "AS v(cliente, mesero, producto, cantidad, mesa, estado) "
                + "LEFT JOIN platos pl ON pl.nombre = v.producto");
    }

    /**
     * 8 semanas de consumo histórico simulado (más los viernes, sábados y
     * domingos, con algo de ruido) para que el modelo de IA tenga datos.
     * Son registros históricos: no cambian el stock actual.
     */
    private void sembrarHistorial() {
        if (!vacia("movimientos")) {
            return;
        }
        Map<String, Double> consumoBase = Map.of("Pollo entero", 5.0, "Papas", 8.0, "Rocoto", 1.8,
                "Arroz", 4.0, "Inca Kola 1.5L", 3.0, "Cerveza", 6.0);
        double[] factorDia = {0.8, 0.75, 0.85, 0.95, 1.25, 1.5, 1.4}; // lunes..domingo
        Random azar = new Random(2026);
        List<Object[]> filas = new ArrayList<>();
        jdbc.query("SELECT id, producto FROM inventario ORDER BY id", rs -> {
            Double base = consumoBase.get(rs.getString("producto"));
            if (base == null) {
                return;
            }
            for (int d = 56; d >= 1; d--) {
                LocalDate dia = LocalDate.now().minusDays(d);
                double valor = base * factorDia[dia.getDayOfWeek().getValue() - 1]
                        * (1 + (56 - d) * 0.003) * (0.85 + azar.nextDouble() * 0.3);
                filas.add(new Object[] {rs.getInt("id"),
                    BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP), Timestamp.valueOf(dia.atTime(13, 0))});
            }
        });
        jdbc.batchUpdate("INSERT INTO movimientos (insumo_id, tipo, cantidad, motivo, referencia, usuario, creado_en) "
                + "VALUES (?, 'SALIDA', ?, 'Consumo histórico (datos demo)', 'HIST', 'sistema', ?)", filas);
        LOG.info("CalSoft: historial de consumo demo sembrado (" + filas.size() + " movimientos)");
    }
}
