package com.daytona.render.theme;

import com.daytona.config.Settings;
import com.daytona.render.SpriteAtlas;
import com.daytona.render.car.CarRenderer;
import com.daytona.render.car.MeshCarRenderer;
import com.daytona.render.hud.Arcade94HudSkin;
import com.daytona.render.hud.HudSkin;

/** Arcade 1994: polígonos de bordes duros, HUD de 7 segmentos y monitor CRT. */
public final class Arcade94Theme implements VisualTheme {

    private final PostProcessor crt = new CrtPostProcessor();

    @Override
    public CarRenderer createCarRenderer(SpriteAtlas atlas) {
        return new MeshCarRenderer(false);
    }

    @Override
    public HudSkin createHudSkin(Settings settings) {
        return new Arcade94HudSkin(settings);
    }

    @Override
    public PostProcessor postProcessor() {
        return crt;
    }
}
