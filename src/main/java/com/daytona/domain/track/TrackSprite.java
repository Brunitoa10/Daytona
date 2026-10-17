package com.daytona.domain.track;

/**
 * Objeto colocado junto a la pista.
 *
 * @param offset posición lateral en medias-anchuras de calzada (|offset| &gt; 1 = fuera de la pista)
 * @param variant variante visual (color, texto del cartel, etc.)
 */
public final class TrackSprite {
    private final SpriteKind kind;
    private final double offset;
    private final int variant;

    public TrackSprite(SpriteKind kind, double offset, int variant) {
        this.kind = kind;
        this.offset = offset;
        this.variant = variant;
    }

    public SpriteKind kind() {
        return kind;
    }

    public double offset() {
        return offset;
    }

    public int variant() {
        return variant;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TrackSprite)) {
            return false;
        }
        TrackSprite other = (TrackSprite) o;
        return java.util.Objects.equals(kind, other.kind) && java.util.Objects.equals(offset, other.offset) && java.util.Objects.equals(variant, other.variant);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(kind, offset, variant);
    }
}
