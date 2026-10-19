package com.daytona.domain.track;

/** Tipos de objetos de escenario. El ancho está en unidades de mundo. */
public enum SpriteKind {
    PALM(1100, true),
    PINE(1000, true),
    SNOW_PINE(1050, true),
    TREE(1300, true),
    CACTUS(650, true),
    ROCK(1500, true),
    CLIFF(5200, true),
    BUILDING(5600, true),
    TOWER(4200, true),
    LAMP(420, true),
    BILLBOARD(2600, true),
    GRANDSTAND(9000, true),
    TIRES(700, true),
    FLAG(450, true),
    CHEVRON_LEFT(900, true),
    CHEVRON_RIGHT(900, true),
    /** Pórtico de largada que cruza la pista (no colisiona). */
    GANTRY(4700, false),
    /** Pórtico de checkpoint (no colisiona). */
    CHECKPOINT(4700, false);

    private final double worldWidth;
    private final boolean collidable;

    private SpriteKind(double worldWidth, boolean collidable) {
        this.worldWidth = worldWidth;
        this.collidable = collidable;
    }

    public double worldWidth() {
        return worldWidth;
    }

    public boolean collidable() {
        return collidable;
    }

    public SpriteKind mirrored() {
        switch (this) {
            case CHEVRON_LEFT:
                return CHEVRON_RIGHT;
            case CHEVRON_RIGHT:
                return CHEVRON_LEFT;
            default:
                return this;
        }
    }
}
