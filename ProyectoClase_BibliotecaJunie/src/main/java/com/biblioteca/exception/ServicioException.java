package com.biblioteca.exception;

/**
 * Excepción lanzada cuando ocurre un error interno del servidor (HTTP 500).
 */
public class ServicioException extends RuntimeException {

    public ServicioException(String message) {
        super(message);
    }

    public ServicioException(String message, Throwable cause) {
        super(message, cause);
    }
}
