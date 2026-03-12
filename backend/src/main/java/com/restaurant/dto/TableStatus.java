package com.restaurant.dto;

import com.restaurant.model.TableFeature;

import java.time.LocalTime;
import java.util.Set;

public record TableStatus(
        Long tableId,
        String tableName,
        String zone,
        int capacity,
        String status,
        Set<TableFeature> features,
        Long reservationId,
        String guestName,
        LocalTime reservationStart,
        LocalTime reservationEnd
) {
    public TableStatus(Long tableId, String tableName, String zone, int capacity,
                       String status, Set<TableFeature> features) {
        this(tableId, tableName, zone, capacity, status, features, null, null, null, null);
    }
}
