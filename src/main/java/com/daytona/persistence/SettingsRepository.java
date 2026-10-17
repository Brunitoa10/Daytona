package com.daytona.persistence;

import java.util.Map;

/** Repositorio clave/valor de preferencias del jugador. */
public interface SettingsRepository {

    Map<String, String> loadAll();

    void save(String key, String value);
}
