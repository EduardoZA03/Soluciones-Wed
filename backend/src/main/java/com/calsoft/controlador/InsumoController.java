package com.calsoft.controlador;

import com.calsoft.dto.Dtos.InsumoRequest;
import com.calsoft.dto.Dtos.Mensaje;
import com.calsoft.dto.Dtos.MovimientoRequest;
import com.calsoft.error.ValidacionException;
import com.calsoft.modelo.Insumo;
import com.calsoft.modelo.Movimiento;
import com.calsoft.seguridad.UsuarioActual;
import com.calsoft.servicio.InventarioService;
import com.calsoft.servicio.Numeros;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * CRUD de la tabla maestra Insumos (HU-02) y sus movimientos de stock.
 *   GET    /api/insumos?q=&categoriaId=&estado=   listar con búsqueda y filtros
 *   GET    /api/insumos/{id}                       ver uno
 *   POST   /api/insumos                            crear (con stock inicial)
 *   PUT    /api/insumos/{id}                       actualizar datos (no el stock)
 *   DELETE /api/insumos/{id}                       eliminar
 *   POST   /api/insumos/{id}/movimientos           entrada, merma o ajuste
 *   GET    /api/movimientos?insumoId=              kardex
 */
@RestController
@RequestMapping("/api")
public class InsumoController {

    private final InventarioService servicio;

    public InsumoController(InventarioService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/insumos")
    public List<Insumo> listar(@RequestParam(required = false) String q,
                               @RequestParam(required = false) Integer categoriaId,
                               @RequestParam(required = false) String estado) {
        return servicio.listar(q, categoriaId, estado);
    }

    @GetMapping("/insumos/{id}")
    public Insumo ver(@PathVariable Integer id) {
        return servicio.buscar(id);
    }

    @PostMapping("/insumos")
    @ResponseStatus(HttpStatus.CREATED)
    public Insumo crear(@Valid @RequestBody InsumoRequest r) {
        return servicio.crear(r, UsuarioActual.nombre());
    }

    @PutMapping("/insumos/{id}")
    public Insumo actualizar(@PathVariable Integer id, @Valid @RequestBody InsumoRequest r) {
        return servicio.actualizar(id, r);
    }

    @DeleteMapping("/insumos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer id) {
        servicio.eliminar(id);
    }

    @PostMapping("/insumos/{id}/movimientos")
    public Mensaje movimiento(@PathVariable Integer id, @Valid @RequestBody MovimientoRequest r) {
        if (!Movimiento.TIPOS_MANUALES.contains(r.tipo())) {
            throw new ValidacionException("Tipo de movimiento inválido");
        }
        BigDecimal stock = servicio.registrarMovimiento(id, r.tipo(), r.cantidad(), r.motivo(), UsuarioActual.nombre());
        return new Mensaje("Movimiento registrado. Stock actual: " + Numeros.texto(stock));
    }

    @GetMapping("/movimientos")
    public List<Movimiento> kardex(@RequestParam(required = false) Integer insumoId) {
        return servicio.kardex(insumoId, 300);
    }
}
