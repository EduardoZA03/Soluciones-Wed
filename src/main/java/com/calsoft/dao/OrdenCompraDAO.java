package com.calsoft.dao;

import com.calsoft.config.Conexion;
import com.calsoft.modelo.Conteo;
import com.calsoft.modelo.OrdenCompra;
import com.calsoft.modelo.OrdenEvento;
import com.calsoft.modelo.OrdenItem;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Órdenes de compra, sus ítems y su línea de tiempo (eventos). */
public class OrdenCompraDAO {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String LETRAS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private static final String SELECT = "SELECT o.*, p.nombre AS proveedor_nombre, "
            + "COALESCE((SELECT SUM(cantidad * precio_unitario) FROM orden_items WHERE orden_id = o.id), 0) AS total, "
            + "(SELECT COUNT(*) FROM orden_items WHERE orden_id = o.id) AS cantidad_items "
            + "FROM ordenes_compra o JOIN proveedores p ON p.id = o.proveedor_id ";

    /** Código único que va en el QR, p. ej. OC-20261019-7KQ2XD. */
    private static String nuevoCodigo() {
        StringBuilder sb = new StringBuilder("OC-")
                .append(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)).append('-');
        for (int i = 0; i < 6; i++) {
            sb.append(LETRAS.charAt(RANDOM.nextInt(LETRAS.length())));
        }
        return sb.toString();
    }

    /** Crea una orden en estado Borrador y registra el primer evento. */
    public int crear(Connection cn, int proveedorId, Integer alertaId, String usuario, String detalle)
            throws SQLException {
        String sql = "INSERT INTO ordenes_compra (codigo, proveedor_id, alerta_id, creado_por) VALUES (?, ?, ?, ?)";
        int id;
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nuevoCodigo());
            ps.setInt(2, proveedorId);
            if (alertaId == null) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, alertaId);
            }
            ps.setString(4, usuario);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                id = rs.getInt("id");
            }
        }
        registrarEvento(cn, id, OrdenCompra.BORRADOR, detalle, usuario);
        return id;
    }

    /** Agrega el insumo a la orden; si ya estaba, suma la cantidad. */
    public void agregarItem(Connection cn, int ordenId, int insumoId, BigDecimal cantidad, BigDecimal precio)
            throws SQLException {
        String sql = "INSERT INTO orden_items (orden_id, insumo_id, cantidad, precio_unitario) VALUES (?, ?, ?, ?) "
                + "ON CONFLICT (orden_id, insumo_id) DO UPDATE SET cantidad = orden_items.cantidad + EXCLUDED.cantidad";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, ordenId);
            ps.setInt(2, insumoId);
            ps.setBigDecimal(3, cantidad);
            ps.setBigDecimal(4, precio);
            ps.executeUpdate();
        }
    }

    public void actualizarItem(Connection cn, int ordenId, int itemId, BigDecimal cantidad, BigDecimal precio)
            throws SQLException {
        String sql = "UPDATE orden_items SET cantidad = ?, precio_unitario = ? WHERE id = ? AND orden_id = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setBigDecimal(1, cantidad);
            ps.setBigDecimal(2, precio);
            ps.setInt(3, itemId);
            ps.setInt(4, ordenId);
            ps.executeUpdate();
        }
    }

    public void quitarItem(Connection cn, int ordenId, int itemId) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("DELETE FROM orden_items WHERE id = ? AND orden_id = ?")) {
            ps.setInt(1, itemId);
            ps.setInt(2, ordenId);
            ps.executeUpdate();
        }
    }

    public void cambiarEstado(Connection cn, int id, String estado, String detalle, String usuario)
            throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("UPDATE ordenes_compra SET estado = ? WHERE id = ?")) {
            ps.setString(1, estado);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
        registrarEvento(cn, id, estado, detalle, usuario);
    }

    /** Guarda en la línea de tiempo un evento (también los errores de la API). */
    public void registrarEvento(Connection cn, int ordenId, String estado, String detalle, String usuario)
            throws SQLException {
        String sql = "INSERT INTO orden_eventos (orden_id, estado, detalle, usuario) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, ordenId);
            ps.setString(2, estado);
            ps.setString(3, detalle);
            ps.setString(4, usuario);
            ps.executeUpdate();
        }
    }

    /** Guarda la respuesta del proveedor: N° de confirmación, fecha de entrega. */
    public void guardarConfirmacion(Connection cn, int id, String confirmacion, LocalDate entrega)
            throws SQLException {
        String sql = "UPDATE ordenes_compra SET confirmacion_proveedor = ?, fecha_entrega = ? WHERE id = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, confirmacion);
            if (entrega == null) {
                ps.setNull(2, Types.DATE);
            } else {
                ps.setDate(2, Date.valueOf(entrega));
            }
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    public void marcarDisponibilidad(Connection cn, int itemId, boolean disponible) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("UPDATE orden_items SET disponible = ? WHERE id = ?")) {
            ps.setBoolean(1, disponible);
            ps.setInt(2, itemId);
            ps.executeUpdate();
        }
    }

    public void registrarRecibido(Connection cn, int itemId, BigDecimal recibido) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("UPDATE orden_items SET cantidad_recibida = ? WHERE id = ?")) {
            ps.setBigDecimal(1, recibido);
            ps.setInt(2, itemId);
            ps.executeUpdate();
        }
    }

    public List<OrdenCompra> listar(String estado) throws SQLException {
        String sql = SELECT + (estado == null ? "" : "WHERE o.estado = ? ") + "ORDER BY o.id DESC LIMIT 200";
        List<OrdenCompra> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            if (estado != null) {
                ps.setString(1, estado);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /** Orden completa (ítems y eventos) por id, o null. */
    public OrdenCompra buscar(Connection cn, int id) throws SQLException {
        return buscarPor(cn, "o.id = ?", id);
    }

    /** Orden completa por el código del QR, o null. */
    public OrdenCompra buscarPorCodigo(Connection cn, String codigo) throws SQLException {
        return buscarPor(cn, "o.codigo = ?", codigo);
    }

    private OrdenCompra buscarPor(Connection cn, String condicion, Object valor) throws SQLException {
        OrdenCompra o;
        try (PreparedStatement ps = cn.prepareStatement(SELECT + "WHERE " + condicion)) {
            ps.setObject(1, valor);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                o = mapear(rs);
            }
        }
        String items = "SELECT it.*, i.producto, i.unidad FROM orden_items it "
                + "JOIN inventario i ON i.id = it.insumo_id WHERE it.orden_id = ? ORDER BY i.producto";
        try (PreparedStatement ps = cn.prepareStatement(items)) {
            ps.setInt(1, o.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrdenItem it = new OrdenItem();
                    it.setId(rs.getInt("id"));
                    it.setInsumoId(rs.getInt("insumo_id"));
                    it.setInsumo(rs.getString("producto"));
                    it.setUnidad(rs.getString("unidad"));
                    it.setCantidad(rs.getBigDecimal("cantidad"));
                    it.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    boolean disp = rs.getBoolean("disponible");
                    it.setDisponible(rs.wasNull() ? null : disp);
                    it.setCantidadRecibida(rs.getBigDecimal("cantidad_recibida"));
                    o.getItems().add(it);
                }
            }
        }
        String eventos = "SELECT * FROM orden_eventos WHERE orden_id = ? ORDER BY creado_en, id";
        try (PreparedStatement ps = cn.prepareStatement(eventos)) {
            ps.setInt(1, o.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrdenEvento e = new OrdenEvento();
                    e.setEstado(rs.getString("estado"));
                    e.setDetalle(rs.getString("detalle"));
                    e.setUsuario(rs.getString("usuario"));
                    e.setCreadoEn(rs.getTimestamp("creado_en").toLocalDateTime());
                    o.getEventos().add(e);
                }
            }
        }
        return o;
    }

    /** Cantidad de órdenes por estado (para reportes). */
    public List<Conteo> contarPorEstado(Connection cn) throws SQLException {
        List<Conteo> lista = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(
                "SELECT estado, COUNT(*) FROM ordenes_compra GROUP BY estado ORDER BY COUNT(*) DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Conteo(rs.getString(1), rs.getInt(2)));
            }
        }
        return lista;
    }

    /** Órdenes que aún no terminan (ni recibidas, ni rechazadas, ni canceladas). */
    public int contarEnCurso(Connection cn) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("SELECT COUNT(*) FROM ordenes_compra "
                + "WHERE estado IN ('Borrador', 'Aprobada', 'Enviada', 'Confirmada')");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private static OrdenCompra mapear(ResultSet rs) throws SQLException {
        OrdenCompra o = new OrdenCompra();
        o.setId(rs.getInt("id"));
        o.setCodigo(rs.getString("codigo"));
        o.setProveedorId(rs.getInt("proveedor_id"));
        o.setProveedorNombre(rs.getString("proveedor_nombre"));
        o.setEstado(rs.getString("estado"));
        int alerta = rs.getInt("alerta_id");
        o.setAlertaId(rs.wasNull() ? null : alerta);
        Date entrega = rs.getDate("fecha_entrega");
        o.setFechaEntrega(entrega == null ? null : entrega.toLocalDate());
        o.setConfirmacionProveedor(rs.getString("confirmacion_proveedor"));
        o.setCreadoPor(rs.getString("creado_por"));
        o.setCreadoEn(rs.getTimestamp("creado_en").toLocalDateTime());
        o.setTotal(rs.getBigDecimal("total"));
        o.setCantidadItems(rs.getInt("cantidad_items"));
        return o;
    }
}
