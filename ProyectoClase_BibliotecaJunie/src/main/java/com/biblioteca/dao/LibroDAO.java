package com.biblioteca.dao;

import com.biblioteca.config.DataSourceProvider;
import com.biblioteca.model.Libro;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la entidad Libro.
 */
public class LibroDAO {

    private DataSource dataSource;

    public LibroDAO() {
        this.dataSource = DataSourceProvider.getInstance();
    }

    public LibroDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Libro create(Libro libro) throws DAOException {
        String sql = "INSERT INTO libro (titulo, isbn, anio_publicacion, stock, autor_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setLibroParams(ps, libro);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) libro.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate")) {
                throw new DAOException("El ISBN ya está registrado: " + libro.getIsbn(), e);
            }
            throw new DAOException("Error al crear libro: " + e.getMessage(), e);
        }
        return libro;
    }

    public Libro findById(Integer id) throws DAOException {
        String sql = "SELECT id, titulo, isbn, anio_publicacion, stock, autor_id FROM libro WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSet(rs);
            }
        } catch (SQLException e) {
            throw new DAOException("Error al buscar libro: " + e.getMessage(), e);
        }
        return null;
    }

    public List<Libro> findAll() throws DAOException {
        String sql = "SELECT id, titulo, isbn, anio_publicacion, stock, autor_id FROM libro ORDER BY titulo";
        List<Libro> libros = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) libros.add(mapResultSet(rs));
        } catch (SQLException e) {
            throw new DAOException("Error al listar libros: " + e.getMessage(), e);
        }
        return libros;
    }

    public List<Libro> findByTitulo(String titulo) throws DAOException {
        String sql = "SELECT id, titulo, isbn, anio_publicacion, stock, autor_id FROM libro WHERE titulo LIKE ? ORDER BY titulo";
        List<Libro> libros = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + titulo + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) libros.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Error al buscar libros por título: " + e.getMessage(), e);
        }
        return libros;
    }

    public List<Libro> findByAutor(Integer autorId) throws DAOException {
        String sql = "SELECT id, titulo, isbn, anio_publicacion, stock, autor_id FROM libro WHERE autor_id = ? ORDER BY titulo";
        List<Libro> libros = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, autorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) libros.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Error al buscar libros por autor: " + e.getMessage(), e);
        }
        return libros;
    }

    public Libro update(Libro libro) throws DAOException {
        String sql = "UPDATE libro SET titulo = ?, isbn = ?, anio_publicacion = ?, stock = ?, autor_id = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setLibroParams(ps, libro);
            ps.setInt(6, libro.getId());
            if (ps.executeUpdate() == 0) {
                throw new DAOException("Libro no encontrado con id: " + libro.getId());
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate")) {
                throw new DAOException("El ISBN ya está registrado: " + libro.getIsbn(), e);
            }
            throw new DAOException("Error al actualizar libro: " + e.getMessage(), e);
        }
        return libro;
    }

    public void delete(Integer id) throws DAOException {
        String sql = "DELETE FROM libro WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new DAOException("Libro no encontrado con id: " + id);
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("foreign key")) {
                throw new DAOException("No se puede borrar el libro porque tiene préstamos registrados.", e);
            }
            throw new DAOException("Error al borrar libro: " + e.getMessage(), e);
        }
    }

    public int getStock(Integer libroId) throws DAOException {
        String sql = "SELECT stock FROM libro WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, libroId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("stock");
            }
        } catch (SQLException e) {
            throw new DAOException("Error al obtener stock: " + e.getMessage(), e);
        }
        return 0;
    }

    public void decrementStock(Integer libroId) throws DAOException {
        decrementStock(null, libroId);
    }

    public void decrementStock(Connection conn, Integer libroId) throws DAOException {
        String sql = "UPDATE libro SET stock = stock - 1 WHERE id = ? AND stock > 0";
        try (Connection connection = conn == null ? dataSource.getConnection() : null;
             PreparedStatement ps = (conn != null ? conn : connection).prepareStatement(sql)) {
            ps.setInt(1, libroId);
            if (ps.executeUpdate() == 0) {
                throw new DAOException("No hay stock disponible para el libro id: " + libroId);
            }
        } catch (SQLException e) {
            throw new DAOException("Error al decrementar stock: " + e.getMessage(), e);
        }
    }

    public void incrementStock(Integer libroId) throws DAOException {
        incrementStock(null, libroId);
    }

    public void incrementStock(Connection conn, Integer libroId) throws DAOException {
        String sql = "UPDATE libro SET stock = stock + 1 WHERE id = ?";
        try (Connection connection = conn == null ? dataSource.getConnection() : null;
             PreparedStatement ps = (conn != null ? conn : connection).prepareStatement(sql)) {
            ps.setInt(1, libroId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Error al incrementar stock: " + e.getMessage(), e);
        }
    }

    private void setLibroParams(PreparedStatement ps, Libro libro) throws SQLException {
        ps.setString(1, libro.getTitulo());
        ps.setString(2, libro.getIsbn());
        ps.setInt(3, libro.getAnioPublicacion());
        ps.setInt(4, libro.getStock());
        ps.setInt(5, libro.getAutorId());
    }

    private Libro mapResultSet(ResultSet rs) throws SQLException {
        Libro libro = new Libro();
        libro.setId(rs.getInt("id"));
        libro.setTitulo(rs.getString("titulo"));
        libro.setIsbn(rs.getString("isbn"));
        libro.setAnioPublicacion(rs.getInt("anio_publicacion"));
        libro.setStock(rs.getInt("stock"));
        libro.setAutorId(rs.getInt("autor_id"));
        return libro;
    }

    // --- Métodos con Connection (transacciones) ---

    public Libro create(Connection conn, Libro libro) throws DAOException {
        String sql = "INSERT INTO libro (titulo, isbn, anio_publicacion, stock, autor_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setLibroParams(ps, libro);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) libro.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate")) {
                throw new DAOException("El ISBN ya está registrado: " + libro.getIsbn(), e);
            }
            throw new DAOException("Error al crear libro: " + e.getMessage(), e);
        }
        return libro;
    }

    public Libro findById(Connection conn, Integer id) throws DAOException {
        String sql = "SELECT id, titulo, isbn, anio_publicacion, stock, autor_id FROM libro WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSet(rs);
            }
        } catch (SQLException e) {
            throw new DAOException("Error al buscar libro: " + e.getMessage(), e);
        }
        return null;
    }

    public Libro update(Connection conn, Libro libro) throws DAOException {
        String sql = "UPDATE libro SET titulo = ?, isbn = ?, anio_publicacion = ?, stock = ?, autor_id = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setLibroParams(ps, libro);
            ps.setInt(6, libro.getId());
            if (ps.executeUpdate() == 0) {
                throw new DAOException("Libro no encontrado con id: " + libro.getId());
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate")) {
                throw new DAOException("El ISBN ya está registrado: " + libro.getIsbn(), e);
            }
            throw new DAOException("Error al actualizar libro: " + e.getMessage(), e);
        }
        return libro;
    }

    public void delete(Connection conn, Integer id) throws DAOException {
        String sql = "DELETE FROM libro WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new DAOException("Libro no encontrado con id: " + id);
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("foreign key")) {
                throw new DAOException("No se puede borrar el libro porque tiene préstamos registrados.", e);
            }
            throw new DAOException("Error al borrar libro: " + e.getMessage(), e);
        }
    }
}
