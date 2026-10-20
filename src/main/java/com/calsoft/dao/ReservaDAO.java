package com.calsoft.dao;

import com.calsoft.config.Conexion;
import com.calsoft.modelo.Reserva;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

/** Acceso a la tabla reservas. Mismo patrón que PedidoDAO. */
public class ReservaDAO {

    public List<Reserva> listar() throws SQLException {
        return consultar("SELECT * FROM reservas ORDER BY fecha ASC, hora ASC", 0);
    }

    /** Reservas de hoy en adelante que no están canceladas (para el Inicio). */
    public List<Reserva> listarProximas(int limite) throws SQLException {
        return consultar("SELECT * FROM reservas WHERE estado <> 'Cancelada' AND fecha >= CURRENT_DATE "
                + "ORDER BY fecha ASC, hora ASC LIMIT ?", limite);
    }

    private List<Reserva> consultar(String sql, int limite) throws SQLException {
        List<Reserva> lista = new ArrayList<>();
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

    public void crear(Reserva r) throws SQLException {
        String sql = "INSERT INTO reservas (cliente, telefono, fecha, hora, personas, mesa, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, r.getCliente());
            ps.setString(2, r.getTelefono());
            ps.setDate(3, Date.valueOf(r.getFecha()));
            ps.setTime(4, Time.valueOf(r.getHora()));
            ps.setInt(5, r.getPersonas());
            ps.setInt(6, r.getMesa());
            ps.setString(7, r.getEstado());
            ps.executeUpdate();
        }
    }

    public boolean actualizarEstado(int id, String estado) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("UPDATE reservas SET estado = ? WHERE id = ?")) {
            ps.setString(1, estado);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int id) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM reservas WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private static Reserva mapear(ResultSet rs) throws SQLException {
        Reserva r = new Reserva();
        r.setId(rs.getInt("id"));
        r.setCliente(rs.getString("cliente"));
        r.setTelefono(rs.getString("telefono"));
        r.setFecha(rs.getDate("fecha").toLocalDate());
        r.setHora(rs.getTime("hora").toLocalTime());
        r.setPersonas(rs.getInt("personas"));
        r.setMesa(rs.getInt("mesa"));
        r.setEstado(rs.getString("estado"));
        return r;
    }
}
