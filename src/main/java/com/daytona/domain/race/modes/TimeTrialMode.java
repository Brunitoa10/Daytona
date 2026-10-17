package com.daytona.domain.race.modes;

import com.daytona.domain.race.GameMode;
import com.daytona.domain.race.GameModeType;

/** Contrarreloj: sin rivales, con fantasma de la mejor vuelta. */
public final class TimeTrialMode implements GameMode {

    @Override
    public GameModeType type() {
        return GameModeType.TIME_TRIAL;
    }

    @Override
    public int rivals(int preferred) {
        return 0;
    }

    @Override
    public boolean timeLimited() {
        return false;
    }

    @Override
    public boolean ghostEnabled() {
        return true;
    }

    @Override
    public double rubberBand() {
        return 0;
    }

    @Override
    public double gridSlot() {
        return 0;
    }
}
