package com.biblioteca.dao;

import com.biblioteca.config.DataSourceProvider;
import com.biblioteca.model.Prestamo;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la entidad Prestamo.
 */
public class PrestamoDAO {

    private DataSource dataSource;

    public PrestamoDAO() {
        this.dataSource = DataSourceProvider.getInstance();
    }

    public PrestamoDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Prestamo create(Prestamo prestamo) throws DAOException {
        String sql = "INSERT INTO prestamo (socio_id, libro_id, fecha_prestamo, estado) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, prestamo.getSocioId());
            ps.setInt(2, prestamo.getLibroId());
            ps.setTimestamp(3, Timestamp.valueOf(prestamo.getFechaPrestamo()));
            ps.setString(4, prestamo.getEstado());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) prestamo.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new DAOException("Error al crear préstamo: " + e.getMessage(), e);
        }
        return prestamo;
    }

    public Prestamo findById(Integer id) throws DAOException {
        String sql = "SELECT id, socio_id, libro_id, fecha_prestamo, fecha_devolucion, estado FROM prestamo WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSet(rs);
            }
        } catch (SQLException e) {
            throw new DAOException("Error al buscar préstamo: " + e.getMessage(), e);
        }
        return null;
    }

    public List<Prestamo> findAll() throws DAOException {
        String sql = "SELECT id, socio_id, libro_id, fecha_prestamo, fecha_devolucion, estado FROM prestamo ORDER BY fecha_prestamo DESC";
        List<Prestamo> prestamos = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) prestamos.add(mapResultSet(rs));
        } catch (SQLException e) {
            throw new DAOException("Error al listar préstamos: " + e.getMessage(), e);
        }
        return prestamos;
    }

    public List<Prestamo> findBySocioId(Integer socioId) throws DAOException {
        String sql = "SELECT id, socio_id, libro_id, fecha_prestamo, fecha_devolucion, estado FROM prestamo WHERE socio_id = ? ORDER BY fecha_prestamo DESC";
        List<Prestamo> prestamos = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, socioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) prestamos.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Error al buscar préstamos por socio: " + e.getMessage(), e);
        }
        return prestamos;
    }

    public List<Prestamo> findByEstado(String estado) throws DAOException {
        String sql = "SELECT id, socio_id, libro_id, fecha_prestamo, fecha_devolucion, estado FROM prestamo WHERE estado = ? ORDER BY fecha_prestamo DESC";
        List<Prestamo> prestamos = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, estado);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) prestamos.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Error al buscar préstamos por estado: " + e.getMessage(), e);
        }
        return prestamos;
    }

    public int countActivosBySocioId(Integer socioId) throws DAOException {
        String sql = "SELECT COUNT(*) FROM prestamo WHERE socio_id = ? AND estado = 'prestado'";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, socioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DAOException("Error al contar préstamos activos: " + e.getMessage(), e);
        }
        return 0;
    }

    public Prestamo update(Prestamo prestamo) throws DAOException {
        String sql = "UPDATE prestamo SET fecha_devolucion = ?, estado = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, prestamo.getFechaDevolucion() != null ? Timestamp.valueOf(prestamo.getFechaDevolucion()) : null);
            ps.setString(2, prestamo.getEstado());
            ps.setInt(3, prestamo.getId());
            if (ps.executeUpdate() == 0) {
                throw new DAOException("Préstamo no encontrado con id: " + prestamo.getId());
            }
        } catch (SQLException e) {
            throw new DAOException("Error al actualizar préstamo: " + e.getMessage(), e);
        }
        return prestamo;
    }

    public void delete(Integer id) throws DAOException {
        String sql = "DELETE FROM prestamo WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new DAOException("Préstamo no encontrado con id: " + id);
            }
        } catch (SQLException e) {
            throw new DAOException("Error al borrar préstamo: " + e.getMessage(), e);
        }
    }

    private Prestamo mapResultSet(ResultSet rs) throws SQLException {
        Prestamo prestamo = new Prestamo();
        prestamo.setId(rs.getInt("id"));
        prestamo.setSocioId(rs.getInt("socio_id"));
        prestamo.setLibroId(rs.getInt("libro_id"));
        prestamo.setFechaPrestamo(rs.getTimestamp("fecha_prestamo").toLocalDateTime());
        Timestamp devolucion = rs.getTimestamp("fecha_devolucion");
        prestamo.setFechaDevolucion(devolucion != null ? devolucion.toLocalDateTime() : null);
        prestamo.setEstado(rs.getString("estado"));
        return prestamo;
    }

    // --- Métodos con Connection (transacciones) ---

    public Prestamo create(Connection conn, Prestamo prestamo) throws DAOException {
        String sql = "INSERT INTO prestamo (socio_id, libro_id, fecha_prestamo, estado) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, prestamo.getSocioId());
            ps.setInt(2, prestamo.getLibroId());
            ps.setTimestamp(3, Timestamp.valueOf(prestamo.getFechaPrestamo()));
            ps.setString(4, prestamo.getEstado());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) prestamo.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new DAOException("Error al crear préstamo: " + e.getMessage(), e);
        }
        return prestamo;
    }

    public Prestamo findById(Connection conn, Integer id) throws DAOException {
        String sql = "SELECT id, socio_id, libro_id, fecha_prestamo, fecha_devolucion, estado FROM prestamo WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSet(rs);
            }
        } catch (SQLException e) {
            throw new DAOException("Error al buscar préstamo: " + e.getMessage(), e);
        }
        return null;
    }

    public Prestamo update(Connection conn, Prestamo prestamo) throws DAOException {
        String sql = "UPDATE prestamo SET fecha_devolucion = ?, estado = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, prestamo.getFechaDevolucion() != null ? Timestamp.valueOf(prestamo.getFechaDevolucion()) : null);
            ps.setString(2, prestamo.getEstado());
            ps.setInt(3, prestamo.getId());
            if (ps.executeUpdate() == 0) {
                throw new DAOException("Préstamo no encontrado con id: " + prestamo.getId());
            }
        } catch (SQLException e) {
            throw new DAOException("Error al actualizar préstamo: " + e.getMessage(), e);
        }
        return prestamo;
    }

    public int countActivosBySocioId(Connection conn, Integer socioId) throws DAOException {
        String sql = "SELECT COUNT(*) FROM prestamo WHERE socio_id = ? AND estado = 'prestado'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, socioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DAOException("Error al contar préstamos activos: " + e.getMessage(), e);
        }
        return 0;
    }
}
