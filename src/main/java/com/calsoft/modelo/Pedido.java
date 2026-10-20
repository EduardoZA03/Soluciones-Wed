package com.calsoft.modelo;

import java.util.List;

/** Pedido de una mesa. */
public class Pedido {

    public static final List<String> ESTADOS = List.of("Pendiente", "En cocina", "Entregado");

    private int id;
    private String cliente;
    private String mesero;
    private String producto;
    private int cantidad;
    private int mesa;
    private String estado;

    /** Estado al que pasa el pedido con el botón "Cambiar" (ciclo Pendiente → En cocina → Entregado). */
    public static String siguienteEstado(String estado) {
        int i = ESTADOS.indexOf(estado);
        return ESTADOS.get((i + 1) % ESTADOS.size());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getMesero() {
        return mesero;
    }

    public void setMesero(String mesero) {
        this.mesero = mesero;
    }

    public String getProducto() {
        return producto;
    }

    public void setProducto(String producto) {
        this.producto = producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public int getMesa() {
        return mesa;
    }

    public void setMesa(int mesa) {
        this.mesa = mesa;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    /** Tono del badge según el estado (usado por las vistas). */
    public String getTono() {
        switch (estado) {
            case "Entregado":
                return "success";
            case "En cocina":
                return "info";
            default:
                return "warning";
        }
    }
}
