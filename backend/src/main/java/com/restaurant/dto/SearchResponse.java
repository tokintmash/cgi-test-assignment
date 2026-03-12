package com.restaurant.dto;

import java.util.List;

public record SearchResponse(
        List<TableRecommendation> recommendations,
        List<TableCombination> combinations,
        List<TableStatus> allTables,
        WeatherData weather,
        String weatherWarning
) {
}
