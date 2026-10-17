package com.daytona.persistence;

import com.daytona.core.Log;

import com.daytona.config.AppConfig;

import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Conexión única a la base de datos + migraciones del esquema.
 * <p>
 * Por defecto usa SQLite en un archivo local (portable y sin red). Si se configuró
 * PostgreSQL y no responde, cae a SQLite; si tampoco puede, usa SQLite en memoria
 * para que el juego nunca deje de arrancar.
 */
public final class Database implements AutoCloseable {

    private static final Log LOG = Log.of("db");

    private final Connection connection;
    private final SqlDialect dialect;
    private final String description;

    private Database(Connection connection, SqlDialect dialect, String description) {
        this.connection = connection;
        this.dialect = dialect;
        this.description = description;
    }

    public static Database open(AppConfig config) {
        loadDriver("org.sqlite.JDBC");
        loadDriver("org.postgresql.Driver");
        if (config.dbType() == AppConfig.DbType.POSTGRES) {
            try {
                Connection c = DriverManager.getConnection(config.postgresUrl(), config.postgresUser(), config.postgresPassword());
                return migrated(new Database(c, SqlDialect.POSTGRES, "PostgreSQL"));
            } catch (SQLException e) {
                LOG.warn("PostgreSQL no disponible, uso SQLite: " + e.getMessage());
            }
        }
        try {
            Files.createDirectories(config.sqliteFile().getParent());
            Connection c = DriverManager.getConnection("jdbc:sqlite:" + config.sqliteFile());
            return migrated(new Database(c, SqlDialect.SQLITE, "SQLite (" + config.sqliteFile().getFileName() + ")"));
        } catch (Exception e) {
            LOG.warn("SQLite en archivo no disponible, uso memoria: " + e.getMessage());
        }
        try {
            Connection c = DriverManager.getConnection("jdbc:sqlite::memory:");
            return migrated(new Database(c, SqlDialect.SQLITE, "SQLite (memoria)"));
        } catch (SQLException e) {
            throw new IllegalStateException("No hay ningún motor de base de datos disponible", e);
        }
    }

    /** Registra el driver explícitamente (algunos class loaders no usan ServiceLoader). */
    private static void loadDriver(String className) {
        try {
            Class.forName(className);
        } catch (ClassNotFoundException ignored) {
            // driver opcional no incluido
        }
    }

    private static Database migrated(Database db) throws SQLException {
        db.migrate();
        return db;
    }

    private void migrate() throws SQLException {
        String id = dialect.autoIdPk();
        String blob = dialect.blobType();
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS settings (" +
                    "skey VARCHAR(80) PRIMARY KEY, svalue VARCHAR(400) NOT NULL)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS race_results (" +
                    "id " + id + ", track_id VARCHAR(40) NOT NULL, mirror INTEGER NOT NULL, mode VARCHAR(20) NOT NULL, " +
                    "car_id VARCHAR(40) NOT NULL, transmission VARCHAR(10) NOT NULL, player_name VARCHAR(12) NOT NULL, " +
                    "position INTEGER NOT NULL, total_ms BIGINT NOT NULL, best_lap_ms BIGINT NOT NULL, created_at BIGINT NOT NULL)");
            st.executeUpdate("CREATE INDEX IF NOT EXISTS ix_results_track ON race_results(track_id, mirror, mode, total_ms)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS lap_records (" +
                    "track_id VARCHAR(40) NOT NULL, mirror INTEGER NOT NULL, lap_ms BIGINT NOT NULL, " +
                    "player_name VARCHAR(12) NOT NULL, car_id VARCHAR(40) NOT NULL, ghost " + blob + ", created_at BIGINT NOT NULL, " +
                    "PRIMARY KEY (track_id, mirror))");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS input_bindings (" +
                    "device_key VARCHAR(120) NOT NULL, action VARCHAR(30) NOT NULL, kind VARCHAR(10) NOT NULL, " +
                    "idx INTEGER NOT NULL, rest_value REAL NOT NULL, full_value REAL NOT NULL, " +
                    "PRIMARY KEY (device_key, action))");
        }
    }

    public Connection connection() {
        return connection;
    }

    public SqlDialect dialect() {
        return dialect;
    }

    public String description() {
        return description;
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException ignored) {
            // cierre al salir del juego
        }
    }
}
