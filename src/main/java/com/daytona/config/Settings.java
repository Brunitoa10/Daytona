package com.daytona.config;

import com.daytona.core.Log;

import com.daytona.domain.car.TransmissionType;
import com.daytona.domain.race.Difficulty;
import com.daytona.input.ControlMode;
import com.daytona.input.ShifterType;
import com.daytona.persistence.SettingsRepository;
import com.daytona.render.ViewMode;

import java.util.HashMap;
import java.util.Map;

/**
 * Preferencias tipadas del jugador, persistidas en la base de datos.
 * Cada setter guarda inmediatamente (no hay que acordarse de "guardar").
 */
public final class Settings {

    private static final Log LOG = Log.of("settings");

    private final SettingsRepository repository;
    private final Map<String, String> values;

    public Settings(SettingsRepository repository) {
        this.repository = repository;
        Map<String, String> loaded;
        try {
            loaded = repository.loadAll();
        } catch (RuntimeException e) {
            LOG.warn("Opciones por defecto: " + e.getMessage());
            loaded = java.util.Collections.emptyMap();
        }
        this.values = new HashMap<>(loaded);
    }

    // ---- Video ----
    public Resolution resolution() { return getEnum("video.resolution", Resolution.class, Resolution.R640); }
    public void setResolution(Resolution r) { put("video.resolution", r.name()); }

    public boolean fullscreen() { return getBool("video.fullscreen", false); }
    public void setFullscreen(boolean v) { put("video.fullscreen", Boolean.toString(v)); }

    /** 0 = sin límite. */
    public int fpsLimit() { return getInt("video.fps", 120); }
    public void setFpsLimit(int fps) { put("video.fps", Integer.toString(fps)); }

    public boolean showFps() { return getBool("video.showFps", false); }
    public void setShowFps(boolean v) { put("video.showFps", Boolean.toString(v)); }

    public int drawDistance() { return getInt("video.drawDistance", 300); }
    public void setDrawDistance(int v) { put("video.drawDistance", Integer.toString(v)); }

    public VisualStyle visualStyle() { return getEnum("video.style", VisualStyle.class, VisualStyle.ARCADE_94); }
    public void setVisualStyle(VisualStyle v) { put("video.style", v.name()); }

    // ---- Audio ----
    public int volume() { return getInt("audio.volume", 70); }
    public void setVolume(int v) { put("audio.volume", Integer.toString(Math.max(0, Math.min(100, v)))); }

    // ---- Juego ----
    public SpeedUnit speedUnit() { return getEnum("game.units", SpeedUnit.class, SpeedUnit.KMH); }
    public void setSpeedUnit(SpeedUnit u) { put("game.units", u.name()); }

    public Difficulty difficulty() { return getEnum("game.difficulty", Difficulty.class, Difficulty.NORMAL); }
    public void setDifficulty(Difficulty d) { put("game.difficulty", d.name()); }

    public RaceLength raceLength() { return getEnum("game.raceLength", RaceLength.class, RaceLength.NORMAL); }
    public void setRaceLength(RaceLength l) { put("game.raceLength", l.name()); }

    public int rivals() { return getInt("game.rivals", 15); }
    public void setRivals(int n) { put("game.rivals", Integer.toString(Math.max(1, Math.min(39, n)))); }

    public ViewMode viewMode() { return getEnum("game.view", ViewMode.class, ViewMode.CHASE_FAR); }
    public void setViewMode(ViewMode v) { put("game.view", v.name()); }

    public String playerName() { return values.getOrDefault("game.playerName", "AAA"); }
    public void setPlayerName(String n) { put("game.playerName", n); }

    public String lastCar() { return values.getOrDefault("game.lastCar", "falcon"); }
    public void setLastCar(String id) { put("game.lastCar", id); }

    public String lastTrack() { return values.getOrDefault("game.lastTrack", "sunrise"); }
    public void setLastTrack(String id) { put("game.lastTrack", id); }

    // ---- Controles ----
    public ControlMode controlMode() { return getEnum("input.mode", ControlMode.class, ControlMode.KEYBOARD); }
    public void setControlMode(ControlMode m) { put("input.mode", m.name()); }

    public TransmissionType transmission() { return getEnum("input.transmission", TransmissionType.class, TransmissionType.AUTOMATIC); }
    public void setTransmission(TransmissionType t) { put("input.transmission", t.name()); }

    public ShifterType shifter() { return getEnum("input.shifter", ShifterType.class, ShifterType.SEQUENTIAL); }
    public void setShifter(ShifterType s) { put("input.shifter", s.name()); }

    /** Clave del dispositivo elegido (vacío = automático). */
    public String deviceKey() { return values.getOrDefault("input.device", ""); }
    public void setDeviceKey(String k) { put("input.device", k); }

    /** Sensibilidad de dirección 50..150 %. */
    public int steerSensitivity() { return getInt("input.steerSens", 100); }
    public void setSteerSensitivity(int v) { put("input.steerSens", Integer.toString(Math.max(50, Math.min(150, v)))); }

    /** Zona muerta 0..25 %. */
    public int deadzone() { return getInt("input.deadzone", 6); }
    public void setDeadzone(int v) { put("input.deadzone", Integer.toString(Math.max(0, Math.min(25, v)))); }

    // ---- Estadísticas acumuladas ----
    public long stat(String name) { return getLong("stats." + name, 0); }
    public void addStat(String name, long delta) { put("stats." + name, Long.toString(stat(name) + delta)); }

    // ---- helpers ----
    private void put(String key, String value) {
        values.put(key, value);
        try {
            repository.save(key, value);
        } catch (RuntimeException e) {
            LOG.warn("No se guardó " + key + ": " + e.getMessage());
        }
    }

    private int getInt(String key, int def) {
        try {
            return Integer.parseInt(values.getOrDefault(key, Integer.toString(def)));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private long getLong(String key, long def) {
        try {
            return Long.parseLong(values.getOrDefault(key, Long.toString(def)));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private boolean getBool(String key, boolean def) {
        String v = values.get(key);
        return v == null ? def : Boolean.parseBoolean(v);
    }

    private <E extends Enum<E>> E getEnum(String key, Class<E> type, E def) {
        String v = values.get(key);
        if (v == null) {
            return def;
        }
        try {
            return Enum.valueOf(type, v);
        } catch (IllegalArgumentException e) {
            return def;
        }
    }
}
