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
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Un paso en la línea de tiempo de una orden (trazabilidad). */
@Entity
@Table(name = "orden_eventos")
@Getter
@Setter
@NoArgsConstructor
public class OrdenEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "orden_id")
    private OrdenCompra orden;

    private String estado;
    private String detalle;
    private String usuario;

    @Column(name = "creado_en", updatable = false)
    private OffsetDateTime creadoEn;

    public OrdenEvento(OrdenCompra orden, String estado, String detalle, String usuario) {
        this.orden = orden;
        this.estado = estado;
        this.detalle = detalle;
        this.usuario = usuario;
        this.creadoEn = OffsetDateTime.now();
    }
}
