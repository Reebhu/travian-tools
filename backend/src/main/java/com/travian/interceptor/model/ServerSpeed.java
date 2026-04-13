package com.travian.interceptor.model;

import java.util.Arrays;
import java.util.List;

public enum ServerSpeed {
    X1(1, 1, "x1"),
    X2(2, 2, "x2"),
    X3(3, 2, "x3"),
    X5(5, 2, "x5"),
    X10(10, 4, "x10");

    private final int worldSpeed;
    private final int troopSpeed;
    private final String label;

    ServerSpeed(int worldSpeed, int troopSpeed, String label) {
        this.worldSpeed = worldSpeed;
        this.troopSpeed = troopSpeed;
        this.label = label;
    }

    public int getWorldSpeed() {
        return worldSpeed;
    }

    public int getTroopSpeed() {
        return troopSpeed;
    }

    public String getLabel() {
        return label;
    }

    public static boolean supports(int worldSpeed) {
        return Arrays.stream(values())
                .anyMatch(speed -> speed.worldSpeed == worldSpeed);
    }

    public static List<Integer> supportedMultipliers() {
        return Arrays.stream(values())
                .map(ServerSpeed::getWorldSpeed)
                .toList();
    }

    public static ServerSpeed fromWorldSpeed(int worldSpeed) {
        return Arrays.stream(values())
                .filter(speed -> speed.worldSpeed == worldSpeed)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Supported server speeds are x1, x2, x3, x5, and x10."));
    }
}
