package com.calsoft.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/** Insumo del inventario (tabla inventario). */
public class Producto {

    /** Días de anticipación con que se avisa que un insumo va a vencer. */
    public static final int DIAS_AVISO_VENCIMIENTO = 3;

    private int id;
    private String producto;
    private String categoria;
    private BigDecimal stock = BigDecimal.ZERO;
    private String unidad;
    private BigDecimal stockMinimo = BigDecimal.ZERO;
    private BigDecimal precio = BigDecimal.ZERO;
    private LocalDate fechaVencimiento;
    private Integer proveedorId;
    private String proveedorNombre;

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

    public BigDecimal getStock() {
        return stock;
    }

    public void setStock(BigDecimal stock) {
        this.stock = stock;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public BigDecimal getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(BigDecimal stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public Integer getProveedorId() {
        return proveedorId;
    }

    public void setProveedorId(Integer proveedorId) {
        this.proveedorId = proveedorId;
    }

    public String getProveedorNombre() {
        return proveedorNombre;
    }

    public void setProveedorNombre(String proveedorNombre) {
        this.proveedorNombre = proveedorNombre;
    }

    /** true si el stock está en o por debajo del mínimo. */
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

    /** Estado para mostrar y filtrar: Vencido, Por vencer, Bajo stock u OK. */
    public String getEstado() {
        if (isVencido()) {
            return "Vencido";
        }
        if (isPorVencer()) {
            return "Por vencer";
        }
        return isBajoStock() ? "Bajo stock" : "OK";
    }

    /** Color de Bootstrap para el estado. */
    public String getTonoEstado() {
        switch (getEstado()) {
            case "Vencido":
            case "Bajo stock":
                return "danger";
            case "Por vencer":
                return "warning";
            default:
                return "success";
        }
    }

    public String getStockTexto() {
        return Numeros.texto(stock);
    }

    public String getStockMinimoTexto() {
        return Numeros.texto(stockMinimo);
    }

    /** Precio con dos decimales, p. ej. "18.00". */
    public String getPrecioTexto() {
        return precio.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    public String getFechaVencimientoTexto() {
        return fechaVencimiento == null ? "" : fechaVencimiento.toString();
    }
}
