package com.daytona.domain.car;

import java.util.List;
import java.util.Random;

/** Patrón Factory: autos seleccionables y libreas de los rivales. */
public final class CarCatalog {

    private static final List<CarSpec> PLAYABLE = java.util.Collections.unmodifiableList(java.util.Arrays.asList(
            new CarSpec("falcon", "Falcon GT", "Equilibrado. El auto ideal para empezar.",
                    0xD7261E, 0xF4F4F4, 312, 1.00, 1.00, 1.00, 1.00, 4, 55),
            new CarSpec("thunder", "Thunder V8", "Velocidad punta brutal, pero cuesta doblar.",
                    0x1747B8, 0xF7C919, 332, 0.88, 0.90, 0.92, 0.95, 4, 23),
            new CarSpec("viper", "Viper RS", "Liviano: acelera y dobla como ninguno.",
                    0xF2C21B, 0x151515, 296, 1.16, 1.13, 1.12, 1.10, 4, 7),
            new CarSpec("comet", "Comet X", "Prototipo de 6 marchas. Solo para expertos.",
                    0x16A05A, 0xF4F4F4, 348, 0.97, 0.96, 1.02, 1.05, 6, 99)));

    private static final int[][] RIVAL_LIVERIES = {
            {0xF07F13, 0x1A1A1A}, {0x8E2DE2, 0xFFFFFF}, {0x0FA3B1, 0xFFFFFF}, {0xE5E5E5, 0xD7261E},
            {0x222222, 0xF2C21B}, {0xE83F8C, 0xFFFFFF}, {0x5B8C2A, 0xF4F4F4}, {0x7A4B2A, 0xF2E3C6},
            {0x2E86DE, 0xF5F5F5}, {0xB71540, 0xF5F5F5}, {0x9AA5B1, 0x1E3799}, {0xF5F5F5, 0x0A3D62},
            {0x38ADA9, 0x1E272E}, {0xFA983A, 0xFFFFFF}, {0x6A89CC, 0xFFFFFF}, {0xC23616, 0xFBC531},
            {0x44BD32, 0x2F3640}, {0x40407A, 0xF5F6FA}, {0xEB2F06, 0x1E272E}, {0x1E3799, 0xFBC531}};

    private static final String[] RIVAL_NAMES = {
            "R. SOLANO", "M. KAITO", "J. BRENNER", "L. FARIAS", "D. OKAFOR", "S. LINDQVIST", "P. MOREAU",
            "T. HAYES", "A. ROSSETTI", "K. NOVAK", "E. VARGAS", "B. TANAKA", "C. DUBOIS", "F. ALMEIDA",
            "G. KOWALSKI", "H. NIELSEN", "I. PETROV", "N. OCONNOR", "O. SILVA", "V. MARQUEZ"};

    public List<CarSpec> playable() {
        return PLAYABLE;
    }

    public CarSpec byId(String id) {
        return PLAYABLE.stream().filter(c -> c.id().equals(id)).findFirst().orElse(PLAYABLE.get(0));
    }

    public int indexOf(String id) {
        for (int i = 0; i < PLAYABLE.size(); i++) {
            if (PLAYABLE.get(i).id().equals(id)) {
                return i;
            }
        }
        return 0;
    }

    /** Auto rival {@code i}: base de un auto jugable con librea y número propios. */
    public CarSpec rival(int i, long seed) {
        Random r = new Random(seed * 31 + i);
        CarSpec base = PLAYABLE.get(r.nextInt(3));
        int[] livery = RIVAL_LIVERIES[i % RIVAL_LIVERIES.length];
        return base.withLivery(livery[0], livery[1], 2 + (i * 7) % 97);
    }

    /** Nombre único del rival {@code i} (con 40 autos en pista se agregan sufijos). */
    public String rivalName(int i) {
        String base = RIVAL_NAMES[i % RIVAL_NAMES.length];
        int round = i / RIVAL_NAMES.length;
        return round == 0 ? base : base + " " + (round + 1);
    }
}
