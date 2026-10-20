package com.calsoft.modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Un paso en la línea de tiempo de una orden de compra (HU-09). */
public class OrdenEvento {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String estado;
    private String detalle;
    private String usuario;
    private LocalDateTime creadoEn;

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    public String getFechaTexto() {
        return creadoEn == null ? "" : creadoEn.format(FECHA);
    }

    /** "Error API" se muestra en rojo; el resto, según el estado de la orden. */
    public String getTono() {
        return estado.startsWith("Error") ? "danger" : OrdenCompra.tono(estado);
    }
}
