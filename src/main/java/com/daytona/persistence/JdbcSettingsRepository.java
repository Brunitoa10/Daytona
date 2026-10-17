package com.daytona.persistence;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public final class JdbcSettingsRepository implements SettingsRepository {

    private final Database db;

    public JdbcSettingsRepository(Database db) {
        this.db = db;
    }

    @Override
    public synchronized Map<String, String> loadAll() {
        Map<String, String> map = new HashMap<>();
        try (PreparedStatement ps = db.connection().prepareStatement("SELECT skey, svalue FROM settings");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString(1), rs.getString(2));
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudieron leer las opciones", e);
        }
        return map;
    }

    @Override
    public synchronized void save(String key, String value) {
        String sql = "INSERT INTO settings(skey, svalue) VALUES(?, ?) " +
                "ON CONFLICT(skey) DO UPDATE SET svalue = excluded.svalue";
        try (PreparedStatement ps = db.connection().prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo guardar la opción " + key, e);
        }
    }
}
