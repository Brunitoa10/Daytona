package com.daytona.domain.race.modes;

import com.daytona.domain.race.Difficulty;
import com.daytona.domain.race.GameMode;
import com.daytona.domain.race.GameModeType;
import com.daytona.domain.track.Track;
import com.daytona.domain.track.TrackConstants;

/** Arcade clásico: reloj que se extiende en cada checkpoint, muchos rivales. */
public final class ArcadeMode implements GameMode {

    /** Velocidad media de referencia para calcular los tiempos (km/h). */
    private static final double REFERENCE_KMH = 215;

    @Override
    public GameModeType type() {
        return GameModeType.ARCADE;
    }

    @Override
    public int rivals(int preferred) {
        return preferred;
    }

    @Override
    public boolean timeLimited() {
        return true;
    }

    @Override
    public double initialTime(Track track, Difficulty difficulty) {
        return Math.ceil(sectionSeconds(track, 0) * difficulty.timeFactor() * 1.25 + 4);
    }

    @Override
    public double sectionBonus(Track track, Difficulty difficulty, int section) {
        return Math.ceil(sectionSeconds(track, section + 1) * difficulty.timeFactor());
    }

    /** Tiempo de referencia para recorrer el tramo {@code section} (0 = largada a 1er checkpoint). */
    private static double sectionSeconds(Track track, int section) {
        int[] cps = track.checkpoints();
        int sections = cps.length + 1;
        int idx = Math.floorMod(section, sections);
        int from = idx == 0 ? 0 : cps[idx - 1];
        int to = idx == cps.length ? track.segmentCount() : cps[idx];
        double units = (to - from) * TrackConstants.SEGMENT_LENGTH;
        return units / TrackConstants.kmhToUnits(REFERENCE_KMH);
    }

    @Override
    public boolean ghostEnabled() {
        return false;
    }

    @Override
    public double rubberBand() {
        return 0.25;
    }

    @Override
    public double gridSlot() {
        return 0.55;
    }
}
