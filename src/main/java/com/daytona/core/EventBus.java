package com.daytona.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Patrón Observer: publica eventos del juego (vueltas, choques, checkpoints)
 * a quien quiera escucharlos (audio, HUD, estadísticas) sin acoplarlos a la carrera.
 */
public final class EventBus {

    private static final class Subscription<T extends GameEvent> {
        private final Class<T> type;
        private final Consumer<? super T> handler;

        private Subscription(Class<T> type, Consumer<? super T> handler) {
            this.type = type;
            this.handler = handler;
        }

        public Class<T> type() {
            return type;
        }

        public Consumer<? super T> handler() {
            return handler;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Subscription)) {
                return false;
            }
            Subscription<?> other = (Subscription<?>) o;
            return java.util.Objects.equals(type, other.type) && java.util.Objects.equals(handler, other.handler);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(type, handler);
        }
        private void deliver(GameEvent event) {
            if (type.isInstance(event)) {
                handler.accept(type.cast(event));
            }
        }
    }

    private final List<Subscription<?>> subscriptions = new CopyOnWriteArrayList<>();

    /** Suscribe un manejador y devuelve un token para desuscribirse. */
    public <T extends GameEvent> AutoCloseable subscribe(Class<T> type, Consumer<? super T> handler) {
        Subscription<T> s = new Subscription<>(type, handler);
        subscriptions.add(s);
        return () -> subscriptions.remove(s);
    }

    public void publish(GameEvent event) {
        for (Subscription<?> s : subscriptions) {
            s.deliver(event);
        }
    }

    /** Agrupa varias suscripciones para cerrarlas juntas al salir de una pantalla. */
    public static final class Subscriptions implements AutoCloseable {
        private final List<AutoCloseable> tokens = new ArrayList<>();

        public void add(AutoCloseable token) {
            tokens.add(token);
        }

        @Override
        public void close() {
            for (AutoCloseable t : tokens) {
                try {
                    t.close();
                } catch (Exception ignored) {
                    // desuscribir nunca debería fallar
                }
            }
            tokens.clear();
        }
    }
}
