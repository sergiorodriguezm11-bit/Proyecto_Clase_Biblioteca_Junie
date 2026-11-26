# AI Workflow - Registro de Ingeniería

## Prompts Clave Dados a Junie

### 1. Planificación Inicial
- **Prompt:** Planificar una aplicación web full-stack en Java con JDBC, Servlets, Docker Compose, para gestión de biblioteca.
- **Resultado:** Plan completo por 9 fases con checklist, modelo ER, contrato API, árbol de proyecto, estrategia de testing y decisiones arquitectónicas.

### 2. Implementación
- **Prompt:** Implementar según el plan.
- **Resultado:** Aplicación completa con todas las capas (config, model, dao, dto, mapper, service, controller, util, frontend).

## Decisiones Arquitectónicas Tomadas

### JSON Library: Jackson
- **Decisión:** Jackson sobre Gson.
- **Justificación:** Estándar en Jakarta EE/Tomcat, mejor rendimiento con streams grandes, anotación `@JsonProperty` para mapeo explícito.

### Tests de Integración: Testcontainers con MySQL
- **Decisión:** Testcontainers con MySQL 8 sobre H2.
- **Justificación:** Prueba contra la misma BD que producción, compatibilidad real de SQL y tipos de datos.

### Transacciones entre DAOs
- **Decisión:** Patrón de ConnectionProvider en el servicio.
- **Justificación:** El servicio obtiene una Connection del DataSource, la pasa a los DAOs durante la transacción, y gestiona commit/rollback. Los DAOs aceptan la Connection como parámetro opcional.

### Manejo Centralizado de Errores
- **Decisión:** `ErrorHandlingFilter` como Jakarta Filter.
- **Justificación:** Intercepta todas las peticiones, captura excepciones personalizadas y las convierte en respuestas JSON uniformes.

### Git Strategy
- **Decisión:** Trabajo en `main` con commits atómicos pequeños.
- **Justificación:** Proyecto de un solo desarrollador, las ramas añadirían complejidad innecesaria.

### Docker Network
- **Decisión:** Red bridge privada (`biblioteca-network`).
- **Justificación:** Solo se publican los puertos 8080 (app) y 8081 (adminer). El puerto 3306 de MySQL NO se publica al host.

### Imagen MySQL
- **Decisión:** `mysql:8.0.41-debian` (174 MB) sobre `mysql:8.0.41` (246 MB).
- **Justificación:** La versión Debian es más ligera al no incluir mysqlshell, que infla la imagen sin aportar valor para este proyecto.

## Codificación UTF-8 y reparación de datos

### Causa raíz confirmada
- `GET /api/socios` devolvía nombres con mojibake real desde la API; no era solo un problema de renderizado del navegador.
- Las tablas usaban `utf8mb4`, pero la sesión MySQL que importó los literales informaba `latin1` para cliente, conexión y resultados. `db/init.sql` no fijaba `SET NAMES`, por lo que texto español podía guardarse ya doblemente codificado.
- `MYSQL_DATABASE` crea la base antes de que `CREATE DATABASE IF NOT EXISTS` del script pueda establecer su collation. El servidor/base efectivos usaban `utf8mb4_0900_ai_ci`, aunque las tablas declaraban `utf8mb4_unicode_ci`.
- `mysql_data` es persistente: los cambios preventivos en `init.sql` no corrigen filas existentes ni vuelven a importar los datos.

### Prevención aplicada
- `db/init.sql` fija `SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci` antes del DDL/DML; el servicio MySQL configura los mismos defaults de servidor.
- JDBC declara `useUnicode=true`, `characterEncoding=UTF-8` y `connectionCollation=utf8mb4_unicode_ci`, conservando las variables de entorno existentes.
- `ErrorHandlingFilter` establece UTF-8 para solicitud y respuesta antes de los servlets; `api.js` envía JSON con `charset=UTF-8`.
- La etapa runtime del `Dockerfile` configura `LANG`/`LC_ALL` como `C.UTF-8` y `JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8`. Tomcat 10.1 conserva su `URIEncoding=UTF-8` predeterminado; HTML y Maven ya estaban en UTF-8.

### Reparación manual de la base persistente
1. Detenga las escrituras de la aplicación y haga un respaldo externo de la base objetivo antes de la migración. No elimine ni recree `mysql_data`.
2. Conéctese a la base correcta con charset de cliente `utf8mb4` y ejecute `db/repair-utf8-mojibake.sql` tal como está: la bandera predeterminada `@apply_utf8_mojibake_repair` es `0`, así que presenta candidatos y valores reparados sin actualizar filas. La vista previa puede crear la tabla de respaldo vacía.
3. Revise cada candidato y confirme que el valor propuesto es el esperado. El script limita la detección a firmas de doble codificación confirmadas, excluye valores con caracteres fuera de Latin-1 y considera solo `autor.nombre`, `autor.nacionalidad`, `categoria.nombre`, `libro.titulo` y `socio.nombre`; no convierte correo, ISBN ni estado de préstamo.
4. Solo después de aprobar la vista previa, establezca `@apply_utf8_mojibake_repair = 1` en esa sesión y vuelva a ejecutar el script. Se guardan los valores originales y propuestos en `utf8_mojibake_repair_backup`; las actualizaciones son transaccionales y solo sustituyen filas que aún coinciden byte por byte con la vista previa.
5. Si la bandera era `1` y la reparación terminó correctamente, ejecute por separado `ALTER DATABASE CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;` con la base objetivo seleccionada. MySQL no permite ejecutar `ALTER DATABASE` mediante `PREPARE`; el script imprime la instrucción y no la ejecuta automáticamente.
6. Verifique los valores legibles y sus `HEX`, el respaldo y la búsqueda/lectura por API. El script es manual y no se monta en la ruta de inicialización. Al detener Compose, use `docker compose down` sin `-v` para conservar `mysql_data`.

### Verificación realizada
- `mvn clean test` terminó con `BUILD SUCCESS`; compiló 45 fuentes y no hay archivos de prueba bajo `src/test`. `docker compose build app` también terminó correctamente.
- En una instancia Compose aislada se simularon valores doblemente codificados; la vista previa mostró antes/después, el script corrigió las filas verificadas y el respaldo conservó los bytes anteriores. Un valor sintético con emoji quedó excluido y la vista previa posterior no encontró candidatos.
- La API devolvió `Ana García López`, la búsqueda con `cólera` encontró el libro, el POST/GET temporal con `á`, `ñ` y `ü` conservó sus bytes UTF-8 y el registro se eliminó después. Los errores JSON y las páginas HTML respondieron con charset UTF-8; servidor, base, tablas y sesión de prueba usaron `utf8mb4`.
- Estas comprobaciones se hicieron en un volumen Compose desechable; no se ejecutó la migración sobre el `mysql_data` persistente del usuario. Aplique el procedimiento manual anterior solo después de respaldar y revisar sus candidatos.

## Alternativas Descartadas

1. **H2 en modo MySQL** — Descartado por diferencias en funciones de fecha, auto_increment, etc.
2. **Spring Data / Hibernate** — Descartado porque el objetivo es JDBC explícito.
3. **phpMyAdmin** — Descartado por tamaño (~250 MB vs ~50 MB de Adminer).
4. **SPA con una sola index.html** — Descartado a favor de páginas HTML separadas por entidad para simplicidad.

## Prompts Dados a Junie

/plan

# ROL Y OBJETIVO
Actúa como arquitecto de software senior en Java. Quiero que PLANIFIQUES (sin escribir todavía el código completo) una aplicación web full-stack en Java con persistencia en base de datos relacional, desplegada con Docker Compose. Es una práctica del módulo "Acceso a Datos" de 2.º CFGS DAM y se evalúa con una rúbrica concreta, así que el plan debe cubrir cada punto de ella.

Tu respuesta debe ser un plan por fases, con checklist, ficheros a crear en cada fase, decisiones tomadas y criterio de "hecho" de cada fase. No empieces a implementar hasta que yo apruebe el plan. Si algo es ambiguo, haz una pregunta concreta en vez de asumirlo.

# 1. STACK TECNOLÓGICO (fijo, no cambiar)
- Java 17, Maven (un solo módulo).
- Servlets Jakarta (Jakarta EE 10) empaquetados como .war, desplegados en Tomcat 10.1.
- Acceso a datos con JDBC puro: PreparedStatement SIEMPRE, nunca Statement con concatenación, y pool de conexiones Apache Commons DBCP2 con DataSource.
- Base de datos: MySQL 8 (imagen oficial).
- JSON con Jackson (o Gson, elige uno y justifícalo).
- Frontend: HTML5 semántico, CSS3 y JavaScript moderno (ES modules, fetch/async-await). Sin frameworks pesados.
- Tests: JUnit 5 + Mockito. Para los tests de integración del DAO, Testcontainers con MySQL (o H2 en modo MySQL si Testcontainers no es viable; explícame la alternativa).
- Control de versiones: Git.

# 2. TEMÁTICA Y MODELO DE DATOS: GESTIÓN DE BIBLIOTECA
Diseña un modelo entidad-relación normalizado (3FN) con estas tablas:
- autor (id, nombre, nacionalidad)
- categoria (id, nombre UNIQUE)
- libro (id, titulo, isbn UNIQUE, anio_publicacion, stock, autor_id FK)  -> relación 1:N con autor
- libro_categoria (libro_id FK, categoria_id FK, PK compuesta)  -> relación N:M entre libro y categoria
- socio (id, nombre, email UNIQUE, fecha_alta)
- prestamo (id, socio_id FK, libro_id FK, fecha_prestamo, fecha_devolucion NULL, estado)

Requisitos del modelo:
- Integridad referencial con ON DELETE / ON UPDATE explícitos y justificados (por ejemplo: borrar un autor con libros se RESTRINGE; borrar un libro elimina sus filas en libro_categoria con CASCADE; no se puede borrar un socio con préstamos activos).
- Restricciones CHECK o validaciones equivalentes (stock >= 0, anio_publicacion razonable).
- Índices en claves foráneas y columnas de búsqueda habituales.
- Explícame en el plan el porqué de cada decisión de borrado en cascada o restrictivo.

# 3. ARQUITECTURA POR CAPAS (separación estricta)
Flujo obligatorio: Controlador -> Servicio -> DAO -> BD. Ninguna capa salta a otra.

Estructura de paquetes propuesta (confírmala o mejórala en el plan):
- controller (Servlets)
- service (interfaces + implementaciones)
- dao (interfaces + implementaciones JDBC)
- model (entidades que mapean tablas)
- dto (objetos de transferencia: request/response)
- mapper (entidad <-> DTO)
- exception (excepciones personalizadas: RecursoNoEncontradoException, ValidacionException, ConflictoException, DaoException)
- config (DataSourceProvider, lectura de variables de entorno)
- util (helpers JSON, manejo de errores)

Reglas por capa:
- Controlador: solo gestiona HTTP. Lee la petición, valida el formato de entrada, llama al servicio, serializa JSON y devuelve el código HTTP correcto. Cero lógica de negocio y cero SQL.
- Servicio: reglas de negocio, validaciones complejas y transacciones (commit/rollback). Recibe y devuelve DTOs, nunca entidades hacia fuera.
- DAO: único lugar con SQL. Un DAO por entidad con CRUD completo (create, findById, findAll, update, delete) más las consultas específicas necesarias. try-with-resources en Connection, PreparedStatement y ResultSet. Traduce SQLException a excepciones propias (por ejemplo, clave duplicada -> ConflictoException).
- DTOs: obligatorios para toda transferencia entre capas. Las entidades de BD no se exponen jamás en la API.
- Gestión transaccional: las operaciones que tocan varias tablas (por ejemplo, crear un préstamo = insertar préstamo + decrementar stock) deben ejecutarse en una única transacción con setAutoCommit(false), commit y rollback ante error. Planifica cómo compartir la Connection entre DAOs dentro de una transacción sin romper el desacoplamiento.

# 4. API REST (contrato)
Define en el plan todos los endpoints bajo /api/ con método, ruta, cuerpo, respuesta y códigos HTTP:
- /api/autores, /api/categorias, /api/libros, /api/socios: CRUD completo (GET lista, GET por id, POST, PUT, DELETE).
- /api/libros con filtros por query param (autor, categoria, título parcial).
- /api/prestamos: POST (crear préstamo), PUT /api/prestamos/{id}/devolucion (devolver), GET (listar y filtrar por socio o estado).

Códigos HTTP semánticos obligatorios: 200 OK, 201 Created (con cabecera Location), 204 No Content en borrados, 400 Bad Request (validación), 404 Not Found, 409 Conflict (duplicados o restricciones de negocio), 500 Internal Server Error. Todas las respuestas de error usan un mismo formato JSON: { "timestamp", "status", "error", "mensaje", "detalles": [] }. Prevé un manejador de errores centralizado (por ejemplo, un Filter o una clase base de Servlet) para no repetir try/catch.

Reglas de negocio mínimas a implementar en el servicio:
- No se puede prestar un libro sin stock.
- Un socio no puede tener más de 3 préstamos activos.
- No se puede devolver un préstamo ya devuelto.
- ISBN y email únicos (error 409 con mensaje claro).
- Validación de datos en servidor: campos obligatorios, longitudes, formato de email, rango de año.

# 5. CONFIGURACIÓN EXTERNALIZADA (sin credenciales en el código)
- PROHIBIDO hardcodear usuario, contraseña, host, puerto o URL de la BD en cualquier fichero Java o de recursos.
- La clase config del DataSource lee todo con System.getenv(): DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD, y parámetros del pool (DB_POOL_MAX, etc.) con valores por defecto sensatos solo para lo no sensible.
- Si falta una variable obligatoria, la app debe fallar al arrancar con un mensaje claro.
- Crea .env.example (con valores de ejemplo, sin secretos reales) y .env en .gitignore.
- Docker Compose inyecta las variables desde .env tanto a db como a app.

# 6. BASE DE DATOS: init.sql
- Un único init.sql con DDL (CREATE TABLE con todas las restricciones) y DML (datos de prueba realistas: al menos 5 autores, 6 categorías, 15 libros, 8 socios, 10 préstamos con estados variados, y varias filas en libro_categoria).
- Se monta en /docker-entrypoint-initdb.d/ del contenedor MySQL.
- Debe ser idempotente en lo posible y compatible con MySQL 8 (utf8mb4).

# 7. DOCKER Y ORQUESTACIÓN
Dockerfile multi-stage:
- Etapa 1 (build): imagen maven con JDK 17, copiar el pom.xml primero y ejecutar dependency:go-offline para aprovechar la caché de capas, luego copiar src y compilar el .war saltando los tests (-DskipTests) en esta etapa.
- Etapa 2 (runtime): imagen tomcat:10.1-jdk17, borrar las webapps por defecto, copiar el .war como ROOT.war, exponer 8080, ejecutar con un usuario no root si es viable.

docker-compose.yml con estos servicios:
- db: mysql:8, variables MYSQL_* desde .env, volumen NOMBRADO para /var/lib/mysql (persistencia), init.sql montado en solo lectura en /docker-entrypoint-initdb.d/, healthcheck con mysqladmin ping (interval, timeout, retries y start_period razonables).
- app: build desde el Dockerfile, depends_on con condition: service_healthy sobre db, variables de entorno desde .env, puerto 8080 publicado, reinicio razonable.
- adminer (o phpMyAdmin): depends_on db, puerto publicado (por ejemplo 8081).
- Red interna propia (bridge) a la que se conectan los tres servicios; solo se publican los puertos estrictamente necesarios (la BD no se publica al host salvo que lo justifiques).
- Sección volumes con el volumen nombrado declarado.
- Demostración de persistencia: el plan debe incluir los pasos para comprobar que los datos sobreviven a docker compose down y up (y que solo se pierden con down -v).

# 8. FRONTEND (src/main/webapp)
- Estructura: index.html más páginas o secciones por entidad (libros, autores, categorías, socios, préstamos), css/ y js/ separados. Cero Java o SQL en las vistas (nada de JSP con scriptlets).
- JS moderno: módulos ES, async/await, fetch contra /api/..., un módulo api.js que centralice las peticiones y el manejo de errores.
- Funcionalidad: listar, crear, editar y borrar desde la interfaz para todas las entidades principales; crear y devolver préstamos; filtros de búsqueda.
- UX: diseño responsivo (CSS Grid/Flexbox, mobile-first), estados de carga (spinner o "Cargando..."), mensajes de error claros ante fallos de validación (mostrar los detalles que devuelve la API) o de servidor, confirmación antes de borrar, navegación fluida, sin errores en la consola del navegador.
- HTML semántico (header, nav, main, section, footer, form con label asociados) y accesibilidad básica.

# 9. TESTING (JUnit 5 + Mockito)
Planifica la batería de pruebas:
- Tests de integración de la capa DAO contra una BD real de pruebas (Testcontainers MySQL cargando init.sql o un esquema de test): CRUD completo de cada DAO, más casos límite: clave única duplicada (ISBN, email), registro inexistente (findById/update/delete), violación de FK (borrar autor con libros), valores nulos o fuera de rango.
- Tests unitarios de la capa Servicio con Mockito (DAOs simulados): camino feliz y todos los casos de error de las reglas de negocio (sin stock, límite de 3 préstamos, devolución duplicada, validaciones), verificando también que se hace rollback ante fallo.
- Uso de @ParameterizedTest donde aporte valor, nombres de test descriptivos (@DisplayName) y estructura Arrange-Act-Assert.
- Los tests deben ejecutarse con mvn test y pasar en limpio. Indica en el plan cómo se ejecutan y cuál es la cobertura aproximada que esperas.

# 10. GIT Y FLUJO DE TRABAJO
- Repositorio con .gitignore (target/, .env, .idea/, *.war, etc.) y .env.example.
- Historial de commits ATÓMICOS y descriptivos (un cambio lógico por commit, mensajes en imperativo tipo "Añadir DAO de libros con CRUD JDBC"). El plan debe proponer la lista de commits previstos, en orden, asociados a cada fase.
- Ramas: trabajo en main con commits pequeños, o una rama por fase si lo ves mejor (justifícalo).

# 11. DOCUMENTACIÓN
README.md profesional con:
- Descripción del proyecto y stack.
- Diagrama Entidad-Relación (en Mermaid dentro del README) y esquema de la arquitectura de contenedores (también Mermaid).
- Explicación de la arquitectura por capas y estructura de carpetas.
- Tabla de variables de entorno (nombre, descripción, ejemplo).
- Instrucciones paso a paso: clonar, copiar .env.example a .env, ajustar valores, ejecutar docker compose up --build -d, comprobar healthchecks, acceder a la app y a Adminer.
- Credenciales de prueba, puertos expuestos, tabla de endpoints de la API y manual breve de uso.
- Cómo ejecutar los tests y cómo comprobar la persistencia.

AI_WORKFLOW.md (registro de ingeniería) con secciones:
- Prompts clave dados a Junie (incluido este /plan) y qué produjo cada uno.
- Decisiones arquitectónicas tomadas y alternativas descartadas.
- Errores o malas prácticas de Junie que corregí manualmente (con ejemplos antes/después).
- Refactorizaciones realizadas por mí.
Deja la plantilla preparada y ve proponiendo en cada fase qué anotar, pero el contenido de "errores corregidos" lo rellenaré yo con lo que ocurra de verdad.

# 12. FASES DE EJECUCIÓN (orden obligatorio)
Estructura el plan en estas fases, cada una con sus ficheros, pasos, criterio de aceptación y commit asociado:
1. Esqueleto del proyecto: pom.xml, estructura de paquetes, .gitignore, .env.example.
2. Base de datos: init.sql y docker-compose mínimo (solo db + healthcheck) verificando que arranca con los datos.
3. Capa de configuración y DAO: DataSource, excepciones, entidades, DAOs con CRUD y sus tests de integración.
4. Capa de Servicio y DTOs: reglas de negocio, transacciones y sus tests unitarios con Mockito.
5. Capa de Control/API REST: Servlets, manejo centralizado de errores y códigos HTTP.
6. Frontend: vistas, estilos y JS.
7. Dockerfile multi-stage y docker-compose completo (app + db + adminer), verificación de arranque ordenado y de persistencia.
8. Documentación: README.md y AI_WORKFLOW.md.
9. Revisión final contra la rúbrica.

# 13. RESTRICCIONES (lo que NO debes hacer)
- No concatenar SQL con entradas del usuario bajo ninguna circunstancia.
- No exponer entidades en la API ni mezclar capas.
- No hardcodear credenciales ni URLs.
- No usar frameworks que oculten el acceso a datos (Spring Data, Hibernate) porque el objetivo es JDBC explícito.
- No dejar código muerto, comentarios de relleno ni dependencias que no se usen.
- No implementar nada antes de que apruebe el plan.

# 14. FORMATO DE LA RESPUESTA
1. Resumen de decisiones de arquitectura (breve).
2. Árbol completo de carpetas y ficheros previstos.
3. Modelo E-R detallado (tablas, columnas, tipos, restricciones).
4. Contrato de la API (tabla de endpoints).
5. Plan por fases con checklist, y la lista de commits.
6. Riesgos y puntos donde necesitas mi decisión.
7. Checklist final de autoevaluación contra la rúbrica (6 objetivos, 10 puntos).

--

/plan Revisa el proyecto y elabora el plan para completar o corregir estos tres puntos ajustándonos al enunciado de la práctica y su rúbrica:

Batería de pruebas con JUnit 5 en dao y service:
Revisa pom.xml y planifica clases de prueba con JUnit 5 (y Mockito si corresponde) dentro de src/test/java/com/biblioteca/dao/ y src/test/java/com/biblioteca/service/.
Cubre tanto el camino feliz (CRUD y flujos principales) como casos límite y errores esperados (por ejemplo: clave duplicada, registro inexistente, validaciones y reglas de negocio de préstamos/stock).
Incluye la comprobación de que mvn test compile y ejecute todas las pruebas sin errores.

Auditoría y mejora de AI_WORKFLOW.md, README.md y .env.example:
Revisa AI_WORKFLOW.md para documentar los prompts clave dados a Junie, las decisiones arquitectónicas, las refactorizaciones/correcciones manuales (incluida la investigación y reparación del problema de codificación UTF-8 / tildes y ñ) y el trabajo de pruebas.
Revisa README.md para que incluya diagrama Entidad-Relación, esquema de arquitectura de contenedores, pasos para crear .env desde .env.example, despliegue con docker compose up --build -d, credenciales de prueba, puertos expuestos y manual breve de uso.
Revisa .env.example y .gitignore para confirmar que no haya credenciales reales hardcodeadas ni filtradas y que .env, target/ y .git/ queden excluidos según corresponda.

Preparación del empaquetado final (sin target/ ni .git/):
Verifica docker-compose.yml y Dockerfile (healthcheck en la BD, depends_on con service_healthy, red interna, variables de entorno y volumen nombrado persistente).
Detalla los pasos exactos para generar el .zip de entrega excluyendo target/ y .git/ y comprobar que los tests pasan.

No modifiques archivos todavía hasta presentar el plan detallado con los cambios previstos por archivo.

--

/plan

El proyecto no compila. Planifica SOLO esto, sin implementar hasta que apruebe:

Haz unchecked (RuntimeException) ConflictoException, RecursoNoEncontradoException, ServicioException y ValidacionException: los controladores solo declaran throws IOException.
Revisa los catch de ErrorHandlingFilter tras ese cambio.
JsonUtil solo tiene fromJson(String, Class), pero los controladores le pasan byte[]. Añade la sobrecarga.
Falta jackson-datatype-jsr310 en el pom.xml (JsonUtil usa JavaTimeModule).
En los DAOs, los métodos con Connection (ej. LibroDAO.decrementStock) llaman a dataSource.getConnection() fuera del try. Unifica el patrón en los 6 DAOs.
En el pom.xml, <n> debe ser <name>.

No cambies el stack ni los paquetes. Dime qué ficheros tocas y cómo verificas con mvn clean compile.

--


/plan

Planifica SOLO esto, sin implementar hasta que apruebe:

Los servlets de autores, categorías, libros y socios están mapeados a "/api/xxx" sin "/*", así que "/api/libros/5" da 404. Corrígelo en los 4 controladores (PrestamoController ya lo tiene bien).
Crea un helper común que extraiga el id de getPathInfo() y lance ValidacionException (400) si falta o no es numérico. Úsalo en todos los controladores.
En el Dockerfile, cambia maven:3.9-jdk-17 por maven:3.9-eclipse-temurin-17 y arregla USER tomcat (esa imagen no tiene ese usuario).

Dime qué ficheros tocas y cómo verificas.

--

/plan Investiga y resuelve el problema de codificación de caracteres (tildes, ñ y caracteres especiales en español, como "Ana GarcÃa LÃ³pez") en toda la aplicación web Java desplegada en Docker Compose.

Revisa de extremo a extremo:
Base de datos y script de inicialización (db/init.sql y servicio db en docker-compose.yml): verifica charset/collation (utf8mb4 / utf8mb4_unicode_ci o utf8mb4_general_ci) en base de datos, tablas, columnas y conexión del cliente/init.
Conexión JDBC / pool DBCP2 (DataSourceProvider y variables de entorno): asegúrate de que la URL JDBC y las propiedades fuercen UTF-8 (p. ej. characterEncoding=UTF-8, useUnicode=true y collation/charset coherente con MySQL 8) sin romper las variables de entorno existentes.
Capa Control / API y filtros (Servlets, ErrorHandlingFilter, JsonUtil, DTOs/Mappers): verifica que las peticiones y respuestas HTTP usen UTF-8 tanto en lectura de cuerpos/parámetros como en cabeceras Content-Type (application/json; charset=UTF-8 o text/html; charset=UTF-8).
Capa de presentación (src/main/webapp/*.html, js/api.js, css/styles.css) y compilación Maven/Docker (pom.xml, Dockerfile): verifica <meta charset="UTF-8">, codificación real UTF-8 sin BOM o mal guardada en los archivos fuente/SQL/HTML, y encoding UTF-8 en Maven/JVM/Tomcat si aplica.
Pruebas y documentación: comprueba si los tests existentes compilan y pasan, y documenta el diagnóstico y la solución aplicada en AI_WORKFLOW.md si corresponde.

No modifiques archivos todavía hasta presentar el plan detallado con la causa raíz detectada y los cambios exactos por archivo.

## Errores Corregidos Manualmente

-Cambio de version manualmente en el pom, debido a que junie no podia hacer test con Docker, surgio un problema con un test que no se podia validar y daba error con una prueba manual, esto se debia al uso de una version anterior en el pom (linea 24).