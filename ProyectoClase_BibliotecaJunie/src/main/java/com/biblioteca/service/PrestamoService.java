package com.biblioteca.service;

import com.biblioteca.util.TransactionManager;
import com.biblioteca.config.DataSourceProvider;
import com.biblioteca.dao.*;
import com.biblioteca.dto.*;
import com.biblioteca.exception.*;
import com.biblioteca.mapper.*;
import com.biblioteca.model.Libro;
import com.biblioteca.model.Prestamo;
import com.biblioteca.model.Socio;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para la entidad Prestamo con transacciones y reglas de negocio.
 */
public class PrestamoService {

    private final PrestamoDAO prestamoDAO;
    private final LibroDAO libroDAO;
    private final SocioDAO socioDAO;
    private final AutorDAO autorDAO;
    private final CategoriaDAO categoriaDAO;
    private final LibroCategoriaDAO libroCategoriaDAO;
    private final TransactionManager transactionManager;

    public PrestamoService() {
        this.prestamoDAO = new PrestamoDAO();
        this.libroDAO = new LibroDAO();
        this.socioDAO = new SocioDAO();
        this.autorDAO = new AutorDAO();
        this.categoriaDAO = new CategoriaDAO();
        this.libroCategoriaDAO = new LibroCategoriaDAO();
        this.transactionManager = new TransactionManager(DataSourceProvider.getInstance());
    }

    public PrestamoService(PrestamoDAO prestamoDAO, LibroDAO libroDAO, SocioDAO socioDAO,
                           AutorDAO autorDAO, CategoriaDAO categoriaDAO,
                           LibroCategoriaDAO libroCategoriaDAO, DataSource dataSource) {
        this.prestamoDAO = prestamoDAO;
        this.libroDAO = libroDAO;
        this.socioDAO = socioDAO;
        this.autorDAO = autorDAO;
        this.categoriaDAO = categoriaDAO;
        this.libroCategoriaDAO = libroCategoriaDAO;
        this.transactionManager = new TransactionManager(dataSource);
    }

    public List<PrestamoDTO> listar() throws ServicioException {
        return listar(null, null);
    }

    public List<PrestamoDTO> listar(Integer socioId, String estado) throws ServicioException {
        try {
            List<Prestamo> prestamos;
            if (socioId != null) {
                prestamos = prestamoDAO.findBySocioId(socioId);
            } else if (estado != null && !estado.isEmpty()) {
                prestamos = prestamoDAO.findByEstado(estado);
            } else {
                prestamos = prestamoDAO.findAll();
            }
            return prestamos.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        } catch (DAOException e) {
            throw new ServicioException("Error al listar préstamos", e);
        }
    }

    public PrestamoDTO buscarPorId(Integer id) throws RecursoNoEncontradoException, ServicioException {
        try {
            Prestamo prestamo = prestamoDAO.findById(id);
            if (prestamo == null) {
                throw new RecursoNoEncontradoException("Préstamo no encontrado con id: " + id);
            }
            return toDTO(prestamo);
        } catch (DAOException e) {
            throw new ServicioException("Error al buscar préstamo", e);
        }
    }

    public PrestamoDTO crear(CrearPrestamoRequest req) throws RecursoNoEncontradoException,
            ConflictoException, ServicioException {
        try {
            // Validar que socio existe
            Socio socio = socioDAO.findById(req.getSocioId());
            if (socio == null) {
                throw new RecursoNoEncontradoException("Socio no encontrado con id: " + req.getSocioId());
            }

            // Validar que libro existe
            Libro libro = libroDAO.findById(req.getLibroId());
            if (libro == null) {
                throw new RecursoNoEncontradoException("Libro no encontrado con id: " + req.getLibroId());
            }

            // Validar stock
            int stock = libroDAO.getStock(req.getLibroId());
            if (stock <= 0) {
                throw new ConflictoException("No hay stock disponible para el libro: " + libro.getTitulo());
            }

            // Validar límite de 3 préstamos activos
            int activos = prestamoDAO.countActivosBySocioId(req.getSocioId());
            if (activos >= 3) {
                throw new ConflictoException("El socio ya tiene 3 préstamos activos.");
            }

            return transactionManager.ejecutar("Error transaccional al crear préstamo", conn -> {
                Prestamo prestamo = new Prestamo();
                prestamo.setSocioId(req.getSocioId());
                prestamo.setLibroId(req.getLibroId());
                prestamo.setFechaPrestamo(LocalDateTime.now());
                prestamo.setEstado("prestado");
                prestamo = prestamoDAO.create(conn, prestamo);

                libroDAO.decrementStock(conn, req.getLibroId());

                return toDTO(prestamo);
            });
        } catch (DAOException e) {
            throw new ServicioException("Error al crear préstamo", e);
        }
    }

    public PrestamoDTO devolver(Integer id) throws RecursoNoEncontradoException,
            ConflictoException, ServicioException {
        try {
            Prestamo prestamoExistente = prestamoDAO.findById(id);
            if (prestamoExistente == null) {
                throw new RecursoNoEncontradoException("Préstamo no encontrado con id: " + id);
            }

            if ("devuelto".equals(prestamoExistente.getEstado())) {
                throw new ConflictoException("El préstamo ya ha sido devuelto.");
            }

            return transactionManager.ejecutar("Error transaccional al devolver préstamo", conn -> {
                prestamoExistente.setEstado("devuelto");
                prestamoExistente.setFechaDevolucion(LocalDateTime.now());
                Prestamo prestamoActualizado = prestamoDAO.update(conn, prestamoExistente);

                libroDAO.incrementStock(conn, prestamoActualizado.getLibroId());

                return toDTO(prestamoActualizado);
            });
        } catch (DAOException e) {
            throw new ServicioException("Error al devolver préstamo", e);
        }
    }

    private PrestamoDTO toDTO(Prestamo prestamo) {
        try {
            Socio socio = socioDAO.findById(prestamo.getSocioId());
            SocioDTO socioDTO = socio != null ? SocioMapper.toDTO(socio) : null;

            Libro libro = libroDAO.findById(prestamo.getLibroId());
            LibroDTO libroDTO = null;
            if (libro != null) {
                var autor = autorDAO.findById(libro.getAutorId());
                AutorDTO autorDTO = autor != null ? AutorMapper.toDTO(autor) : null;
                var lcList = libroCategoriaDAO.findByLibroId(libro.getId());
                List<CategoriaDTO> catDTOs = LibroMapper.mapCategorias(lcList, categoriaDAO);
                libroDTO = LibroMapper.toDTO(libro, autorDTO, catDTOs);
            }

            return PrestamoMapper.toDTO(prestamo, socioDTO, libroDTO);
        } catch (DAOException e) {
            throw new RuntimeException("Error al mapear préstamo a DTO", e);
        }
    }
}
