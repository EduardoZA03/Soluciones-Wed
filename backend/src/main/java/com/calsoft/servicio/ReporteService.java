package com.calsoft.servicio;

import com.calsoft.dto.Dtos.Conteo;
import com.calsoft.modelo.OrdenCompra;
import com.calsoft.modelo.Pedido;
import com.calsoft.modelo.Reserva;
import com.calsoft.repositorio.OrdenCompraRepository;
import com.calsoft.repositorio.PedidoRepository;
import com.calsoft.repositorio.ReservaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Estadísticas para el Inicio y los Reportes (consultas de agregación). */
@Service
public class ReporteService {

    private final JdbcTemplate jdbc;
    private final AlertaService alertas;
    private final PrediccionService prediccion;
    private final OrdenCompraRepository ordenRepo;
    private final PedidoRepository pedidoRepo;
    private final ReservaRepository reservaRepo;

    public ReporteService(JdbcTemplate jdbc, AlertaService alertas, PrediccionService prediccion,
            OrdenCompraRepository ordenRepo, PedidoRepository pedidoRepo, ReservaRepository reservaRepo) {
        this.jdbc = jdbc;
        this.alertas = alertas;
        this.prediccion = prediccion;
        this.ordenRepo = ordenRepo;
        this.pedidoRepo = pedidoRepo;
        this.reservaRepo = reservaRepo;
    }

    /** Datos de la pantalla de Inicio. */
    @Transactional
    public Map<String, Object> inicio() {
        alertas.evaluarTodo();
        Map<String, Object> r = new LinkedHashMap<>(resumen());
        r.put("pedidosRecientes", pedidoRepo.findTop5ByOrderByIdDesc());
        r.put("reservasProximas", reservaRepo
                .findTop5ByEstadoNotAndFechaGreaterThanEqualOrderByFechaAscHoraAsc("Cancelada", LocalDate.now()));
        return r;
    }

    /** Todos los indicadores de la pantalla de Reportes. */
    @Transactional(readOnly = true)
    public Map<String, Object> reportes() {
        Map<String, Object> r = new LinkedHashMap<>(resumen());
        r.put("usuariosPorRol", conteos("SELECT rol, COUNT(*) FROM usuarios GROUP BY rol ORDER BY 2 DESC"));
        r.put("topProductos", conteos("SELECT producto, SUM(cantidad) FROM pedidos GROUP BY producto ORDER BY 2 DESC LIMIT 5"));
        r.put("topConsumo", conteos("SELECT i.producto, SUM(m.cantidad * i.precio) FROM movimientos m "
                + "JOIN inventario i ON i.id = m.insumo_id WHERE m.tipo = 'SALIDA' AND m.creado_en >= CURRENT_DATE - 30 "
                + "GROUP BY i.producto ORDER BY 2 DESC LIMIT 5"));
        r.put("ordenesPorEstado", conteos("SELECT estado, COUNT(*) FROM ordenes_compra GROUP BY estado ORDER BY 2 DESC"));
        r.put("consumo30", valorMovimientos("SALIDA"));
        r.put("mermas30", valorMovimientos("MERMA"));
        r.put("proyeccionCompras7", prediccion.predecir(7).stream()
                .map(PrediccionService.Prediccion::costoSugerido).reduce(BigDecimal.ZERO, BigDecimal::add));
        return r;
    }

    private Map<String, Object> resumen() {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("pedidos", porEstado("pedidos", Pedido.ESTADOS));
        r.put("reservas", porEstado("reservas", Reserva.ESTADOS));
        r.put("inventario", jdbc.queryForMap("SELECT COUNT(*) AS total, "
                + "COUNT(*) FILTER (WHERE stock <= stock_minimo) AS \"bajoStock\", "
                + "COALESCE(SUM(stock * precio), 0) AS \"valorTotal\" FROM inventario"));
        r.put("alertasPendientes", alertas.contarPendientes());
        r.put("ordenesEnCurso", ordenRepo.countByEstadoIn(List.of(OrdenCompra.BORRADOR, OrdenCompra.APROBADA,
                OrdenCompra.ENVIADA, OrdenCompra.CONFIRMADA)));
        return r;
    }

    /** {"total": n, "Pendiente": n, ...} */
    private Map<String, Long> porEstado(String tabla, List<String> estados) {
        Map<String, Long> m = new LinkedHashMap<>();
        m.put("total", 0L);
        estados.forEach(e -> m.put(e, 0L));
        jdbc.query("SELECT estado, COUNT(*) FROM " + tabla + " GROUP BY estado", rs -> {
            m.put(rs.getString(1), rs.getLong(2));
            m.merge("total", rs.getLong(2), Long::sum);
        });
        return m;
    }

    private List<Conteo> conteos(String sql) {
        return jdbc.query(sql, (rs, i) -> new Conteo(rs.getString(1), rs.getBigDecimal(2)));
    }

    private BigDecimal valorMovimientos(String tipo) {
        return jdbc.queryForObject("SELECT COALESCE(SUM(ABS(m.cantidad) * i.precio), 0) FROM movimientos m "
                + "JOIN inventario i ON i.id = m.insumo_id WHERE m.tipo = ? AND m.creado_en >= CURRENT_DATE - 30",
                BigDecimal.class, tipo);
    }
}
