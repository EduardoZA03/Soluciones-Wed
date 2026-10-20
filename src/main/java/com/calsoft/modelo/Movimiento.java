package com.calsoft.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Movimiento de stock (kardex): entrada, salida, merma o ajuste. */
public class Movimiento {

    /** Tipos que el usuario puede registrar a mano (las salidas las genera el POS). */
    public static final List<String> TIPOS_MANUALES = List.of("ENTRADA", "MERMA", "AJUSTE");

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private int id;
    private int insumoId;
    private String insumo;
    private String unidad;
    private String tipo;
    private BigDecimal cantidad;
    private BigDecimal stockResultante;
    private String motivo;
    private String referencia;
    private String usuario;
    private LocalDateTime creadoEn;

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

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getStockResultante() {
        return stockResultante;
    }

    public void setStockResultante(BigDecimal stockResultante) {
        this.stockResultante = stockResultante;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
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

    public String getCantidadTexto() {
        return Numeros.texto(cantidad);
    }

    public String getStockResultanteTexto() {
        return Numeros.texto(stockResultante);
    }

    /** Color de Bootstrap según el tipo. */
    public String getTono() {
        switch (tipo) {
            case "ENTRADA":
                return "success";
            case "SALIDA":
                return "primary";
            case "MERMA":
                return "danger";
            default:
                return "secondary";
        }
    }
}
