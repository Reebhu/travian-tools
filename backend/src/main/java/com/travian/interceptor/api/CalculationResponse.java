package com.travian.interceptor.api;

import java.time.Instant;
import java.util.List;

public record CalculationResponse(
        DistanceSummary distances,
        InterceptorSummary interceptor,
        List<CandidateWindow> candidates
) {
    public record DistanceSummary(
            double attackerToDefender,
            double interceptorToAttacker
    ) {
    }

    public record InterceptorSummary(
            String tribeId,
            String unitId,
            String unitName,
            double unitBaseSpeed
    ) {
    }

    public record CandidateWindow(
            String attackerUnitId,
            String attackerUnitName,
            double attackerUnitBaseSpeed,
            String category,
            Instant estimatedAttackSentAt,
            Instant attackLandingAt,
            Instant estimatedReturnHomeAt,
            Instant interceptLaunchAt,
            long outboundTravelSeconds,
            long returnTravelSeconds,
            long interceptorTravelSeconds,
            String speedSummary
    ) {
    }
}
