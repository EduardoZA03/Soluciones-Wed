package com.calsoft.modelo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Reserva de mesa. */
public class Reserva {

    public static final List<String> ESTADOS = List.of("Pendiente", "Confirmada", "Cancelada");

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private int id;
    private String cliente;
    private String telefono;
    private LocalDate fecha;
    private LocalTime hora;
    private int personas;
    private int mesa;
    private String estado;

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

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public int getPersonas() {
        return personas;
    }

    public void setPersonas(int personas) {
        this.personas = personas;
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

    public String getFechaTexto() {
        return fecha == null ? "" : fecha.toString();
    }

    public String getHoraTexto() {
        return hora == null ? "" : hora.format(HORA);
    }

    /** Color de Bootstrap del badge según el estado (usado por las vistas). */
    public String getTono() {
        switch (estado) {
            case "Confirmada":
                return "success";
            case "Cancelada":
                return "secondary";
            default:
                return "warning";
        }
    }
}
