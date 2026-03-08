package com.restaurant.dto;

import com.restaurant.model.TableFeature;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public record SearchRequest(
        @NotNull LocalDate date,
        @NotNull LocalTime startTime,
        @Min(1) int partySize,
        int duration,
        String zone,
        Set<TableFeature> preferences
) {
    public SearchRequest {
        if (duration == 0) {
            duration = 120;
        }
        if (preferences == null) {
            preferences = Set.of();
        }
    }
}
