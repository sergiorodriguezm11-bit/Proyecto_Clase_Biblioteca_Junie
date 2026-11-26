package com.biblioteca.util;

import com.biblioteca.dao.DAOException;
import com.biblioteca.exception.ServicioException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T ejecutar(String mensajeErrorTransaccional, TransactionCallback<T> callback)
            throws DAOException, ServicioException {
        Connection connection;
        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            throw new ServicioException("Error de conexión", e);
        }

        try {
            connection.setAutoCommit(false);
            T resultado = callback.ejecutar(connection);
            connection.commit();
            return resultado;
        } catch (DAOException e) {
            rollback(connection);
            throw e;
        } catch (SQLException e) {
            rollback(connection);
            throw new ServicioException(mensajeErrorTransaccional, e);
        } catch (RuntimeException | Error e) {
            rollback(connection);
            throw e;
        } finally {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
        }
    }

    @FunctionalInterface
    public interface TransactionCallback<T> {
        T ejecutar(Connection connection) throws DAOException;
    }
}