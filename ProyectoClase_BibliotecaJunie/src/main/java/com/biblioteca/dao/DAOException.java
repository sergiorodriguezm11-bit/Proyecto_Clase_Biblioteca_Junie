package com.biblioteca.dao;

/**
 * Excepción base para errores de acceso a datos.
 */
public class DAOException extends Exception {

    public DAOException(String message) {
        super(message);
    }

    public DAOException(String message, Throwable cause) {
        super(message, cause);
    }
}
