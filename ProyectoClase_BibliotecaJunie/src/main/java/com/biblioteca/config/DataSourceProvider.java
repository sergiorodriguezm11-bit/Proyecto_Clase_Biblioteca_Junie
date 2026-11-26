package com.biblioteca.config;

import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Proveedor singleton de DataSource con pool de conexiones DBCP2.
 * Lee la configuración desde variables de entorno.
 */
public class DataSourceProvider {

    private static volatile DataSource dataSource;

    private DataSourceProvider() {}

    public static DataSource getInstance() {
        if (dataSource == null) {
            synchronized (DataSourceProvider.class) {
                if (dataSource == null) {
                    dataSource = createDataSource();
                }
            }
        }
        return dataSource;
    }

    private static DataSource createDataSource() {
        String host = getEnvOrFail("DB_HOST");
        String port = getEnvOrFail("DB_PORT");
        String dbName = getEnvOrFail("DB_NAME");
        String user = getEnvOrFail("DB_USER");
        String password = getEnvOrFail("DB_PASSWORD");

        String url = String.format("jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8&connectionCollation=utf8mb4_unicode_ci",
                host, port, dbName);

        int maxTotal = Integer.parseInt(System.getenv().getOrDefault("DB_POOL_MAX", "10"));
        int minIdle = Integer.parseInt(System.getenv().getOrDefault("DB_POOL_MIN", "2"));
        int maxIdle = Integer.parseInt(System.getenv().getOrDefault("DB_POOL_IDLE", "5"));

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl(url);
        dataSource.setUsername(user);
        dataSource.setPassword(password);
        dataSource.setMaxTotal(maxTotal);
        dataSource.setMinIdle(minIdle);
        dataSource.setMaxIdle(maxIdle);
        dataSource.setInitialSize(minIdle);
        dataSource.setTestOnBorrow(true);
        dataSource.setValidationQuery("SELECT 1");

        return dataSource;
    }

    private static String getEnvOrFail(String key) {
        String value = System.getenv(key);
        if (value == null || value.isEmpty()) {
            throw new IllegalStateException(
                    "Variable de entorno obligatoria no definida: " + key);
        }
        return value;
    }

    /**
     * Reinicia el DataSource (útil para tests).
     */
    public static void reset() throws SQLException {
        if (dataSource != null) {
            if (dataSource instanceof BasicDataSource bds) {
                bds.close();
            }
            dataSource = null;
        }
    }
}
