-- =============================================================================
-- Biblioteca - Gestión de Préstamos
-- Esquema de base de datos y datos de prueba
-- MySQL 8.0+
-- =============================================================================

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS biblioteca_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE biblioteca_db;

-- -----------------------------------------------------------------------------
-- Tablas
-- -----------------------------------------------------------------------------

CREATE TABLE autor (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL,
    nacionalidad    VARCHAR(60)  NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE categoria (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(60)  NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE libro (
    id                    INT AUTO_INCREMENT PRIMARY KEY,
    titulo                VARCHAR(200) NOT NULL,
    isbn                  VARCHAR(13)  NOT NULL UNIQUE,
    anio_publicacion      YEAR         NOT NULL,
    stock                 INT          NOT NULL DEFAULT 0,
    autor_id              INT          NOT NULL,
    CONSTRAINT chk_libro_anio CHECK (anio_publicacion >= 1450 AND anio_publicacion <= 2099),
    CONSTRAINT chk_libro_stock CHECK (stock >= 0),
    CONSTRAINT fk_libro_autor FOREIGN KEY (autor_id)
        REFERENCES autor(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE libro_categoria (
    libro_id        INT NOT NULL,
    categoria_id    INT NOT NULL,
    PRIMARY KEY (libro_id, categoria_id),
    CONSTRAINT fk_lc_libro FOREIGN KEY (libro_id)
        REFERENCES libro(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_lc_categoria FOREIGN KEY (categoria_id)
        REFERENCES categoria(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE socio (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL,
    email           VARCHAR(150) NOT NULL UNIQUE,
    fecha_alta      DATE         NOT NULL DEFAULT (CURRENT_DATE)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE prestamo (
    id                    INT AUTO_INCREMENT PRIMARY KEY,
    socio_id              INT          NOT NULL,
    libro_id              INT          NOT NULL,
    fecha_prestamo        DATETIME     NOT NULL DEFAULT (CURRENT_TIMESTAMP),
    fecha_devolucion      DATETIME     NULL,
    estado                ENUM('prestado','devuelto') NOT NULL DEFAULT 'prestado',
    CONSTRAINT fk_prestamo_socio FOREIGN KEY (socio_id)
        REFERENCES socio(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_prestamo_libro FOREIGN KEY (libro_id)
        REFERENCES libro(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- Índices
-- -----------------------------------------------------------------------------

CREATE INDEX idx_autor_nombre ON autor(nombre);
CREATE INDEX idx_categoria_nombre ON categoria(nombre);
CREATE INDEX idx_libro_titulo ON libro(titulo);
CREATE INDEX idx_libro_autor_id ON libro(autor_id);
CREATE INDEX idx_libro_categoria_categoria_id ON libro_categoria(categoria_id);
CREATE INDEX idx_socio_nombre ON socio(nombre);
CREATE INDEX idx_prestamo_socio_id ON prestamo(socio_id);
CREATE INDEX idx_prestamo_libro_id ON prestamo(libro_id);
CREATE INDEX idx_prestamo_estado ON prestamo(estado);
CREATE INDEX idx_prestamo_fecha_prestamo ON prestamo(fecha_prestamo);

-- -----------------------------------------------------------------------------
-- Datos de prueba
-- -----------------------------------------------------------------------------

-- Autores (6)
INSERT INTO autor (nombre, nacionalidad) VALUES
    ('Gabriel García Márquez', 'Colombiana'),
    ('Jorge Luis Borges', 'Argentina'),
    ('Isabel Allende', 'Chilena'),
    ('Mario Vargas Llosa', 'Peruana'),
    ('Paulo Coelho', 'Brasileña'),
    ('Haruki Murakami', 'Japonesa');

-- Categorías (7)
INSERT INTO categoria (nombre) VALUES
    ('Ficción'),
    ('Ciencia Ficción'),
    ('Historia'),
    ('Biografía'),
    ('Poesía'),
    ('Ensayo'),
    ('Fantasía');

-- Libros (15)
INSERT INTO libro (titulo, isbn, anio_publicacion, stock, autor_id) VALUES
    ('Cien años de soledad', '9780307474728', 1967, 5, 1),
    ('El amor en los tiempos del cólera', '9780307474735', 1985, 3, 1),
    ('Crónica de una muerte anunciada', '9780307474742', 1981, 7, 1),
    ('Ficciones', '9780811215407', 1944, 4, 2),
    ('El Aleph', '9780811215414', 1949, 2, 2),
    ('La casa de los espíritus', '9780307472772', 1982, 6, 3),
    ('Inés del alma mía', '9780307472789', 1991, 0, 3),
    ('La ciudad y los perros', '9780307472796', 1963, 8, 4),
    ('Conversación en La Catedral', '9780307472802', 1969, 3, 4),
    ('El alquimista', '9780062315007', 1988, 10, 5),
    ('Veronika decide morrer', '9780062315014', 1998, 4, 5),
    ('Norwegian Wood', '9780375704024', 1987, 6, 6),
    ('Kafka on the Shore', '9780375704031', 2002, 5, 6),
    ('1Q84', '9780307593313', 2009, 2, 6),
    ('El otoño del patriarca', '9780307474759', 1975, 1, 1);

-- Libro_Categoría (12+)
INSERT INTO libro_categoria (libro_id, categoria_id) VALUES
    (1, 1),  -- Cien años de soledad -> Ficción
    (1, 7),  -- Cien años de soledad -> Fantasía
    (2, 1),  -- El amor en los tiempos -> Ficción
    (3, 1),  -- Crónica -> Ficción
    (4, 1),  -- Ficciones -> Ficción
    (5, 1),  -- El Aleph -> Ficción
    (6, 1),  -- La casa de los espíritus -> Ficción
    (7, 1),  -- Inés del alma mía -> Ficción
    (8, 1),  -- La ciudad y los perros -> Ficción
    (9, 1),  -- Conversación en La Catedral -> Ficción
    (10, 1), -- El alquimista -> Ficción
    (10, 7), -- El alquimista -> Fantasía
    (11, 1), -- Veronika -> Ficción
    (12, 1), -- Norwegian Wood -> Ficción
    (13, 1), -- Kafka on the Shore -> Ficción
    (13, 7), -- Kafka on the Shore -> Fantasía
    (14, 1), -- 1Q84 -> Ficción
    (14, 2), -- 1Q84 -> Ciencia Ficción
    (15, 1), -- El otoño del patriarca -> Ficción
    (4, 5),  -- Ficciones -> Poesía
    (8, 3),  -- La ciudad y los perros -> Historia
    (9, 6);  -- Conversación en La Catedral -> Ensayo

-- Socios (8)
INSERT INTO socio (nombre, email, fecha_alta) VALUES
    ('Ana García López', 'ana.garcia@email.com', '2024-01-15'),
    ('Carlos Martínez Ruiz', 'carlos.martinez@email.com', '2024-02-20'),
    ('María Fernández Torres', 'maria.fernandez@email.com', '2024-03-10'),
    ('Pedro Sánchez Moreno', 'pedro.sanchez@email.com', '2024-04-05'),
    ('Laura Jiménez Díaz', 'laura.jimenez@email.com', '2024-05-12'),
    ('Diego Romero Castro', 'diego.romero@email.com', '2024-06-18'),
    ('Sofía Herrera Vega', 'sofia.herrera@email.com', '2024-07-22'),
    ('Andrés Morales Gil', 'andres.morales@email.com', '2024-08-30');

-- Préstamos (10)
INSERT INTO prestamo (socio_id, libro_id, fecha_prestamo, fecha_devolucion, estado) VALUES
    (1, 1, '2024-09-01 10:00:00', '2024-09-15 14:30:00', 'devuelto'),
    (1, 4, '2024-09-20 09:00:00', NULL, 'prestado'),
    (2, 10, '2024-10-01 11:00:00', '2024-10-14 16:00:00', 'devuelto'),
    (2, 12, '2024-10-15 08:30:00', NULL, 'prestado'),
    (3, 6, '2024-10-20 13:00:00', '2024-11-03 10:00:00', 'devuelto'),
    (3, 13, '2024-11-05 09:00:00', NULL, 'prestado'),
    (4, 2, '2024-11-01 15:00:00', NULL, 'prestado'),
    (5, 8, '2024-10-10 10:00:00', '2024-10-25 12:00:00', 'devuelto'),
    (5, 14, '2024-11-01 14:00:00', NULL, 'prestado'),
    (6, 3, '2024-09-15 11:00:00', '2024-09-30 09:00:00', 'devuelto');
