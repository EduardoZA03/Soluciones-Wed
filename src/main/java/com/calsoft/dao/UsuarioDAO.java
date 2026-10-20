package com.calsoft.dao;

import com.calsoft.config.Conexion;
import com.calsoft.modelo.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;

/** Acceso a la tabla usuarios. Nunca devuelve la columna "clave". */
public class UsuarioDAO {

    /** SQLSTATE de PostgreSQL para violación de UNIQUE. */
    public static final String DUPLICADO = "23505";

    /**
     * Valida usuario y clave contra el hash bcrypt guardado.
     *
     * @return el usuario si las credenciales son correctas; null en caso contrario
     */
    public Usuario autenticar(String usuario, String clave) throws SQLException {
        String sql = "SELECT id, usuario, clave, rol, creado_en FROM usuarios WHERE usuario = ?";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                if (!claveCorrecta(clave, rs.getString("clave"))) {
                    return null;
                }
                return mapear(rs);
            }
        }
    }

    private static boolean claveCorrecta(String clave, String hash) {
        // jBCrypt solo reconoce el prefijo $2a$; los hashes $2b$/$2y$
        // (p. ej. creados por el backend Node original) son equivalentes.
        String compatible = hash.replaceFirst("^\\$2[by]\\$", "\\$2a\\$");
        try {
            return BCrypt.checkpw(clave, compatible);
        } catch (IllegalArgumentException e) {
            return false; // hash con formato inválido
        }
    }

    private static String hashear(String clave) {
        return BCrypt.hashpw(clave, BCrypt.gensalt(10));
    }

    public List<Usuario> listar() throws SQLException {
        String sql = "SELECT id, usuario, rol, creado_en FROM usuarios ORDER BY usuario ASC";
        List<Usuario> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /** Crea el usuario con la clave hasheada. Lanza SQLException con estado 23505 si ya existe. */
    public void crear(String usuario, String clave, String rol) throws SQLException {
        String sql = "INSERT INTO usuarios (usuario, clave, rol) VALUES (?, ?, ?)";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, hashear(clave));
            ps.setString(3, rol);
            ps.executeUpdate();
        }
    }

    public boolean actualizarRol(int id, String rol) throws SQLException {
        return actualizar("UPDATE usuarios SET rol = ? WHERE id = ?", rol, id);
    }

    public boolean actualizarClave(int id, String clave) throws SQLException {
        return actualizar("UPDATE usuarios SET clave = ? WHERE id = ?", hashear(clave), id);
    }

    private boolean actualizar(String sql, String valor, int id) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, valor);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int id) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM usuarios WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private static Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        u.setUsuario(rs.getString("usuario"));
        u.setRol(rs.getString("rol"));
        Timestamp creado = rs.getTimestamp("creado_en");
        if (creado != null) {
            u.setCreadoEn(creado.toLocalDateTime());
        }
        return u;
    }
}
