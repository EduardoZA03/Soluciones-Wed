package com.calsoft.modelo;

/** Par nombre / cantidad para los reportes (usuarios por rol, top productos). */
public class Conteo {

    private final String nombre;
    private final int cantidad;

    public Conteo(String nombre, int cantidad) {
        this.nombre = nombre;
        this.cantidad = cantidad;
    }

    public String getNombre() {
        return nombre;
    }

    public int getCantidad() {
        return cantidad;
    }
}
