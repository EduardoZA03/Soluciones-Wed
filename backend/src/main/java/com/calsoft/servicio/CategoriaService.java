package com.calsoft.servicio;

import com.calsoft.error.NoEncontradoException;
import com.calsoft.error.ValidacionException;
import com.calsoft.modelo.Categoria;
import com.calsoft.repositorio.CategoriaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** CRUD de la tabla maestra Categorías. */
@Service
public class CategoriaService {

    private final CategoriaRepository repo;

    public CategoriaService(CategoriaRepository repo) {
        this.repo = repo;
    }

    public List<Categoria> listar() {
        return repo.findAllByOrderByNombreAsc();
    }

    public Categoria buscar(Integer id) {
        return repo.findById(id).orElseThrow(() -> new NoEncontradoException("Categoría no encontrada"));
    }

    @Transactional
    public Categoria crear(Categoria datos) {
        String nombre = datos.getNombre().trim();
        if (repo.existsByNombreIgnoreCase(nombre)) {
            throw new ValidacionException("Ya existe una categoría llamada " + nombre);
        }
        Categoria c = new Categoria(nombre);
        c.setDescripcion(vacioANull(datos.getDescripcion()));
        return repo.save(c);
    }

    @Transactional
    public Categoria actualizar(Integer id, Categoria datos) {
        Categoria c = buscar(id);
        String nombre = datos.getNombre().trim();
        if (repo.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new ValidacionException("Ya existe una categoría llamada " + nombre);
        }
        c.setNombre(nombre);
        c.setDescripcion(vacioANull(datos.getDescripcion()));
        return c;
    }

    @Transactional
    public void eliminar(Integer id) {
        Categoria c = buscar(id);
        long usos = repo.contarInsumos(id);
        if (usos > 0) {
            throw new ValidacionException("No se puede eliminar \"" + c.getNombre() + "\": la usan " + usos
                    + " insumo(s). Cámbialos de categoría primero.");
        }
        repo.delete(c);
    }

    static String vacioANull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
