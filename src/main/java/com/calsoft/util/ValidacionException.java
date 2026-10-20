package com.calsoft.util;

/** Error de validación de un formulario; su mensaje se muestra al usuario. */
public class ValidacionException extends Exception {

    private static final long serialVersionUID = 1L;

    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}
