package com.daytona.domain.race;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Lleva los puntos de un campeonato a lo largo de varias pistas. */
public final class ChampionshipSession {

    private static final int[] POINTS = {10, 8, 6, 5, 4, 3, 2, 1};

    public static final class Standing {
        private final String name;
        private final boolean player;
        private final int points;
        private final int bodyRgb;

        public Standing(String name, boolean player, int points, int bodyRgb) {
            this.name = name;
            this.player = player;
            this.points = points;
            this.bodyRgb = bodyRgb;
        }

        public String name() {
            return name;
        }

        public boolean player() {
            return player;
        }

        public int points() {
            return points;
        }

        public int bodyRgb() {
            return bodyRgb;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Standing)) {
                return false;
            }
            Standing other = (Standing) o;
            return java.util.Objects.equals(name, other.name) && java.util.Objects.equals(player, other.player) && java.util.Objects.equals(points, other.points) && java.util.Objects.equals(bodyRgb, other.bodyRgb);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(name, player, points, bodyRgb);
        }
    }

    private final List<String> trackIds;
    private final boolean mirror;
    private final Map<String, Standing> table = new LinkedHashMap<>();
    private int round;

    public ChampionshipSession(List<String> trackIds, boolean mirror) {
        this.trackIds = Collections.unmodifiableList(new ArrayList<>(trackIds));
        this.mirror = mirror;
    }

    public String currentTrackId() {
        return trackIds.get(Math.min(round, trackIds.size() - 1));
    }

    public boolean mirror() {
        return mirror;
    }

    public int round() {
        return round + 1;
    }

    public int rounds() {
        return trackIds.size();
    }

    public boolean isComplete() {
        return round >= trackIds.size();
    }

    public long seedForRound() {
        return 1000L + round;
    }

    public static int pointsFor(int position) {
        return position >= 1 && position <= POINTS.length ? POINTS[position - 1] : 0;
    }

    public void addResults(List<RaceResult> results) {
        for (RaceResult r : results) {
            Standing prev = table.getOrDefault(r.name(), new Standing(r.name(), r.player(), 0, r.bodyRgb()));
            table.put(r.name(), new Standing(r.name(), r.player(), prev.points() + pointsFor(r.position()), r.bodyRgb()));
        }
        round++;
    }

    public List<Standing> standings() {
        List<Standing> list = new ArrayList<>(table.values());
        list.sort((a, b) -> Integer.compare(b.points(), a.points()));
        return Collections.unmodifiableList(list);
    }

    public int playerRank() {
        List<Standing> s = standings();
        for (int i = 0; i < s.size(); i++) {
            if (s.get(i).player()) {
                return i + 1;
            }
        }
        return s.size();
    }
}
