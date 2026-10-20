package com.calsoft.repositorio;

import com.calsoft.modelo.Pedido;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    List<Pedido> findAllByOrderByIdAsc();

    List<Pedido> findTop5ByOrderByIdDesc();

    List<Pedido> findByEstadoInOrderByIdAsc(List<String> estados);
}
