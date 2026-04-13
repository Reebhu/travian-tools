package com.travian.interceptor.model;

import java.util.Arrays;

public enum Tribe {
    ROMANS(
            "romans",
            "Romans",
            "Infantry, cavalry, and scout base speeds match the official regular-world comparison table."
    ),
    TEUTONS(
            "teutons",
            "Teutons",
            "Infantry, cavalry, and scout base speeds match the official regular-world comparison table."
    ),
    GAULS(
            "gauls",
            "Gauls",
            "Infantry, cavalry, and scout base speeds match the official regular-world comparison table."
    ),
    EGYPTIANS(
            "egyptians",
            "Egyptians",
            "Infantry, cavalry, and scout base speeds match the official regular-world comparison table."
    ),
    HUNS(
            "huns",
            "Huns",
            "Infantry, cavalry, and scout base speeds match the official regular-world comparison table."
    );

    private final String id;
    private final String displayName;
    private final String sourceNote;

    Tribe(String id, String displayName, String sourceNote) {
        this.id = id;
        this.displayName = displayName;
        this.sourceNote = sourceNote;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSourceNote() {
        return sourceNote;
    }

    public static Tribe fromId(String tribeId) {
        return Arrays.stream(values())
                .filter(tribe -> tribe.id.equals(tribeId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown tribe: " + tribeId));
    }
}
