package com.restaurant.dto;

public record ScoreBreakdown(
        double efficiency,
        double preferenceMatch,
        double zoneMatch,
        double weatherPenalty,
        double base
) {
}
