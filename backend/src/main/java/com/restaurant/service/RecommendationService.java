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
// import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private static final double WEIGHT_EFFICIENCY = 0.35;
    private static final double WEIGHT_PREFERENCE = 0.30;
    private static final double WEIGHT_ZONE = 0.10;
    private static final double WEIGHT_WEATHER = 0.20;
    private static final double WEIGHT_BASE = 0.05;
    private static final double BASE_SCORE = 0.1;

    private static final String TERRACE_ZONE = "Terrace";

    private final TableRepository tableRepository;
    private final ReservationRepository reservationRepository;
    private final WeatherService weatherService;

    public RecommendationService(TableRepository tableRepository,
                                  ReservationRepository reservationRepository,
                                  WeatherService weatherService) {
        this.tableRepository = tableRepository;
        this.reservationRepository = reservationRepository;
        this.weatherService = weatherService;
    }

    public SearchResponse search(SearchRequest request) {
        var allTables = tableRepository.findAll();
        var endTime = request.startTime().plusMinutes(request.duration());

        var reservationsOnDate = reservationRepository.findByDate(request.date());

        var weather = weatherService.getCurrentWeather();

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
                .filter(table -> calculateWeatherPenalty(table.getZone(), weather) > -1.0)
                .map(table -> toRecommendation(table, request, weather))
                .sorted(Comparator.comparingDouble(TableRecommendation::score).reversed())
                .toList();

        // Find table combinations only when no single table fits the party
        List<TableCombination> combinations;
        if (recommendations.isEmpty()) {
            var availableTables = allTables.stream()
                    .filter(table -> !table.getFeatures().contains(TableFeature.PRIVATE))
                    .filter(table -> isAvailable(table, request, endTime, reservationsOnDate))
                    .filter(table -> matchesZone(table, request.zone()))
                    .filter(table -> calculateWeatherPenalty(table.getZone(), weather) > -1.0)
                    .toList();
            combinations = findCombinations(availableTables, request, weather);
        } else {
            combinations = List.of();
        }

        String weatherWarning = buildWeatherWarning(weather);

        return new SearchResponse(recommendations, combinations, allTableStatuses, weather, weatherWarning);
    }

    private String buildWeatherWarning(WeatherData weather) {
        if (weather == null) {
            return null;
        }
        double penalty = calculateWeatherPenalty(TERRACE_ZONE, weather);
        if (penalty >= 0.0) {
            return null;
        }
        return String.format("Outdoor seating may be uncomfortable — temperature %.0f°C, wind %.0f m/s",
                weather.temperatureC(), weather.windSpeedKmh() / 3.6);
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
        var overlapping = reservationsOnDate.stream()
                .filter(r -> r.getTableId().equals(table.getId())
                        && r.getStartTime().isBefore(endTime)
                        && r.getEndTime().isAfter(request.startTime()))
                .findFirst();

        if (overlapping.isPresent()) {
            var r = overlapping.get();
            return new TableStatus(
                    table.getId(), table.getName(), table.getZone(), table.getCapacity(),
                    "reserved", table.getFeatures(),
                    r.getId(), r.getGuestName(), r.getStartTime(), r.getEndTime()
            );
        }

        return new TableStatus(
                table.getId(), table.getName(), table.getZone(), table.getCapacity(),
                "available", table.getFeatures()
        );
    }

    TableRecommendation toRecommendation(RestaurantTable table, SearchRequest request, WeatherData weather) {
        var breakdown = calculateScore(table, request, weather);
        double totalScore = (breakdown.efficiency() * WEIGHT_EFFICIENCY)
                + (breakdown.preferenceMatch() * WEIGHT_PREFERENCE)
                + (breakdown.zoneMatch() * WEIGHT_ZONE)
                + (breakdown.weatherPenalty() * WEIGHT_WEATHER)
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

    ScoreBreakdown calculateScore(RestaurantTable table, SearchRequest request, WeatherData weather) {
        double efficiency = calculateEfficiency(table.getCapacity(), request.partySize());
        double preferenceMatch = calculatePreferenceMatch(table.getFeatures(), request.preferences());
        double zoneMatch = calculateZoneMatch(table.getZone(), request.zone());
        double weatherPenalty = calculateWeatherPenalty(table.getZone(), weather);
        return new ScoreBreakdown(efficiency, preferenceMatch, zoneMatch, weatherPenalty, BASE_SCORE);
    }

    double calculateWeatherPenalty(String zone, WeatherData weather) {
        if (weather == null || !TERRACE_ZONE.equalsIgnoreCase(zone)) {
            return 0.0;
        }

        double tempPenalty = 0.0;
        if (weather.temperatureC() <= 5.0) {
            tempPenalty = -1.0;
        } else if (weather.temperatureC() < 15.0) {
            tempPenalty = -(15.0 - weather.temperatureC()) / 10.0;
        }

        double windPenalty = 0.0;
        if (weather.windSpeedKmh() >= 40.0) {
            windPenalty = -1.0;
        } else if (weather.windSpeedKmh() > 20.0) {
            windPenalty = -(weather.windSpeedKmh() - 20.0) / 20.0;
        }

        return Math.min(tempPenalty, windPenalty);
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

    private List<TableCombination> findCombinations(List<RestaurantTable> availableTables,
                                                     SearchRequest request,
                                                     WeatherData weather) {
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

                var breakdown = calculateCombinationScore(t1, t2, request, weather);
                double totalScore = (breakdown.efficiency() * WEIGHT_EFFICIENCY)
                        + (breakdown.preferenceMatch() * WEIGHT_PREFERENCE)
                        + (breakdown.zoneMatch() * WEIGHT_ZONE)
                        + (breakdown.weatherPenalty() * WEIGHT_WEATHER)
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

    private ScoreBreakdown calculateCombinationScore(RestaurantTable t1, RestaurantTable t2,
                                                      SearchRequest request, WeatherData weather) {
        int combined = t1.getCapacity() + t2.getCapacity();
        double efficiency = calculateEfficiency(combined, request.partySize());

        var combinedFeatures = new HashSet<>(t1.getFeatures());
        combinedFeatures.addAll(t2.getFeatures());
        double preferenceMatch = calculatePreferenceMatch(combinedFeatures, request.preferences());

        double zoneMatch = calculateZoneMatch(t1.getZone(), request.zone());

        // Combinations inherit the worst weather penalty of the two tables
        double wp1 = calculateWeatherPenalty(t1.getZone(), weather);
        double wp2 = calculateWeatherPenalty(t2.getZone(), weather);
        double weatherPenalty = Math.min(wp1, wp2);

        return new ScoreBreakdown(efficiency, preferenceMatch, zoneMatch, weatherPenalty, BASE_SCORE);
    }
}
