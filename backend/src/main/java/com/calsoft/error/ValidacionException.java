package com.calsoft.error;

/** Regla de negocio no cumplida: se responde HTTP 400 con el mensaje para el usuario. */
public class ValidacionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}
