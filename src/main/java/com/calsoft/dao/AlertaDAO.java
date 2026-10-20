package com.calsoft.dao;

import com.calsoft.config.Conexion;
import com.calsoft.modelo.Alerta;
import com.calsoft.modelo.Producto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Alertas automáticas (HU-04).
 *
 * Las alertas se generan con INSERT ... SELECT: la base de datos decide qué
 * insumos están en el mínimo o por vencer. El índice único parcial
 * ux_alertas_pendiente evita crear dos alertas pendientes iguales.
 */
public class AlertaDAO {

    private static final String SIN_DUPLICAR =
            " ON CONFLICT (insumo_id, tipo) WHERE estado = 'Pendiente' DO NOTHING";

    /**
     * Revisa el stock de un insumo (o de todos si insumoId es null):
     * crea la alerta si está en el mínimo y la cierra sola si ya se repuso.
     */
    public void evaluarStock(Connection cn, Integer insumoId) throws SQLException {
        String filtro = insumoId == null ? "" : " AND id = ?";
        String crear = "INSERT INTO alertas (insumo_id, tipo, mensaje) "
                + "SELECT id, 'STOCK_MINIMO', 'Stock en ' || trim_scale(stock) || ' ' || unidad "
                + "|| ' (mínimo ' || trim_scale(stock_minimo) || ')' "
                + "FROM inventario WHERE stock <= stock_minimo" + filtro + SIN_DUPLICAR;
        String cerrar = "UPDATE alertas a SET estado = 'Atendida', atendida_en = now(), atendida_por = 'Sistema' "
                + "FROM inventario i WHERE a.insumo_id = i.id AND a.tipo = 'STOCK_MINIMO' "
                + "AND a.estado = 'Pendiente' AND i.stock > i.stock_minimo"
                + (insumoId == null ? "" : " AND i.id = ?");
        for (String sql : new String[] {crear, cerrar}) {
            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                if (insumoId != null) {
                    ps.setInt(1, insumoId);
                }
                ps.executeUpdate();
            }
        }
    }

    /** Crea alertas para insumos vencidos o que vencen en los próximos días (con stock). */
    public void evaluarVencimientos(Connection cn) throws SQLException {
        String sql = "INSERT INTO alertas (insumo_id, tipo, mensaje) "
                + "SELECT id, 'VENCIMIENTO', CASE WHEN fecha_vencimiento < CURRENT_DATE "
                + "THEN 'Vencido desde ' ELSE 'Vence el ' END || to_char(fecha_vencimiento, 'YYYY-MM-DD') "
                + "FROM inventario WHERE fecha_vencimiento IS NOT NULL AND stock > 0 "
                + "AND fecha_vencimiento <= CURRENT_DATE + " + Producto.DIAS_AVISO_VENCIMIENTO + SIN_DUPLICAR;
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

    /** Revisa stock y vencimientos de todo el inventario. */
    public void evaluarTodo() throws SQLException {
        try (Connection cn = Conexion.obtener()) {
            evaluarStock(cn, null);
            evaluarVencimientos(cn);
        }
    }

    public int contarPendientes() throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("SELECT COUNT(*) FROM alertas WHERE estado = 'Pendiente'");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /** Alertas de un estado ("Pendiente" o "Atendida"), con la orden de compra que generó cada una. */
    public List<Alerta> listar(String estado, int limite) throws SQLException {
        String sql = "SELECT a.*, i.producto, o.id AS orden_id, o.codigo AS orden_codigo FROM alertas a "
                + "JOIN inventario i ON i.id = a.insumo_id "
                + "LEFT JOIN LATERAL (SELECT id, codigo FROM ordenes_compra WHERE alerta_id = a.id "
                + "AND estado <> 'Cancelada' ORDER BY id DESC LIMIT 1) o ON true "
                + "WHERE a.estado = ? ORDER BY a.creado_en DESC LIMIT ?";
        List<Alerta> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public Alerta buscar(Connection cn, int id) throws SQLException {
        String sql = "SELECT a.*, i.producto, NULL::int AS orden_id, NULL AS orden_codigo FROM alertas a "
                + "JOIN inventario i ON i.id = a.insumo_id WHERE a.id = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public boolean atender(Connection cn, int id, String usuario) throws SQLException {
        String sql = "UPDATE alertas SET estado = 'Atendida', atendida_en = now(), atendida_por = ? "
                + "WHERE id = ? AND estado = 'Pendiente'";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean atender(int id, String usuario) throws SQLException {
        try (Connection cn = Conexion.obtener()) {
            return atender(cn, id, usuario);
        }
    }

    private static Alerta mapear(ResultSet rs) throws SQLException {
        Alerta a = new Alerta();
        a.setId(rs.getInt("id"));
        a.setInsumoId(rs.getInt("insumo_id"));
        a.setInsumo(rs.getString("producto"));
        a.setTipo(rs.getString("tipo"));
        a.setMensaje(rs.getString("mensaje"));
        a.setEstado(rs.getString("estado"));
        a.setCreadoEn(rs.getTimestamp("creado_en").toLocalDateTime());
        Timestamp atendida = rs.getTimestamp("atendida_en");
        a.setAtendidaEn(atendida == null ? null : atendida.toLocalDateTime());
        a.setAtendidaPor(rs.getString("atendida_por"));
        int orden = rs.getInt("orden_id");
        a.setOrdenId(rs.wasNull() ? null : orden);
        a.setOrdenCodigo(rs.getString("orden_codigo"));
        return a;
    }
}
