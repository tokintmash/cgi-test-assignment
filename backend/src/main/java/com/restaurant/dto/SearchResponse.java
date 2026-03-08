package com.restaurant.dto;

import java.util.List;

public record SearchResponse(
        List<TableRecommendation> recommendations,
        List<TableStatus> allTables
) {
}
