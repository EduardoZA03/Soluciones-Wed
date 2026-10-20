package com.calsoft.repositorio;

import com.calsoft.modelo.Alerta;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AlertaRepository extends JpaRepository<Alerta, Integer> {

    @Query("SELECT a FROM Alerta a WHERE a.estado = ?1 ORDER BY a.creadoEn DESC")
    List<Alerta> porEstado(String estado, Pageable pagina);

    long countByEstado(String estado);
}
