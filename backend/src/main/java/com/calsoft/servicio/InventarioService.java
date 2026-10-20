package com.calsoft.servicio;

import com.calsoft.dto.Dtos.InsumoRequest;
import com.calsoft.error.NoEncontradoException;
import com.calsoft.error.ValidacionException;
import com.calsoft.modelo.Categoria;
import com.calsoft.modelo.Insumo;
import com.calsoft.modelo.Movimiento;
import com.calsoft.modelo.Pedido;
import com.calsoft.modelo.Plato;
import com.calsoft.modelo.RecetaItem;
import com.calsoft.repositorio.CategoriaRepository;
import com.calsoft.repositorio.InsumoRepository;
import com.calsoft.repositorio.MovimientoRepository;
import com.calsoft.repositorio.PedidoRepository;
import com.calsoft.repositorio.PlatoRepository;
import com.calsoft.repositorio.ProveedorRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tabla maestra Insumos y reglas del stock. Todo cambio de stock pasa por aquí para que:
 *  1) quede registrado en el kardex (movimientos),
 *  2) se generen o cierren las alertas de stock mínimo,
 *  3) se haga en una sola transacción.
 */
@Service
public class InventarioService {

    private final InsumoRepository insumoRepo;
    private final CategoriaRepository categoriaRepo;
    private final ProveedorRepository proveedorRepo;
    private final MovimientoRepository movimientoRepo;
    private final PlatoRepository platoRepo;
    private final PedidoRepository pedidoRepo;
    private final AlertaService alertas;

    public InventarioService(InsumoRepository insumoRepo, CategoriaRepository categoriaRepo,
            ProveedorRepository proveedorRepo, MovimientoRepository movimientoRepo, PlatoRepository platoRepo,
            PedidoRepository pedidoRepo, AlertaService alertas) {
        this.insumoRepo = insumoRepo;
        this.categoriaRepo = categoriaRepo;
        this.proveedorRepo = proveedorRepo;
        this.movimientoRepo = movimientoRepo;
        this.platoRepo = platoRepo;
        this.pedidoRepo = pedidoRepo;
        this.alertas = alertas;
    }

    // ---------- CRUD de insumos (HU-02) ----------

    /** Lista con búsqueda por nombre y filtros por categoría y estado. */
    @Transactional
    public List<Insumo> listar(String texto, Integer categoriaId, String estado) {
        alertas.evaluarTodo();
        String q = texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
        return insumoRepo.findAllByOrderByNombreAsc().stream()
                .filter(i -> q.isEmpty() || i.getNombre().toLowerCase(Locale.ROOT).contains(q))
                .filter(i -> categoriaId == null || (i.getCategoria() != null && categoriaId.equals(i.getCategoria().getId())))
                .filter(i -> estado == null || estado.isBlank() || estado.equals(i.getEstado()))
                .toList();
    }

    public Insumo buscar(Integer id) {
        return insumoRepo.findById(id).orElseThrow(() -> new NoEncontradoException("Insumo no encontrado"));
    }

    /** Crea el insumo; si trae stock, lo registra como entrada inicial en el kardex. */
    @Transactional
    public Insumo crear(InsumoRequest r, String usuario) {
        Insumo i = new Insumo();
        copiar(r, i);
        i.setStock(r.stock() == null ? BigDecimal.ZERO : r.stock());
        insumoRepo.save(i);
        if (i.getStock().signum() > 0) {
            movimientoRepo.save(new Movimiento(i, "ENTRADA", i.getStock(), i.getStock(), "Stock inicial", null, usuario));
        }
        insumoRepo.flush();
        alertas.evaluarStock(i.getId());
        alertas.evaluarVencimientos();
        return i;
    }

    /** Actualiza los datos del insumo. El stock NO cambia aquí: solo con movimientos (kardex). */
    @Transactional
    public Insumo actualizar(Integer id, InsumoRequest r) {
        Insumo i = buscar(id);
        copiar(r, i);
        insumoRepo.flush();
        alertas.evaluarStock(id);
        alertas.evaluarVencimientos();
        return i;
    }

    @Transactional
    public void eliminar(Integer id) {
        Insumo i = buscar(id);
        if (insumoRepo.contarEnOrdenes(id) > 0) {
            throw new ValidacionException("No se puede eliminar \"" + i.getNombre()
                    + "\": está en órdenes de compra. Puedes dejar su stock en 0.");
        }
        insumoRepo.delete(i);
    }

    private void copiar(InsumoRequest r, Insumo i) {
        Categoria c = categoriaRepo.findById(r.categoriaId())
                .orElseThrow(() -> new ValidacionException("La categoría elegida no existe"));
        i.setNombre(r.nombre().trim());
        i.setCategoria(c);
        i.setUnidad(r.unidad().trim());
        i.setStockMinimo(r.stockMinimo());
        i.setPrecio(r.precio());
        i.setFechaVencimiento(r.fechaVencimiento());
        i.setProveedor(r.proveedorId() == null ? null : proveedorRepo.findById(r.proveedorId())
                .orElseThrow(() -> new ValidacionException("El proveedor elegido no existe")));
    }

    // ---------- Movimientos y kardex ----------

    public List<Movimiento> kardex(Integer insumoId, int limite) {
        PageRequest pagina = PageRequest.of(0, limite);
        return insumoId == null ? movimientoRepo.ultimos(pagina) : movimientoRepo.ultimosDeInsumo(insumoId, pagina);
    }

    /**
     * Movimiento manual: ENTRADA suma, MERMA resta (dañado o vencido),
     * AJUSTE fija el stock al conteo físico y registra la diferencia.
     *
     * @return el stock resultante
     */
    @Transactional
    public BigDecimal registrarMovimiento(Integer insumoId, String tipo, BigDecimal cantidad, String motivo,
            String usuario) {
        Insumo insumo = buscar(insumoId);
        BigDecimal resultante;
        BigDecimal registrada = cantidad;
        switch (tipo) {
            case "ENTRADA" -> {
                exigirPositivo(cantidad);
                insumoRepo.sumarStock(insumoId, cantidad);
                resultante = insumoRepo.stockActual(insumoId);
            }
            case "MERMA" -> {
                exigirPositivo(cantidad);
                if (insumoRepo.sumarStock(insumoId, cantidad.negate()) == 0) {
                    throw new ValidacionException("La merma es mayor que el stock disponible");
                }
                resultante = insumoRepo.stockActual(insumoId);
            }
            case "AJUSTE" -> {
                // Diferencia entre lo contado y lo que decía el sistema (puede ser negativa).
                registrada = cantidad.subtract(insumoRepo.stockActual(insumoId));
                insumoRepo.sumarStock(insumoId, registrada);
                resultante = insumoRepo.stockActual(insumoId);
            }
            default -> throw new ValidacionException("Tipo de movimiento inválido");
        }
        movimientoRepo.save(new Movimiento(insumo, tipo, registrada, resultante,
                CategoriaService.vacioANull(motivo), null, usuario));
        movimientoRepo.flush();
        alertas.evaluarStock(insumoId);
        return resultante;
    }

    private static void exigirPositivo(BigDecimal cantidad) {
        if (cantidad.signum() <= 0) {
            throw new ValidacionException("La cantidad debe ser mayor que cero");
        }
    }

    // ---------- Venta del POS con descuento por receta (HU-03) ----------

    /**
     * Registra el pedido y descuenta automáticamente los insumos según la receta.
     * Si algún insumo no alcanza, no se guarda nada (ni el pedido ni los descuentos).
     *
     * @return un resumen de lo descontado
     */
    @Transactional
    public String registrarPedido(Pedido p, String usuario) {
        Plato plato = platoRepo.findById(p.getPlatoId())
                .orElseThrow(() -> new ValidacionException("El plato seleccionado no existe"));
        p.setProducto(plato.getNombre());
        // Se marca como procesado en la misma transacción: no se puede descontar dos veces.
        p.setStockDescontado(!plato.getReceta().isEmpty());
        pedidoRepo.saveAndFlush(p);

        if (plato.getReceta().isEmpty()) {
            return "El plato no tiene receta: no se descontaron insumos.";
        }
        StringBuilder resumen = new StringBuilder("Insumos descontados: ");
        for (RecetaItem item : plato.getReceta()) {
            Insumo insumo = item.getInsumo();
            BigDecimal necesario = item.getCantidad().multiply(BigDecimal.valueOf(p.getCantidad()));
            if (insumoRepo.sumarStock(insumo.getId(), necesario.negate()) == 0) {
                throw new ValidacionException("Stock insuficiente de " + insumo.getNombre() + ": se necesitan "
                        + Numeros.texto(necesario) + " " + insumo.getUnidad() + ". Registra una entrada en Insumos.");
            }
            movimientoRepo.save(new Movimiento(insumo, "SALIDA", necesario, insumoRepo.stockActual(insumo.getId()),
                    "Venta POS: " + p.getCantidad() + " × " + plato.getNombre(), "PED-" + p.getId(), usuario));
            resumen.append(insumo.getNombre()).append(' ').append(Numeros.texto(necesario)).append(' ')
                    .append(insumo.getUnidad()).append(", ");
        }
        movimientoRepo.flush();
        plato.getReceta().forEach(item -> alertas.evaluarStock(item.getInsumo().getId()));
        return resumen.substring(0, resumen.length() - 2) + ".";
    }
}
