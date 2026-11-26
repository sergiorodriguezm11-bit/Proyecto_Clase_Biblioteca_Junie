package com.biblioteca.exception;

/**
 * Excepción lanzada cuando los datos de entrada no son válidos (HTTP 400).
 */
public class ValidacionException extends RuntimeException {

    private final String campo;

    public ValidacionException(String message) {
        super(message);
        this.campo = null;
    }

    public ValidacionException(String campo, String message) {
        super(message);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
