package com.restaurant.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationRequest(
        @NotNull Long tableId,
        @NotNull LocalDate date,
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
}
