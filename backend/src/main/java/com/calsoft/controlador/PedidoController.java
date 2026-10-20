package com.calsoft.controlador;

import com.calsoft.dto.Dtos.EstadoRequest;
import com.calsoft.dto.Dtos.Mensaje;
import com.calsoft.dto.Dtos.PedidoRequest;
import com.calsoft.error.NoEncontradoException;
import com.calsoft.error.ValidacionException;
import com.calsoft.modelo.Pedido;
import com.calsoft.repositorio.PedidoRepository;
import com.calsoft.seguridad.UsuarioActual;
import com.calsoft.servicio.InventarioService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * POS: pedidos (ventas). Al registrar un pedido se descuentan sus insumos según la receta (HU-03).
 * La vista de Cocina usa GET ?activos=true y PATCH .../estado.
 */
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoRepository pedidoRepo;
    private final InventarioService inventario;

    public PedidoController(PedidoRepository pedidoRepo, InventarioService inventario) {
        this.pedidoRepo = pedidoRepo;
        this.inventario = inventario;
    }

    @GetMapping
    public List<Pedido> listar(@RequestParam(defaultValue = "false") boolean activos) {
        return activos
                ? pedidoRepo.findByEstadoInOrderByIdAsc(List.of("Pendiente", "En cocina"))
                : pedidoRepo.findAllByOrderByIdAsc();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mensaje crear(@Valid @RequestBody PedidoRequest r) {
        String estado = r.estado() == null || r.estado().isBlank() ? "Pendiente" : r.estado();
        if (!Pedido.ESTADOS.contains(estado)) {
            throw new ValidacionException("Estado inválido");
        }
        Pedido p = new Pedido();
        p.setCliente(r.cliente().trim());
        p.setMesero(r.mesero().trim());
        p.setPlatoId(r.platoId());
        p.setCantidad(r.cantidad());
        p.setMesa(r.mesa());
        p.setEstado(estado);
        return new Mensaje("Pedido registrado. " + inventario.registrarPedido(p, UsuarioActual.nombre()));
    }

    /** Sin estado en el cuerpo pasa al siguiente del ciclo; con estado, lo fija. */
    @PatchMapping("/{id}/estado")
    @Transactional
    public Pedido cambiarEstado(@PathVariable Integer id, @RequestBody(required = false) EstadoRequest r) {
        Pedido p = pedidoRepo.findById(id).orElseThrow(() -> new NoEncontradoException("Pedido no encontrado"));
        String nuevo = r == null ? Pedido.siguienteEstado(p.getEstado()) : r.estado();
        if (!Pedido.ESTADOS.contains(nuevo)) {
            throw new ValidacionException("Estado inválido");
        }
        p.setEstado(nuevo);
        return p;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer id) {
        if (!pedidoRepo.existsById(id)) {
            throw new NoEncontradoException("Pedido no encontrado");
        }
        pedidoRepo.deleteById(id);
    }
}
