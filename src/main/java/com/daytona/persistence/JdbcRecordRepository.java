package com.daytona.persistence;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class JdbcRecordRepository implements RecordRepository {

    private final Database db;

    public JdbcRecordRepository(Database db) {
        this.db = db;
    }

    @Override
    public synchronized void saveResult(RaceResultEntry e) {
        String sql = "INSERT INTO race_results(track_id, mirror, mode, car_id, transmission, player_name, position, " +
                "total_ms, best_lap_ms, created_at) VALUES(?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = db.connection().prepareStatement(sql)) {
            ps.setString(1, e.trackId());
            ps.setInt(2, e.mirror() ? 1 : 0);
            ps.setString(3, e.mode());
            ps.setString(4, e.carId());
            ps.setString(5, e.transmission());
            ps.setString(6, e.playerName());
            ps.setInt(7, e.position());
            ps.setLong(8, e.totalMs());
            ps.setLong(9, e.bestLapMs());
            ps.setLong(10, e.createdAt());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new PersistenceException("No se pudo guardar el resultado", ex);
        }
    }

    @Override
    public synchronized List<RaceResultEntry> topResults(String trackId, boolean mirror, String mode, int limit) {
        String sql = "SELECT track_id, mirror, mode, car_id, transmission, player_name, position, total_ms, best_lap_ms, created_at " +
                "FROM race_results WHERE track_id = ? AND mirror = ? AND mode = ? ORDER BY total_ms ASC LIMIT ?";
        List<RaceResultEntry> list = new ArrayList<>();
        try (PreparedStatement ps = db.connection().prepareStatement(sql)) {
            ps.setString(1, trackId);
            ps.setInt(2, mirror ? 1 : 0);
            ps.setString(3, mode);
            ps.setInt(4, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new RaceResultEntry(rs.getString(1), rs.getInt(2) == 1, rs.getString(3), rs.getString(4),
                            rs.getString(5), rs.getString(6), rs.getInt(7), rs.getLong(8), rs.getLong(9), rs.getLong(10)));
                }
            }
        } catch (SQLException ex) {
            throw new PersistenceException("No se pudieron leer los récords", ex);
        }
        return list;
    }

    @Override
    public boolean qualifies(String trackId, boolean mirror, String mode, long totalMs, int limit) {
        List<RaceResultEntry> top = topResults(trackId, mirror, mode, limit);
        return top.size() < limit || totalMs < top.get(top.size() - 1).totalMs();
    }

    @Override
    public synchronized Optional<LapRecord> bestLap(String trackId, boolean mirror) {
        String sql = "SELECT lap_ms, player_name, car_id, ghost FROM lap_records WHERE track_id = ? AND mirror = ?";
        try (PreparedStatement ps = db.connection().prepareStatement(sql)) {
            ps.setString(1, trackId);
            ps.setInt(2, mirror ? 1 : 0);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new LapRecord(trackId, mirror, rs.getLong(1), rs.getString(2), rs.getString(3), rs.getBytes(4)));
                }
            }
        } catch (SQLException ex) {
            throw new PersistenceException("No se pudo leer la mejor vuelta", ex);
        }
        return Optional.empty();
    }

    @Override
    public synchronized boolean saveLapIfBetter(LapRecord lap) {
        Optional<LapRecord> current = bestLap(lap.trackId(), lap.mirror());
        if (current.isPresent() && current.get().lapMs() <= lap.lapMs()) {
            return false;
        }
        String sql = "INSERT INTO lap_records(track_id, mirror, lap_ms, player_name, car_id, ghost, created_at) " +
                "VALUES(?,?,?,?,?,?,?) ON CONFLICT(track_id, mirror) DO UPDATE SET lap_ms = excluded.lap_ms, " +
                "player_name = excluded.player_name, car_id = excluded.car_id, ghost = excluded.ghost, created_at = excluded.created_at";
        try (PreparedStatement ps = db.connection().prepareStatement(sql)) {
            ps.setString(1, lap.trackId());
            ps.setInt(2, lap.mirror() ? 1 : 0);
            ps.setLong(3, lap.lapMs());
            ps.setString(4, lap.playerName());
            ps.setString(5, lap.carId());
            ps.setBytes(6, lap.ghost());
            ps.setLong(7, System.currentTimeMillis());
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            throw new PersistenceException("No se pudo guardar la mejor vuelta", ex);
        }
    }

    @Override
    public synchronized long countResults() {
        try (PreparedStatement ps = db.connection().prepareStatement("SELECT COUNT(*) FROM race_results");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException ex) {
            throw new PersistenceException("No se pudo contar resultados", ex);
        }
    }
}
