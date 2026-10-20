package com.calsoft.modelo;

import java.math.BigDecimal;

/** Un insumo de la receta de un plato y la cantidad que usa por porción. */
public class RecetaItem {

    private int id;
    private int insumoId;
    private String insumo;
    private String unidad;
    private BigDecimal cantidad;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getInsumoId() {
        return insumoId;
    }

    public void setInsumoId(int insumoId) {
        this.insumoId = insumoId;
    }

    public String getInsumo() {
        return insumo;
    }

    public void setInsumo(String insumo) {
        this.insumo = insumo;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public String getCantidadTexto() {
        return Numeros.texto(cantidad);
    }
}
