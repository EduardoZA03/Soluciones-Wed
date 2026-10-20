package com.calsoft.error;

/** El registro pedido no existe: se responde HTTP 404. */
public class NoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
