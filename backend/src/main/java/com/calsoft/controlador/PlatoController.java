package com.calsoft.controlador;

import com.calsoft.dto.Dtos.IngredienteRequest;
import com.calsoft.dto.Dtos.PlatoRequest;
import com.calsoft.error.NoEncontradoException;
import com.calsoft.error.ValidacionException;
import com.calsoft.modelo.Plato;
import com.calsoft.modelo.RecetaItem;
import com.calsoft.repositorio.InsumoRepository;
import com.calsoft.repositorio.PlatoRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
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

/** Carta del restaurante y recetas (HU-03). */
@RestController
@RequestMapping("/api/platos")
public class PlatoController {

    private final PlatoRepository platoRepo;
    private final InsumoRepository insumoRepo;

    public PlatoController(PlatoRepository platoRepo, InsumoRepository insumoRepo) {
        this.platoRepo = platoRepo;
        this.insumoRepo = insumoRepo;
    }

    /** ?activos=true para el POS (solo los que se venden). */
    @GetMapping
    public List<Plato> listar(@RequestParam(defaultValue = "false") boolean activos) {
        return activos ? platoRepo.findByActivoTrueOrderByNombreAsc() : platoRepo.findAllByOrderByNombreAsc();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public Plato crear(@Valid @RequestBody PlatoRequest r) {
        if (platoRepo.existsByNombreIgnoreCase(r.nombre().trim())) {
            throw new ValidacionException("Ya existe un plato con ese nombre");
        }
        Plato p = new Plato();
        copiar(r, p);
        return platoRepo.save(p);
    }

    @PutMapping("/{id}")
    @Transactional
    public Plato actualizar(@PathVariable Integer id, @Valid @RequestBody PlatoRequest r) {
        if (platoRepo.existsByNombreIgnoreCaseAndIdNot(r.nombre().trim(), id)) {
            throw new ValidacionException("Ya existe un plato con ese nombre");
        }
        Plato p = buscar(id);
        copiar(r, p);
        return p;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void eliminar(@PathVariable Integer id) {
        platoRepo.delete(buscar(id));
    }

    /** Agrega el insumo a la receta, o cambia su cantidad si ya estaba. */
    @PostMapping("/{id}/receta")
    @Transactional
    public Plato guardarIngrediente(@PathVariable Integer id, @Valid @RequestBody IngredienteRequest r) {
        Plato p = buscar(id);
        RecetaItem item = p.getReceta().stream()
                .filter(it -> it.getInsumo().getId().equals(r.insumoId())).findFirst().orElse(null);
        if (item == null) {
            item = new RecetaItem();
            item.setPlato(p);
            item.setInsumo(insumoRepo.findById(r.insumoId())
                    .orElseThrow(() -> new ValidacionException("Insumo no encontrado")));
            p.getReceta().add(item);
        }
        item.setCantidad(r.cantidad());
        return platoRepo.save(p);
    }

    @DeleteMapping("/{id}/receta/{recetaId}")
    @Transactional
    public Plato quitarIngrediente(@PathVariable Integer id, @PathVariable Integer recetaId) {
        Plato p = buscar(id);
        p.getReceta().removeIf(it -> it.getId().equals(recetaId));
        return p;
    }

    private Plato buscar(Integer id) {
        return platoRepo.findById(id).orElseThrow(() -> new NoEncontradoException("Plato no encontrado"));
    }

    private static void copiar(PlatoRequest r, Plato p) {
        p.setNombre(r.nombre().trim());
        p.setPrecio(r.precio());
        p.setActivo(r.activo());
    }
}
