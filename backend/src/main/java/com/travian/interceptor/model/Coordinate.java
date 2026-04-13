package com.travian.interceptor.model;

import jakarta.validation.constraints.NotNull;

public record Coordinate(
        @NotNull Integer x,
        @NotNull Integer y
) {
}
