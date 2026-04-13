package com.travian.interceptor.service;

import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class TravelTimeService {

    public double calculateWrappedDistance(int worldSize, int x1, int y1, int x2, int y2) {
        int mapWidth = (worldSize * 2) + 1;
        int dx = Math.abs(x1 - x2);
        int dy = Math.abs(y1 - y2);
        int wrappedDx = Math.min(dx, mapWidth - dx);
        int wrappedDy = Math.min(dy, mapWidth - dy);

        return Math.hypot(wrappedDx, wrappedDy);
    }

    public Duration calculateTravelTime(
            double distance,
            double unitBaseSpeed,
            int troopSpeedMultiplier,
            int tournamentSquareLevel,
            double globalSpeedBonusPercent,
            double postTwentySpeedBonusPercent
    ) {
        double baseSpeed = unitBaseSpeed * troopSpeedMultiplier;
        double globalMultiplier = 1.0 + (globalSpeedBonusPercent / 100.0);
        double firstLegSpeed = baseSpeed * globalMultiplier;
        double postTwentyMultiplier = 1.0 + (tournamentSquareLevel * 0.2) + (postTwentySpeedBonusPercent / 100.0);
        double laterLegSpeed = firstLegSpeed * postTwentyMultiplier;

        double hours;
        if (distance <= 20.0) {
            hours = distance / firstLegSpeed;
        } else {
            hours = (20.0 / firstLegSpeed) + ((distance - 20.0) / laterLegSpeed);
        }

        return Duration.ofSeconds(Math.round(hours * 3600.0));
    }
}
