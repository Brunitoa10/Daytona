package com.daytona.input;

import com.daytona.domain.car.DriveCommand;

/** Patrón Strategy: de dónde sale el manejo (teclado, gamepad, volante). */
public interface DriveInputSource {

    /** Lee el comando de manejo de este paso. */
    public DriveCommand read(double dt);

    /** Flanco del botón de cambiar cámara. */
    public boolean viewPressed();

    /** Flanco del botón de pausa del dispositivo de manejo. */
    public boolean pausePressed();

    public String describe();
}
