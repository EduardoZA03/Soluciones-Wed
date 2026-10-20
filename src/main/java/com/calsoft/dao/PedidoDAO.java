package com.calsoft.dao;

import com.calsoft.config.Conexion;
import com.calsoft.modelo.Pedido;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** Acceso a la tabla pedidos. Todas las consultas son parametrizadas. */
public class PedidoDAO {

    public List<Pedido> listar() throws SQLException {
        return consultar("SELECT * FROM pedidos ORDER BY id ASC", 0);
    }

    /** Los últimos pedidos registrados (para el Inicio). */
    public List<Pedido> listarRecientes(int limite) throws SQLException {
        return consultar("SELECT * FROM pedidos ORDER BY id DESC LIMIT ?", limite);
    }

    private List<Pedido> consultar(String sql, int limite) throws SQLException {
        List<Pedido> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            if (limite > 0) {
                ps.setInt(1, limite);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /** Registra el pedido (venta del POS) y devuelve su id. */
    public int crear(Connection cn, Pedido p) throws SQLException {
        String sql = "INSERT INTO pedidos (cliente, mesero, producto, cantidad, mesa, estado, plato_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getCliente());
            ps.setString(2, p.getMesero());
            ps.setString(3, p.getProducto());
            ps.setInt(4, p.getCantidad());
            ps.setInt(5, p.getMesa());
            ps.setString(6, p.getEstado());
            if (p.getPlatoId() == null) {
                ps.setNull(7, Types.INTEGER);
            } else {
                ps.setInt(7, p.getPlatoId());
            }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt("id");
            }
        }
    }

    /** Marca el pedido como ya procesado en el inventario, para no descontarlo dos veces. */
    public boolean marcarDescontado(Connection cn, int id) throws SQLException {
        String sql = "UPDATE pedidos SET stock_descontado = true WHERE id = ? AND NOT stock_descontado";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean actualizarEstado(int id, String estado) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("UPDATE pedidos SET estado = ? WHERE id = ?")) {
            ps.setString(1, estado);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    /** Estado actual del pedido, o null si no existe. */
    public String obtenerEstado(int id) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("SELECT estado FROM pedidos WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    public boolean eliminar(int id) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM pedidos WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private static Pedido mapear(ResultSet rs) throws SQLException {
        Pedido p = new Pedido();
        p.setId(rs.getInt("id"));
        p.setCliente(rs.getString("cliente"));
        p.setMesero(rs.getString("mesero"));
        p.setProducto(rs.getString("producto"));
        p.setCantidad(rs.getInt("cantidad"));
        p.setMesa(rs.getInt("mesa"));
        p.setEstado(rs.getString("estado"));
        int plato = rs.getInt("plato_id");
        p.setPlatoId(rs.wasNull() ? null : plato);
        p.setStockDescontado(rs.getBoolean("stock_descontado"));
        return p;
    }
}
