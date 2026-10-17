package com.daytona.app;

import com.daytona.audio.AudioService;
import com.daytona.audio.SynthAudio;
import com.daytona.config.AppConfig;
import com.daytona.config.Settings;
import com.daytona.core.ScreenManager;
import com.daytona.domain.car.CarCatalog;
import com.daytona.domain.track.TrackCatalog;
import com.daytona.input.InputManager;
import com.daytona.input.KeyboardState;
import com.daytona.input.joystick.JoystickBackends;
import com.daytona.persistence.Database;
import com.daytona.persistence.JdbcBindingRepository;
import com.daytona.persistence.JdbcRecordRepository;
import com.daytona.persistence.JdbcSettingsRepository;
import com.daytona.render.BackgroundRenderer;
import com.daytona.render.MinimapRenderer;
import com.daytona.render.SpriteAtlas;
import com.daytona.render.theme.VisualThemes;
import com.daytona.screens.GameContext;
import com.daytona.screens.Navigator;

/** Raíz de composición: crea y conecta todos los servicios (Dependency Injection manual). */
public final class GameApp {

    public void run() {
        AppConfig config = AppConfig.load();
        Database db = Database.open(config);
        Settings settings = new Settings(new JdbcSettingsRepository(db));
        KeyboardState keys = new KeyboardState();
        VisualThemes themes = new VisualThemes(settings);
        GameWindow window = new GameWindow("DAYTONA", settings, keys, () -> themes.current().postProcessor());
        InputManager input = new InputManager(keys, JoystickBackends.create(), new JdbcBindingRepository(db), settings);
        input.rescan();
        AudioService audio = SynthAudio.open();
        audio.setVolume(settings.volume() / 100.0);

        ScreenManager screens = new ScreenManager(window::width, window::height);
        Navigator nav = new Navigator();
        GameContext ctx = new GameContext(settings, db, new JdbcRecordRepository(db), input, audio,
                new CarCatalog(), new TrackCatalog(), new SpriteAtlas(), new BackgroundRenderer(),
                new MinimapRenderer(), screens, window, nav, themes);
        nav.bind(ctx);
        nav.title();
        try {
            new GameLoop(window, screens, input, settings).run();
        } finally {
            audio.close();
            input.close();
            db.close();
            window.dispose();
        }
    }
}
