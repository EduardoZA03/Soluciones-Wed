package com.calsoft.dao;

import com.calsoft.config.Conexion;
import com.calsoft.modelo.Plato;
import com.calsoft.modelo.RecetaItem;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Carta (platos) y recetas (insumos por porción). */
public class PlatoDAO {

    /** Todos los platos con su receta. Si soloActivos, solo los que se pueden vender. */
    public List<Plato> listar(boolean soloActivos) throws SQLException {
        Map<Integer, Plato> platos = new LinkedHashMap<>();
        try (Connection cn = Conexion.obtener()) {
            String sql = "SELECT * FROM platos " + (soloActivos ? "WHERE activo " : "") + "ORDER BY nombre";
            try (PreparedStatement ps = cn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Plato p = mapear(rs);
                    platos.put(p.getId(), p);
                }
            }
            String receta = "SELECT r.*, i.producto, i.unidad FROM recetas r "
                    + "JOIN inventario i ON i.id = r.insumo_id ORDER BY i.producto";
            try (PreparedStatement ps = cn.prepareStatement(receta);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Plato p = platos.get(rs.getInt("plato_id"));
                    if (p != null) {
                        p.getReceta().add(mapearItem(rs));
                    }
                }
            }
        }
        return new ArrayList<>(platos.values());
    }

    /** Plato con su receta, o null si no existe. */
    public Plato buscar(Connection cn, int id) throws SQLException {
        Plato plato;
        try (PreparedStatement ps = cn.prepareStatement("SELECT * FROM platos WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                plato = mapear(rs);
            }
        }
        String receta = "SELECT r.*, i.producto, i.unidad FROM recetas r "
                + "JOIN inventario i ON i.id = r.insumo_id WHERE r.plato_id = ? ORDER BY i.producto";
        try (PreparedStatement ps = cn.prepareStatement(receta)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    plato.getReceta().add(mapearItem(rs));
                }
            }
        }
        return plato;
    }

    public void guardar(Plato p) throws SQLException {
        String sql = p.getId() == 0
                ? "INSERT INTO platos (nombre, precio, activo) VALUES (?, ?, ?)"
                : "UPDATE platos SET nombre = ?, precio = ?, activo = ? WHERE id = ?";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setBigDecimal(2, p.getPrecio());
            ps.setBoolean(3, p.isActivo());
            if (p.getId() != 0) {
                ps.setInt(4, p.getId());
            }
            ps.executeUpdate();
        }
    }

    public boolean eliminar(int id) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM platos WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /** Agrega el insumo a la receta, o cambia su cantidad si ya estaba. */
    public void guardarIngrediente(int platoId, int insumoId, BigDecimal cantidad) throws SQLException {
        String sql = "INSERT INTO recetas (plato_id, insumo_id, cantidad) VALUES (?, ?, ?) "
                + "ON CONFLICT (plato_id, insumo_id) DO UPDATE SET cantidad = EXCLUDED.cantidad";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, platoId);
            ps.setInt(2, insumoId);
            ps.setBigDecimal(3, cantidad);
            ps.executeUpdate();
        }
    }

    public boolean quitarIngrediente(int recetaId) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM recetas WHERE id = ?")) {
            ps.setInt(1, recetaId);
            return ps.executeUpdate() > 0;
        }
    }

    private static Plato mapear(ResultSet rs) throws SQLException {
        Plato p = new Plato();
        p.setId(rs.getInt("id"));
        p.setNombre(rs.getString("nombre"));
        p.setPrecio(rs.getBigDecimal("precio"));
        p.setActivo(rs.getBoolean("activo"));
        return p;
    }

    private static RecetaItem mapearItem(ResultSet rs) throws SQLException {
        RecetaItem r = new RecetaItem();
        r.setId(rs.getInt("id"));
        r.setInsumoId(rs.getInt("insumo_id"));
        r.setInsumo(rs.getString("producto"));
        r.setUnidad(rs.getString("unidad"));
        r.setCantidad(rs.getBigDecimal("cantidad"));
        return r;
    }
}
