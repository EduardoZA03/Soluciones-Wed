package com.calsoft.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Se ejecuta una vez al desplegar la aplicación. Reemplaza al "npm run seed":
 * <ol>
 *   <li>Crea la base de datos si no existe.</li>
 *   <li>Aplica el esquema (schema.sql, idempotente).</li>
 *   <li>Crea el usuario admin (clave 1234) si no existe.</li>
 *   <li>Siembra datos demo solo si las tablas están vacías.</li>
 * </ol>
 */
@WebListener
public class InicializadorBD implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(InicializadorBD.class.getName());

    /** SQLSTATE de PostgreSQL cuando la base de datos no existe. */
    private static final String BASE_INEXISTENTE = "3D000";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            crearBaseSiNoExiste();
            try (Connection cn = Conexion.obtener()) {
                aplicarEsquema(cn);
                sembrarAdmin(cn);
                sembrarInventarioDemo(cn);
                sembrarReservasDemo(cn);
                sembrarPedidosDemo(cn);
            }
            LOG.info("CalSoft: base de datos lista");
        } catch (SQLException | IOException e) {
            // No detenemos el despliegue: el login mostrará el error de conexión.
            LOG.log(Level.SEVERE, "CalSoft: no se pudo inicializar la base de datos. "
                    + "Revisa src/main/resources/db.properties", e);
        }
    }

    private void crearBaseSiNoExiste() throws SQLException {
        try (Connection cn = Conexion.obtener()) {
            // La base existe; no hay nada que hacer.
        } catch (SQLException e) {
            if (!BASE_INEXISTENTE.equals(e.getSQLState())) {
                throw e;
            }
            String nombre = Conexion.nombreBase();
            if (!nombre.matches("[A-Za-z_][A-Za-z0-9_]*")) {
                throw new SQLException("Nombre de base inválido: " + nombre);
            }
            try (Connection cn = Conexion.obtenerAdministrativa();
                 Statement st = cn.createStatement()) {
                st.executeUpdate("CREATE DATABASE " + nombre);
                LOG.info("CalSoft: base de datos '" + nombre + "' creada");
            }
        }
    }

    private void aplicarEsquema(Connection cn) throws SQLException, IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("schema.sql")) {
            if (in == null) {
                throw new IOException("No se encontró schema.sql en el classpath");
            }
            String sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            try (Statement st = cn.createStatement()) {
                // El driver de PostgreSQL acepta varias sentencias en un solo execute.
                st.execute(sql);
            }
        }
    }

    private void sembrarAdmin(Connection cn) throws SQLException {
        // ON CONFLICT DO NOTHING: si el admin ya existe no se le cambia la clave.
        String sql = "INSERT INTO usuarios (usuario, clave, rol) VALUES (?, ?, ?) "
                + "ON CONFLICT (usuario) DO NOTHING";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, "admin");
            ps.setString(2, BCrypt.hashpw("1234", BCrypt.gensalt(10)));
            ps.setString(3, "Administrador");
            if (ps.executeUpdate() > 0) {
                LOG.info("CalSoft: usuario 'admin' creado (clave de desarrollo: 1234)");
            }
        }
    }

    private boolean tablaVacia(Connection cn, String tabla) throws SQLException {
        try (Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + tabla)) {
            rs.next();
            return rs.getInt(1) == 0;
        }
    }

    private void sembrarInventarioDemo(Connection cn) throws SQLException {
        if (!tablaVacia(cn, "inventario")) {
            return;
        }
        Object[][] items = {
            {"Pollo entero", "Carnes", 40, "unidad", 10, "18.00"},
            {"Papas", "Verduras", 120, "kg", 30, "2.50"},
            {"Rocoto", "Verduras", 25, "kg", 10, "6.00"},
            {"Arroz", "Abarrotes", 80, "kg", 20, "3.20"},
            {"Inca Kola 1.5L", "Bebidas", 8, "unidad", 12, "7.50"}, // bajo stock a propósito
            {"Cerveza", "Bebidas", 60, "unidad", 24, "6.00"},
        };
        String sql = "INSERT INTO inventario (producto, categoria, stock, unidad, stock_minimo, precio) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            for (Object[] i : items) {
                ps.setString(1, (String) i[0]);
                ps.setString(2, (String) i[1]);
                ps.setInt(3, (Integer) i[2]);
                ps.setString(4, (String) i[3]);
                ps.setInt(5, (Integer) i[4]);
                ps.setBigDecimal(6, new BigDecimal((String) i[5]));
                ps.addBatch();
            }
            ps.executeBatch();
        }
        LOG.info("CalSoft: inventario demo sembrado (6 productos)");
    }

    private void sembrarReservasDemo(Connection cn) throws SQLException {
        if (!tablaVacia(cn, "reservas")) {
            return;
        }
        Object[][] reservas = {
            {"Familia Quispe", "987654321", "2026-05-25", "13:30:00", 6, 4, "Confirmada"},
            {"Pedro Ramos", "999111222", "2026-05-25", "20:00:00", 2, 7, "Pendiente"},
            {"Empresa XYZ", "988777666", "2026-05-26", "14:00:00", 12, 1, "Pendiente"},
        };
        String sql = "INSERT INTO reservas (cliente, telefono, fecha, hora, personas, mesa, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            for (Object[] r : reservas) {
                ps.setString(1, (String) r[0]);
                ps.setString(2, (String) r[1]);
                ps.setDate(3, Date.valueOf((String) r[2]));
                ps.setTime(4, Time.valueOf((String) r[3]));
                ps.setInt(5, (Integer) r[4]);
                ps.setInt(6, (Integer) r[5]);
                ps.setString(7, (String) r[6]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
        LOG.info("CalSoft: reservas demo sembradas (3)");
    }

    private void sembrarPedidosDemo(Connection cn) throws SQLException {
        if (!tablaVacia(cn, "pedidos")) {
            return;
        }
        Object[][] pedidos = {
            {"Ana", "Carlos", "Pollo a la brasa", 2, 4, "Pendiente"},
            {"Luis", "Marta", "Anticuchos", 1, 7, "En cocina"},
            {"Sofía", "Carlos", "Rocoto relleno", 3, 2, "Entregado"},
        };
        String sql = "INSERT INTO pedidos (cliente, mesero, producto, cantidad, mesa, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            for (Object[] p : pedidos) {
                ps.setString(1, (String) p[0]);
                ps.setString(2, (String) p[1]);
                ps.setString(3, (String) p[2]);
                ps.setInt(4, (Integer) p[3]);
                ps.setInt(5, (Integer) p[4]);
                ps.setString(6, (String) p[5]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
        LOG.info("CalSoft: pedidos demo sembrados (3)");
    }
}
