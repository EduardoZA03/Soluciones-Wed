package com.calsoft.repositorio;

import com.calsoft.modelo.Insumo;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/** CRUD de insumos y operaciones atómicas de stock. */
public interface InsumoRepository extends JpaRepository<Insumo, Integer> {

    List<Insumo> findAllByOrderByNombreAsc();

    Optional<Insumo> findFirstByNombre(String nombre);

    @Query("SELECT COUNT(o) FROM OrdenItem o WHERE o.insumo.id = ?1")
    long contarEnOrdenes(Integer insumoId);

    /**
     * Suma (o resta, si delta es negativo) al stock sin dejarlo en negativo.
     * Se hace en una sola sentencia UPDATE para que dos ventas simultáneas no
     * se pisen. Devuelve 0 si no alcanza el stock.
     */
    @Modifying(flushAutomatically = true)
    @Query("UPDATE Insumo i SET i.stock = i.stock + ?2 WHERE i.id = ?1 AND i.stock + ?2 >= 0")
    int sumarStock(Integer id, BigDecimal delta);

    /** Stock actual leído directo de la BD (después de un sumarStock). */
    @Query("SELECT i.stock FROM Insumo i WHERE i.id = ?1")
    BigDecimal stockActual(Integer id);
}
