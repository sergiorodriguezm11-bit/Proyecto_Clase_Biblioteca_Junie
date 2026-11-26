package com.biblioteca.service;

import com.biblioteca.dao.AutorDAO;
import com.biblioteca.dao.CategoriaDAO;
import com.biblioteca.dao.DAOException;
import com.biblioteca.dao.LibroCategoriaDAO;
import com.biblioteca.dao.LibroDAO;
import com.biblioteca.dao.PrestamoDAO;
import com.biblioteca.dao.SocioDAO;
import com.biblioteca.dto.CrearPrestamoRequest;
import com.biblioteca.dto.PrestamoDTO;
import com.biblioteca.exception.ConflictoException;
import com.biblioteca.exception.RecursoNoEncontradoException;
import com.biblioteca.exception.ServicioException;
import com.biblioteca.model.Autor;
import com.biblioteca.model.Libro;
import com.biblioteca.model.Prestamo;
import com.biblioteca.model.Socio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PrestamoServiceTest {

    private final PrestamoDAO prestamoDAO = mock(PrestamoDAO.class);
    private final LibroDAO libroDAO = mock(LibroDAO.class);
    private final SocioDAO socioDAO = mock(SocioDAO.class);
    private final AutorDAO autorDAO = mock(AutorDAO.class);
    private final CategoriaDAO categoriaDAO = mock(CategoriaDAO.class);
    private final LibroCategoriaDAO libroCategoriaDAO = mock(LibroCategoriaDAO.class);
    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private PrestamoService service;

    @BeforeEach
    void setUp() throws SQLException {
        service = new PrestamoService(prestamoDAO, libroDAO, socioDAO, autorDAO,
                categoriaDAO, libroCategoriaDAO, dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
    }

    @Test
    void informaSocioInexistenteAntesDeAbrirTransaccion() throws Exception {
        when(socioDAO.findById(3)).thenReturn(null);

        assertThrows(RecursoNoEncontradoException.class, () -> service.crear(prestamoRequest()));

        verify(dataSource, never()).getConnection();
    }

    @Test
    void noPermitePrestarUnLibroSinStock() throws Exception {
        when(socioDAO.findById(3)).thenReturn(new Socio(3, "Socio", "socio@example.com", LocalDate.now()));
        when(libroDAO.findById(7)).thenReturn(new Libro(7, "Libro", "9780000000007", 2020, 0, 8));
        when(libroDAO.getStock(7)).thenReturn(0);

        assertThrows(ConflictoException.class, () -> service.crear(prestamoRequest()));

        verify(dataSource, never()).getConnection();
    }

    @Test
    void revierteLaCreacionSiFallaElDescuentoDeStock() throws Exception {
        prepararPrestamoValido();
        when(prestamoDAO.create(eq(connection), any(Prestamo.class))).thenAnswer(invocation -> {
            Prestamo prestamo = invocation.getArgument(1);
            prestamo.setId(15);
            return prestamo;
        });
        doThrow(new DAOException("No se pudo descontar stock"))
                .when(libroDAO).decrementStock(connection, 7);

        assertThrows(ServicioException.class, () -> service.crear(prestamoRequest()));

        verify(connection).rollback();
        verify(connection, never()).commit();
        verify(connection).close();
    }

    @Test
    void creaPrestamoYConfirmaLaTransaccion() throws Exception {
        prepararPrestamoValido();
        when(prestamoDAO.create(eq(connection), any(Prestamo.class))).thenAnswer(invocation -> {
            Prestamo prestamo = invocation.getArgument(1);
            prestamo.setId(15);
            return prestamo;
        });
        when(socioDAO.findById(3)).thenReturn(new Socio(3, "Socio", "socio@example.com", LocalDate.now()));
        when(libroDAO.findById(7)).thenReturn(new Libro(7, "Libro", "9780000000007", 2020, 2, 8));
        when(autorDAO.findById(8)).thenReturn(new Autor(8, "Autor", "Local"));
        when(libroCategoriaDAO.findByLibroId(7)).thenReturn(List.of());

        PrestamoDTO resultado = service.crear(prestamoRequest());

        assertEquals(15, resultado.getId());
        assertEquals("prestado", resultado.getEstado());
        verify(libroDAO).decrementStock(connection, 7);
        verify(connection).commit();
    }

    @Test
    void devuelvePrestamoActualizandoElEstadoYElStock() throws Exception {
        LocalDateTime fecha = LocalDateTime.of(2025, 1, 10, 12, 0);
        Prestamo prestamo = new Prestamo(15, 3, 7, fecha, null, "prestado");
        when(prestamoDAO.findById(15)).thenReturn(prestamo);
        when(prestamoDAO.update(eq(connection), any(Prestamo.class)))
                .thenAnswer(invocation -> invocation.getArgument(1));
        when(socioDAO.findById(3)).thenReturn(new Socio(3, "Socio", "socio@example.com", LocalDate.now()));
        when(libroDAO.findById(7)).thenReturn(new Libro(7, "Libro", "9780000000007", 2020, 1, 8));
        when(autorDAO.findById(8)).thenReturn(new Autor(8, "Autor", "Local"));
        when(libroCategoriaDAO.findByLibroId(7)).thenReturn(List.of());

        PrestamoDTO resultado = service.devolver(15);

        assertEquals("devuelto", resultado.getEstado());
        assertNotNull(resultado.getFechaDevolucion());
        verify(libroDAO).incrementStock(connection, 7);
        verify(connection).commit();
    }

    @Test
    void noDevuelveDosVecesElMismoPrestamo() throws Exception {
        when(prestamoDAO.findById(15)).thenReturn(new Prestamo(15, 3, 7,
                LocalDateTime.now(), LocalDateTime.now(), "devuelto"));

        assertThrows(ConflictoException.class, () -> service.devolver(15));

        verify(dataSource, never()).getConnection();
    }

    private void prepararPrestamoValido() throws DAOException {
        when(socioDAO.findById(3)).thenReturn(new Socio(3, "Socio", "socio@example.com", LocalDate.now()));
        when(libroDAO.findById(7)).thenReturn(new Libro(7, "Libro", "9780000000007", 2020, 2, 8));
        when(libroDAO.getStock(7)).thenReturn(2);
        when(prestamoDAO.countActivosBySocioId(3)).thenReturn(0);
    }

    private CrearPrestamoRequest prestamoRequest() {
        CrearPrestamoRequest request = new CrearPrestamoRequest();
        request.setSocioId(3);
        request.setLibroId(7);
        return request;
    }
}