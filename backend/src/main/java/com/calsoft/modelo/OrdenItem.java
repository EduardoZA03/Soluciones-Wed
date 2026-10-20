package com.calsoft.modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Ítem de una orden de compra. */
@Entity
@Table(name = "orden_items")
@Getter
@Setter
@NoArgsConstructor
public class OrdenItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "orden_id")
    private OrdenCompra orden;

    @ManyToOne
    @JoinColumn(name = "insumo_id")
    private Insumo insumo;

    private BigDecimal cantidad;

    @Column(name = "precio_unitario")
    private BigDecimal precioUnitario = BigDecimal.ZERO;

    /** Respuesta del proveedor: true/false, o null si aún no respondió. */
    private Boolean disponible;

    @Column(name = "cantidad_recibida")
    private BigDecimal cantidadRecibida;

    public BigDecimal getSubtotal() {
        return cantidad.multiply(precioUnitario);
    }
}
