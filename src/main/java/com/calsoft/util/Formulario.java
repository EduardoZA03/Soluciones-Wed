package com.calsoft.util;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

/** Lectura y validación de los parámetros que llegan de los formularios. */
public final class Formulario {

    private final HttpServletRequest req;

    public Formulario(HttpServletRequest req) {
        this.req = req;
    }

    /** Texto sin espacios al inicio/fin; nunca null. */
    public String texto(String nombre) {
        String v = req.getParameter(nombre);
        return v == null ? "" : v.trim();
    }

    /** Texto o el valor por defecto si llega vacío. */
    public String texto(String nombre, String porDefecto) {
        String v = texto(nombre);
        return v.isEmpty() ? porDefecto : v;
    }

    /** Entero mayor o igual a {@code minimo}; si no, lanza el mensaje indicado. */
    public int entero(String nombre, int minimo, String mensaje) throws ValidacionException {
        try {
            int v = Integer.parseInt(texto(nombre));
            if (v < minimo) {
                throw new ValidacionException(mensaje);
            }
            return v;
        } catch (NumberFormatException e) {
            throw new ValidacionException(mensaje);
        }
    }

    /** Entero >= 0; si el campo viene vacío devuelve 0. */
    public int enteroOpcional(String nombre, String mensaje) throws ValidacionException {
        return texto(nombre).isEmpty() ? 0 : entero(nombre, 0, mensaje);
    }

    /** Decimal >= 0; si el campo viene vacío devuelve 0. */
    public BigDecimal decimal(String nombre, String mensaje) throws ValidacionException {
        String v = texto(nombre);
        if (v.isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            BigDecimal d = new BigDecimal(v);
            if (d.signum() < 0) {
                throw new ValidacionException(mensaje);
            }
            return d;
        } catch (NumberFormatException e) {
            throw new ValidacionException(mensaje);
        }
    }

    public LocalDate fecha(String nombre, String mensaje) throws ValidacionException {
        try {
            return LocalDate.parse(texto(nombre));
        } catch (DateTimeParseException e) {
            throw new ValidacionException(mensaje);
        }
    }

    public LocalTime hora(String nombre, String mensaje) throws ValidacionException {
        try {
            return LocalTime.parse(texto(nombre));
        } catch (DateTimeParseException e) {
            throw new ValidacionException(mensaje);
        }
    }

    /** Id del registro sobre el que se actúa (campo oculto "id"). */
    public int id() throws ValidacionException {
        return entero("id", 1, "Registro inválido");
    }
}
