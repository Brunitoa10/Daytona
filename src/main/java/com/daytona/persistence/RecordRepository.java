package com.daytona.persistence;

import java.util.List;
import java.util.Optional;

/** Récords: tiempos totales por modo y mejor vuelta (con fantasma) por pista. */
public interface RecordRepository {

    public void saveResult(RaceResultEntry entry);

    public List<RaceResultEntry> topResults(String trackId, boolean mirror, String mode, int limit);

    /** true si el tiempo entraría en el top {@code limit}. */
    public boolean qualifies(String trackId, boolean mirror, String mode, long totalMs, int limit);

    public Optional<LapRecord> bestLap(String trackId, boolean mirror);

    /** Guarda la vuelta solo si mejora el récord. Devuelve true si se guardó. */
    public boolean saveLapIfBetter(LapRecord lap);

    public long countResults();
}
