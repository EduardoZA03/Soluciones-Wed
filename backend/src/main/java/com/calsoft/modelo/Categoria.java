package com.calsoft.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Tabla maestra: categoría de insumo (Carnes, Verduras, Bebidas...). */
@Entity
@Table(name = "categorias")
@Getter
@Setter
@NoArgsConstructor
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre admite hasta 50 caracteres")
    private String nombre;

    @Size(max = 200, message = "La descripción admite hasta 200 caracteres")
    private String descripcion;

    @Column(name = "creado_en", updatable = false)
    private OffsetDateTime creadoEn;

    @PrePersist
    void alCrear() {
        creadoEn = OffsetDateTime.now();
    }

    public Categoria(String nombre) {
        this.nombre = nombre;
    }
}
