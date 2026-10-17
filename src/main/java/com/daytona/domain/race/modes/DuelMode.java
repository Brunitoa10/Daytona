package com.daytona.domain.race.modes;

import com.daytona.domain.race.GameMode;
import com.daytona.domain.race.GameModeType;

/** Duelo uno contra uno contra un rival más hábil. */
public final class DuelMode implements GameMode {

    @Override
    public GameModeType type() {
        return GameModeType.DUEL;
    }

    @Override
    public int rivals(int preferred) {
        return 1;
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
        return 0.15;
    }

    @Override
    public double aiSkillBonus() {
        return 0.05;
    }

    @Override
    public double gridSlot() {
        return 1.0;
    }
}
