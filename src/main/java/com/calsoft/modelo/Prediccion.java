package com.calsoft.modelo;

import java.math.BigDecimal;

/** Resultado del modelo de IA de reposición para un insumo (HU-05). */
public class Prediccion {

    private Producto insumo;
    private int horizonteDias;
    private int diasConDatos;
    private BigDecimal consumoDiario = BigDecimal.ZERO;
    private BigDecimal consumoEstimado = BigDecimal.ZERO;
    private BigDecimal sugerido = BigDecimal.ZERO;
    private Integer diasCobertura;
    private String confianza;
    private String tendencia;

    public Producto getInsumo() {
        return insumo;
    }

    public void setInsumo(Producto insumo) {
        this.insumo = insumo;
    }

    public int getHorizonteDias() {
        return horizonteDias;
    }

    public void setHorizonteDias(int horizonteDias) {
        this.horizonteDias = horizonteDias;
    }

    public int getDiasConDatos() {
        return diasConDatos;
    }

    public void setDiasConDatos(int diasConDatos) {
        this.diasConDatos = diasConDatos;
    }

    public BigDecimal getConsumoDiario() {
        return consumoDiario;
    }

    public void setConsumoDiario(BigDecimal consumoDiario) {
        this.consumoDiario = consumoDiario;
    }

    public BigDecimal getConsumoEstimado() {
        return consumoEstimado;
    }

    public void setConsumoEstimado(BigDecimal consumoEstimado) {
        this.consumoEstimado = consumoEstimado;
    }

    public BigDecimal getSugerido() {
        return sugerido;
    }

    public void setSugerido(BigDecimal sugerido) {
        this.sugerido = sugerido;
    }

    public Integer getDiasCobertura() {
        return diasCobertura;
    }

    public void setDiasCobertura(Integer diasCobertura) {
        this.diasCobertura = diasCobertura;
    }

    public String getConfianza() {
        return confianza;
    }

    public void setConfianza(String confianza) {
        this.confianza = confianza;
    }

    public String getTendencia() {
        return tendencia;
    }

    public void setTendencia(String tendencia) {
        this.tendencia = tendencia;
    }

    public boolean isConviene() {
        return sugerido.signum() > 0;
    }

    public String getConsumoDiarioTexto() {
        return Numeros.texto(consumoDiario);
    }

    public String getConsumoEstimadoTexto() {
        return Numeros.texto(consumoEstimado);
    }

    public String getSugeridoTexto() {
        return Numeros.texto(sugerido);
    }

    /** Costo aproximado de comprar lo sugerido. */
    public BigDecimal getCostoSugerido() {
        return sugerido.multiply(insumo.getPrecio());
    }

    public String getTonoConfianza() {
        switch (confianza) {
            case "Alta":
                return "success";
            case "Media":
                return "warning";
            default:
                return "secondary";
        }
    }
}
