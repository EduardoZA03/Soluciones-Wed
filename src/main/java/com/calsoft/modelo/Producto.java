package com.calsoft.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Producto del inventario. */
public class Producto {

    private int id;
    private String producto;
    private String categoria;
    private int stock;
    private String unidad;
    private int stockMinimo;
    private BigDecimal precio = BigDecimal.ZERO;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProducto() {
        return producto;
    }

    public void setProducto(String producto) {
        this.producto = producto;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    /** true si el stock está en o por debajo del mínimo. */
    public boolean isBajoStock() {
        return stock <= stockMinimo;
    }

    /** Precio con dos decimales, p. ej. "18.00". */
    public String getPrecioTexto() {
        return precio.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
