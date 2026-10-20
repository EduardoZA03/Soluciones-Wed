package com.calsoft.modelo;

import java.math.BigDecimal;

/** Ítem de una orden de compra. */
public class OrdenItem {

    private int id;
    private int insumoId;
    private String insumo;
    private String unidad;
    private BigDecimal cantidad;
    private BigDecimal precioUnitario = BigDecimal.ZERO;
    private Boolean disponible;
    private BigDecimal cantidadRecibida;

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

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public Boolean getDisponible() {
        return disponible;
    }

    public void setDisponible(Boolean disponible) {
        this.disponible = disponible;
    }

    public BigDecimal getCantidadRecibida() {
        return cantidadRecibida;
    }

    public void setCantidadRecibida(BigDecimal cantidadRecibida) {
        this.cantidadRecibida = cantidadRecibida;
    }

    public BigDecimal getSubtotal() {
        return cantidad.multiply(precioUnitario);
    }

    public String getSubtotalTexto() {
        return Numeros.soles(getSubtotal());
    }

    public String getCantidadTexto() {
        return Numeros.texto(cantidad);
    }

    public String getPrecioTexto() {
        return Numeros.soles(precioUnitario);
    }

    public String getCantidadRecibidaTexto() {
        return Numeros.texto(cantidadRecibida);
    }
}
