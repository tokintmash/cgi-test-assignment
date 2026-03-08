package com.restaurant.service;

import com.restaurant.dto.*;
import com.restaurant.model.Reservation;
import com.restaurant.model.RestaurantTable;
import com.restaurant.model.TableFeature;
import com.restaurant.repository.ReservationRepository;
import com.restaurant.repository.TableRepository;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
public class RecommendationService {

    private static final double WEIGHT_EFFICIENCY = 0.40;
    private static final double WEIGHT_PREFERENCE = 0.35;
    private static final double WEIGHT_ZONE = 0.15;
    private static final double WEIGHT_BASE = 0.10;
    private static final double BASE_SCORE = 0.1;

    private final TableRepository tableRepository;
    private final ReservationRepository reservationRepository;

    public RecommendationService(TableRepository tableRepository,
                                  ReservationRepository reservationRepository) {
        this.tableRepository = tableRepository;
        this.reservationRepository = reservationRepository;
    }

    public SearchResponse search(SearchRequest request) {
        var allTables = tableRepository.findAll();
        var endTime = request.startTime().plusMinutes(request.duration());

        var reservationsOnDate = reservationRepository.findByDate(request.date());

        var allTableStatuses = allTables.stream()
                .map(table -> toTableStatus(table, request, endTime, reservationsOnDate))
                .toList();

        var recommendations = allTables.stream()
                .filter(table -> table.getCapacity() >= request.partySize())
                .filter(table -> isAvailable(table, request, endTime, reservationsOnDate))
                .map(table -> toRecommendation(table, request))
                .sorted(Comparator.comparingDouble(TableRecommendation::score).reversed())
                .toList();

        return new SearchResponse(recommendations, allTableStatuses);
    }

    private boolean isAvailable(RestaurantTable table, SearchRequest request,
                                LocalTime endTime, List<Reservation> reservationsOnDate) {
        return reservationsOnDate.stream()
                .noneMatch(r -> r.getTableId().equals(table.getId())
                        && r.getStartTime().isBefore(endTime)
                        && r.getEndTime().isAfter(request.startTime()));
    }

    private TableStatus toTableStatus(RestaurantTable table, SearchRequest request,
                                      LocalTime endTime, List<Reservation> reservationsOnDate) {
        var available = isAvailable(table, request, endTime, reservationsOnDate);
        return new TableStatus(
                table.getId(),
                table.getName(),
                table.getZone(),
                table.getCapacity(),
                available ? "available" : "reserved",
                table.getFeatures()
        );
    }

    TableRecommendation toRecommendation(RestaurantTable table, SearchRequest request) {
        var breakdown = calculateScore(table, request);
        double totalScore = (breakdown.efficiency() * WEIGHT_EFFICIENCY)
                + (breakdown.preferenceMatch() * WEIGHT_PREFERENCE)
                + (breakdown.zoneMatch() * WEIGHT_ZONE)
                + (breakdown.base() * WEIGHT_BASE);

        return new TableRecommendation(
                table.getId(),
                table.getName(),
                table.getZone(),
                table.getCapacity(),
                table.getFeatures(),
                Math.round(totalScore * 100.0) / 100.0,
                breakdown,
                table.getPosX(),
                table.getPosY(),
                table.getWidth(),
                table.getHeight(),
                table.getShape()
        );
    }

    ScoreBreakdown calculateScore(RestaurantTable table, SearchRequest request) {
        double efficiency = calculateEfficiency(table.getCapacity(), request.partySize());
        double preferenceMatch = calculatePreferenceMatch(table.getFeatures(), request.preferences());
        double zoneMatch = calculateZoneMatch(table.getZone(), request.zone());
        return new ScoreBreakdown(efficiency, preferenceMatch, zoneMatch, BASE_SCORE);
    }

    private double calculateEfficiency(int capacity, int partySize) {
        double score = 1.0 - ((double) (capacity - partySize) / capacity);
        return Math.max(score, 0.3);
    }

    private double calculatePreferenceMatch(Set<TableFeature> tableFeatures, Set<TableFeature> preferences) {
        if (preferences == null || preferences.isEmpty()) {
            return 1.0;
        }
        long matched = preferences.stream()
                .filter(tableFeatures::contains)
                .count();
        return (double) matched / preferences.size();
    }

    private double calculateZoneMatch(String tableZone, String requestedZone) {
        if (requestedZone == null || requestedZone.isBlank()) {
            return 1.0;
        }
        return requestedZone.equalsIgnoreCase(tableZone) ? 1.0 : 0.5;
    }
}
