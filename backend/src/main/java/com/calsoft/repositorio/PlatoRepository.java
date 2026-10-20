package com.calsoft.repositorio;

import com.calsoft.modelo.Plato;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatoRepository extends JpaRepository<Plato, Integer> {

    List<Plato> findAllByOrderByNombreAsc();

    List<Plato> findByActivoTrueOrderByNombreAsc();

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Integer id);

    boolean existsByNombreIgnoreCase(String nombre);
}
