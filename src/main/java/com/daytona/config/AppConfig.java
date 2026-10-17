package com.daytona.config;

import java.nio.file.Paths;

import com.daytona.core.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Configuración de arranque leída de {@code config.properties} junto al ejecutable.
 * Todo es relativo a la carpeta del juego para que sea portable (pendrive, sin red).
 */
public final class AppConfig {

    public enum DbType { SQLITE, POSTGRES }

    private static final String FILE_NAME = "config.properties";

    private final Path baseDir;
    private final Properties props;

    private AppConfig(Path baseDir, Properties props) {
        this.baseDir = baseDir;
        this.props = props;
    }

    public static AppConfig load() {
        Path base = detectBaseDir();
        Properties p = defaults();
        Path file = base.resolve(FILE_NAME);
        if (Files.exists(file)) {
            try (InputStream in = Files.newInputStream(file)) {
                p.load(in);
            } catch (IOException e) {
                Log.of("config").warn("No se pudo leer " + file, e);
            }
        } else {
            writeTemplate(file, p);
        }
        return new AppConfig(base, p);
    }

    private static Properties defaults() {
        Properties p = new Properties();
        p.setProperty("db.type", "sqlite");
        p.setProperty("db.sqlite.file", "data/daytona.db");
        p.setProperty("db.postgres.url", "jdbc:postgresql://localhost:5432/daytona");
        p.setProperty("db.postgres.user", "postgres");
        p.setProperty("db.postgres.password", "");
        return p;
    }

    private static void writeTemplate(Path file, Properties p) {
        try (OutputStream out = Files.newOutputStream(file)) {
            p.store(out, "Daytona - db.type = sqlite (portable, recomendado) o postgres");
        } catch (IOException ignored) {
            // carpeta de solo lectura: se usan los valores por defecto
        }
    }

    /** Carpeta donde está el .jar (o el directorio actual al ejecutar desde el IDE). */
    private static Path detectBaseDir() {
        try {
            Path code = Paths.get(AppConfig.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path dir = Files.isRegularFile(code) ? code.getParent() : Paths.get("").toAbsolutePath();
            return dir == null ? Paths.get("").toAbsolutePath() : dir;
        } catch (URISyntaxException | SecurityException | NullPointerException e) {
            return Paths.get("").toAbsolutePath();
        }
    }

    public Path baseDir() {
        return baseDir;
    }

    public DbType dbType() {
        return "postgres".equalsIgnoreCase(props.getProperty("db.type", "sqlite").trim()) ? DbType.POSTGRES : DbType.SQLITE;
    }

    public Path sqliteFile() {
        Path f = Paths.get(props.getProperty("db.sqlite.file"));
        return f.isAbsolute() ? f : baseDir.resolve(f);
    }

    public String postgresUrl() {
        return props.getProperty("db.postgres.url");
    }

    public String postgresUser() {
        return props.getProperty("db.postgres.user");
    }

    public String postgresPassword() {
        return props.getProperty("db.postgres.password");
    }
}
