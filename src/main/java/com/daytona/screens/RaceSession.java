package com.daytona.screens;

import com.daytona.domain.race.ChampionshipSession;
import com.daytona.domain.race.GameModeType;

/** Elecciones del jugador que viajan entre pantallas (modo, auto, pista...). */
public final class RaceSession {

    private final GameModeType mode;
    private String carId;
    private String trackId;
    private boolean mirror;
    private ChampionshipSession championship;

    public RaceSession(GameModeType mode, String carId, String trackId) {
        this.mode = mode;
        this.carId = carId;
        this.trackId = trackId;
    }

    public GameModeType mode() { return mode; }
    public String carId() { return carId; }
    public void setCarId(String id) { this.carId = id; }
    public String trackId() { return championship != null ? championship.currentTrackId() : trackId; }
    public void setTrackId(String id) { this.trackId = id; }
    public boolean mirror() { return championship != null ? championship.mirror() : mirror; }
    public void setMirror(boolean m) { this.mirror = m; }
    public ChampionshipSession championship() { return championship; }
    public void setChampionship(ChampionshipSession c) { this.championship = c; }
    public boolean isChampionship() { return championship != null; }
}
