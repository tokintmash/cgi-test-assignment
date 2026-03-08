package com.restaurant.dto;

import com.restaurant.model.TableFeature;

import java.util.Set;

public record TableRecommendation(
        Long tableId,
        String tableName,
        String zone,
        int capacity,
        Set<TableFeature> features,
        double score,
        ScoreBreakdown scoreBreakdown,
        double posX,
        double posY,
        double width,
        double height,
        String shape
) {
}
