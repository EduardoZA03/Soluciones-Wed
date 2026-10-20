package com.calsoft.dao;

import com.calsoft.config.Conexion;
import com.calsoft.modelo.Conteo;
import com.calsoft.modelo.Movimiento;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Kardex: registro de cada movimiento de stock. */
public class MovimientoDAO {

    public void registrar(Connection cn, int insumoId, String tipo, BigDecimal cantidad,
            BigDecimal stockResultante, String motivo, String referencia, String usuario) throws SQLException {
        String sql = "INSERT INTO movimientos (insumo_id, tipo, cantidad, stock_resultante, motivo, referencia, usuario) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, insumoId);
            ps.setString(2, tipo);
            ps.setBigDecimal(3, cantidad);
            if (stockResultante == null) {
                ps.setNull(4, Types.NUMERIC);
            } else {
                ps.setBigDecimal(4, stockResultante);
            }
            ps.setString(5, motivo);
            ps.setString(6, referencia);
            ps.setString(7, usuario);
            ps.executeUpdate();
        }
    }

    /** Últimos movimientos; si insumoId es null, de todos los insumos. */
    public List<Movimiento> listar(Integer insumoId, int limite) throws SQLException {
        String sql = "SELECT m.*, i.producto, i.unidad FROM movimientos m JOIN inventario i ON i.id = m.insumo_id "
                + (insumoId == null ? "" : "WHERE m.insumo_id = ? ")
                + "ORDER BY m.creado_en DESC, m.id DESC LIMIT ?";
        List<Movimiento> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            int i = 1;
            if (insumoId != null) {
                ps.setInt(i++, insumoId);
            }
            ps.setInt(i, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Movimiento m = new Movimiento();
                    m.setId(rs.getInt("id"));
                    m.setInsumoId(rs.getInt("insumo_id"));
                    m.setInsumo(rs.getString("producto"));
                    m.setUnidad(rs.getString("unidad"));
                    m.setTipo(rs.getString("tipo"));
                    m.setCantidad(rs.getBigDecimal("cantidad"));
                    m.setStockResultante(rs.getBigDecimal("stock_resultante"));
                    m.setMotivo(rs.getString("motivo"));
                    m.setReferencia(rs.getString("referencia"));
                    m.setUsuario(rs.getString("usuario"));
                    m.setCreadoEn(rs.getTimestamp("creado_en").toLocalDateTime());
                    lista.add(m);
                }
            }
        }
        return lista;
    }

    /**
     * Consumo (salidas) por insumo y por día en los últimos {@code dias} días, sin contar hoy.
     * Devuelve insumoId → arreglo de {@code dias} valores, del más antiguo al más reciente.
     * Es la serie histórica con la que aprende el modelo de predicción.
     */
    public Map<Integer, double[]> consumoDiario(Connection cn, int dias) throws SQLException {
        String sql = "SELECT insumo_id, creado_en::date AS dia, SUM(cantidad) AS total FROM movimientos "
                + "WHERE tipo = 'SALIDA' AND creado_en >= CURRENT_DATE - ? AND creado_en < CURRENT_DATE "
                + "GROUP BY insumo_id, creado_en::date";
        LocalDate inicio = LocalDate.now().minusDays(dias);
        Map<Integer, double[]> series = new HashMap<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, dias);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int indice = (int) (rs.getDate("dia").toLocalDate().toEpochDay() - inicio.toEpochDay());
                    if (indice >= 0 && indice < dias) {
                        series.computeIfAbsent(rs.getInt("insumo_id"), k -> new double[dias])[indice] =
                                rs.getBigDecimal("total").doubleValue();
                    }
                }
            }
        }
        return series;
    }

    /** Insumos más consumidos (salidas) en los últimos días, valorizados en soles. */
    public List<Conteo> topConsumo(Connection cn, int dias, int limite) throws SQLException {
        String sql = "SELECT i.producto, SUM(m.cantidad * i.precio) AS valor FROM movimientos m "
                + "JOIN inventario i ON i.id = m.insumo_id "
                + "WHERE m.tipo = 'SALIDA' AND m.creado_en >= CURRENT_DATE - ? "
                + "GROUP BY i.producto ORDER BY valor DESC LIMIT ?";
        List<Conteo> lista = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, dias);
            ps.setInt(2, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Conteo(rs.getString(1), rs.getBigDecimal(2).intValue()));
                }
            }
        }
        return lista;
    }

    /** Valor total en soles de un tipo de movimiento en los últimos días (p. ej. mermas). */
    public BigDecimal valorPorTipo(Connection cn, String tipo, int dias) throws SQLException {
        String sql = "SELECT COALESCE(SUM(ABS(m.cantidad) * i.precio), 0) FROM movimientos m "
                + "JOIN inventario i ON i.id = m.insumo_id WHERE m.tipo = ? AND m.creado_en >= CURRENT_DATE - ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, tipo);
            ps.setInt(2, dias);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        }
    }

    public boolean hayMovimientos(Connection cn) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("SELECT EXISTS (SELECT 1 FROM movimientos)");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getBoolean(1);
        }
    }
}
