package com.calsoft.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/** Estadísticas agregadas para el Inicio y los Reportes. */
public class Resumen {

    private int pedidosTotal;
    private int pedidosPendientes;
    private int pedidosEnCocina;
    private int pedidosEntregados;

    private int reservasTotal;
    private int reservasPendientes;
    private int reservasConfirmadas;
    private int reservasCanceladas;

    private int productosTotal;
    private int bajoStock;
    private BigDecimal valorInventario = BigDecimal.ZERO;

    private List<Conteo> usuariosPorRol = new ArrayList<>();
    private List<Conteo> topProductos = new ArrayList<>();

    public int getPedidosTotal() {
        return pedidosTotal;
    }

    public void setPedidosTotal(int pedidosTotal) {
        this.pedidosTotal = pedidosTotal;
    }

    public int getPedidosPendientes() {
        return pedidosPendientes;
    }

    public void setPedidosPendientes(int pedidosPendientes) {
        this.pedidosPendientes = pedidosPendientes;
    }

    public int getPedidosEnCocina() {
        return pedidosEnCocina;
    }

    public void setPedidosEnCocina(int pedidosEnCocina) {
        this.pedidosEnCocina = pedidosEnCocina;
    }

    public int getPedidosEntregados() {
        return pedidosEntregados;
    }

    public void setPedidosEntregados(int pedidosEntregados) {
        this.pedidosEntregados = pedidosEntregados;
    }

    /** Pedidos pendientes + en cocina. */
    public int getPedidosActivos() {
        return pedidosPendientes + pedidosEnCocina;
    }

    public int getReservasTotal() {
        return reservasTotal;
    }

    public void setReservasTotal(int reservasTotal) {
        this.reservasTotal = reservasTotal;
    }

    public int getReservasPendientes() {
        return reservasPendientes;
    }

    public void setReservasPendientes(int reservasPendientes) {
        this.reservasPendientes = reservasPendientes;
    }

    public int getReservasConfirmadas() {
        return reservasConfirmadas;
    }

    public void setReservasConfirmadas(int reservasConfirmadas) {
        this.reservasConfirmadas = reservasConfirmadas;
    }

    public int getReservasCanceladas() {
        return reservasCanceladas;
    }

    public void setReservasCanceladas(int reservasCanceladas) {
        this.reservasCanceladas = reservasCanceladas;
    }

    public int getProductosTotal() {
        return productosTotal;
    }

    public void setProductosTotal(int productosTotal) {
        this.productosTotal = productosTotal;
    }

    public int getBajoStock() {
        return bajoStock;
    }

    public void setBajoStock(int bajoStock) {
        this.bajoStock = bajoStock;
    }

    public BigDecimal getValorInventario() {
        return valorInventario;
    }

    public void setValorInventario(BigDecimal valorInventario) {
        this.valorInventario = valorInventario;
    }

    /** Valor del inventario con dos decimales. */
    public String getValorInventarioTexto() {
        return valorInventario.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    /** Valor del inventario sin decimales (tarjeta del Inicio). */
    public String getValorInventarioEntero() {
        return valorInventario.setScale(0, RoundingMode.HALF_UP).toPlainString();
    }

    public List<Conteo> getUsuariosPorRol() {
        return usuariosPorRol;
    }

    public void setUsuariosPorRol(List<Conteo> usuariosPorRol) {
        this.usuariosPorRol = usuariosPorRol;
    }

    public List<Conteo> getTopProductos() {
        return topProductos;
    }

    public void setTopProductos(List<Conteo> topProductos) {
        this.topProductos = topProductos;
    }

    /** Cantidad máxima del top (mínimo 1), para calcular el ancho de las barras. */
    public int getMaxTopProductos() {
        int max = 1;
        for (Conteo c : topProductos) {
            max = Math.max(max, c.getCantidad());
        }
        return max;
    }
}
