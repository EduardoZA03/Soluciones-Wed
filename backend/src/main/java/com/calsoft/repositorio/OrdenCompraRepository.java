package com.calsoft.repositorio;

import com.calsoft.modelo.OrdenCompra;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, Integer> {

    List<OrdenCompra> findAllByOrderByIdDesc();

    List<OrdenCompra> findByEstadoOrderByIdDesc(String estado);

    Optional<OrdenCompra> findByCodigo(String codigo);

    /** La orden activa generada desde una alerta (para enlazarla en la pantalla de alertas). */
    Optional<OrdenCompra> findFirstByAlertaIdAndEstadoNotOrderByIdDesc(Integer alertaId, String estado);

    long countByEstadoIn(List<String> estados);
}
