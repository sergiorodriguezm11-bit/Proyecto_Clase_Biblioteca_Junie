package com.biblioteca.service;

import com.biblioteca.util.TransactionManager;
import com.biblioteca.config.DataSourceProvider;
import com.biblioteca.dao.*;
import com.biblioteca.dto.*;
import com.biblioteca.exception.*;
import com.biblioteca.mapper.*;
import com.biblioteca.model.Libro;
import com.biblioteca.model.LibroCategoria;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para la entidad Libro con transacciones.
 */
public class LibroService {

    private final LibroDAO libroDAO;
    private final AutorDAO autorDAO;
    private final CategoriaDAO categoriaDAO;
    private final LibroCategoriaDAO libroCategoriaDAO;
    private final TransactionManager transactionManager;

    public LibroService() {
        this.libroDAO = new LibroDAO();
        this.autorDAO = new AutorDAO();
        this.categoriaDAO = new CategoriaDAO();
        this.libroCategoriaDAO = new LibroCategoriaDAO();
        this.transactionManager = new TransactionManager(DataSourceProvider.getInstance());
    }

    public LibroService(LibroDAO libroDAO, AutorDAO autorDAO, CategoriaDAO categoriaDAO,
                        LibroCategoriaDAO libroCategoriaDAO, DataSource dataSource) {
        this.libroDAO = libroDAO;
        this.autorDAO = autorDAO;
        this.categoriaDAO = categoriaDAO;
        this.libroCategoriaDAO = libroCategoriaDAO;
        this.transactionManager = new TransactionManager(dataSource);
    }

    public List<LibroDTO> listar() throws ServicioException {
        return listar(null, null, null);
    }

    public List<LibroDTO> listar(String titulo, String autor, String categoria) throws ServicioException {
        try {
            List<Libro> libros;
            if (titulo != null && !titulo.isEmpty()) {
                libros = libroDAO.findByTitulo(titulo);
            } else if (autor != null && !autor.isEmpty()) {
                // Buscar autor por nombre
                List<com.biblioteca.model.Autor> autores = autorDAO.findAll();
                var encontrado = autores.stream()
                        .filter(a -> a.getNombre().toLowerCase().contains(autor.toLowerCase()))
                        .findFirst();
                if (encontrado.isPresent()) {
                    libros = libroDAO.findByAutor(encontrado.get().getId());
                } else {
                    return new ArrayList<>();
                }
            } else if (categoria != null && !categoria.isEmpty()) {
                var cats = categoriaDAO.findAll();
                var catEncontrada = cats.stream()
                        .filter(c -> c.getNombre().toLowerCase().contains(categoria.toLowerCase()))
                        .findFirst();
                if (catEncontrada.isPresent()) {
                    var lcList = libroCategoriaDAO.findByCategoriaId(catEncontrada.get().getId());
                    libros = new ArrayList<>();
                    for (var lc : lcList) {
                        var l = libroDAO.findById(lc.getLibroId());
                        if (l != null) libros.add(l);
                    }
                } else {
                    return new ArrayList<>();
                }
            } else {
                libros = libroDAO.findAll();
            }

            return libros.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        } catch (DAOException e) {
            throw new ServicioException("Error al listar libros", e);
        }
    }

    public LibroDTO buscarPorId(Integer id) throws RecursoNoEncontradoException, ServicioException {
        try {
            Libro libro = libroDAO.findById(id);
            if (libro == null) {
                throw new RecursoNoEncontradoException("Libro no encontrado con id: " + id);
            }
            return toDTO(libro);
        } catch (DAOException e) {
            throw new ServicioException("Error al buscar libro", e);
        }
    }

    public LibroDTO crear(CrearLibroRequest req) throws ValidacionException, RecursoNoEncontradoException,
            ConflictoException, ServicioException {
        validarLibroRequest(req);
        try {
            // Verificar que el autor existe
            var autor = autorDAO.findById(req.getAutorId());
            if (autor == null) {
                throw new RecursoNoEncontradoException("Autor no encontrado con id: " + req.getAutorId());
            }

            return transactionManager.ejecutar("Error transaccional al crear libro", conn -> {
                Libro libro = LibroMapper.toEntity(req);
                libro = libroDAO.create(conn, libro);

                // Insertar categorías
                if (req.getCategoriaIds() != null) {
                    for (Integer catId : req.getCategoriaIds()) {
                        var cat = categoriaDAO.findById(conn, catId);
                        if (cat == null) {
                            throw new RecursoNoEncontradoException("Categoría no encontrada con id: " + catId);
                        }
                        libroCategoriaDAO.insert(conn, libro.getId(), catId);
                    }
                }

                return toDTO(libro);
            });
        } catch (DAOException e) {
            if (e.getMessage().contains("Duplicate") || e.getMessage().contains("ISBN")) {
                throw new ConflictoException("El ISBN ya está registrado: " + req.getIsbn());
            }
            throw new ServicioException("Error al crear libro", e);
        }
    }

    public LibroDTO actualizar(Integer id, ActualizarLibroRequest req) throws ValidacionException,
            RecursoNoEncontradoException, ConflictoException, ServicioException {
        CrearLibroRequest crearReq = toCrearLibroRequest(req);
        validarLibroRequest(crearReq);
        try {
            var existente = libroDAO.findById(id);
            if (existente == null) {
                throw new RecursoNoEncontradoException("Libro no encontrado con id: " + id);
            }

            var autor = autorDAO.findById(req.getAutorId());
            if (autor == null) {
                throw new RecursoNoEncontradoException("Autor no encontrado con id: " + req.getAutorId());
            }

            return transactionManager.ejecutar("Error transaccional al actualizar libro", conn -> {
                Libro libro = LibroMapper.toEntity(crearReq);
                libro.setId(id);
                libro = libroDAO.update(conn, libro);

                // Eliminar categorías antiguas y insertar nuevas
                libroCategoriaDAO.deleteByLibroId(conn, id);
                if (req.getCategoriaIds() != null) {
                    for (Integer catId : req.getCategoriaIds()) {
                        var cat = categoriaDAO.findById(conn, catId);
                        if (cat == null) {
                            throw new RecursoNoEncontradoException("Categoría no encontrada con id: " + catId);
                        }
                        libroCategoriaDAO.insert(conn, id, catId);
                    }
                }

                return toDTO(libro);
            });
        } catch (DAOException e) {
            if (e.getMessage().contains("Duplicate") || e.getMessage().contains("ISBN")) {
                throw new ConflictoException("El ISBN ya está registrado: " + req.getIsbn());
            }
            throw new ServicioException("Error al actualizar libro", e);
        }
    }

    public void eliminar(Integer id) throws RecursoNoEncontradoException, ConflictoException, ServicioException {
        try {
            var existente = libroDAO.findById(id);
            if (existente == null) {
                throw new RecursoNoEncontradoException("Libro no encontrado con id: " + id);
            }
            libroDAO.delete(id);
        } catch (DAOException e) {
            if (e.getMessage().contains("foreign key") || e.getMessage().contains("No se puede borrar")) {
                throw new ConflictoException("No se puede borrar el libro porque tiene préstamos registrados.");
            }
            throw new ServicioException("Error al eliminar libro", e);
        }
    }

    private void validarLibroRequest(CrearLibroRequest req) throws ValidacionException {
        if (req.getTitulo() == null || req.getTitulo().trim().isEmpty()) {
            throw new ValidacionException("titulo", "El título es obligatorio.");
        }
        if (req.getIsbn() == null || req.getIsbn().trim().isEmpty()) {
            throw new ValidacionException("isbn", "El ISBN es obligatorio.");
        }
        if (req.getAnioPublicacion() < 1450 || req.getAnioPublicacion() > 2099) {
            throw new ValidacionException("anioPublicacion", "El año debe estar entre 1450 y 2099.");
        }
        if (req.getStock() < 0) {
            throw new ValidacionException("stock", "El stock no puede ser negativo.");
        }
    }

    private CrearLibroRequest toCrearLibroRequest(ActualizarLibroRequest req) {
        CrearLibroRequest crearReq = new CrearLibroRequest();
        crearReq.setTitulo(req.getTitulo());
        crearReq.setIsbn(req.getIsbn());
        crearReq.setAnioPublicacion(req.getAnioPublicacion());
        crearReq.setStock(req.getStock());
        crearReq.setAutorId(req.getAutorId());
        crearReq.setCategoriaIds(req.getCategoriaIds());
        return crearReq;
    }

    private LibroDTO toDTO(Libro libro) {
        try {
            var autor = autorDAO.findById(libro.getAutorId());
            AutorDTO autorDTO = autor != null ? AutorMapper.toDTO(autor) : null;

            var lcList = libroCategoriaDAO.findByLibroId(libro.getId());
            List<CategoriaDTO> catDTOs = LibroMapper.mapCategorias(lcList, categoriaDAO);

            return LibroMapper.toDTO(libro, autorDTO, catDTOs);
        } catch (DAOException e) {
            throw new RuntimeException("Error al mapear libro a DTO", e);
        }
    }
}
