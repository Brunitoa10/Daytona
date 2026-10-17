package com.daytona;

import com.daytona.input.binding.Binding;
import com.daytona.input.binding.BindingProfile;
import com.daytona.input.binding.InputAction;
import com.daytona.persistence.JdbcBindingRepository;
import com.daytona.persistence.JdbcRecordRepository;
import com.daytona.persistence.JdbcSettingsRepository;
import com.daytona.persistence.LapRecord;
import com.daytona.persistence.RaceResultEntry;
import com.daytona.persistence.Database;
import com.daytona.config.AppConfig;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersistenceTest {

    private final Database db = Database.open(AppConfig.load());

    @Test
    void settingsRoundTrip() {
        JdbcSettingsRepository repo = new JdbcSettingsRepository(db);
        repo.save("test.key", "uno");
        repo.save("test.key", "dos");
        assertEquals("dos", repo.loadAll().get("test.key"));
    }

    @Test
    void recordsAreOrderedAndLapOnlyImproves() {
        JdbcRecordRepository repo = new JdbcRecordRepository(db);
        String track = "t" + System.nanoTime();
        repo.saveResult(new RaceResultEntry(track, false, "ARCADE", "falcon", "AT", "BBB", 2, 90_000, 30_000, 1));
        repo.saveResult(new RaceResultEntry(track, false, "ARCADE", "falcon", "MT", "AAA", 1, 80_000, 28_000, 2));
        assertEquals("AAA", repo.topResults(track, false, "ARCADE", 10).get(0).playerName());
        assertTrue(repo.saveLapIfBetter(new LapRecord(track, false, 30_000, "AAA", "falcon", new byte[]{1, 2})));
        assertFalse(repo.saveLapIfBetter(new LapRecord(track, false, 31_000, "BBB", "falcon", null)));
        assertTrue(repo.saveLapIfBetter(new LapRecord(track, false, 29_000, "CCC", "viper", null)));
        assertEquals("CCC", repo.bestLap(track, false).get().playerName());
    }

    @Test
    void bindingProfileRoundTrip() {
        JdbcBindingRepository repo = new JdbcBindingRepository(db);
        Map<InputAction, Binding> m = new EnumMap<>(InputAction.class);
        m.put(InputAction.STEER, Binding.axis(0, 0.01f, 0.98f));
        m.put(InputAction.THROTTLE, Binding.axis(2, 1f, -1f));
        m.put(InputAction.GEAR_1, Binding.button(12));
        repo.save(new BindingProfile("test:dev", m, true));
        BindingProfile p = repo.find("test:dev").get();
        assertEquals(-1f, p.get(InputAction.THROTTLE).get().full(), 1e-6);
        assertTrue(p.hasHShifter());
        repo.delete("test:dev");
        assertFalse(repo.find("test:dev").isPresent());
    }
}
