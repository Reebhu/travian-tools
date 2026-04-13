package com.travian.interceptor.service;

import com.travian.interceptor.api.CalculationRequest;
import com.travian.interceptor.api.CalculationResponse;
import com.travian.interceptor.model.Coordinate;
import com.travian.interceptor.model.ServerSpeed;
import com.travian.interceptor.model.TroopUnit;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
public class InterceptCalculatorService {

    private final TroopCatalogService troopCatalogService;
    private final TravelTimeService travelTimeService;

    public InterceptCalculatorService(
            TroopCatalogService troopCatalogService,
            TravelTimeService travelTimeService
    ) {
        this.troopCatalogService = troopCatalogService;
        this.travelTimeService = travelTimeService;
    }

    public CalculationResponse calculate(CalculationRequest request) {
        validateCoordinateRange(request.worldSize(), request.attackerVillage(), "Attacker");
        validateCoordinateRange(request.worldSize(), request.defenderVillage(), "Defender");
        validateCoordinateRange(request.worldSize(), request.interceptorVillage(), "Interceptor");
        ServerSpeed serverSpeed = ServerSpeed.fromWorldSpeed(request.serverSpeed());

        TroopUnit interceptorUnit = troopCatalogService.getUnit(request.interceptorTribeId(), request.interceptorUnitId());
        List<TroopUnit> attackerUnits = troopCatalogService.getAttackerUnits(request.attackerTribeId(), request.attackerUnitId());

        double attackerDistance = travelTimeService.calculateWrappedDistance(
                request.worldSize(),
                request.attackerVillage().x(),
                request.attackerVillage().y(),
                request.defenderVillage().x(),
                request.defenderVillage().y()
        );

        double interceptorDistance = travelTimeService.calculateWrappedDistance(
                request.worldSize(),
                request.interceptorVillage().x(),
                request.interceptorVillage().y(),
                request.attackerVillage().x(),
                request.attackerVillage().y()
        );

        Duration interceptorTravel = travelTimeService.calculateTravelTime(
                interceptorDistance,
                interceptorUnit.getBaseSpeed(),
                serverSpeed.getTroopSpeed(),
                request.interceptorTournamentSquareLevel(),
                0,
                request.interceptorBootsBonusPercent()
        );

        List<CalculationResponse.CandidateWindow> candidates = attackerUnits.stream()
                .map(attackerUnit -> buildCandidate(
                        request,
                attackerDistance,
                interceptorTravel,
                serverSpeed,
                attackerUnit
                ))
                .sorted(Comparator.comparing(CalculationResponse.CandidateWindow::estimatedReturnHomeAt))
                .toList();

        return new CalculationResponse(
                new CalculationResponse.DistanceSummary(roundToTwo(attackerDistance), roundToTwo(interceptorDistance)),
                new CalculationResponse.InterceptorSummary(
                        request.interceptorTribeId(),
                        interceptorUnit.getId(),
                        interceptorUnit.getName(),
                        interceptorUnit.getBaseSpeed()
                ),
                candidates
        );
    }

    private CalculationResponse.CandidateWindow buildCandidate(
            CalculationRequest request,
            double attackerDistance,
            Duration interceptorTravel,
            ServerSpeed serverSpeed,
            TroopUnit attackerUnit
    ) {
        Duration outboundTravel = travelTimeService.calculateTravelTime(
                attackerDistance,
                attackerUnit.getBaseSpeed(),
                serverSpeed.getTroopSpeed(),
                request.attackerTournamentSquareLevel(),
                0,
                request.attackerBootsBonusPercent()
        );

        Duration returnTravel = travelTimeService.calculateTravelTime(
                attackerDistance,
                attackerUnit.getBaseSpeed(),
                serverSpeed.getTroopSpeed(),
                request.attackerTournamentSquareLevel(),
                request.attackerReturnMapBonusPercent(),
                request.attackerBootsBonusPercent()
        );

        Instant sentAt = request.landingTime().minus(outboundTravel);
        Instant returnHomeAt = request.landingTime().plus(returnTravel);
        Instant interceptLaunchAt = returnHomeAt.minus(interceptorTravel);

        String speedSummary = buildSpeedSummary(request);

        return new CalculationResponse.CandidateWindow(
                attackerUnit.getId(),
                attackerUnit.getName(),
                attackerUnit.getBaseSpeed(),
                attackerUnit.getCategory().name(),
                sentAt,
                request.landingTime(),
                returnHomeAt,
                interceptLaunchAt,
                outboundTravel.toSeconds(),
                returnTravel.toSeconds(),
                interceptorTravel.toSeconds(),
                speedSummary
        );
    }

    private void validateCoordinateRange(int worldSize, Coordinate coordinate, String label) {
        if (coordinate.x() < -worldSize || coordinate.x() > worldSize
                || coordinate.y() < -worldSize || coordinate.y() > worldSize) {
            throw new IllegalArgumentException(label + " village coordinates must stay within ±" + worldSize + ".");
        }
    }

    private double roundToTwo(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private String buildSpeedSummary(CalculationRequest request) {
        StringBuilder summary = new StringBuilder();

        if (request.attackerTournamentSquareLevel() > 0) {
            summary.append("Attacker TS ").append(request.attackerTournamentSquareLevel());
        }

        if (request.attackerReturnMapBonusPercent() > 0) {
            if (summary.length() > 0) {
                summary.append(" • ");
            }
            summary.append("Return map +").append(stripTrailingZeros(request.attackerReturnMapBonusPercent())).append("%");
        }

        if (request.attackerBootsBonusPercent() > 0) {
            if (summary.length() > 0) {
                summary.append(" • ");
            }
            summary.append("Attacker boots +").append(stripTrailingZeros(request.attackerBootsBonusPercent())).append("% after 20");
        }

        if (request.interceptorTournamentSquareLevel() > 0) {
            if (summary.length() > 0) {
                summary.append(" • ");
            }
            summary.append("Interceptor TS ").append(request.interceptorTournamentSquareLevel());
        }

        if (request.interceptorBootsBonusPercent() > 0) {
            if (summary.length() > 0) {
                summary.append(" • ");
            }
            summary.append("Interceptor boots +").append(stripTrailingZeros(request.interceptorBootsBonusPercent())).append("% after 20");
        }

        return summary.length() == 0 ? "Base speed only" : summary.toString();
    }

    private String stripTrailingZeros(double value) {
        if (value == Math.rint(value)) {
            return Integer.toString((int) value);
        }
        return Double.toString(value);
    }
}
