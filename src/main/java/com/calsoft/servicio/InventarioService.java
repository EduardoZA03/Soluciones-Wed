package com.calsoft.servicio;

import com.calsoft.config.Transaccion;
import com.calsoft.dao.AlertaDAO;
import com.calsoft.dao.InventarioDAO;
import com.calsoft.dao.MovimientoDAO;
import com.calsoft.dao.PedidoDAO;
import com.calsoft.dao.PlatoDAO;
import com.calsoft.modelo.Numeros;
import com.calsoft.modelo.Pedido;
import com.calsoft.modelo.Plato;
import com.calsoft.modelo.Producto;
import com.calsoft.modelo.RecetaItem;
import com.calsoft.util.ValidacionException;
import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * Reglas del inventario. Todo cambio de stock pasa por aquí para que:
 *  1) quede registrado en el kardex (movimientos),
 *  2) se generen o cierren las alertas de stock mínimo (HU-04),
 *  3) se haga en una sola transacción.
 */
public class InventarioService {

    private final InventarioDAO inventarioDAO = new InventarioDAO();
    private final MovimientoDAO movimientoDAO = new MovimientoDAO();
    private final AlertaDAO alertaDAO = new AlertaDAO();
    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final PlatoDAO platoDAO = new PlatoDAO();

    /** Crea el insumo; si trae stock, lo registra como entrada inicial en el kardex. */
    public int crearInsumo(Producto p, String usuario) throws SQLException, ValidacionException {
        return Transaccion.ejecutar(cn -> {
            int id = inventarioDAO.crear(cn, p);
            if (p.getStock().signum() > 0) {
                movimientoDAO.registrar(cn, id, "ENTRADA", p.getStock(), p.getStock(), "Stock inicial", null, usuario);
            }
            alertaDAO.evaluarStock(cn, id);
            alertaDAO.evaluarVencimientos(cn);
            return id;
        });
    }

    /** Actualiza los datos del insumo (no el stock) y vuelve a revisar sus alertas. */
    public boolean actualizarInsumo(Producto p) throws SQLException, ValidacionException {
        return Transaccion.ejecutar(cn -> {
            boolean ok = inventarioDAO.actualizar(cn, p);
            if (ok) {
                alertaDAO.evaluarStock(cn, p.getId());
                alertaDAO.evaluarVencimientos(cn);
            }
            return ok;
        });
    }

    /**
     * Movimiento manual de stock (HU-02):
     *  ENTRADA suma, MERMA resta (insumo dañado o vencido), AJUSTE fija el stock al conteo físico.
     *
     * @return el stock resultante
     */
    public BigDecimal registrarMovimiento(int insumoId, String tipo, BigDecimal cantidad, String motivo,
            String usuario) throws SQLException, ValidacionException {
        return Transaccion.ejecutar(cn -> {
            BigDecimal resultante;
            BigDecimal registrada = cantidad;
            switch (tipo) {
                case "ENTRADA":
                    resultante = inventarioDAO.sumarStock(cn, insumoId, cantidad);
                    break;
                case "MERMA":
                    resultante = inventarioDAO.sumarStock(cn, insumoId, cantidad.negate());
                    if (resultante == null) {
                        throw new ValidacionException("La merma es mayor que el stock disponible");
                    }
                    break;
                case "AJUSTE":
                    BigDecimal actual = inventarioDAO.stockParaActualizar(cn, insumoId);
                    if (actual == null) {
                        throw new ValidacionException("Insumo no encontrado");
                    }
                    inventarioDAO.fijarStock(cn, insumoId, cantidad);
                    registrada = cantidad.subtract(actual); // diferencia con lo que decía el sistema
                    resultante = cantidad;
                    break;
                default:
                    throw new ValidacionException("Tipo de movimiento inválido");
            }
            if (resultante == null) {
                throw new ValidacionException("Insumo no encontrado");
            }
            movimientoDAO.registrar(cn, insumoId, tipo, registrada, resultante, motivo, null, usuario);
            alertaDAO.evaluarStock(cn, insumoId);
            return resultante;
        });
    }

    /**
     * Registra una venta del POS y descuenta automáticamente los insumos según la receta (HU-03).
     * Si algún insumo no alcanza, no se guarda nada (ni el pedido ni los descuentos).
     *
     * @return un resumen de lo descontado, para mostrar al usuario
     */
    public String registrarPedido(Pedido p, String usuario) throws SQLException, ValidacionException {
        return Transaccion.ejecutar(cn -> {
            Plato plato = platoDAO.buscar(cn, p.getPlatoId());
            if (plato == null) {
                throw new ValidacionException("El plato seleccionado no existe");
            }
            p.setProducto(plato.getNombre());
            int pedidoId = pedidoDAO.crear(cn, p);

            if (plato.getReceta().isEmpty()) {
                return "El plato no tiene receta: no se descontaron insumos.";
            }
            // Si el pedido ya estaba procesado, no se vuelve a descontar.
            if (!pedidoDAO.marcarDescontado(cn, pedidoId)) {
                return "El pedido ya había sido procesado.";
            }
            StringBuilder resumen = new StringBuilder("Insumos descontados: ");
            String referencia = "PED-" + pedidoId;
            for (RecetaItem item : plato.getReceta()) {
                BigDecimal necesario = item.getCantidad().multiply(BigDecimal.valueOf(p.getCantidad()));
                BigDecimal resultante = inventarioDAO.sumarStock(cn, item.getInsumoId(), necesario.negate());
                if (resultante == null) {
                    throw new ValidacionException("Stock insuficiente de " + item.getInsumo() + ": se necesitan "
                            + Numeros.texto(necesario) + " " + item.getUnidad() + ". Registra una entrada en Inventario.");
                }
                movimientoDAO.registrar(cn, item.getInsumoId(), "SALIDA", necesario, resultante,
                        "Venta POS: " + p.getCantidad() + " × " + plato.getNombre(), referencia, usuario);
                alertaDAO.evaluarStock(cn, item.getInsumoId());
                resumen.append(item.getInsumo()).append(' ').append(Numeros.texto(necesario)).append(' ')
                        .append(item.getUnidad()).append(", ");
            }
            return resumen.substring(0, resumen.length() - 2) + ".";
        });
    }
}
