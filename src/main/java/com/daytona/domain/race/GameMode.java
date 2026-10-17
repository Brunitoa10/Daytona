package com.daytona.domain.race;

import com.daytona.domain.track.Track;

/**
 * Patrón Strategy: reglas de cada modo de juego. La carrera ({@link Race}) es la
 * misma; cambia el tiempo límite, los rivales, el fantasma y la ayuda a la IA.
 */
public interface GameMode {

    GameModeType type();

    /** Cantidad de rivales para este modo (puede ignorar la preferencia del jugador). */
    int rivals(int preferred);

    boolean timeLimited();

    /** Segundos iniciales del reloj (solo si {@link #timeLimited()}). */
    default double initialTime(Track track, Difficulty difficulty) {
        return 0;
    }

    /** Segundos extra al pasar el tramo {@code section} (checkpoint o meta). */
    default double sectionBonus(Track track, Difficulty difficulty, int section) {
        return 0;
    }

    boolean ghostEnabled();

    /** Fuerza del "rubber band" de la IA (0 = sin ayuda). */
    double rubberBand();

    /** Ajuste de habilidad de la IA sobre la dificultad. */
    default double aiSkillBonus() {
        return 0;
    }

    /** Posición de largada del jugador: 0 = pole, 1 = último. */
    double gridSlot();
}
