package com.daytona.persistence;

/** Error de acceso a datos sin chequeo obligatorio, para no ensuciar la lógica del juego. */
public final class PersistenceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
