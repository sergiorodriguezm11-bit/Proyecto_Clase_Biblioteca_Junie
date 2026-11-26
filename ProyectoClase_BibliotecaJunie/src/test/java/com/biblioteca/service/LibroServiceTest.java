package com.biblioteca.service;

import com.biblioteca.dao.AutorDAO;
import com.biblioteca.dao.CategoriaDAO;
import com.biblioteca.dao.DAOException;
import com.biblioteca.dao.LibroCategoriaDAO;
import com.biblioteca.dao.LibroDAO;
import com.biblioteca.dto.CrearLibroRequest;
import com.biblioteca.exception.ConflictoException;
import com.biblioteca.exception.RecursoNoEncontradoException;
import com.biblioteca.exception.ValidacionException;
import com.biblioteca.model.Autor;
import com.biblioteca.model.Libro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LibroServiceTest {

    private final LibroDAO libroDAO = mock(LibroDAO.class);
    private final AutorDAO autorDAO = mock(AutorDAO.class);
    private final CategoriaDAO categoriaDAO = mock(CategoriaDAO.class);
    private final LibroCategoriaDAO libroCategoriaDAO = mock(LibroCategoriaDAO.class);
    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private LibroService service;

    @BeforeEach
    void setUp() throws SQLException {
        service = new LibroService(libroDAO, autorDAO, categoriaDAO, libroCategoriaDAO, dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
    }

    @Test
    void validarLibroAntesDeAccederALosDaos() throws Exception {
        CrearLibroRequest request = libroRequest();
        request.setTitulo(" ");

        assertThrows(ValidacionException.class, () -> service.crear(request));

        verify(dataSource, never()).getConnection();
        verify(autorDAO, never()).findById(any());
    }

    @Test
    void informarAutorInexistenteSinAbrirTransaccion() throws Exception {
        when(autorDAO.findById(7)).thenReturn(null);

        assertThrows(RecursoNoEncontradoException.class, () -> service.crear(libroRequest()));

        verify(dataSource, never()).getConnection();
    }

    @Test
    void informaConflictoPorIsbnDuplicadoYRevierteLaTransaccion() throws Exception {
        when(autorDAO.findById(7)).thenReturn(new Autor(7, "Autor", "Local"));
        when(libroDAO.create(eq(connection), any(Libro.class))).thenThrow(new DAOException("Duplicate ISBN"));

        assertThrows(ConflictoException.class, () -> service.crear(libroRequest()));

        verify(connection).rollback();
        verify(connection, never()).commit();
    }

    @Test
    void revierteLibroCuandoUnaCategoriaNoExiste() throws Exception {
        when(autorDAO.findById(7)).thenReturn(new Autor(7, "Autor", "Local"));
        when(libroDAO.create(eq(connection), any(Libro.class))).thenAnswer(invocation -> {
            Libro libro = invocation.getArgument(1);
            libro.setId(12);
            return libro;
        });
        when(categoriaDAO.findById(connection, 99)).thenReturn(null);
        CrearLibroRequest request = libroRequest();
        request.setCategoriaIds(List.of(99));

        assertThrows(RecursoNoEncontradoException.class, () -> service.crear(request));

        verify(connection).rollback();
        verify(connection, never()).commit();
        verify(connection).close();
    }

    @Test
    void convierteErrorDeBorradoRestringidoEnConflicto() throws DAOException {
        when(libroDAO.findById(12)).thenReturn(new Libro(12, "Libro", "9780000000001", 2020, 1, 7));
        doThrow(new DAOException("No se puede borrar por foreign key"))
                .when(libroDAO).delete(12);

        assertThrows(ConflictoException.class, () -> service.eliminar(12));
    }

    private CrearLibroRequest libroRequest() {
        CrearLibroRequest request = new CrearLibroRequest();
        request.setTitulo("Libro de prueba");
        request.setIsbn("9780000000001");
        request.setAnioPublicacion(2020);
        request.setStock(2);
        request.setAutorId(7);
        return request;
    }
}