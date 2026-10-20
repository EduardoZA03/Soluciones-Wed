package com.calsoft.modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

/** Un insumo de la receta de un plato y la cantidad que usa por porción. */
@Entity
@Table(name = "recetas")
@Getter
@Setter
@NoArgsConstructor
public class RecetaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "plato_id")
    private Plato plato;

    @ManyToOne
    @JoinColumn(name = "insumo_id")
    private Insumo insumo;

    private BigDecimal cantidad;
}
