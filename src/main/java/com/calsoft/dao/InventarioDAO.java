package com.calsoft.dao;

import com.calsoft.config.Conexion;
import com.calsoft.modelo.Producto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** CRUD de la tabla inventario (productos y stock). */
public class InventarioDAO {

    public List<Producto> listar() throws SQLException {
        List<Producto> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("SELECT * FROM inventario ORDER BY producto ASC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public void crear(Producto p) throws SQLException {
        String sql = "INSERT INTO inventario (producto, categoria, stock, unidad, stock_minimo, precio) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            asignar(ps, p);
            ps.executeUpdate();
        }
    }

    public boolean actualizar(Producto p) throws SQLException {
        String sql = "UPDATE inventario SET producto = ?, categoria = ?, stock = ?, "
                + "unidad = ?, stock_minimo = ?, precio = ? WHERE id = ?";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            asignar(ps, p);
            ps.setInt(7, p.getId());
            return ps.executeUpdate() > 0;
        }
    }

    // TODO: AGREGAR UN SOFT DELETE
    public boolean eliminar(int id) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM inventario WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private static void asignar(PreparedStatement ps, Producto p) throws SQLException {
        ps.setString(1, p.getProducto());
        ps.setString(2, p.getCategoria());
        ps.setInt(3, p.getStock());
        ps.setString(4, p.getUnidad());
        ps.setInt(5, p.getStockMinimo());
        ps.setBigDecimal(6, p.getPrecio());
    }

    private static Producto mapear(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setId(rs.getInt("id"));
        p.setProducto(rs.getString("producto"));
        p.setCategoria(rs.getString("categoria"));
        p.setStock(rs.getInt("stock"));
        p.setUnidad(rs.getString("unidad"));
        p.setStockMinimo(rs.getInt("stock_minimo"));
        p.setPrecio(rs.getBigDecimal("precio"));
        return p;
    }
}
