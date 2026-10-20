package com.calsoft.modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Plato de la carta, con la receta (insumos que consume cada porción). */
public class Plato {

    private int id;
    private String nombre;
    private BigDecimal precio = BigDecimal.ZERO;
    private boolean activo = true;
    private List<RecetaItem> receta = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public List<RecetaItem> getReceta() {
        return receta;
    }

    public void setReceta(List<RecetaItem> receta) {
        this.receta = receta;
    }

    public String getPrecioTexto() {
        return Numeros.soles(precio);
    }
}
