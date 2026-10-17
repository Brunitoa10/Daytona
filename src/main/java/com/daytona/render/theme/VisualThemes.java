package com.daytona.render.theme;

import com.daytona.config.Settings;
import com.daytona.config.VisualStyle;

import java.util.EnumMap;
import java.util.Map;

/** Entrega el tema correspondiente al estilo elegido en Opciones (Factory + caché). */
public final class VisualThemes {

    private final Settings settings;
    private final Map<VisualStyle, VisualTheme> themes = new EnumMap<>(VisualStyle.class);

    public VisualThemes(Settings settings) {
        this.settings = settings;
        themes.put(VisualStyle.ARCADE_94, new Arcade94Theme());
        themes.put(VisualStyle.ARCADE_HD, new ArcadeHdTheme());
        themes.put(VisualStyle.CLASSIC_2D, new Classic2dTheme());
    }

    public VisualTheme current() {
        return themes.get(settings.visualStyle());
    }
}
