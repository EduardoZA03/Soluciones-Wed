package com.calsoft.repositorio;

import com.calsoft.modelo.Movimiento;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** Kardex. */
public interface MovimientoRepository extends JpaRepository<Movimiento, Integer> {

    @Query("SELECT m FROM Movimiento m ORDER BY m.creadoEn DESC, m.id DESC")
    List<Movimiento> ultimos(Pageable pagina);

    @Query("SELECT m FROM Movimiento m WHERE m.insumo.id = ?1 ORDER BY m.creadoEn DESC, m.id DESC")
    List<Movimiento> ultimosDeInsumo(Integer insumoId, Pageable pagina);
}
