-- Ejecutar conectado a la base que se desea reparar. Este archivo no se carga al iniciar Compose.
-- La primera ejecución es solo una vista previa. Revise las filas antes de activar la reparación.
SET @apply_utf8_mojibake_repair = COALESCE(@apply_utf8_mojibake_repair, 0);

SELECT DATABASE() AS base_objetivo, @apply_utf8_mojibake_repair AS aplicar_reparacion;

DROP TEMPORARY TABLE IF EXISTS utf8_mojibake_candidates;
CREATE TEMPORARY TABLE utf8_mojibake_candidates (
    table_name      VARCHAR(32) NOT NULL,
    record_id       INT NOT NULL,
    column_name     VARCHAR(32) NOT NULL,
    original_value  TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    repaired_value  TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    PRIMARY KEY (table_name, record_id, column_name)
) ENGINE=InnoDB;

INSERT INTO utf8_mojibake_candidates
    (table_name, record_id, column_name, original_value, repaired_value)
SELECT text_values.table_name,
       text_values.record_id,
       text_values.column_name,
       text_values.original_value,
       CONVERT(CAST(CONVERT(text_values.original_value USING latin1) AS BINARY) USING utf8mb4)
FROM (
    SELECT 'autor' AS table_name, id AS record_id, 'nombre' AS column_name, nombre AS original_value FROM autor
    UNION ALL
    SELECT 'autor', id, 'nacionalidad', nacionalidad FROM autor
    UNION ALL
    SELECT 'categoria', id, 'nombre', nombre FROM categoria
    UNION ALL
    SELECT 'libro', id, 'titulo', titulo FROM libro
    UNION ALL
    SELECT 'socio', id, 'nombre', nombre FROM socio
) AS text_values
WHERE HEX(text_values.original_value) REGEXP 'C3(8[2-9A-F]|9[0-9A-F]|A[0-9A-F])C2[89AB][0-9A-F]'
  AND HEX(text_values.original_value) NOT REGEXP
      '^([0-9A-F]{2})*(C[4-9A-F][89AB][0-9A-F]|D[0-9-F][89AB][0-9A-F]|E[0-9-F][0-9A-F]{4}|F[0-4][0-9A-F]{6})'
  AND BINARY CONVERT(CAST(CONVERT(text_values.original_value USING latin1) AS BINARY) USING utf8mb4)
      <> BINARY text_values.original_value;

-- Revise estos candidatos y sus valores reparados antes de activar la bandera de aplicación.
SELECT table_name, record_id, column_name, original_value, repaired_value
FROM utf8_mojibake_candidates
ORDER BY table_name, record_id, column_name;

-- La tabla puede quedar vacía tras una vista previa; solo las filas candidatas se respaldan al aplicar.
CREATE TABLE IF NOT EXISTS utf8_mojibake_repair_backup (
    backup_id       BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    backed_up_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    table_name      VARCHAR(32) NOT NULL,
    record_id       INT NOT NULL,
    column_name     VARCHAR(32) NOT NULL,
    original_value  TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    repaired_value  TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    PRIMARY KEY (backup_id),
    UNIQUE KEY uq_utf8_mojibake_source (table_name, record_id, column_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

START TRANSACTION;

INSERT IGNORE INTO utf8_mojibake_repair_backup
    (table_name, record_id, column_name, original_value, repaired_value)
SELECT table_name, record_id, column_name, original_value, repaired_value
FROM utf8_mojibake_candidates
WHERE @apply_utf8_mojibake_repair = 1;

UPDATE autor AS target
JOIN utf8_mojibake_candidates AS candidate
  ON candidate.table_name = 'autor'
 AND candidate.column_name = 'nombre'
 AND candidate.record_id = target.id
SET target.nombre = candidate.repaired_value
WHERE @apply_utf8_mojibake_repair = 1
  AND BINARY target.nombre = BINARY candidate.original_value;

UPDATE autor AS target
JOIN utf8_mojibake_candidates AS candidate
  ON candidate.table_name = 'autor'
 AND candidate.column_name = 'nacionalidad'
 AND candidate.record_id = target.id
SET target.nacionalidad = candidate.repaired_value
WHERE @apply_utf8_mojibake_repair = 1
  AND BINARY target.nacionalidad = BINARY candidate.original_value;

UPDATE categoria AS target
JOIN utf8_mojibake_candidates AS candidate
  ON candidate.table_name = 'categoria'
 AND candidate.column_name = 'nombre'
 AND candidate.record_id = target.id
SET target.nombre = candidate.repaired_value
WHERE @apply_utf8_mojibake_repair = 1
  AND BINARY target.nombre = BINARY candidate.original_value;

UPDATE libro AS target
JOIN utf8_mojibake_candidates AS candidate
  ON candidate.table_name = 'libro'
 AND candidate.column_name = 'titulo'
 AND candidate.record_id = target.id
SET target.titulo = candidate.repaired_value
WHERE @apply_utf8_mojibake_repair = 1
  AND BINARY target.titulo = BINARY candidate.original_value;

UPDATE socio AS target
JOIN utf8_mojibake_candidates AS candidate
  ON candidate.table_name = 'socio'
 AND candidate.column_name = 'nombre'
 AND candidate.record_id = target.id
SET target.nombre = candidate.repaired_value
WHERE @apply_utf8_mojibake_repair = 1
  AND BINARY target.nombre = BINARY candidate.original_value;

COMMIT;

-- MySQL no admite ALTER DATABASE en PREPARE: ejecute esta sentencia por separado
-- solo después de revisar los candidatos y confirmar el respaldo cuando la bandera sea 1.
SELECT IF(
    @apply_utf8_mojibake_repair = 1,
    'Aplicacion realizada; ejecute ALTER DATABASE CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci por separado.',
    'Vista previa: la collation de la base no se modifico.'
) AS siguiente_paso;