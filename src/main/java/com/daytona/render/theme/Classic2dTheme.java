package com.daytona.render.theme;

import com.daytona.config.Settings;
import com.daytona.render.SpriteAtlas;
import com.daytona.render.car.CarRenderer;
import com.daytona.render.car.SpriteCarRenderer;
import com.daytona.render.hud.HudSkin;
import com.daytona.render.hud.ModernHudSkin;

/** Autos como sprites 2D y HUD moderno con tacómetro de aguja. */
public final class Classic2dTheme implements VisualTheme {

    private final PostProcessor smooth = new SmoothPostProcessor();

    @Override
    public CarRenderer createCarRenderer(SpriteAtlas atlas) {
        return new SpriteCarRenderer(atlas);
    }

    @Override
    public HudSkin createHudSkin(Settings settings) {
        return new ModernHudSkin(settings);
    }

    @Override
    public PostProcessor postProcessor() {
        return smooth;
    }
}
