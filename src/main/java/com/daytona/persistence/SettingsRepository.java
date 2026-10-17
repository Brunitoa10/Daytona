package com.daytona.persistence;

import java.util.Map;

/** Repositorio clave/valor de preferencias del jugador. */
public interface SettingsRepository {

    public Map<String, String> loadAll();

    public void save(String key, String value);
}
