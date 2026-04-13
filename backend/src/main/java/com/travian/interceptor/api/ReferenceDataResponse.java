package com.travian.interceptor.api;

import com.travian.interceptor.model.ReferenceSource;
import com.travian.interceptor.model.TribeDefinition;

import java.util.List;

public record ReferenceDataResponse(
        List<TribeDefinition> tribes,
        List<Integer> worldSizes,
        List<Integer> serverSpeeds,
        List<MapBonusOption> mapBonuses,
        List<BootsOption> bootsOptions,
        List<ReferenceSource> sources
) {
    public record MapBonusOption(String label, double percentBonus) {
    }

    public record BootsOption(String label, double percentBonus) {
    }
}
