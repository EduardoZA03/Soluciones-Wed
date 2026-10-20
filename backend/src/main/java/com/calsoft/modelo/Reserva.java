package com.calsoft.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Reserva de mesa. */
@Entity
@Table(name = "reservas")
@Getter
@Setter
@NoArgsConstructor
public class Reserva {

    public static final List<String> ESTADOS = List.of("Pendiente", "Confirmada", "Cancelada");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String cliente;
    private String telefono;
    private LocalDate fecha;
    private LocalTime hora;
    private int personas;
    private int mesa;
    private String estado = "Pendiente";

    @Column(name = "creado_en", updatable = false)
    private OffsetDateTime creadoEn;

    @PrePersist
    void alCrear() {
        creadoEn = OffsetDateTime.now();
    }
}
