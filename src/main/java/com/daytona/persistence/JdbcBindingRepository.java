package com.daytona.persistence;

import com.daytona.input.binding.Binding;
import com.daytona.input.binding.BindingProfile;
import com.daytona.input.binding.BindingRepository;
import com.daytona.input.binding.InputAction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public final class JdbcBindingRepository implements BindingRepository {

    private final Database db;

    public JdbcBindingRepository(Database db) {
        this.db = db;
    }

    @Override
    public synchronized Optional<BindingProfile> find(String deviceKey) {
        String sql = "SELECT action, kind, idx, rest_value, full_value FROM input_bindings WHERE device_key = ?";
        Map<InputAction, Binding> map = new EnumMap<>(InputAction.class);
        try (PreparedStatement ps = db.connection().prepareStatement(sql)) {
            ps.setString(1, deviceKey);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    try {
                        InputAction action = InputAction.valueOf(rs.getString(1));
                        Binding.Kind kind = Binding.Kind.valueOf(rs.getString(2));
                        map.put(action, new Binding(kind, rs.getInt(3), rs.getFloat(4), rs.getFloat(5)));
                    } catch (IllegalArgumentException ignored) {
                        // acción de una versión anterior: se ignora
                    }
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo leer el perfil de controles", e);
        }
        return map.isEmpty() ? Optional.empty() : Optional.of(new BindingProfile(deviceKey, map, true));
    }

    @Override
    public synchronized void save(BindingProfile profile) {
        Connection c = db.connection();
        try {
            boolean auto = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                deleteRows(profile.deviceKey());
                String sql = "INSERT INTO input_bindings(device_key, action, kind, idx, rest_value, full_value) VALUES(?,?,?,?,?,?)";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    for (Map.Entry<InputAction, Binding> e : profile.asMap().entrySet()) {
                        ps.setString(1, profile.deviceKey());
                        ps.setString(2, e.getKey().name());
                        ps.setString(3, e.getValue().kind().name());
                        ps.setInt(4, e.getValue().index());
                        ps.setFloat(5, e.getValue().rest());
                        ps.setFloat(6, e.getValue().full());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(auto);
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo guardar el perfil de controles", e);
        }
    }

    @Override
    public synchronized void delete(String deviceKey) {
        try {
            deleteRows(deviceKey);
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo borrar el perfil", e);
        }
    }

    private void deleteRows(String deviceKey) throws SQLException {
        try (PreparedStatement ps = db.connection().prepareStatement("DELETE FROM input_bindings WHERE device_key = ?")) {
            ps.setString(1, deviceKey);
            ps.executeUpdate();
        }
    }
}
