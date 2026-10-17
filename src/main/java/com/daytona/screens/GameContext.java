package com.daytona.screens;

import com.daytona.app.DisplayControl;
import com.daytona.audio.AudioService;
import com.daytona.config.Settings;
import com.daytona.core.ScreenManager;
import com.daytona.domain.car.CarCatalog;
import com.daytona.domain.track.TrackCatalog;
import com.daytona.input.InputManager;
import com.daytona.persistence.Database;
import com.daytona.persistence.RecordRepository;
import com.daytona.render.BackgroundRenderer;
import com.daytona.render.MinimapRenderer;
import com.daytona.render.SpriteAtlas;
import com.daytona.render.theme.VisualThemes;

/**
 * Servicios compartidos que reciben las pantallas (inyección de dependencias manual).
 * Se arma una sola vez en la raíz de composición ({@code GameApp}).
 */
public final class GameContext {
    private final Settings settings;
    private final Database database;
    private final RecordRepository records;
    private final InputManager input;
    private final AudioService audio;
    private final CarCatalog cars;
    private final TrackCatalog tracks;
    private final SpriteAtlas atlas;
    private final BackgroundRenderer background;
    private final MinimapRenderer minimap;
    private final ScreenManager screens;
    private final DisplayControl display;
    private final Navigator nav;
    private final VisualThemes themes;

    public GameContext(Settings settings, Database database, RecordRepository records, InputManager input, AudioService audio, CarCatalog cars, TrackCatalog tracks, SpriteAtlas atlas, BackgroundRenderer background, MinimapRenderer minimap, ScreenManager screens, DisplayControl display, Navigator nav, VisualThemes themes) {
        this.settings = settings;
        this.database = database;
        this.records = records;
        this.input = input;
        this.audio = audio;
        this.cars = cars;
        this.tracks = tracks;
        this.atlas = atlas;
        this.background = background;
        this.minimap = minimap;
        this.screens = screens;
        this.display = display;
        this.nav = nav;
        this.themes = themes;
    }

    public Settings settings() {
        return settings;
    }

    public Database database() {
        return database;
    }

    public RecordRepository records() {
        return records;
    }

    public InputManager input() {
        return input;
    }

    public AudioService audio() {
        return audio;
    }

    public CarCatalog cars() {
        return cars;
    }

    public TrackCatalog tracks() {
        return tracks;
    }

    public SpriteAtlas atlas() {
        return atlas;
    }

    public BackgroundRenderer background() {
        return background;
    }

    public MinimapRenderer minimap() {
        return minimap;
    }

    public ScreenManager screens() {
        return screens;
    }

    public DisplayControl display() {
        return display;
    }

    public VisualThemes themes() {
        return themes;
    }

    public Navigator nav() {
        return nav;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GameContext)) {
            return false;
        }
        GameContext other = (GameContext) o;
        return java.util.Objects.equals(settings, other.settings) && java.util.Objects.equals(database, other.database) && java.util.Objects.equals(records, other.records) && java.util.Objects.equals(input, other.input) && java.util.Objects.equals(audio, other.audio) && java.util.Objects.equals(cars, other.cars) && java.util.Objects.equals(tracks, other.tracks) && java.util.Objects.equals(atlas, other.atlas) && java.util.Objects.equals(background, other.background) && java.util.Objects.equals(minimap, other.minimap) && java.util.Objects.equals(screens, other.screens) && java.util.Objects.equals(display, other.display) && java.util.Objects.equals(nav, other.nav);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(settings, database, records, input, audio, cars, tracks, atlas, background, minimap, screens, display, nav);
    }
}
