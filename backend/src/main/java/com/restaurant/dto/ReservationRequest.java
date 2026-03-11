package com.restaurant.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ReservationRequest(
        Long tableId,
        List<Long> tableIds,
        @NotNull @FutureOrPresent LocalDate date,
        @NotNull LocalTime startTime,
        @Min(30) int duration,
        @Min(1) int partySize,
        @NotBlank String guestName
) {
    public ReservationRequest {
        if (duration == 0) {
            duration = 120;
        }
    }

    public List<Long> resolvedTableIds() {
        if (tableIds != null && !tableIds.isEmpty()) {
            return tableIds;
        }
        if (tableId != null) {
            return List.of(tableId);
        }
        return List.of();
    }
}
