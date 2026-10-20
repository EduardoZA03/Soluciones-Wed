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
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

/**
 * Tabla maestra: insumo del inventario (tabla "inventario").
 * DynamicUpdate: al guardar solo se actualizan las columnas cambiadas, así
 * editar el nombre nunca pisa un stock que otra operación acaba de cambiar.
 */
@Entity
@DynamicUpdate
@Table(name = "inventario")
@Getter
@Setter
@NoArgsConstructor
public class Insumo {

    /** Días de anticipación con que se avisa que un insumo va a vencer. */
    public static final int DIAS_AVISO_VENCIMIENTO = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Nombre del insumo (la columna se llama "producto" por compatibilidad). */
    @Column(name = "producto")
    private String nombre;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    private BigDecimal stock = BigDecimal.ZERO;

    private String unidad = "unidad";

    @Column(name = "stock_minimo")
    private BigDecimal stockMinimo = BigDecimal.ZERO;

    private BigDecimal precio = BigDecimal.ZERO;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @ManyToOne
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;

    @Column(name = "creado_en", updatable = false)
    private OffsetDateTime creadoEn;

    @PrePersist
    void alCrear() {
        creadoEn = OffsetDateTime.now();
    }

    public boolean isBajoStock() {
        return stock.compareTo(stockMinimo) <= 0;
    }

    public boolean isVencido() {
        return fechaVencimiento != null && fechaVencimiento.isBefore(LocalDate.now());
    }

    public boolean isPorVencer() {
        return fechaVencimiento != null && !isVencido()
                && !fechaVencimiento.isAfter(LocalDate.now().plusDays(DIAS_AVISO_VENCIMIENTO));
    }

    /** Vencido, Por vencer, Bajo stock u OK. */
    public String getEstado() {
        if (isVencido()) {
            return "Vencido";
        }
        if (isPorVencer()) {
            return "Por vencer";
        }
        return isBajoStock() ? "Bajo stock" : "OK";
    }
}
