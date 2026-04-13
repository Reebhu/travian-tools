package com.travian.interceptor.service;

import com.travian.interceptor.api.CalculationRequest;
import com.travian.interceptor.api.CalculationResponse;
import com.travian.interceptor.model.Coordinate;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InterceptCalculatorServiceTest {

    private final InterceptCalculatorService service = new InterceptCalculatorService(
            new TroopCatalogService(),
            new TravelTimeService()
    );

    @Test
    void calculatesCandidateWindowsForAllSelectedAttackerUnits() {
        CalculationRequest request = new CalculationRequest(
                new Coordinate(0, 0),
                new Coordinate(30, 0),
                new Coordinate(10, 0),
                Instant.parse("2026-04-13T12:00:00Z"),
                "teutons",
                "all",
                "gauls",
                "theutates-thunder",
                400,
                1,
                10,
                0,
                0,
                0,
                30
        );

        CalculationResponse response = service.calculate(request);

        assertThat(response.candidates()).hasSize(10);
        assertThat(response.distances().attackerToDefender()).isEqualTo(30.0);
        assertThat(response.distances().interceptorToAttacker()).isEqualTo(10.0);
        assertThat(response.interceptor().unitName()).isEqualTo("Theutates Thunder");
        assertThat(response.candidates().get(0).interceptorTravelSeconds()).isPositive();
    }

    @Test
    void narrowsToSingleChosenAttackerUnit() {
        CalculationRequest request = new CalculationRequest(
                new Coordinate(0, 0),
                new Coordinate(15, 0),
                new Coordinate(5, 0),
                Instant.parse("2026-04-13T12:00:00Z"),
                "romans",
                "equites-caesaris",
                "romans",
                "equites-imperatoris",
                400,
                1,
                0,
                0,
                0,
                0,
                0
        );

        CalculationResponse response = service.calculate(request);

        assertThat(response.candidates()).singleElement()
                .satisfies(candidate -> assertThat(candidate.attackerUnitName()).isEqualTo("Equites Caesaris"));
    }

    @Test
    void supportsNamedServerSpeeds() {
        CalculationRequest request = new CalculationRequest(
                new Coordinate(0, 0),
                new Coordinate(40, 0),
                new Coordinate(5, 0),
                Instant.parse("2026-04-13T12:00:00Z"),
                "romans",
                "equites-caesaris",
                "romans",
                "equites-imperatoris",
                400,
                10,
                0,
                0,
                0,
                0,
                0
        );

        CalculationResponse response = service.calculate(request);

        assertThat(response.candidates()).singleElement()
                .satisfies(candidate -> assertThat(candidate.outboundTravelSeconds()).isEqualTo(3600));
    }

    @Test
    void rejectsUnsupportedServerSpeeds() {
        CalculationRequest request = new CalculationRequest(
                new Coordinate(0, 0),
                new Coordinate(15, 0),
                new Coordinate(5, 0),
                Instant.parse("2026-04-13T12:00:00Z"),
                "romans",
                "equites-caesaris",
                "romans",
                "equites-imperatoris",
                400,
                4,
                0,
                0,
                0,
                0,
                0
        );

        assertThatThrownBy(() -> service.calculate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Supported server speeds are x1, x2, x3, x5, and x10.");
    }
}
