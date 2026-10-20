package com.calsoft.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Crea la base de datos si todavía no existe, conectándose a la base "postgres".
 * Se ejecuta antes de iniciar Spring, porque Spring necesita que la base exista.
 *
 * Lee los mismos datos que application.properties: primero el archivo
 * application-local.properties y luego las variables de entorno DB_*.
 */
public final class BaseDeDatos {

    private static final Logger LOG = Logger.getLogger(BaseDeDatos.class.getName());
    private static final String BASE_INEXISTENTE = "3D000";

    private BaseDeDatos() {
    }

    public static void crearSiNoExiste() {
        Properties local = new Properties();
        try (InputStream in = new FileInputStream("application-local.properties")) {
            local.load(in);
        } catch (IOException e) {
            // Sin archivo local: se usan las variables de entorno o los valores por defecto.
        }
        String host = valor(local, "db.host", "DB_HOST", "localhost");
        String puerto = valor(local, "db.port", "DB_PORT", "5432");
        String base = valor(local, "db.name", "DB_NAME", "restaurante");
        String usuario = valor(local, "db.user", "DB_USER", "postgres");
        String clave = valor(local, "db.password", "DB_PASSWORD", "postgres");
        String url = "jdbc:postgresql://" + host + ":" + puerto + "/";

        try (Connection cn = DriverManager.getConnection(url + base, usuario, clave)) {
            return; // ya existe
        } catch (SQLException e) {
            if (!BASE_INEXISTENTE.equals(e.getSQLState())) {
                return; // otro problema (clave, servidor apagado): Spring mostrará el error
            }
        }
        if (!base.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            return;
        }
        try (Connection cn = DriverManager.getConnection(url + "postgres", usuario, clave);
             Statement st = cn.createStatement()) {
            st.executeUpdate("CREATE DATABASE " + base);
            LOG.info("CalSoft: base de datos '" + base + "' creada");
        } catch (SQLException e) {
            LOG.warning("CalSoft: no se pudo crear la base '" + base + "': " + e.getMessage());
        }
    }

    /** Mismo orden que Spring: archivo local, luego variable de entorno, luego valor por defecto. */
    private static String valor(Properties local, String clave, String variable, String porDefecto) {
        String v = local.getProperty(clave);
        if (v == null || v.isBlank()) {
            v = System.getenv(variable);
        }
        return (v == null || v.isBlank()) ? porDefecto : v.trim();
    }
}
