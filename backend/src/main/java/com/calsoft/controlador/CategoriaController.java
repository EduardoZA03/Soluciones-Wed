package com.calsoft.controlador;

import com.calsoft.modelo.Categoria;
import com.calsoft.servicio.CategoriaService;
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
 * CRUD de la tabla maestra Categorías.
 *   GET    /api/categorias        listar
 *   GET    /api/categorias/{id}   ver una
 *   POST   /api/categorias        crear
 *   PUT    /api/categorias/{id}   actualizar
 *   DELETE /api/categorias/{id}   eliminar (si ningún insumo la usa)
 */
@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService servicio;

    public CategoriaController(CategoriaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<Categoria> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    public Categoria ver(@PathVariable Integer id) {
        return servicio.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Categoria crear(@Valid @RequestBody Categoria datos) {
        return servicio.crear(datos);
    }

    @PutMapping("/{id}")
    public Categoria actualizar(@PathVariable Integer id, @Valid @RequestBody Categoria datos) {
        return servicio.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer id) {
        servicio.eliminar(id);
    }
}
