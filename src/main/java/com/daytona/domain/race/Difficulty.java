package com.daytona.domain.race;

/** Dificultad: afecta la velocidad de la IA y el tiempo extra de los checkpoints. */
public enum Difficulty {
    EASY("Fácil", 0.90, 1.25),
    NORMAL("Normal", 0.97, 1.12),
    HARD("Difícil", 1.03, 1.04),
    EXPERT("Experto", 1.08, 0.98);

    private final String label;
    private final double aiSkill;
    private final double timeFactor;

    private Difficulty(String label, double aiSkill, double timeFactor) {
        this.label = label;
        this.aiSkill = aiSkill;
        this.timeFactor = timeFactor;
    }

    public String label() {
        return label;
    }

    public double aiSkill() {
        return aiSkill;
    }

    public double timeFactor() {
        return timeFactor;
    }
}
