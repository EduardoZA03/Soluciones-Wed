package com.calsoft.repositorio;

import com.calsoft.modelo.Proveedor;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** CRUD de proveedores. */
public interface ProveedorRepository extends JpaRepository<Proveedor, Integer> {

    List<Proveedor> findAllByOrderByNombreAsc();

    Optional<Proveedor> findFirstByOrderByIdAsc();

    boolean existsByApiKey(String apiKey);

    @Query("SELECT COUNT(o) FROM OrdenCompra o WHERE o.proveedor.id = ?1")
    long contarOrdenes(Integer proveedorId);
}
