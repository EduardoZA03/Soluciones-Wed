package com.calsoft.dao;

import com.calsoft.config.Conexion;
import com.calsoft.modelo.Conteo;
import com.calsoft.modelo.Resumen;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Consultas de agregación sobre las tablas del sistema. */
public class ReporteDAO {

    private final OrdenCompraDAO ordenDAO = new OrdenCompraDAO();
    private final MovimientoDAO movimientoDAO = new MovimientoDAO();

    public Resumen resumen() throws SQLException {
        Resumen r = new Resumen();
        try (Connection cn = Conexion.obtener()) {
            // Pedidos por estado + total
            try (PreparedStatement ps = cn.prepareStatement(
                    "SELECT COUNT(*), "
                    + "COUNT(*) FILTER (WHERE estado = 'Pendiente'), "
                    + "COUNT(*) FILTER (WHERE estado = 'En cocina'), "
                    + "COUNT(*) FILTER (WHERE estado = 'Entregado') "
                    + "FROM pedidos");
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                r.setPedidosTotal(rs.getInt(1));
                r.setPedidosPendientes(rs.getInt(2));
                r.setPedidosEnCocina(rs.getInt(3));
                r.setPedidosEntregados(rs.getInt(4));
            }

            // Reservas por estado + total
            try (PreparedStatement ps = cn.prepareStatement(
                    "SELECT COUNT(*), "
                    + "COUNT(*) FILTER (WHERE estado = 'Pendiente'), "
                    + "COUNT(*) FILTER (WHERE estado = 'Confirmada'), "
                    + "COUNT(*) FILTER (WHERE estado = 'Cancelada') "
                    + "FROM reservas");
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                r.setReservasTotal(rs.getInt(1));
                r.setReservasPendientes(rs.getInt(2));
                r.setReservasConfirmadas(rs.getInt(3));
                r.setReservasCanceladas(rs.getInt(4));
            }

            // Inventario: total de productos, bajo stock y valor total
            try (PreparedStatement ps = cn.prepareStatement(
                    "SELECT COUNT(*), "
                    + "COUNT(*) FILTER (WHERE stock <= stock_minimo), "
                    + "COALESCE(SUM(stock * precio), 0) "
                    + "FROM inventario");
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                r.setProductosTotal(rs.getInt(1));
                r.setBajoStock(rs.getInt(2));
                r.setValorInventario(rs.getBigDecimal(3));
            }

            // Usuarios por rol
            r.setUsuariosPorRol(conteos(cn,
                    "SELECT rol, COUNT(*) AS cantidad FROM usuarios "
                    + "GROUP BY rol ORDER BY cantidad DESC"));

            // Top 5 productos más pedidos (por cantidad acumulada)
            r.setTopProductos(conteos(cn,
                    "SELECT producto, SUM(cantidad) AS total FROM pedidos "
                    + "GROUP BY producto ORDER BY total DESC LIMIT 5"));

            // Inventario con IA e interoperabilidad: alertas, órdenes, consumo y mermas (30 días)
            try (PreparedStatement ps = cn.prepareStatement(
                    "SELECT COUNT(*) FROM alertas WHERE estado = 'Pendiente'");
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                r.setAlertasPendientes(rs.getInt(1));
            }
            r.setOrdenesEnCurso(ordenDAO.contarEnCurso(cn));
            r.setOrdenesPorEstado(ordenDAO.contarPorEstado(cn));
            r.setTopConsumo(movimientoDAO.topConsumo(cn, 30, 5));
            r.setConsumo30(movimientoDAO.valorPorTipo(cn, "SALIDA", 30));
            r.setMermas30(movimientoDAO.valorPorTipo(cn, "MERMA", 30));
        }
        return r;
    }

    private static List<Conteo> conteos(Connection cn, String sql) throws SQLException {
        List<Conteo> lista = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Conteo(rs.getString(1), rs.getInt(2)));
            }
        }
        return lista;
    }
}
