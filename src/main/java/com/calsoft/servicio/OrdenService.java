package com.calsoft.servicio;

import com.calsoft.config.Conexion;
import com.calsoft.config.Transaccion;
import com.calsoft.dao.AlertaDAO;
import com.calsoft.dao.InventarioDAO;
import com.calsoft.dao.MovimientoDAO;
import com.calsoft.dao.OrdenCompraDAO;
import com.calsoft.dao.ProveedorDAO;
import com.calsoft.integracion.ProveedorApiCliente;
import com.calsoft.integracion.RespuestaProveedor;
import com.calsoft.modelo.Alerta;
import com.calsoft.modelo.Numeros;
import com.calsoft.modelo.OrdenCompra;
import com.calsoft.modelo.OrdenItem;
import com.calsoft.modelo.Producto;
import com.calsoft.modelo.Proveedor;
import com.calsoft.util.ValidacionException;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Ciclo de vida de las órdenes de compra:
 *
 *   Borrador → Aprobada → Enviada → Confirmada → Recibida
 *                 ↑  (error de API: vuelve a Aprobada para reintentar)
 *                       ↘ Rechazada        ↘ Recibida con incidencia
 *
 * Cada paso queda en la línea de tiempo de la orden (HU-09).
 */
public class OrdenService {

    /** Resultado del envío al proveedor: si respondió y qué dijo. */
    public record ResultadoEnvio(boolean exito, String mensaje) {
    }

    private static final int HORIZONTE_SUGERENCIA = 7;

    private final OrdenCompraDAO ordenDAO = new OrdenCompraDAO();
    private final InventarioDAO inventarioDAO = new InventarioDAO();
    private final ProveedorDAO proveedorDAO = new ProveedorDAO();
    private final AlertaDAO alertaDAO = new AlertaDAO();
    private final MovimientoDAO movimientoDAO = new MovimientoDAO();
    private final PrediccionService prediccion = new PrediccionService();
    private final ProveedorApiCliente apiCliente = new ProveedorApiCliente();

    public OrdenCompra buscar(int id) throws SQLException {
        try (Connection cn = Conexion.obtener()) {
            return ordenDAO.buscar(cn, id);
        }
    }

    public OrdenCompra buscarPorCodigo(String codigo) throws SQLException {
        try (Connection cn = Conexion.obtener()) {
            return ordenDAO.buscarPorCodigo(cn, codigo);
        }
    }

    /** Orden vacía para un proveedor elegido a mano. */
    public int crearVacia(int proveedorId, String usuario) throws SQLException, ValidacionException {
        return Transaccion.ejecutar(cn -> {
            if (proveedorDAO.buscar(cn, proveedorId) == null) {
                throw new ValidacionException("Proveedor no encontrado");
            }
            return ordenDAO.crear(cn, proveedorId, null, usuario, "Orden creada manualmente");
        });
    }

    /**
     * Genera una orden en Borrador desde una alerta (HU-06). La cantidad la
     * propone el módulo de IA; el administrador puede editarla antes de aprobar.
     */
    public int crearDesdeAlerta(int alertaId, String usuario) throws SQLException, ValidacionException {
        return Transaccion.ejecutar(cn -> {
            Alerta alerta = alertaDAO.buscar(cn, alertaId);
            if (alerta == null || !alerta.isPendiente()) {
                throw new ValidacionException("La alerta no existe o ya fue atendida");
            }
            Producto insumo = inventarioDAO.buscar(cn, alerta.getInsumoId());
            int proveedorId = proveedorDe(cn, insumo);
            BigDecimal cantidad = prediccion.sugeridoPara(cn, insumo, HORIZONTE_SUGERENCIA);
            if (cantidad.signum() <= 0) {
                cantidad = insumo.getStockMinimo().max(BigDecimal.ONE);
            }
            int id = ordenDAO.crear(cn, proveedorId, alertaId, usuario,
                    "Creada desde la alerta de " + alerta.getMotivo().toLowerCase() + ": " + insumo.getProducto()
                    + ". Cantidad sugerida por IA: " + Numeros.texto(cantidad) + " " + insumo.getUnidad());
            ordenDAO.agregarItem(cn, id, insumo.getId(), cantidad, insumo.getPrecio());
            return id;
        });
    }

    /**
     * Crea órdenes en Borrador con las cantidades aceptadas/ajustadas en la
     * pantalla de predicción (HU-05). Agrupa los insumos por proveedor.
     */
    public List<Integer> crearDesdeSugerencias(Map<Integer, BigDecimal> cantidades, String usuario)
            throws SQLException, ValidacionException {
        return Transaccion.ejecutar(cn -> {
            Map<Integer, List<Producto>> porProveedor = new LinkedHashMap<>();
            for (Integer insumoId : cantidades.keySet()) {
                Producto insumo = inventarioDAO.buscar(cn, insumoId);
                if (insumo != null) {
                    porProveedor.computeIfAbsent(proveedorDe(cn, insumo), k -> new ArrayList<>()).add(insumo);
                }
            }
            List<Integer> ids = new ArrayList<>();
            for (Map.Entry<Integer, List<Producto>> e : porProveedor.entrySet()) {
                int id = ordenDAO.crear(cn, e.getKey(), null, usuario, "Creada desde la predicción de reposición (IA)");
                for (Producto insumo : e.getValue()) {
                    ordenDAO.agregarItem(cn, id, insumo.getId(), cantidades.get(insumo.getId()), insumo.getPrecio());
                }
                ids.add(id);
            }
            return ids;
        });
    }

    private int proveedorDe(Connection cn, Producto insumo) throws SQLException, ValidacionException {
        if (insumo.getProveedorId() != null) {
            return insumo.getProveedorId();
        }
        Proveedor primero = proveedorDAO.primero(cn);
        if (primero == null) {
            throw new ValidacionException("Registra al menos un proveedor antes de crear órdenes de compra");
        }
        return primero.getId();
    }

    public void agregarItem(int ordenId, int insumoId, BigDecimal cantidad) throws SQLException, ValidacionException {
        Transaccion.ejecutar(cn -> {
            exigirEditable(cn, ordenId);
            Producto insumo = inventarioDAO.buscar(cn, insumoId);
            if (insumo == null) {
                throw new ValidacionException("Insumo no encontrado");
            }
            ordenDAO.agregarItem(cn, ordenId, insumoId, cantidad, insumo.getPrecio());
            return null;
        });
    }

    /** Guarda las cantidades y precios editados en el Borrador. Mapa: itemId → {cantidad, precio}. */
    public void actualizarItems(int ordenId, Map<Integer, BigDecimal[]> items) throws SQLException, ValidacionException {
        Transaccion.ejecutar(cn -> {
            exigirEditable(cn, ordenId);
            for (Map.Entry<Integer, BigDecimal[]> e : items.entrySet()) {
                ordenDAO.actualizarItem(cn, ordenId, e.getKey(), e.getValue()[0], e.getValue()[1]);
            }
            return null;
        });
    }

    public void quitarItem(int ordenId, int itemId) throws SQLException, ValidacionException {
        Transaccion.ejecutar(cn -> {
            exigirEditable(cn, ordenId);
            ordenDAO.quitarItem(cn, ordenId, itemId);
            return null;
        });
    }

    private OrdenCompra exigirEditable(Connection cn, int ordenId) throws SQLException, ValidacionException {
        OrdenCompra o = ordenDAO.buscar(cn, ordenId);
        if (o == null) {
            throw new ValidacionException("Orden no encontrada");
        }
        if (!o.isEditable()) {
            throw new ValidacionException("Solo se puede editar una orden en Borrador");
        }
        return o;
    }

    public void cancelar(int ordenId, String usuario) throws SQLException, ValidacionException {
        Transaccion.ejecutar(cn -> {
            OrdenCompra o = ordenDAO.buscar(cn, ordenId);
            if (o == null || !o.isCancelable()) {
                throw new ValidacionException("La orden no se puede cancelar en su estado actual");
            }
            ordenDAO.cambiarEstado(cn, ordenId, OrdenCompra.CANCELADA, "Orden cancelada", usuario);
            return null;
        });
    }

    /** Aprueba el Borrador (solo administrador) y lo envía de inmediato al proveedor por API. */
    public ResultadoEnvio aprobarYEnviar(int ordenId, String usuario, String urlSimulador)
            throws SQLException, ValidacionException {
        Transaccion.ejecutar(cn -> {
            OrdenCompra o = exigirEditable(cn, ordenId);
            if (o.getItems().isEmpty()) {
                throw new ValidacionException("Agrega al menos un insumo antes de aprobar");
            }
            ordenDAO.cambiarEstado(cn, ordenId, OrdenCompra.APROBADA,
                    "Aprobada · " + o.getItems().size() + " ítem(s) · total S/ " + o.getTotalTexto(), usuario);
            return null;
        });
        return enviar(ordenId, usuario, urlSimulador);
    }

    /**
     * Envía la orden aprobada al proveedor por su API (HU-07) y guarda la respuesta.
     * Si la API falla, la orden vuelve a "Aprobada" y el error queda en la línea de tiempo.
     */
    public ResultadoEnvio enviar(int ordenId, String usuario, String urlSimulador)
            throws SQLException, ValidacionException {
        OrdenCompra orden;
        Proveedor proveedor;
        try (Connection cn = Conexion.obtener()) {
            orden = ordenDAO.buscar(cn, ordenId);
            if (orden == null || !orden.isReenviable()) {
                throw new ValidacionException("Solo se envían órdenes aprobadas");
            }
            proveedor = proveedorDAO.buscar(cn, orden.getProveedorId());
        }
        String url = proveedor.isSimulado() ? urlSimulador : proveedor.getApiUrl();

        Transaccion.ejecutar(cn -> {
            ordenDAO.cambiarEstado(cn, ordenId, OrdenCompra.ENVIADA,
                    "POST " + url + (proveedor.isSimulado() ? " (proveedor simulado)" : ""), usuario);
            return null;
        });

        RespuestaProveedor respuesta;
        try {
            respuesta = apiCliente.enviarOrden(url, proveedor.getApiKey(), orden);
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            String detalle = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            Transaccion.ejecutar(cn -> {
                ordenDAO.registrarEvento(cn, ordenId, "Error API", detalle, usuario);
                ordenDAO.cambiarEstado(cn, ordenId, OrdenCompra.APROBADA, "Pendiente de reenvío", usuario);
                return null;
            });
            return new ResultadoEnvio(false, "El proveedor no respondió: " + detalle);
        }

        final RespuestaProveedor r = respuesta;
        LocalDate entrega = null;
        if (r.fechaEntrega != null) {
            try {
                entrega = LocalDate.parse(r.fechaEntrega);
            } catch (DateTimeParseException e) {
                entrega = null;
            }
        }
        LocalDate fechaEntrega = entrega;
        Transaccion.ejecutar(cn -> {
            ordenDAO.guardarConfirmacion(cn, ordenId, r.confirmacion, fechaEntrega);
            if (r.items != null) {
                for (RespuestaProveedor.Item it : r.items) {
                    ordenDAO.marcarDisponibilidad(cn, it.itemId, it.disponible);
                }
            }
            if (r.aceptada) {
                ordenDAO.cambiarEstado(cn, ordenId, OrdenCompra.CONFIRMADA, "Confirmación " + r.confirmacion
                        + (fechaEntrega == null ? "" : " · entrega " + fechaEntrega) + " · " + r.mensaje, usuario);
            } else {
                ordenDAO.cambiarEstado(cn, ordenId, OrdenCompra.RECHAZADA, r.mensaje, usuario);
            }
            return null;
        });
        return new ResultadoEnvio(r.aceptada, r.aceptada
                ? "Proveedor confirmó la orden (" + r.confirmacion + "), entrega " + fechaEntrega
                : "El proveedor rechazó la orden: " + r.mensaje);
    }

    /**
     * Recepción de la mercadería (HU-08): suma al stock lo recibido, lo registra
     * en el kardex y compara con lo confirmado. Si no coincide, la orden queda
     * "Recibida con incidencia" para reclamar al proveedor.
     *
     * @param recibido itemId → cantidad recibida
     * @return el estado final de la orden
     */
    public String recibir(int ordenId, Map<Integer, BigDecimal> recibido, String observacion, String usuario)
            throws SQLException, ValidacionException {
        return Transaccion.ejecutar(cn -> {
            OrdenCompra o = ordenDAO.buscar(cn, ordenId);
            if (o == null || !o.isRecibible()) {
                throw new ValidacionException("Solo se reciben órdenes confirmadas por el proveedor");
            }
            List<String> incidencias = new ArrayList<>();
            for (OrdenItem it : o.getItems()) {
                BigDecimal cantidad = recibido.getOrDefault(it.getId(), BigDecimal.ZERO);
                ordenDAO.registrarRecibido(cn, it.getId(), cantidad);
                if (cantidad.signum() > 0) {
                    BigDecimal resultante = inventarioDAO.sumarStock(cn, it.getInsumoId(), cantidad);
                    movimientoDAO.registrar(cn, it.getInsumoId(), "ENTRADA", cantidad, resultante,
                            "Recepción de orden de compra", o.getCodigo(), usuario);
                    alertaDAO.evaluarStock(cn, it.getInsumoId());
                }
                BigDecimal esperado = Boolean.FALSE.equals(it.getDisponible()) ? BigDecimal.ZERO : it.getCantidad();
                if (cantidad.compareTo(esperado) != 0) {
                    incidencias.add(it.getInsumo() + ": esperado " + Numeros.texto(esperado)
                            + ", recibido " + Numeros.texto(cantidad));
                }
            }
            String estado = incidencias.isEmpty() ? OrdenCompra.RECIBIDA : OrdenCompra.RECIBIDA_INCIDENCIA;
            String detalle = incidencias.isEmpty() ? "Mercadería recibida conforme" : String.join("; ", incidencias);
            if (observacion != null && !observacion.isBlank()) {
                detalle += " · Obs.: " + observacion;
            }
            ordenDAO.cambiarEstado(cn, ordenId, estado, detalle, usuario);
            if (o.getAlertaId() != null) {
                alertaDAO.atender(cn, o.getAlertaId(), usuario);
            }
            return estado;
        });
    }
}
