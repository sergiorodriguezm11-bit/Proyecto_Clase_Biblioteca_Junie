package com.biblioteca.exception;

/**
 * Excepción lanzada cuando un recurso no se encuentra (HTTP 404).
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String message) {
        super(message);
    }
}
