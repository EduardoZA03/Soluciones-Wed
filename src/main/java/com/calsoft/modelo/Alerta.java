package com.calsoft.modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Alerta automática de stock mínimo o de vencimiento de un insumo. */
public class Alerta {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private int id;
    private int insumoId;
    private String insumo;
    private String tipo;
    private String mensaje;
    private String estado;
    private LocalDateTime creadoEn;
    private LocalDateTime atendidaEn;
    private String atendidaPor;
    private Integer ordenId;
    private String ordenCodigo;

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

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    public LocalDateTime getAtendidaEn() {
        return atendidaEn;
    }

    public void setAtendidaEn(LocalDateTime atendidaEn) {
        this.atendidaEn = atendidaEn;
    }

    public String getAtendidaPor() {
        return atendidaPor;
    }

    public void setAtendidaPor(String atendidaPor) {
        this.atendidaPor = atendidaPor;
    }

    public Integer getOrdenId() {
        return ordenId;
    }

    public void setOrdenId(Integer ordenId) {
        this.ordenId = ordenId;
    }

    public String getOrdenCodigo() {
        return ordenCodigo;
    }

    public void setOrdenCodigo(String ordenCodigo) {
        this.ordenCodigo = ordenCodigo;
    }

    public boolean isPendiente() {
        return "Pendiente".equals(estado);
    }

    /** Motivo legible: "Stock mínimo" o "Vencimiento". */
    public String getMotivo() {
        return "STOCK_MINIMO".equals(tipo) ? "Stock mínimo" : "Vencimiento";
    }

    public String getTono() {
        return "STOCK_MINIMO".equals(tipo) ? "danger" : "warning";
    }

    public String getCreadoEnTexto() {
        return creadoEn == null ? "" : creadoEn.format(FECHA);
    }

    public String getAtendidaEnTexto() {
        return atendidaEn == null ? "" : atendidaEn.format(FECHA);
    }
}
