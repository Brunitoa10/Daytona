package com.daytona.domain.race;

import com.daytona.domain.race.modes.ArcadeMode;
import com.daytona.domain.race.modes.ChampionshipMode;
import com.daytona.domain.race.modes.DuelMode;
import com.daytona.domain.race.modes.TimeTrialMode;

import java.util.function.Supplier;

/** Modos disponibles y su fábrica. */
public enum GameModeType {
    ARCADE("Arcade", "Carrera contra reloj: pasá los checkpoints para sumar tiempo.", ArcadeMode::new),
    CHAMPIONSHIP("Campeonato", "Todas las pistas seguidas. Suma puntos según tu posición.", ChampionshipMode::new),
    TIME_TRIAL("Contrarreloj", "Solo vos y tu fantasma. Bajá tu mejor vuelta.", TimeTrialMode::new),
    DUEL("Duelo", "Mano a mano contra el rival más rápido.", DuelMode::new);

    private final String label;
    private final String description;
    private final Supplier<GameMode> factory;

    GameModeType(String label, String description, Supplier<GameMode> factory) {
        this.label = label;
        this.description = description;
        this.factory = factory;
    }

    public String label() {
        return label;
    }

    public String description() {
        return description;
    }

    public GameMode create() {
        return factory.get();
    }
}
