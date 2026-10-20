package com.calsoft.dao;

import com.calsoft.config.Conexion;
import com.calsoft.modelo.Producto;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** Acceso a la tabla inventario (insumos y stock). */
public class InventarioDAO {

    /** SQLSTATE de PostgreSQL cuando una clave foránea impide borrar. */
    public static final String EN_USO = "23503";

    private static final String SELECT = "SELECT i.*, p.nombre AS proveedor_nombre "
            + "FROM inventario i LEFT JOIN proveedores p ON p.id = i.proveedor_id ";

    public List<Producto> listar() throws SQLException {
        try (Connection cn = Conexion.obtener()) {
            return listar(cn);
        }
    }

    public List<Producto> listar(Connection cn) throws SQLException {
        List<Producto> lista = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(SELECT + "ORDER BY i.producto ASC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /** Insumo por id, o null si no existe. */
    public Producto buscar(Connection cn, int id) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SELECT + "WHERE i.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /** Crea el insumo y devuelve su id. */
    public int crear(Connection cn, Producto p) throws SQLException {
        String sql = "INSERT INTO inventario (producto, categoria, stock, unidad, stock_minimo, precio, "
                + "fecha_vencimiento, proveedor_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getProducto());
            ps.setString(2, p.getCategoria());
            ps.setBigDecimal(3, p.getStock());
            ps.setString(4, p.getUnidad());
            ps.setBigDecimal(5, p.getStockMinimo());
            ps.setBigDecimal(6, p.getPrecio());
            asignarOpcionales(ps, 7, p);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt("id");
            }
        }
    }

    /**
     * Actualiza los datos del insumo. El stock NO se toca aquí: solo cambia
     * mediante movimientos (entrada, salida, merma, ajuste) para que quede en el kardex.
     */
    public boolean actualizar(Connection cn, Producto p) throws SQLException {
        String sql = "UPDATE inventario SET producto = ?, categoria = ?, unidad = ?, stock_minimo = ?, "
                + "precio = ?, fecha_vencimiento = ?, proveedor_id = ? WHERE id = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, p.getProducto());
            ps.setString(2, p.getCategoria());
            ps.setString(3, p.getUnidad());
            ps.setBigDecimal(4, p.getStockMinimo());
            ps.setBigDecimal(5, p.getPrecio());
            asignarOpcionales(ps, 6, p);
            ps.setInt(8, p.getId());
            return ps.executeUpdate() > 0;
        }
    }

    private static void asignarOpcionales(PreparedStatement ps, int desde, Producto p) throws SQLException {
        if (p.getFechaVencimiento() == null) {
            ps.setNull(desde, Types.DATE);
        } else {
            ps.setDate(desde, Date.valueOf(p.getFechaVencimiento()));
        }
        if (p.getProveedorId() == null) {
            ps.setNull(desde + 1, Types.INTEGER);
        } else {
            ps.setInt(desde + 1, p.getProveedorId());
        }
    }

    /**
     * Suma (o resta, si delta es negativo) al stock sin dejarlo en negativo.
     *
     * @return el stock resultante, o null si no alcanza o el insumo no existe
     */
    public BigDecimal sumarStock(Connection cn, int id, BigDecimal delta) throws SQLException {
        String sql = "UPDATE inventario SET stock = stock + ? WHERE id = ? AND stock + ? >= 0 RETURNING stock";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setBigDecimal(1, delta);
            ps.setInt(2, id);
            ps.setBigDecimal(3, delta);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : null;
            }
        }
    }

    /** Fija el stock a un valor contado (ajuste de inventario). */
    public void fijarStock(Connection cn, int id, BigDecimal valor) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("UPDATE inventario SET stock = ? WHERE id = ?")) {
            ps.setBigDecimal(1, valor);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    /** Bloquea la fila hasta el fin de la transacción y devuelve el stock actual. */
    public BigDecimal stockParaActualizar(Connection cn, int id) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("SELECT stock FROM inventario WHERE id = ? FOR UPDATE")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : null;
            }
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

    static Producto mapear(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setId(rs.getInt("id"));
        p.setProducto(rs.getString("producto"));
        p.setCategoria(rs.getString("categoria"));
        p.setStock(rs.getBigDecimal("stock"));
        p.setUnidad(rs.getString("unidad"));
        p.setStockMinimo(rs.getBigDecimal("stock_minimo"));
        p.setPrecio(rs.getBigDecimal("precio"));
        Date vence = rs.getDate("fecha_vencimiento");
        p.setFechaVencimiento(vence == null ? null : vence.toLocalDate());
        int prov = rs.getInt("proveedor_id");
        p.setProveedorId(rs.wasNull() ? null : prov);
        p.setProveedorNombre(rs.getString("proveedor_nombre"));
        return p;
    }
}
