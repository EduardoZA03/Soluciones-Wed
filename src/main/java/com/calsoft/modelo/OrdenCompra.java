package com.calsoft.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Orden de compra a un proveedor, con sus ítems y su línea de tiempo. */
public class OrdenCompra {

    public static final String BORRADOR = "Borrador";
    public static final String APROBADA = "Aprobada";
    public static final String ENVIADA = "Enviada";
    public static final String CONFIRMADA = "Confirmada";
    public static final String RECHAZADA = "Rechazada";
    public static final String RECIBIDA = "Recibida";
    public static final String RECIBIDA_INCIDENCIA = "Recibida con incidencia";
    public static final String CANCELADA = "Cancelada";

    public static final List<String> ESTADOS = List.of(BORRADOR, APROBADA, ENVIADA, CONFIRMADA,
            RECHAZADA, RECIBIDA, RECIBIDA_INCIDENCIA, CANCELADA);

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private int id;
    private String codigo;
    private int proveedorId;
    private String proveedorNombre;
    private String estado;
    private Integer alertaId;
    private LocalDate fechaEntrega;
    private String confirmacionProveedor;
    private String creadoPor;
    private LocalDateTime creadoEn;
    private BigDecimal total = BigDecimal.ZERO;
    private int cantidadItems;
    private List<OrdenItem> items = new ArrayList<>();
    private List<OrdenEvento> eventos = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public int getProveedorId() {
        return proveedorId;
    }

    public void setProveedorId(int proveedorId) {
        this.proveedorId = proveedorId;
    }

    public String getProveedorNombre() {
        return proveedorNombre;
    }

    public void setProveedorNombre(String proveedorNombre) {
        this.proveedorNombre = proveedorNombre;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getAlertaId() {
        return alertaId;
    }

    public void setAlertaId(Integer alertaId) {
        this.alertaId = alertaId;
    }

    public LocalDate getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(LocalDate fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public String getConfirmacionProveedor() {
        return confirmacionProveedor;
    }

    public void setConfirmacionProveedor(String confirmacionProveedor) {
        this.confirmacionProveedor = confirmacionProveedor;
    }

    public String getCreadoPor() {
        return creadoPor;
    }

    public void setCreadoPor(String creadoPor) {
        this.creadoPor = creadoPor;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public int getCantidadItems() {
        return cantidadItems;
    }

    public void setCantidadItems(int cantidadItems) {
        this.cantidadItems = cantidadItems;
    }

    public List<OrdenItem> getItems() {
        return items;
    }

    public void setItems(List<OrdenItem> items) {
        this.items = items;
    }

    public List<OrdenEvento> getEventos() {
        return eventos;
    }

    public void setEventos(List<OrdenEvento> eventos) {
        this.eventos = eventos;
    }

    public boolean isEditable() {
        return BORRADOR.equals(estado);
    }

    /** Aprobada pero sin respuesta del proveedor (p. ej. la API falló): se puede reintentar. */
    public boolean isReenviable() {
        return APROBADA.equals(estado);
    }

    public boolean isRecibible() {
        return CONFIRMADA.equals(estado);
    }

    public boolean isCancelable() {
        return BORRADOR.equals(estado) || APROBADA.equals(estado);
    }

    public String getTotalTexto() {
        return Numeros.soles(total);
    }

    public String getCreadoEnTexto() {
        return creadoEn == null ? "" : creadoEn.format(FECHA);
    }

    public String getFechaEntregaTexto() {
        return fechaEntrega == null ? "" : fechaEntrega.toString();
    }

    /** Color de Bootstrap del estado. */
    public String getTono() {
        return tono(estado);
    }

    public static String tono(String estado) {
        switch (estado) {
            case CONFIRMADA:
            case RECIBIDA:
                return "success";
            case APROBADA:
            case ENVIADA:
                return "info";
            case RECHAZADA:
            case RECIBIDA_INCIDENCIA:
                return "danger";
            case BORRADOR:
                return "warning";
            default:
                return "secondary";
        }
    }
}
