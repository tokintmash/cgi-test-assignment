package com.restaurant.controller;

import com.restaurant.dto.SearchRequest;
import com.restaurant.dto.SearchResponse;
import com.restaurant.model.RestaurantTable;
import com.restaurant.service.RecommendationService;
import com.restaurant.service.TableService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
public class TableController {

    private final TableService tableService;
    private final RecommendationService recommendationService;

    public TableController(TableService tableService, RecommendationService recommendationService) {
        this.tableService = tableService;
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public List<RestaurantTable> getAllTables() {
        return tableService.getAllTables();
    }

    @PostMapping("/search")
    public SearchResponse search(@Valid @RequestBody SearchRequest request) {
        return recommendationService.search(request);
    }
}
