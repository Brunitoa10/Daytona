package com.daytona.domain.track;

/**
 * Cada pista es una clase que describe su trazado con el {@link TrackBuilder}.
 * Agregar una pista nueva = una clase nueva registrada en {@link TrackCatalog} (Open/Closed).
 */
public interface TrackDefinition {

    TrackInfo info();

    TrackTheme theme();

    /** Describe el trazado en el builder (el builder ya sabe si es espejo). */
    void layout(TrackBuilder b);
}
