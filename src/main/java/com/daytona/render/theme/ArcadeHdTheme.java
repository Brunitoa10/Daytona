package com.daytona.render.theme;

import com.daytona.config.Settings;
import com.daytona.render.SpriteAtlas;
import com.daytona.render.car.CarRenderer;
import com.daytona.render.car.MeshCarRenderer;
import com.daytona.render.hud.Arcade94HudSkin;
import com.daytona.render.hud.HudSkin;

/** Mismo espíritu arcade, en alta definición: polígonos suavizados y sin CRT. */
public final class ArcadeHdTheme implements VisualTheme {

    private final PostProcessor smooth = new SmoothPostProcessor();

    @Override
    public CarRenderer createCarRenderer(SpriteAtlas atlas) {
        return new MeshCarRenderer(true);
    }

    @Override
    public HudSkin createHudSkin(Settings settings) {
        return new Arcade94HudSkin(settings);
    }

    @Override
    public PostProcessor postProcessor() {
        return smooth;
    }
}
