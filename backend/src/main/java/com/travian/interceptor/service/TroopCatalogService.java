package com.travian.interceptor.service;

import com.travian.interceptor.api.ReferenceDataResponse;
import com.travian.interceptor.model.ReferenceSource;
import com.travian.interceptor.model.ServerSpeed;
import com.travian.interceptor.model.Tribe;
import com.travian.interceptor.model.TribeDefinition;
import com.travian.interceptor.model.TroopUnit;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class TroopCatalogService {

    private final List<TribeDefinition> tribes = Arrays.stream(Tribe.values())
            .map(this::toDefinition)
            .toList();

    private final List<ReferenceSource> sources = List.of(
            new ReferenceSource("Troops Speed Increase & Tournament Square", "https://support.travian.com/en/support/solutions/articles/7000068300-troops-speed-increase-tournament-square"),
            new ReferenceSource("Hero Armour Items", "https://support.travian.com/en/support/solutions/articles/7000068828-hero-armour-items"),
            new ReferenceSource("Hero Left-Hand Items", "https://support.travian.com/en/support/solutions/articles/7000068826-hero-left-hand-items"),
            new ReferenceSource("Regular Infantry and Cavalry Units comparison table", "https://support.travian.com/en/support/solutions/articles/7000090973")
    );

    public ReferenceDataResponse getReferenceData() {
        return new ReferenceDataResponse(
                tribes,
                List.of(200, 400, 800),
                ServerSpeed.supportedMultipliers(),
                List.of(
                        new ReferenceDataResponse.MapBonusOption("No map", 0),
                        new ReferenceDataResponse.MapBonusOption("Small Map (+30%)", 30),
                        new ReferenceDataResponse.MapBonusOption("Map (+40%)", 40),
                        new ReferenceDataResponse.MapBonusOption("Large Map (+50%)", 50)
                ),
                List.of(
                        new ReferenceDataResponse.BootsOption("No boots", 0),
                        new ReferenceDataResponse.BootsOption("Boots of the Mercenary (+25% after 20)", 25),
                        new ReferenceDataResponse.BootsOption("Boots of the Warrior (+50% after 20)", 50),
                        new ReferenceDataResponse.BootsOption("Boots of the Archon (+75% after 20)", 75)
                ),
                sources
        );
    }

    public TribeDefinition getTribe(String tribeId) {
        Tribe tribe = Tribe.fromId(tribeId);
        return toDefinition(tribe);
    }

    public TroopUnit getUnit(String tribeId, String unitId) {
        return TroopUnit.fromIds(tribeId, unitId);
    }

    public List<TroopUnit> getAttackerUnits(String tribeId, String attackerUnitId) {
        Tribe tribe = Tribe.fromId(tribeId);
        if (attackerUnitId == null || attackerUnitId.isBlank() || "all".equals(attackerUnitId)) {
            return TroopUnit.forTribe(tribe);
        }

        return List.of(getUnit(tribeId, attackerUnitId));
    }

    private TribeDefinition toDefinition(Tribe tribe) {
        return new TribeDefinition(
                tribe.getId(),
                tribe.getDisplayName(),
                tribe.getSourceNote(),
                TroopUnit.forTribe(tribe)
        );
    }
}
