package com.restaurant.service;

import com.restaurant.dto.*;
import com.restaurant.model.Reservation;
import com.restaurant.model.RestaurantTable;
import com.restaurant.model.TableFeature;
import com.restaurant.repository.ReservationRepository;
import com.restaurant.repository.TableRepository;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

        int maxCapacity = maxCapacityForParty(request.partySize());

        var recommendations = allTables.stream()
                .filter(table -> table.getCapacity() >= request.partySize())
                .filter(table -> table.getCapacity() <= maxCapacity)
                .filter(table -> isAvailable(table, request, endTime, reservationsOnDate))
                .filter(table -> hasAllPreferences(table, request.preferences()))
                .filter(table -> matchesZone(table, request.zone()))
                .map(table -> toRecommendation(table, request))
                .sorted(Comparator.comparingDouble(TableRecommendation::score).reversed())
                .toList();

        // Find table combinations only when no single table fits the party
        List<TableCombination> combinations;
        if (recommendations.isEmpty()) {
            var availableTables = allTables.stream()
                    .filter(table -> !table.getFeatures().contains(TableFeature.PRIVATE))
                    .filter(table -> isAvailable(table, request, endTime, reservationsOnDate))
                    .filter(table -> matchesZone(table, request.zone()))
                    .toList();
            combinations = findCombinations(availableTables, request);
        } else {
            combinations = List.of();
        }

        return new SearchResponse(recommendations, combinations, allTableStatuses);
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

    private boolean hasAllPreferences(RestaurantTable table, Set<TableFeature> preferences) {
        if (preferences == null || preferences.isEmpty()) {
            return true;
        }
        return table.getFeatures().containsAll(preferences);
    }

    private boolean matchesZone(RestaurantTable table, String requestedZone) {
        if (requestedZone == null || requestedZone.isBlank()) {
            return true;
        }
        return requestedZone.equalsIgnoreCase(table.getZone());
    }

    private double calculateZoneMatch(String tableZone, String requestedZone) {
        if (requestedZone == null || requestedZone.isBlank()) {
            return 1.0;
        }
        return requestedZone.equalsIgnoreCase(tableZone) ? 1.0 : 0.5;
    }

    static int maxCapacityForParty(int partySize) {
        if (partySize <= 2) return 4;
        if (partySize <= 4) return 6;
        return Integer.MAX_VALUE;
    }

    private List<TableCombination> findCombinations(List<RestaurantTable> availableTables, SearchRequest request) {
        var results = new ArrayList<TableCombination>();

        for (int i = 0; i < availableTables.size(); i++) {
            for (int j = i + 1; j < availableTables.size(); j++) {
                var t1 = availableTables.get(i);
                var t2 = availableTables.get(j);

                if (!t1.getZone().equals(t2.getZone())) {
                    continue;
                }

                int combined = t1.getCapacity() + t2.getCapacity();
                if (combined < request.partySize()) {
                    continue;
                }
                if (combined > maxCapacityForParty(request.partySize())) {
                    continue;
                }

                if (!areAdjacent(t1, t2)) {
                    continue;
                }

                var combinedFeatures = new HashSet<>(t1.getFeatures());
                combinedFeatures.addAll(t2.getFeatures());
                combinedFeatures.remove(TableFeature.PRIVATE); // safety

                var breakdown = calculateCombinationScore(t1, t2, request);
                double totalScore = (breakdown.efficiency() * WEIGHT_EFFICIENCY)
                        + (breakdown.preferenceMatch() * WEIGHT_PREFERENCE)
                        + (breakdown.zoneMatch() * WEIGHT_ZONE)
                        + (breakdown.base() * WEIGHT_BASE);

                results.add(new TableCombination(
                        t1.getId(), t1.getName(), t1.getPosX(), t1.getPosY(), t1.getWidth(), t1.getHeight(), t1.getShape(),
                        t2.getId(), t2.getName(), t2.getPosX(), t2.getPosY(), t2.getWidth(), t2.getHeight(), t2.getShape(),
                        t1.getZone(),
                        combined,
                        combinedFeatures,
                        Math.round(totalScore * 100.0) / 100.0,
                        breakdown
                ));
            }
        }

        results.sort((a, b) -> Double.compare(b.score(), a.score()));
        return results.stream().limit(3).toList();
    }

    private boolean areAdjacent(RestaurantTable t1, RestaurantTable t2) {
        double maxGap = 60.0;

        double right1 = t1.getPosX() + t1.getWidth();
        double bottom1 = t1.getPosY() + t1.getHeight();
        double right2 = t2.getPosX() + t2.getWidth();
        double bottom2 = t2.getPosY() + t2.getHeight();

        double dx = Math.max(0, Math.max(t1.getPosX() - right2, t2.getPosX() - right1));
        double dy = Math.max(0, Math.max(t1.getPosY() - bottom2, t2.getPosY() - bottom1));

        return Math.sqrt(dx * dx + dy * dy) <= maxGap;
    }

    private ScoreBreakdown calculateCombinationScore(RestaurantTable t1, RestaurantTable t2, SearchRequest request) {
        int combined = t1.getCapacity() + t2.getCapacity();
        double efficiency = calculateEfficiency(combined, request.partySize());

        var combinedFeatures = new HashSet<>(t1.getFeatures());
        combinedFeatures.addAll(t2.getFeatures());
        double preferenceMatch = calculatePreferenceMatch(combinedFeatures, request.preferences());

        double zoneMatch = calculateZoneMatch(t1.getZone(), request.zone());
        return new ScoreBreakdown(efficiency, preferenceMatch, zoneMatch, BASE_SCORE);
    }
}
