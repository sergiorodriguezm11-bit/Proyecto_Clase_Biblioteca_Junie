package com.biblioteca.exception;

/**
 * Excepción lanzada cuando hay un conflicto de datos (HTTP 409).
 * Ej: ISBN duplicado, email duplicado, sin stock, etc.
 */
public class ConflictoException extends RuntimeException {

    public ConflictoException(String message) {
        super(message);
    }
}
