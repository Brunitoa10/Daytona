package com.daytona.audio;

/** Patrón Null Object: cuando no hay placa de sonido el juego sigue funcionando. */
public final class SilentAudio implements AudioService {

    @Override public void setEngine(double rpm, double throttle, boolean running) { }
    @Override public void setSkid(double level) { }
    @Override public void setScrape(double level) { }
    @Override public void beep(double frequency, double seconds, double delay) { }
    @Override public void crash(double intensity) { }
    @Override public void setVolume(double volume) { }
    @Override public void close() { }
}
