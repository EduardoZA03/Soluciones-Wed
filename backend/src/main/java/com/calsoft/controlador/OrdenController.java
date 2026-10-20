package com.calsoft.controlador;

import com.calsoft.dto.Dtos.OrdenItemNuevo;
import com.calsoft.dto.Dtos.OrdenItemsCambio;
import com.calsoft.dto.Dtos.OrdenNueva;
import com.calsoft.dto.Dtos.Recepcion;
import com.calsoft.dto.Dtos.ResultadoEnvio;
import com.calsoft.modelo.OrdenCompra;
import com.calsoft.seguridad.UsuarioActual;
import com.calsoft.servicio.OrdenService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
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
 * Órdenes de compra (HU-06 a HU-09).
 *   GET  /api/ordenes?estado=            listar
 *   GET  /api/ordenes/{id}               detalle con ítems y línea de tiempo
 *   GET  /api/ordenes/codigo/{codigo}    buscar por el código del QR
 *   POST /api/ordenes                    nueva (Borrador)
 *   POST /api/ordenes/{id}/items         agregar insumo
 *   PUT  /api/ordenes/{id}/items         editar cantidades y precios
 *   DELETE /api/ordenes/{id}/items/{itemId}
 *   POST /api/ordenes/{id}/aprobar       aprobar (Administrador) y enviar por API
 *   POST /api/ordenes/{id}/reenviar      reintentar envío si la API falló
 *   POST /api/ordenes/{id}/cancelar
 *   POST /api/ordenes/{id}/recepcion     registrar lo recibido
 */
@RestController
@RequestMapping("/api/ordenes")
public class OrdenController {

    private final OrdenService servicio;
    private final String urlSimulador;

    public OrdenController(OrdenService servicio, @Value("${calsoft.proveedor-simulado.url}") String urlSimulador) {
        this.servicio = servicio;
        this.urlSimulador = urlSimulador;
    }

    @GetMapping
    public List<OrdenCompra> listar(@RequestParam(required = false) String estado) {
        return servicio.listar(estado);
    }

    @GetMapping("/{id}")
    public OrdenCompra ver(@PathVariable Integer id) {
        return servicio.buscar(id);
    }

    @GetMapping("/codigo/{codigo}")
    public OrdenCompra porCodigo(@PathVariable String codigo) {
        return servicio.buscarPorCodigo(codigo);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrdenCompra crear(@Valid @RequestBody OrdenNueva r) {
        return servicio.crearVacia(r.proveedorId(), UsuarioActual.nombre());
    }

    @PostMapping("/{id}/items")
    public OrdenCompra agregarItem(@PathVariable Integer id, @Valid @RequestBody OrdenItemNuevo r) {
        return servicio.agregarItem(id, r.insumoId(), r.cantidad());
    }

    @PutMapping("/{id}/items")
    public OrdenCompra actualizarItems(@PathVariable Integer id, @Valid @RequestBody OrdenItemsCambio r) {
        return servicio.actualizarItems(id, r.items());
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public OrdenCompra quitarItem(@PathVariable Integer id, @PathVariable Integer itemId) {
        return servicio.quitarItem(id, itemId);
    }

    @PostMapping("/{id}/aprobar")
    public ResultadoEnvio aprobar(@PathVariable Integer id) {
        return servicio.aprobarYEnviar(id, UsuarioActual.nombre(), UsuarioActual.esAdministrador(), urlSimulador);
    }

    @PostMapping("/{id}/reenviar")
    public ResultadoEnvio reenviar(@PathVariable Integer id) {
        return servicio.enviar(id, UsuarioActual.nombre(), urlSimulador);
    }

    @PostMapping("/{id}/cancelar")
    public OrdenCompra cancelar(@PathVariable Integer id) {
        return servicio.cancelar(id, UsuarioActual.nombre());
    }

    @PostMapping("/{id}/recepcion")
    public OrdenCompra recibir(@PathVariable Integer id, @Valid @RequestBody Recepcion r) {
        return servicio.recibir(id, r.recibido(), r.observacion(), UsuarioActual.nombre());
    }
}
