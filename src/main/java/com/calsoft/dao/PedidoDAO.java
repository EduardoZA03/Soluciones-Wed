package com.calsoft.dao;

import com.calsoft.config.Conexion;
import com.calsoft.modelo.Pedido;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    public void crear(Pedido p) throws SQLException {
        String sql = "INSERT INTO pedidos (cliente, mesero, producto, cantidad, mesa, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, p.getCliente());
            ps.setString(2, p.getMesero());
            ps.setString(3, p.getProducto());
            ps.setInt(4, p.getCantidad());
            ps.setInt(5, p.getMesa());
            ps.setString(6, p.getEstado());
            ps.executeUpdate();
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
        return p;
    }
}
