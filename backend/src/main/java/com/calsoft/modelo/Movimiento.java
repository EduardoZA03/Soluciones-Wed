package com.calsoft.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Movimiento de stock (kardex): ENTRADA, SALIDA, MERMA o AJUSTE. */
@Entity
@Table(name = "movimientos")
@Getter
@Setter
@NoArgsConstructor
public class Movimiento {

    /** Tipos que el usuario registra a mano (las salidas las genera el POS). */
    public static final List<String> TIPOS_MANUALES = List.of("ENTRADA", "MERMA", "AJUSTE");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "insumo_id")
    private Insumo insumo;

    private String tipo;
    private BigDecimal cantidad;

    @Column(name = "stock_resultante")
    private BigDecimal stockResultante;

    private String motivo;
    private String referencia;
    private String usuario;

    @Column(name = "creado_en", updatable = false)
    private OffsetDateTime creadoEn;

    @PrePersist
    void alCrear() {
        if (creadoEn == null) {
            creadoEn = OffsetDateTime.now();
        }
    }

    public Movimiento(Insumo insumo, String tipo, BigDecimal cantidad, BigDecimal stockResultante,
            String motivo, String referencia, String usuario) {
        this.insumo = insumo;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.stockResultante = stockResultante;
        this.motivo = motivo;
        this.referencia = referencia;
        this.usuario = usuario;
    }
}
