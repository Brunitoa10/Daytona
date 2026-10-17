package com.daytona.audio;

/** Sonido del juego. Implementaciones: sintetizador en tiempo real o silencio. */
public interface AudioService extends AutoCloseable {

    /** Motor del jugador: rpm 0..1, acelerador 0..1. */
    void setEngine(double rpm, double throttle, boolean running);

    /** Chirrido de gomas / tierra: 0..1. */
    void setSkid(double level);

    /** Roce contra el muro: 0..1. */
    void setScrape(double level);

    default void beep(double frequency, double seconds) {
        beep(frequency, seconds, 0);
    }

    /** Pitido con retardo opcional (para arpegios). */
    void beep(double frequency, double seconds, double delay);

    void crash(double intensity);

    /** 0..1 */
    void setVolume(double volume);

    /** Silencia los sonidos continuos (al salir de una carrera o pausar). */
    default void stopLoops() {
        setEngine(0, 0, false);
        setSkid(0);
        setScrape(0);
    }

    @Override
    void close();
}
