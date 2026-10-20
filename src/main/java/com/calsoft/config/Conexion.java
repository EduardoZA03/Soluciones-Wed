package com.calsoft.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Entrega conexiones JDBC a PostgreSQL.
 *
 * Lee la configuración de {@code db.properties} (src/main/resources) y permite
 * sobrescribir cada valor con variables de entorno (PGHOST, PGPORT, PGUSER,
 * PGPASSWORD, PGDATABASE), igual que el backend original con su archivo .env.
 */
public final class Conexion {

    private static final String HOST;
    private static final String PUERTO;
    private static final String BASE;
    private static final String USUARIO;
    private static final String CLAVE;

    static {
        Properties props = new Properties();
        try (InputStream in = Conexion.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            throw new ExceptionInInitializerError("No se pudo leer db.properties: " + e.getMessage());
        }
        HOST = valor("PGHOST", props.getProperty("db.host", "localhost"));
        PUERTO = valor("PGPORT", props.getProperty("db.port", "5432"));
        BASE = valor("PGDATABASE", props.getProperty("db.name", "restaurante"));
        USUARIO = valor("PGUSER", props.getProperty("db.user", "postgres"));
        CLAVE = valor("PGPASSWORD", props.getProperty("db.password", "postgres"));

        try {
            // Necesario en servidores donde el driver no se registra solo.
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("Falta el driver de PostgreSQL");
        }
    }

    private Conexion() {
    }

    private static String valor(String variableEntorno, String porDefecto) {
        String v = System.getenv(variableEntorno);
        return (v == null || v.isBlank()) ? porDefecto : v;
    }

    /** Conexión a la base de datos de la aplicación. Se debe cerrar con try-with-resources. */
    public static Connection obtener() throws SQLException {
        return DriverManager.getConnection(url(BASE), USUARIO, CLAVE);
    }

    /** Conexión a la base "postgres", usada solo para crear la base de la app si no existe. */
    static Connection obtenerAdministrativa() throws SQLException {
        return DriverManager.getConnection(url("postgres"), USUARIO, CLAVE);
    }

    static String nombreBase() {
        return BASE;
    }

    private static String url(String base) {
        return "jdbc:postgresql://" + HOST + ":" + PUERTO + "/" + base;
    }
}
