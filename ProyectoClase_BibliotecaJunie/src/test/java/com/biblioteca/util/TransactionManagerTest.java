package com.biblioteca.util;

import com.biblioteca.dao.DAOException;
import com.biblioteca.exception.ServicioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransactionManagerTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private TransactionManager transactionManager;

    @BeforeEach
    void setUp() throws SQLException {
        transactionManager = new TransactionManager(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
    }

    @Test
    void confirmaYLiberaLaConexionCuandoLaOperacionTermina() throws Exception {
        String resultado = transactionManager.ejecutar("Error de operación", conn -> "resultado");

        assertEquals("resultado", resultado);
        verify(connection).setAutoCommit(false);
        verify(connection).commit();
        verify(connection).close();
    }

    @Test
    void revierteYPropagaElErrorDelDao() throws Exception {
        DAOException fallo = new DAOException("Fallo de persistencia");

        DAOException capturada = assertThrows(DAOException.class,
                () -> transactionManager.ejecutar("Error de operación", conn -> { throw fallo; }));

        assertSame(fallo, capturada);
        verify(connection).rollback();
        verify(connection).close();
    }

    @Test
    void revierteYTraduceUnFalloSqlTransaccional() throws Exception {
        SQLException fallo = new SQLException("Commit fallido");
        doThrow(fallo).when(connection).commit();

        ServicioException capturada = assertThrows(ServicioException.class,
                () -> transactionManager.ejecutar("Error transaccional al guardar", conn -> "resultado"));

        assertEquals("Error transaccional al guardar", capturada.getMessage());
        assertSame(fallo, capturada.getCause());
        verify(connection).rollback();
        verify(connection).close();
    }
}