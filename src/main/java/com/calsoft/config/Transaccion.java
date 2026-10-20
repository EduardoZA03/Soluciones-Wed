package com.calsoft.config;

import com.calsoft.util.ValidacionException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Ejecuta varias operaciones de BD como una sola transacción:
 * o se guardan todas (commit) o ninguna (rollback).
 *
 * Uso:
 * <pre>
 *   int id = Transaccion.ejecutar(cn -&gt; {
 *       ... varias consultas con la misma conexión cn ...
 *       return id;
 *   });
 * </pre>
 */
public final class Transaccion {

    @FunctionalInterface
    public interface Trabajo<T> {
        T ejecutar(Connection cn) throws SQLException, ValidacionException;
    }

    private Transaccion() {
    }

    public static <T> T ejecutar(Trabajo<T> trabajo) throws SQLException, ValidacionException {
        try (Connection cn = Conexion.obtener()) {
            cn.setAutoCommit(false);
            try {
                T resultado = trabajo.ejecutar(cn);
                cn.commit();
                return resultado;
            } catch (SQLException | ValidacionException | RuntimeException e) {
                cn.rollback();
                throw e;
            }
        }
    }
}
