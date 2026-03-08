package com.restaurant.dto;

import com.restaurant.model.TableFeature;

import java.util.Set;

public record TableStatus(
        Long tableId,
        String tableName,
        String zone,
        int capacity,
        String status,
        Set<TableFeature> features
) {
}
