package com.travian.interceptor.model;

import java.util.List;

public record TribeDefinition(
        String id,
        String name,
        String sourceNote,
        List<TroopUnit> units
) {
}
