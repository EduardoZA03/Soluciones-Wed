package com.calsoft.controlador;

import com.calsoft.modelo.Proveedor;
import com.calsoft.servicio.ProveedorService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * CRUD de la tabla maestra Proveedores.
 *   GET    /api/proveedores        listar
 *   GET    /api/proveedores/{id}   ver uno
 *   POST   /api/proveedores        crear
 *   PUT    /api/proveedores/{id}   actualizar (API key vacía = conservar la actual)
 *   DELETE /api/proveedores/{id}   eliminar (si no tiene órdenes)
 */
@RestController
@RequestMapping("/api/proveedores")
public class ProveedorController {

    private final ProveedorService servicio;

    public ProveedorController(ProveedorService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<Proveedor> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    public Proveedor ver(@PathVariable Integer id) {
        return servicio.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Proveedor crear(@Valid @RequestBody Proveedor datos) {
        return servicio.crear(datos);
    }

    @PutMapping("/{id}")
    public Proveedor actualizar(@PathVariable Integer id, @Valid @RequestBody Proveedor datos) {
        return servicio.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer id) {
        servicio.eliminar(id);
    }
}
