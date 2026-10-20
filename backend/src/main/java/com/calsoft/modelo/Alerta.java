package com.calsoft.modelo;

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

/** Alerta automática de stock mínimo o vencimiento (las genera AlertaService). */
@Entity
@Table(name = "alertas")
@Getter
@Setter
@NoArgsConstructor
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "insumo_id")
    private Insumo insumo;

    private String tipo;
    private String mensaje;
    private String estado;

    @Column(name = "creado_en", insertable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "atendida_en")
    private OffsetDateTime atendidaEn;

    @Column(name = "atendida_por")
    private String atendidaPor;

    public boolean isPendiente() {
        return "Pendiente".equals(estado);
    }

    /** "Stock mínimo" o "Vencimiento". */
    public String getMotivo() {
        return "STOCK_MINIMO".equals(tipo) ? "Stock mínimo" : "Vencimiento";
    }
}
