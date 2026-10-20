package com.calsoft.repositorio;

import com.calsoft.modelo.Categoria;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** CRUD de categorías. Spring Data genera la implementación. */
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    List<Categoria> findAllByOrderByNombreAsc();

    Optional<Categoria> findByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Integer id);

    boolean existsByNombreIgnoreCase(String nombre);

    /** Cuántos insumos usan cada categoría (para no borrar una en uso). */
    @Query("SELECT COUNT(i) FROM Insumo i WHERE i.categoria.id = ?1")
    long contarInsumos(Integer categoriaId);
}
