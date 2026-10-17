package com.daytona.domain.race.modes;

import com.daytona.domain.race.GameMode;
import com.daytona.domain.race.GameModeType;

/** Carrera de campeonato: sin reloj, larga desde el fondo. */
public final class ChampionshipMode implements GameMode {

    @Override
    public GameModeType type() {
        return GameModeType.CHAMPIONSHIP;
    }

    @Override
    public int rivals(int preferred) {
        return Math.min(preferred, 9);
    }

    @Override
    public boolean timeLimited() {
        return false;
    }

    @Override
    public boolean ghostEnabled() {
        return false;
    }

    @Override
    public double rubberBand() {
        return 0.12;
    }

    @Override
    public double gridSlot() {
        return 1.0;
    }
}
