package com.calsoft.servicio;

import com.calsoft.dto.Dtos.OrdenItemCambio;
import com.calsoft.dto.Dtos.ResultadoEnvio;
import com.calsoft.error.NoEncontradoException;
import com.calsoft.error.ValidacionException;
import com.calsoft.integracion.ProveedorApiCliente;
import com.calsoft.integracion.ProveedorApiCliente.ErrorProveedor;
import com.calsoft.integracion.RespuestaProveedor;
import com.calsoft.modelo.Alerta;
import com.calsoft.modelo.Insumo;
import com.calsoft.modelo.Movimiento;
import com.calsoft.modelo.OrdenCompra;
import com.calsoft.modelo.OrdenItem;
import com.calsoft.modelo.Proveedor;
import com.calsoft.repositorio.AlertaRepository;
import com.calsoft.repositorio.InsumoRepository;
import com.calsoft.repositorio.MovimientoRepository;
import com.calsoft.repositorio.OrdenCompraRepository;
import com.calsoft.repositorio.ProveedorRepository;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Ciclo de vida de las órdenes de compra (HU-06 a HU-09):
 *
 *   Borrador → Aprobada → Enviada → Confirmada → Recibida
 *                 ↑  (error de API: vuelve a Aprobada para reintentar)
 *                       ↘ Rechazada        ↘ Recibida con incidencia
 *
 * Cada paso queda en la línea de tiempo de la orden.
 */
@Service
public class OrdenService {

    private static final int HORIZONTE_SUGERENCIA = 7;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String LETRAS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final OrdenCompraRepository ordenRepo;
    private final InsumoRepository insumoRepo;
    private final ProveedorRepository proveedorRepo;
    private final AlertaRepository alertaRepo;
    private final MovimientoRepository movimientoRepo;
    private final AlertaService alertas;
    private final PrediccionService prediccion;
    private final ProveedorApiCliente apiCliente;
    private final TransactionTemplate tx;

    public OrdenService(OrdenCompraRepository ordenRepo, InsumoRepository insumoRepo, ProveedorRepository proveedorRepo,
            AlertaRepository alertaRepo, MovimientoRepository movimientoRepo, AlertaService alertas,
            PrediccionService prediccion, ProveedorApiCliente apiCliente, TransactionTemplate tx) {
        this.ordenRepo = ordenRepo;
        this.insumoRepo = insumoRepo;
        this.proveedorRepo = proveedorRepo;
        this.alertaRepo = alertaRepo;
        this.movimientoRepo = movimientoRepo;
        this.alertas = alertas;
        this.prediccion = prediccion;
        this.apiCliente = apiCliente;
        this.tx = tx;
    }

    public List<OrdenCompra> listar(String estado) {
        return estado == null || estado.isBlank()
                ? ordenRepo.findAllByOrderByIdDesc()
                : ordenRepo.findByEstadoOrderByIdDesc(estado);
    }

    public OrdenCompra buscar(Integer id) {
        return ordenRepo.findById(id).orElseThrow(() -> new NoEncontradoException("Orden no encontrada"));
    }

    public OrdenCompra buscarPorCodigo(String codigo) {
        return ordenRepo.findByCodigo(codigo.trim().toUpperCase())
                .orElseThrow(() -> new NoEncontradoException("No existe una orden con el código " + codigo));
    }

    // ---------- Creación (HU-05, HU-06) ----------

    @Transactional
    public OrdenCompra crearVacia(Integer proveedorId, String usuario) {
        Proveedor p = proveedorRepo.findById(proveedorId)
                .orElseThrow(() -> new ValidacionException("Proveedor no encontrado"));
        return ordenRepo.save(nueva(p, null, usuario, "Orden creada manualmente"));
    }

    /** Orden en Borrador desde una alerta; la cantidad la propone la IA y se puede editar. */
    @Transactional
    public OrdenCompra crearDesdeAlerta(Integer alertaId, String usuario) {
        Alerta alerta = alertaRepo.findById(alertaId)
                .orElseThrow(() -> new ValidacionException("La alerta no existe"));
        if (!alerta.isPendiente()) {
            throw new ValidacionException("La alerta ya fue atendida");
        }
        Insumo insumo = alerta.getInsumo();
        BigDecimal cantidad = prediccion.sugeridoPara(insumo, HORIZONTE_SUGERENCIA);
        if (cantidad.signum() <= 0) {
            cantidad = insumo.getStockMinimo().max(BigDecimal.ONE);
        }
        OrdenCompra o = nueva(proveedorDe(insumo), alertaId, usuario,
                "Creada desde la alerta de " + alerta.getMotivo().toLowerCase() + ": " + insumo.getNombre()
                + ". Cantidad sugerida por IA: " + Numeros.texto(cantidad) + " " + insumo.getUnidad());
        agregar(o, insumo, cantidad);
        return ordenRepo.save(o);
    }

    /** Una orden en Borrador por proveedor con las cantidades aceptadas/ajustadas de la predicción. */
    @Transactional
    public List<Integer> crearDesdeSugerencias(Map<Integer, BigDecimal> cantidades, String usuario) {
        Map<Proveedor, OrdenCompra> porProveedor = new LinkedHashMap<>();
        for (Map.Entry<Integer, BigDecimal> e : cantidades.entrySet()) {
            Insumo insumo = insumoRepo.findById(e.getKey())
                    .orElseThrow(() -> new ValidacionException("Insumo no encontrado"));
            OrdenCompra o = porProveedor.computeIfAbsent(proveedorDe(insumo),
                    p -> nueva(p, null, usuario, "Creada desde la predicción de reposición (IA)"));
            agregar(o, insumo, e.getValue());
        }
        List<Integer> ids = new ArrayList<>();
        for (OrdenCompra o : porProveedor.values()) {
            ids.add(ordenRepo.save(o).getId());
        }
        return ids;
    }

    private OrdenCompra nueva(Proveedor p, Integer alertaId, String usuario, String detalle) {
        OrdenCompra o = new OrdenCompra();
        o.setCodigo(nuevoCodigo());
        o.setProveedor(p);
        o.setAlertaId(alertaId);
        o.setCreadoPor(usuario);
        o.registrarEvento(OrdenCompra.BORRADOR, detalle, usuario);
        return o;
    }

    private Proveedor proveedorDe(Insumo insumo) {
        if (insumo.getProveedor() != null) {
            return insumo.getProveedor();
        }
        return proveedorRepo.findFirstByOrderByIdAsc().orElseThrow(() ->
                new ValidacionException("Registra al menos un proveedor antes de crear órdenes de compra"));
    }

    /** Código único que va en el QR, p. ej. OC-20261019-7KQ2XD. */
    private static String nuevoCodigo() {
        StringBuilder sb = new StringBuilder("OC-")
                .append(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)).append('-');
        for (int i = 0; i < 6; i++) {
            sb.append(LETRAS.charAt(RANDOM.nextInt(LETRAS.length())));
        }
        return sb.toString();
    }

    /** Agrega el insumo a la orden; si ya estaba, suma la cantidad. */
    private static void agregar(OrdenCompra o, Insumo insumo, BigDecimal cantidad) {
        for (OrdenItem it : o.getItems()) {
            if (it.getInsumo().getId().equals(insumo.getId())) {
                it.setCantidad(it.getCantidad().add(cantidad));
                return;
            }
        }
        OrdenItem it = new OrdenItem();
        it.setOrden(o);
        it.setInsumo(insumo);
        it.setCantidad(cantidad);
        it.setPrecioUnitario(insumo.getPrecio());
        o.getItems().add(it);
    }

    // ---------- Edición del Borrador ----------

    @Transactional
    public OrdenCompra agregarItem(Integer ordenId, Integer insumoId, BigDecimal cantidad) {
        OrdenCompra o = editable(ordenId);
        agregar(o, insumoRepo.findById(insumoId).orElseThrow(() -> new ValidacionException("Insumo no encontrado")),
                cantidad);
        return o;
    }

    @Transactional
    public OrdenCompra actualizarItems(Integer ordenId, List<OrdenItemCambio> cambios) {
        OrdenCompra o = editable(ordenId);
        for (OrdenItemCambio c : cambios) {
            o.getItems().stream().filter(it -> it.getId().equals(c.id())).findFirst().ifPresent(it -> {
                it.setCantidad(c.cantidad());
                it.setPrecioUnitario(c.precioUnitario());
            });
        }
        return o;
    }

    @Transactional
    public OrdenCompra quitarItem(Integer ordenId, Integer itemId) {
        OrdenCompra o = editable(ordenId);
        o.getItems().removeIf(it -> it.getId().equals(itemId));
        return o;
    }

    private OrdenCompra editable(Integer ordenId) {
        OrdenCompra o = buscar(ordenId);
        if (!o.isEditable()) {
            throw new ValidacionException("Solo se puede editar una orden en Borrador");
        }
        return o;
    }

    @Transactional
    public OrdenCompra cancelar(Integer ordenId, String usuario) {
        OrdenCompra o = buscar(ordenId);
        if (!o.isCancelable()) {
            throw new ValidacionException("La orden no se puede cancelar en su estado actual");
        }
        o.cambiarEstado(OrdenCompra.CANCELADA, "Orden cancelada", usuario);
        return o;
    }

    // ---------- Aprobación y envío por API (HU-06, HU-07) ----------

    /** Aprueba el Borrador (solo administrador) y lo envía de inmediato al proveedor. */
    public ResultadoEnvio aprobarYEnviar(Integer ordenId, String usuario, boolean esAdministrador, String urlSimulador) {
        if (!esAdministrador) {
            throw new ValidacionException("Solo el Administrador puede aprobar órdenes de compra");
        }
        tx.executeWithoutResult(estado -> {
            OrdenCompra o = editable(ordenId);
            if (o.getItems().isEmpty()) {
                throw new ValidacionException("Agrega al menos un insumo antes de aprobar");
            }
            o.cambiarEstado(OrdenCompra.APROBADA, "Aprobada · " + o.getItems().size() + " ítem(s) · total S/ "
                    + Numeros.soles(o.getTotal()), usuario);
        });
        return enviar(ordenId, usuario, urlSimulador);
    }

    /**
     * Envía la orden aprobada a la API del proveedor y guarda su respuesta.
     * Cada paso se guarda en su propia transacción, para que el envío y un
     * posible error queden en la línea de tiempo aunque la API falle.
     */
    public ResultadoEnvio enviar(Integer ordenId, String usuario, String urlSimulador) {
        OrdenCompra orden = tx.execute(estado -> {
            OrdenCompra o = buscar(ordenId);
            if (!o.isReenviable()) {
                throw new ValidacionException("Solo se envían órdenes aprobadas");
            }
            String url = o.getProveedor().isSimulado() ? urlSimulador : o.getProveedor().getApiUrl();
            o.cambiarEstado(OrdenCompra.ENVIADA, "POST " + url
                    + (o.getProveedor().isSimulado() ? " (proveedor simulado)" : ""), usuario);
            return o;
        });
        Proveedor proveedor = orden.getProveedor();
        String url = proveedor.isSimulado() ? urlSimulador : proveedor.getApiUrl();

        RespuestaProveedor r;
        try {
            r = apiCliente.enviarOrden(url, proveedor.getApiKey(), orden);
        } catch (ErrorProveedor e) {
            tx.executeWithoutResult(estado -> {
                OrdenCompra o = buscar(ordenId);
                o.registrarEvento("Error API", e.getMessage(), usuario);
                o.cambiarEstado(OrdenCompra.APROBADA, "Pendiente de reenvío", usuario);
            });
            return new ResultadoEnvio(false, e.getMessage());
        }

        LocalDate entrega = fecha(r.fechaEntrega());
        tx.executeWithoutResult(estado -> {
            OrdenCompra o = buscar(ordenId);
            o.setConfirmacionProveedor(r.confirmacion());
            o.setFechaEntrega(entrega);
            if (r.items() != null) {
                for (RespuestaProveedor.Item ri : r.items()) {
                    o.getItems().stream().filter(it -> it.getId().equals(ri.itemId())).findFirst()
                            .ifPresent(it -> it.setDisponible(ri.disponible()));
                }
            }
            if (r.aceptada()) {
                o.cambiarEstado(OrdenCompra.CONFIRMADA, "Confirmación " + r.confirmacion()
                        + (entrega == null ? "" : " · entrega " + entrega) + " · " + r.mensaje(), usuario);
            } else {
                o.cambiarEstado(OrdenCompra.RECHAZADA, r.mensaje(), usuario);
            }
        });
        return new ResultadoEnvio(r.aceptada(), r.aceptada()
                ? "Proveedor confirmó la orden (" + r.confirmacion() + "), entrega " + entrega
                : "El proveedor rechazó la orden: " + r.mensaje());
    }

    private static LocalDate fecha(String texto) {
        try {
            return texto == null ? null : LocalDate.parse(texto);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    // ---------- Recepción (HU-08) ----------

    /**
     * Suma al stock lo recibido, lo registra en el kardex y lo compara con lo
     * confirmado: si no coincide, la orden queda "Recibida con incidencia".
     */
    @Transactional
    public OrdenCompra recibir(Integer ordenId, Map<Integer, BigDecimal> recibido, String observacion, String usuario) {
        OrdenCompra o = buscar(ordenId);
        if (!o.isRecibible()) {
            throw new ValidacionException("Solo se reciben órdenes confirmadas por el proveedor");
        }
        List<String> incidencias = new ArrayList<>();
        for (OrdenItem it : o.getItems()) {
            BigDecimal cantidad = recibido.getOrDefault(it.getId(), BigDecimal.ZERO);
            if (cantidad.signum() < 0) {
                throw new ValidacionException("Las cantidades recibidas no pueden ser negativas");
            }
            it.setCantidadRecibida(cantidad);
            Insumo insumo = it.getInsumo();
            if (cantidad.signum() > 0) {
                insumoRepo.sumarStock(insumo.getId(), cantidad);
                movimientoRepo.save(new Movimiento(insumo, "ENTRADA", cantidad, insumoRepo.stockActual(insumo.getId()),
                        "Recepción de orden de compra", o.getCodigo(), usuario));
            }
            BigDecimal esperado = Boolean.FALSE.equals(it.getDisponible()) ? BigDecimal.ZERO : it.getCantidad();
            if (cantidad.compareTo(esperado) != 0) {
                incidencias.add(insumo.getNombre() + ": esperado " + Numeros.texto(esperado)
                        + ", recibido " + Numeros.texto(cantidad));
            }
        }
        String detalle = incidencias.isEmpty() ? "Mercadería recibida conforme" : String.join("; ", incidencias);
        if (observacion != null && !observacion.isBlank()) {
            detalle += " · Obs.: " + observacion.trim();
        }
        o.cambiarEstado(incidencias.isEmpty() ? OrdenCompra.RECIBIDA : OrdenCompra.RECIBIDA_INCIDENCIA, detalle, usuario);
        if (o.getAlertaId() != null) {
            alertaRepo.findById(o.getAlertaId()).filter(Alerta::isPendiente).ifPresent(a -> {
                a.setEstado("Atendida");
                a.setAtendidaEn(OffsetDateTime.now());
                a.setAtendidaPor(usuario);
            });
        }
        ordenRepo.flush();
        o.getItems().forEach(it -> alertas.evaluarStock(it.getInsumo().getId()));
        return o;
    }
}
