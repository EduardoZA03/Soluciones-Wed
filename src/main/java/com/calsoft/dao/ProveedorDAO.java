package com.calsoft.dao;

import com.calsoft.config.Conexion;
import com.calsoft.modelo.Proveedor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Acceso a la tabla proveedores. */
public class ProveedorDAO {

    public List<Proveedor> listar() throws SQLException {
        List<Proveedor> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("SELECT * FROM proveedores ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Proveedor buscar(Connection cn, int id) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("SELECT * FROM proveedores WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /** El primer proveedor registrado, o null si no hay ninguno. */
    public Proveedor primero(Connection cn) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("SELECT * FROM proveedores ORDER BY id LIMIT 1");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? mapear(rs) : null;
        }
    }

    /** Usado por el proveedor simulado para validar la API key recibida. */
    public boolean existeApiKey(String apiKey) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("SELECT EXISTS (SELECT 1 FROM proveedores WHERE api_key = ?)")) {
            ps.setString(1, apiKey);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBoolean(1);
            }
        }
    }

    public void guardar(Proveedor p) throws SQLException {
        String sql = p.getId() == 0
                ? "INSERT INTO proveedores (nombre, ruc, telefono, email, api_url, api_key) VALUES (?, ?, ?, ?, ?, ?)"
                : "UPDATE proveedores SET nombre = ?, ruc = ?, telefono = ?, email = ?, api_url = ?, api_key = ? "
                + "WHERE id = ?";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getRuc());
            ps.setString(3, p.getTelefono());
            ps.setString(4, p.getEmail());
            ps.setString(5, p.getApiUrl());
            ps.setString(6, p.getApiKey());
            if (p.getId() != 0) {
                ps.setInt(7, p.getId());
            }
            ps.executeUpdate();
        }
    }

    public boolean eliminar(int id) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM proveedores WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private static Proveedor mapear(ResultSet rs) throws SQLException {
        Proveedor p = new Proveedor();
        p.setId(rs.getInt("id"));
        p.setNombre(rs.getString("nombre"));
        p.setRuc(rs.getString("ruc"));
        p.setTelefono(rs.getString("telefono"));
        p.setEmail(rs.getString("email"));
        p.setApiUrl(rs.getString("api_url"));
        p.setApiKey(rs.getString("api_key"));
        return p;
    }
}
