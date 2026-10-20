package com.calsoft.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Pedido / venta del POS. */
@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
public class Pedido {

    public static final List<String> ESTADOS = List.of("Pendiente", "En cocina", "Entregado");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String cliente;
    private String mesero;
    /** Nombre del plato al momento de la venta. */
    private String producto;
    private int cantidad;
    private int mesa;
    private String estado = "Pendiente";

    @Column(name = "plato_id")
    private Integer platoId;

    /** true si ya descontó sus insumos (evita procesar la venta dos veces). */
    @Column(name = "stock_descontado")
    private boolean stockDescontado;

    @Column(name = "creado_en", updatable = false)
    private OffsetDateTime creadoEn;

    @PrePersist
    void alCrear() {
        creadoEn = OffsetDateTime.now();
    }

    /** Ciclo Pendiente → En cocina → Entregado. */
    public static String siguienteEstado(String estado) {
        int i = ESTADOS.indexOf(estado);
        return ESTADOS.get((i + 1) % ESTADOS.size());
    }
}
