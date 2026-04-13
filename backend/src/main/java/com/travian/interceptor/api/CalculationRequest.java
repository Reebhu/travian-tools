package com.travian.interceptor.api;

import com.travian.interceptor.model.Coordinate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record CalculationRequest(
        @Valid @NotNull Coordinate attackerVillage,
        @Valid @NotNull Coordinate defenderVillage,
        @Valid @NotNull Coordinate interceptorVillage,
        @NotNull Instant landingTime,
        @NotBlank String attackerTribeId,
        String attackerUnitId,
        @NotBlank String interceptorTribeId,
        @NotBlank String interceptorUnitId,
        @Min(200) @Max(800) int worldSize,
        @Min(1) @Max(10) int serverSpeed,
        @Min(0) @Max(20) int attackerTournamentSquareLevel,
        @Min(0) @Max(20) int interceptorTournamentSquareLevel,
        @Min(0) @Max(100) double attackerBootsBonusPercent,
        @Min(0) @Max(100) double interceptorBootsBonusPercent,
        @Min(0) @Max(100) double attackerReturnMapBonusPercent
) {
}
