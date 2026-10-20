package com.calsoft.modelo;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Orden de compra a un proveedor:
 *   Borrador → Aprobada → Enviada → Confirmada → Recibida
 * Cada paso queda en la lista de eventos (trazabilidad).
 */
@Entity
@Table(name = "ordenes_compra")
@Getter
@Setter
@NoArgsConstructor
public class OrdenCompra {

    public static final String BORRADOR = "Borrador";
    public static final String APROBADA = "Aprobada";
    public static final String ENVIADA = "Enviada";
    public static final String CONFIRMADA = "Confirmada";
    public static final String RECHAZADA = "Rechazada";
    public static final String RECIBIDA = "Recibida";
    public static final String RECIBIDA_INCIDENCIA = "Recibida con incidencia";
    public static final String CANCELADA = "Cancelada";

    public static final List<String> ESTADOS = List.of(BORRADOR, APROBADA, ENVIADA, CONFIRMADA,
            RECHAZADA, RECIBIDA, RECIBIDA_INCIDENCIA, CANCELADA);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Código único que va en el QR, p. ej. OC-20261019-7KQ2XD. */
    private String codigo;

    @ManyToOne
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;

    private String estado = BORRADOR;

    @Column(name = "alerta_id")
    private Integer alertaId;

    @Column(name = "fecha_entrega")
    private LocalDate fechaEntrega;

    @Column(name = "confirmacion_proveedor")
    private String confirmacionProveedor;

    @Column(name = "creado_por")
    private String creadoPor;

    @Column(name = "creado_en", updatable = false)
    private OffsetDateTime creadoEn;

    // SUBSELECT: carga ítems y eventos con una consulta aparte cada uno (evita
    // el error de Hibernate al traer dos listas a la vez con JOIN).
    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @OrderBy("id")
    private List<OrdenItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @OrderBy("creadoEn, id")
    private List<OrdenEvento> eventos = new ArrayList<>();

    @PrePersist
    void alCrear() {
        creadoEn = OffsetDateTime.now();
    }

    public BigDecimal getTotal() {
        return items.stream().map(OrdenItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isEditable() {
        return BORRADOR.equals(estado);
    }

    /** Aprobada sin respuesta del proveedor (p. ej. falló la API): se puede reintentar. */
    public boolean isReenviable() {
        return APROBADA.equals(estado);
    }

    public boolean isRecibible() {
        return CONFIRMADA.equals(estado);
    }

    public boolean isCancelable() {
        return BORRADOR.equals(estado) || APROBADA.equals(estado);
    }

    /** Agrega un paso a la línea de tiempo. */
    public void registrarEvento(String estadoEvento, String detalle, String usuario) {
        eventos.add(new OrdenEvento(this, estadoEvento, detalle, usuario));
    }

    /** Cambia el estado y lo deja en la línea de tiempo. */
    public void cambiarEstado(String nuevo, String detalle, String usuario) {
        this.estado = nuevo;
        registrarEvento(nuevo, detalle, usuario);
    }
}
