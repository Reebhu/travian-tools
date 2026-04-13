package com.travian.interceptor.service;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class TravelTimeServiceTest {

    private final TravelTimeService service = new TravelTimeService();

    @Test
    void wrapsAcrossMapEdges() {
        double distance = service.calculateWrappedDistance(400, -400, 0, 400, 0);

        assertThat(distance).isEqualTo(1.0);
    }

    @Test
    void appliesTournamentSquareOnlyAfterTwentyFields() {
        Duration noBonus = service.calculateTravelTime(30, 10, 1, 0, 0, 0);
        Duration tsTen = service.calculateTravelTime(30, 10, 1, 10, 0, 0);

        assertThat(tsTen).isLessThan(noBonus);
        assertThat(tsTen).isEqualTo(Duration.ofSeconds(8400));
    }

    @Test
    void appliesMapBonusAcrossReturnTrip() {
        Duration normal = service.calculateTravelTime(10, 10, 1, 0, 0, 0);
        Duration mapped = service.calculateTravelTime(10, 10, 1, 0, 50, 0);

        assertThat(mapped).isEqualTo(Duration.ofSeconds(2400));
        assertThat(mapped).isLessThan(normal);
    }

    @Test
    void xTenWorldUsesOfficialTroopSpeedMultiplier() {
        Duration xTenWorld = service.calculateTravelTime(40, 10, 4, 0, 0, 0);

        assertThat(xTenWorld).isEqualTo(Duration.ofHours(1));
    }

    @Test
    void appliesBootsOnlyAfterTwentyFields() {
        Duration withBoots = service.calculateTravelTime(30, 10, 1, 0, 0, 50);

        assertThat(withBoots).isEqualTo(Duration.ofSeconds(9600));
    }
}
