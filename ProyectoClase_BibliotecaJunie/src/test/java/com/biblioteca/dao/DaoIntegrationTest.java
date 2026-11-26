package com.biblioteca.dao;

import com.biblioteca.model.Autor;
import com.biblioteca.model.Categoria;
import com.biblioteca.model.Libro;
import com.biblioteca.model.LibroCategoria;
import com.biblioteca.model.Prestamo;
import com.biblioteca.model.Socio;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class DaoIntegrationTest {

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0.41-debian")
            .withDatabaseName("biblioteca_db")
            .withUsername("test")
            .withPassword("test")
            .withCopyFileToContainer(MountableFile.forHostPath("db/init.sql"), "/docker-entrypoint-initdb.d/01-init.sql");

    private static BasicDataSource dataSource;

    private AutorDAO autorDAO;
    private CategoriaDAO categoriaDAO;
    private LibroDAO libroDAO;
    private LibroCategoriaDAO libroCategoriaDAO;
    private SocioDAO socioDAO;
    private PrestamoDAO prestamoDAO;

    @BeforeAll
    static void crearDataSource() {
        dataSource = new BasicDataSource();
        dataSource.setUrl(MYSQL.getJdbcUrl());
        dataSource.setUsername(MYSQL.getUsername());
        dataSource.setPassword(MYSQL.getPassword());
    }

    @AfterAll
    static void cerrarDataSource() throws SQLException {
        dataSource.close();
    }

    @BeforeEach
    void limpiarTablas() throws SQLException {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("SET FOREIGN_KEY_CHECKS = 0");
            try {
                statement.execute("TRUNCATE TABLE prestamo");
                statement.execute("TRUNCATE TABLE libro_categoria");
                statement.execute("TRUNCATE TABLE libro");
                statement.execute("TRUNCATE TABLE socio");
                statement.execute("TRUNCATE TABLE categoria");
                statement.execute("TRUNCATE TABLE autor");
            } finally {
                statement.execute("SET FOREIGN_KEY_CHECKS = 1");
            }
        }
    }

    @BeforeEach
    void crearDaos() {
        autorDAO = new AutorDAO(dataSource);
        categoriaDAO = new CategoriaDAO(dataSource);
        libroDAO = new LibroDAO(dataSource);
        libroCategoriaDAO = new LibroCategoriaDAO(dataSource);
        socioDAO = new SocioDAO(dataSource);
        prestamoDAO = new PrestamoDAO(dataSource);
    }

    @Test
    void autorCrudYRestricciones() throws DAOException {
        Autor autor = autorDAO.create(new Autor(null, "Autora de prueba", "Española"));
        assertNotNull(autor.getId());
        assertEquals("Autora de prueba", autorDAO.findById(autor.getId()).getNombre());
        assertEquals(1, autorDAO.findAll().size());

        autor.setNombre("Autora actualizada");
        autor.setNacionalidad("Mexicana");
        assertEquals("Autora actualizada", autorDAO.update(autor).getNombre());

        Libro libro = crearLibro(autor.getId(), "Libro relacionado", "9780000000001");
        DAOException restriccion = assertThrows(DAOException.class, () -> autorDAO.delete(autor.getId()));
        assertTrue(restriccion.getMessage().contains("tiene libros asociados"));
        libroDAO.delete(libro.getId());
        autorDAO.delete(autor.getId());
        assertNull(autorDAO.findById(autor.getId()));
        assertNull(autorDAO.findById(Integer.MAX_VALUE));
        assertThrows(DAOException.class, () -> autorDAO.update(new Autor(Integer.MAX_VALUE, "Inexistente", "N/A")));
        assertThrows(DAOException.class, () -> autorDAO.delete(Integer.MAX_VALUE));
    }

    @Test
    void categoriaCrudYNombreUnico() throws DAOException {
        Categoria categoria = categoriaDAO.create(new Categoria(null, "Categoría de prueba"));
        assertNotNull(categoria.getId());
        assertEquals("Categoría de prueba", categoriaDAO.findById(categoria.getId()).getNombre());
        assertEquals(1, categoriaDAO.findAll().size());

        DAOException duplicada = assertThrows(DAOException.class,
                () -> categoriaDAO.create(new Categoria(null, "Categoría de prueba")));
        assertTrue(duplicada.getMessage().contains("ya existe"));

        categoria.setNombre("Categoría actualizada");
        assertEquals("Categoría actualizada", categoriaDAO.update(categoria).getNombre());
        categoriaDAO.delete(categoria.getId());
        assertNull(categoriaDAO.findById(categoria.getId()));
        assertNull(categoriaDAO.findById(Integer.MAX_VALUE));
        assertThrows(DAOException.class,
                () -> categoriaDAO.update(new Categoria(Integer.MAX_VALUE, "Inexistente")));
        assertThrows(DAOException.class, () -> categoriaDAO.delete(Integer.MAX_VALUE));
    }

    @Test
    void libroCrudDuplicadoEIdsInexistentes() throws DAOException {
        Autor autor = crearAutor();
        Libro libro = crearLibro(autor.getId(), "Libro de prueba", "9780000000002");
        assertNotNull(libro.getId());
        assertEquals("Libro de prueba", libroDAO.findById(libro.getId()).getTitulo());
        assertEquals(1, libroDAO.findAll().size());
        assertEquals(1, libroDAO.findByTitulo("prueba").size());
        assertEquals(1, libroDAO.findByAutor(autor.getId()).size());
        assertEquals(3, libroDAO.getStock(libro.getId()));

        DAOException duplicado = assertThrows(DAOException.class,
                () -> crearLibro(autor.getId(), "ISBN repetido", "9780000000002"));
        assertTrue(duplicado.getMessage().contains("ISBN"));

        libro.setTitulo("Libro actualizado");
        libro.setStock(5);
        assertEquals("Libro actualizado", libroDAO.update(libro).getTitulo());
        assertEquals(5, libroDAO.getStock(libro.getId()));
        libroDAO.delete(libro.getId());
        assertNull(libroDAO.findById(libro.getId()));
        assertNull(libroDAO.findById(Integer.MAX_VALUE));
        assertThrows(DAOException.class, () -> libroDAO.update(new Libro(Integer.MAX_VALUE,
                "Inexistente", "9780000000003", 2020, 0, autor.getId())));
        assertThrows(DAOException.class, () -> libroDAO.delete(Integer.MAX_VALUE));
    }

    @Test
    void borrarLibroEliminaSusCategoriasEnCascada() throws DAOException {
        Autor autor = crearAutor();
        Categoria categoria = categoriaDAO.create(new Categoria(null, "Cascada"));
        Libro libro = crearLibro(autor.getId(), "Libro con categoría", "9780000000004");
        libroCategoriaDAO.insert(libro.getId(), categoria.getId());

        assertEquals(1, libroCategoriaDAO.findByLibroId(libro.getId()).size());
        libroDAO.delete(libro.getId());
        assertTrue(libroCategoriaDAO.findByLibroId(libro.getId()).isEmpty());
        assertTrue(libroCategoriaDAO.findByCategoriaId(categoria.getId()).isEmpty());
    }

    @Test
    void libroCategoriaInsertaConsultaYEliminaRelaciones() throws DAOException {
        Autor autor = crearAutor();
        Categoria primera = categoriaDAO.create(new Categoria(null, "Primera"));
        Categoria segunda = categoriaDAO.create(new Categoria(null, "Segunda"));
        Libro libro = crearLibro(autor.getId(), "Libro con varias categorías", "9780000000005");

        libroCategoriaDAO.insert(libro.getId(), primera.getId());
        libroCategoriaDAO.insert(libro.getId(), segunda.getId());
        List<LibroCategoria> delLibro = libroCategoriaDAO.findByLibroId(libro.getId());
        assertEquals(2, delLibro.size());
        assertEquals(1, libroCategoriaDAO.findByCategoriaId(primera.getId()).size());
        assertThrows(DAOException.class, () -> libroCategoriaDAO.insert(libro.getId(), primera.getId()));

        libroCategoriaDAO.deleteByLibroId(libro.getId());
        assertTrue(libroCategoriaDAO.findByLibroId(libro.getId()).isEmpty());
        assertTrue(libroCategoriaDAO.findByCategoriaId(primera.getId()).isEmpty());
    }

    @Test
    void socioCrudEmailUnicoYIdsInexistentes() throws DAOException {
        LocalDate fechaAlta = LocalDate.of(2025, 1, 15);
        Socio socio = socioDAO.create(new Socio(null, "Socio de prueba", "prueba@example.com", fechaAlta));
        assertNotNull(socio.getId());
        assertEquals(fechaAlta, socioDAO.findById(socio.getId()).getFechaAlta());
        assertEquals(1, socioDAO.findAll().size());

        DAOException duplicado = assertThrows(DAOException.class,
                () -> socioDAO.create(new Socio(null, "Otro socio", "prueba@example.com", fechaAlta)));
        assertTrue(duplicado.getMessage().contains("email"));

        socio.setNombre("Socio actualizado");
        socio.setEmail("actualizado@example.com");
        assertEquals("Socio actualizado", socioDAO.update(socio).getNombre());
        socioDAO.delete(socio.getId());
        assertNull(socioDAO.findById(socio.getId()));
        assertNull(socioDAO.findById(Integer.MAX_VALUE));
        assertThrows(DAOException.class,
                () -> socioDAO.update(new Socio(Integer.MAX_VALUE, "Inexistente", "none@example.com", fechaAlta)));
        assertThrows(DAOException.class, () -> socioDAO.delete(Integer.MAX_VALUE));
    }

    @Test
    void prestamoCrudFiltrosYIdsInexistentes() throws DAOException {
        Socio socio = crearSocio();
        Libro libro = crearLibro(crearAutor().getId(), "Libro prestable", "9780000000006");
        LocalDateTime fechaPrestamo = LocalDateTime.of(2025, 2, 1, 10, 30);
        Prestamo prestamo = prestamoDAO.create(new Prestamo(null, socio.getId(), libro.getId(),
                fechaPrestamo, null, "prestado"));

        assertNotNull(prestamo.getId());
        assertEquals("prestado", prestamoDAO.findById(prestamo.getId()).getEstado());
        assertEquals(1, prestamoDAO.findAll().size());
        assertEquals(1, prestamoDAO.findBySocioId(socio.getId()).size());
        assertEquals(1, prestamoDAO.findByEstado("prestado").size());
        assertEquals(1, prestamoDAO.countActivosBySocioId(socio.getId()));

        prestamo.setEstado("devuelto");
        prestamo.setFechaDevolucion(fechaPrestamo.plusDays(7));
        assertEquals("devuelto", prestamoDAO.update(prestamo).getEstado());
        assertEquals(0, prestamoDAO.countActivosBySocioId(socio.getId()));
        assertEquals(fechaPrestamo.plusDays(7), prestamoDAO.findById(prestamo.getId()).getFechaDevolucion());
        prestamoDAO.delete(prestamo.getId());
        assertNull(prestamoDAO.findById(prestamo.getId()));
        assertNull(prestamoDAO.findById(Integer.MAX_VALUE));
        assertThrows(DAOException.class, () -> prestamoDAO.update(new Prestamo(Integer.MAX_VALUE,
                socio.getId(), libro.getId(), fechaPrestamo, fechaPrestamo.plusDays(1), "devuelto")));
        assertThrows(DAOException.class, () -> prestamoDAO.delete(Integer.MAX_VALUE));
    }

    private Autor crearAutor() throws DAOException {
        return autorDAO.create(new Autor(null, "Autor de prueba", "Española"));
    }

    private Libro crearLibro(Integer autorId, String titulo, String isbn) throws DAOException {
        return libroDAO.create(new Libro(null, titulo, isbn, 2020, 3, autorId));
    }

    private Socio crearSocio() throws DAOException {
        return socioDAO.create(new Socio(null, "Socio de prueba", "socio@example.com", LocalDate.of(2025, 1, 1)));
    }

}