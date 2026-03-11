package com.restaurant.dto;

import com.restaurant.model.TableFeature;
import java.util.Set;

public record TableCombination(
        Long tableId1,
        String tableName1,
        double posX1, double posY1, double width1, double height1, String shape1,
        Long tableId2,
        String tableName2,
        double posX2, double posY2, double width2, double height2, String shape2,
        String zone,
        int combinedCapacity,
        Set<TableFeature> combinedFeatures,
        double score,
        ScoreBreakdown scoreBreakdown
) {
}
