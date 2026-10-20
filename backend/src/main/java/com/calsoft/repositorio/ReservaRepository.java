package com.calsoft.repositorio;

import com.calsoft.modelo.Reserva;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaRepository extends JpaRepository<Reserva, Integer> {

    List<Reserva> findAllByOrderByFechaAscHoraAsc();

    /** Reservas de hoy en adelante que no están canceladas. */
    List<Reserva> findTop5ByEstadoNotAndFechaGreaterThanEqualOrderByFechaAscHoraAsc(String estado, LocalDate desde);
}
