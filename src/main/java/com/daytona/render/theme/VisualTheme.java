package com.daytona.render.theme;

import com.daytona.config.Settings;
import com.daytona.render.SpriteAtlas;
import com.daytona.render.car.CarRenderer;
import com.daytona.render.hud.HudSkin;

/**
 * Patrón Abstract Factory: cada tema crea una familia coherente de piezas visuales
 * (autos, HUD y efecto de pantalla) para que nunca se mezclen estilos.
 */
public interface VisualTheme {

    public CarRenderer createCarRenderer(SpriteAtlas atlas);

    public HudSkin createHudSkin(Settings settings);

    public PostProcessor postProcessor();
}
