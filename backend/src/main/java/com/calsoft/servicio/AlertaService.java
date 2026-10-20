package com.calsoft.servicio;

import com.calsoft.dto.Dtos.AlertaVista;
import com.calsoft.error.NoEncontradoException;
import com.calsoft.error.ValidacionException;
import com.calsoft.modelo.Alerta;
import com.calsoft.modelo.Insumo;
import com.calsoft.modelo.OrdenCompra;
import com.calsoft.repositorio.AlertaRepository;
import com.calsoft.repositorio.OrdenCompraRepository;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Alertas automáticas de stock mínimo y vencimiento (HU-04).
 *
 * Se generan con INSERT ... SELECT: la base de datos decide qué insumos están
 * en el mínimo o por vencer. El índice único parcial ux_alertas_pendiente
 * impide crear dos alertas pendientes iguales.
 */
@Service
public class AlertaService {

    private static final String SIN_DUPLICAR =
            " ON CONFLICT (insumo_id, tipo) WHERE estado = 'Pendiente' DO NOTHING";

    private final JdbcTemplate jdbc;
    private final AlertaRepository alertaRepo;
    private final OrdenCompraRepository ordenRepo;

    public AlertaService(JdbcTemplate jdbc, AlertaRepository alertaRepo, OrdenCompraRepository ordenRepo) {
        this.jdbc = jdbc;
        this.alertaRepo = alertaRepo;
        this.ordenRepo = ordenRepo;
    }

    /**
     * Revisa el stock de un insumo (o de todos si insumoId es null): crea la
     * alerta si está en el mínimo y la cierra sola si ya se repuso.
     */
    @Transactional
    public void evaluarStock(Integer insumoId) {
        String filtro = insumoId == null ? "" : " AND id = " + insumoId;
        jdbc.update("INSERT INTO alertas (insumo_id, tipo, mensaje) "
                + "SELECT id, 'STOCK_MINIMO', 'Stock en ' || trim_scale(stock) || ' ' || unidad "
                + "|| ' (mínimo ' || trim_scale(stock_minimo) || ')' "
                + "FROM inventario WHERE stock <= stock_minimo" + filtro + SIN_DUPLICAR);
        jdbc.update("UPDATE alertas a SET estado = 'Atendida', atendida_en = now(), atendida_por = 'Sistema' "
                + "FROM inventario i WHERE a.insumo_id = i.id AND a.tipo = 'STOCK_MINIMO' "
                + "AND a.estado = 'Pendiente' AND i.stock > i.stock_minimo"
                + (insumoId == null ? "" : " AND i.id = " + insumoId));
    }

    /** Crea alertas para insumos vencidos o que vencen pronto (con stock). */
    @Transactional
    public void evaluarVencimientos() {
        jdbc.update("INSERT INTO alertas (insumo_id, tipo, mensaje) "
                + "SELECT id, 'VENCIMIENTO', CASE WHEN fecha_vencimiento < CURRENT_DATE "
                + "THEN 'Vencido desde ' ELSE 'Vence el ' END || to_char(fecha_vencimiento, 'YYYY-MM-DD') "
                + "FROM inventario WHERE fecha_vencimiento IS NOT NULL AND stock > 0 "
                + "AND fecha_vencimiento <= CURRENT_DATE + " + Insumo.DIAS_AVISO_VENCIMIENTO + SIN_DUPLICAR);
    }

    @Transactional
    public void evaluarTodo() {
        evaluarStock(null);
        evaluarVencimientos();
    }

    @Transactional
    public List<AlertaVista> listar(String estado, int limite) {
        if ("Pendiente".equals(estado)) {
            evaluarTodo();
        }
        return alertaRepo.porEstado(estado, PageRequest.of(0, limite)).stream().map(this::vista).toList();
    }

    public long contarPendientes() {
        return alertaRepo.countByEstado("Pendiente");
    }

    @Transactional
    public void atender(Integer id, String usuario) {
        Alerta a = alertaRepo.findById(id).orElseThrow(() -> new NoEncontradoException("Alerta no encontrada"));
        if (!a.isPendiente()) {
            throw new ValidacionException("La alerta ya estaba atendida");
        }
        a.setEstado("Atendida");
        a.setAtendidaEn(OffsetDateTime.now());
        a.setAtendidaPor(usuario);
    }

    private AlertaVista vista(Alerta a) {
        OrdenCompra orden = ordenRepo.findFirstByAlertaIdAndEstadoNotOrderByIdDesc(a.getId(), OrdenCompra.CANCELADA)
                .orElse(null);
        return new AlertaVista(a.getId(), a.getInsumo().getId(), a.getInsumo().getNombre(), a.getTipo(),
                a.getMotivo(), a.getMensaje(), a.getEstado(), a.getCreadoEn(), a.getAtendidaEn(), a.getAtendidaPor(),
                orden == null ? null : orden.getId(), orden == null ? null : orden.getCodigo());
    }
}
