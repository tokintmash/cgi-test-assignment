package com.restaurant.service;

import com.restaurant.dto.SearchRequest;
import com.restaurant.dto.WeatherData;
import com.restaurant.model.Reservation;
import com.restaurant.model.RestaurantTable;
import com.restaurant.model.TableFeature;
import com.restaurant.repository.ReservationRepository;
import com.restaurant.repository.TableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private TableRepository tableRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private WeatherService weatherService;

    private RecommendationService service;

    private static final LocalDate DATE = LocalDate.of(2026, 3, 10);
    private static final LocalTime TIME = LocalTime.of(19, 0);

    @BeforeEach
    void setUp() {
        service = new RecommendationService(tableRepository, reservationRepository, weatherService);
    }

    private RestaurantTable createTable(Long id, String name, int capacity, String zone, Set<TableFeature> features) {
        return new RestaurantTable(id, name, capacity, zone, 0, 0, 60, 60, "rectangle", features);
    }

    @Test
    void perfectMatch_scoresHighest() {
        var table = createTable(1L, "W1", 4, "Window", Set.of(TableFeature.WINDOW, TableFeature.ACCESSIBLE));
        var request = new SearchRequest(DATE, TIME, 4, 120, "Window", Set.of(TableFeature.WINDOW, TableFeature.ACCESSIBLE));

        when(tableRepository.findAll()).thenReturn(List.of(table));
        when(reservationRepository.findByDate(DATE)).thenReturn(List.of());
        when(weatherService.getCurrentWeather()).thenReturn(null);

        var response = service.search(request);

        assertEquals(1, response.recommendations().size());
        var rec = response.recommendations().get(0);
        assertEquals(1L, rec.tableId());
        assertEquals(1.0, rec.scoreBreakdown().efficiency());
        assertEquals(1.0, rec.scoreBreakdown().preferenceMatch());
        assertEquals(1.0, rec.scoreBreakdown().zoneMatch());
        assertEquals(0.0, rec.scoreBreakdown().weatherPenalty());
        // total = (1.0*0.35) + (1.0*0.30) + (1.0*0.10) + (0.0*0.20) + (0.1*0.05) = 0.755 → rounds to 0.75
        assertEquals(0.75, rec.score());
    }

    @Test
    void partialMatch_isExcluded() {
        var table = createTable(1L, "M1", 6, "Main Hall", Set.of(TableFeature.ACCESSIBLE));
        var request = new SearchRequest(DATE, TIME, 4, 120, "Window", Set.of(TableFeature.WINDOW, TableFeature.ACCESSIBLE));

        when(tableRepository.findAll()).thenReturn(List.of(table));
        when(reservationRepository.findByDate(DATE)).thenReturn(List.of());
        when(weatherService.getCurrentWeather()).thenReturn(null);

        var response = service.search(request);

        assertTrue(response.recommendations().isEmpty());
    }

    @Test
    void noPreferences_defaultsToFullPreferenceScore() {
        var table = createTable(1L, "W1", 4, "Window", Set.of(TableFeature.WINDOW));
        var request = new SearchRequest(DATE, TIME, 4, 120, null, null);

        when(tableRepository.findAll()).thenReturn(List.of(table));
        when(reservationRepository.findByDate(DATE)).thenReturn(List.of());
        when(weatherService.getCurrentWeather()).thenReturn(null);

        var response = service.search(request);

        assertEquals(1, response.recommendations().size());
        var rec = response.recommendations().get(0);
        assertEquals(1.0, rec.scoreBreakdown().preferenceMatch());
        assertEquals(1.0, rec.scoreBreakdown().zoneMatch());
    }

    @Test
    void oversizedTable_getsLowerEfficiency() {
        var small = createTable(1L, "W1", 2, "Window", Set.of());
        var large = createTable(2L, "M1", 4, "Window", Set.of());
        var request = new SearchRequest(DATE, TIME, 2, 120, null, null);

        when(tableRepository.findAll()).thenReturn(List.of(small, large));
        when(reservationRepository.findByDate(DATE)).thenReturn(List.of());
        when(weatherService.getCurrentWeather()).thenReturn(null);

        var response = service.search(request);

        assertEquals(2, response.recommendations().size());
        var first = response.recommendations().get(0);
        var second = response.recommendations().get(1);
        // small table (capacity=2 for party=2) should rank higher
        assertEquals(1L, first.tableId());
        assertEquals(1.0, first.scoreBreakdown().efficiency());
        // large table (capacity=4 for party=2): efficiency = 1.0 - 2/4 = 0.5
        assertEquals(0.5, second.scoreBreakdown().efficiency(), 0.001);
        assertTrue(first.score() > second.score());
    }

    @Test
    void noResults_whenAllTablesTooSmall() {
        var table = createTable(1L, "W1", 2, "Window", Set.of());
        var request = new SearchRequest(DATE, TIME, 6, 120, null, null);

        when(tableRepository.findAll()).thenReturn(List.of(table));
        when(reservationRepository.findByDate(DATE)).thenReturn(List.of());
        when(weatherService.getCurrentWeather()).thenReturn(null);

        var response = service.search(request);

        assertTrue(response.recommendations().isEmpty());
        assertEquals(1, response.allTables().size());
    }

    @Test
    void smallParty_excludesOversizedTables() {
        var cap4 = createTable(1L, "W1", 4, "Window", Set.of());
        var cap6 = createTable(2L, "M1", 6, "Main Hall", Set.of());
        var cap8 = createTable(3L, "M2", 8, "Main Hall", Set.of());

        when(tableRepository.findAll()).thenReturn(List.of(cap4, cap6, cap8));
        when(reservationRepository.findByDate(DATE)).thenReturn(List.of());
        when(weatherService.getCurrentWeather()).thenReturn(null);

        // Party of 2: max capacity = 4
        var request2 = new SearchRequest(DATE, TIME, 2, 120, null, null);
        var response2 = service.search(request2);
        assertEquals(1, response2.recommendations().size());
        assertEquals(1L, response2.recommendations().get(0).tableId());

        // Party of 4: max capacity = 6
        var request4 = new SearchRequest(DATE, TIME, 4, 120, null, null);
        var response4 = service.search(request4);
        assertEquals(2, response4.recommendations().size());
        assertTrue(response4.recommendations().stream().noneMatch(r -> r.tableId() == 3L));

        // Party of 5: no cap
        var request5 = new SearchRequest(DATE, TIME, 5, 120, null, null);
        var response5 = service.search(request5);
        assertEquals(2, response5.recommendations().size());
    }

    @Test
    void reservedTables_areExcludedFromRecommendations() {
        var table = createTable(1L, "W1", 4, "Window", Set.of());
        var request = new SearchRequest(DATE, TIME, 2, 120, null, null);

        var overlappingReservation = new Reservation(1L, DATE, LocalTime.of(18, 30), LocalTime.of(20, 30), 2, "Guest");

        when(tableRepository.findAll()).thenReturn(List.of(table));
        when(reservationRepository.findByDate(DATE)).thenReturn(List.of(overlappingReservation));
        when(weatherService.getCurrentWeather()).thenReturn(null);

        var response = service.search(request);

        assertTrue(response.recommendations().isEmpty());
        assertEquals(1, response.allTables().size());
        assertEquals("reserved", response.allTables().get(0).status());
    }

    @Test
    void multipleTablesRankedByScore() {
        var perfectTable = createTable(1L, "W1", 4, "Window", Set.of(TableFeature.WINDOW));
        var okTable = createTable(2L, "W2", 6, "Window", Set.of(TableFeature.WINDOW));
        var request = new SearchRequest(DATE, TIME, 4, 120, "Window", Set.of(TableFeature.WINDOW));

        when(tableRepository.findAll()).thenReturn(List.of(okTable, perfectTable));
        when(reservationRepository.findByDate(DATE)).thenReturn(List.of());
        when(weatherService.getCurrentWeather()).thenReturn(null);

        var response = service.search(request);

        assertEquals(2, response.recommendations().size());
        assertEquals(1L, response.recommendations().get(0).tableId());
        assertEquals(2L, response.recommendations().get(1).tableId());
        assertTrue(response.recommendations().get(0).score() > response.recommendations().get(1).score());
    }

    @Test
    void terraceTable_excludedInColdWeather() {
        var terraceTable = createTable(1L, "T1", 4, "Terrace", Set.of());
        var indoorTable = createTable(2L, "M1", 4, "Main Hall", Set.of());
        var request = new SearchRequest(DATE, TIME, 4, 120, null, null);

        when(tableRepository.findAll()).thenReturn(List.of(terraceTable, indoorTable));
        when(reservationRepository.findByDate(DATE)).thenReturn(List.of());
        // 3°C — at or below 5°C → full penalty (-1.0) → terrace excluded
        when(weatherService.getCurrentWeather()).thenReturn(new WeatherData(3.0, 10.0));

        var response = service.search(request);

        assertEquals(1, response.recommendations().size());
        assertEquals(2L, response.recommendations().get(0).tableId());
        assertEquals(0.0, response.recommendations().get(0).scoreBreakdown().weatherPenalty());
    }

    @Test
    void terraceTable_noPenaltyInWarmWeather() {
        var terraceTable = createTable(1L, "T1", 4, "Terrace", Set.of());
        var request = new SearchRequest(DATE, TIME, 4, 120, null, null);

        when(tableRepository.findAll()).thenReturn(List.of(terraceTable));
        when(reservationRepository.findByDate(DATE)).thenReturn(List.of());
        // 22°C, light wind — no penalty
        when(weatherService.getCurrentWeather()).thenReturn(new WeatherData(22.0, 10.0));

        var response = service.search(request);

        assertEquals(1, response.recommendations().size());
        assertEquals(0.0, response.recommendations().get(0).scoreBreakdown().weatherPenalty());
    }

    @Test
    void weatherUnavailable_noPenaltyApplied() {
        var terraceTable = createTable(1L, "T1", 4, "Terrace", Set.of());
        var request = new SearchRequest(DATE, TIME, 4, 120, null, null);

        when(tableRepository.findAll()).thenReturn(List.of(terraceTable));
        when(reservationRepository.findByDate(DATE)).thenReturn(List.of());
        when(weatherService.getCurrentWeather()).thenReturn(null);

        var response = service.search(request);

        assertEquals(1, response.recommendations().size());
        assertEquals(0.0, response.recommendations().get(0).scoreBreakdown().weatherPenalty());
        assertNull(response.weather());
        assertNull(response.weatherWarning());
    }
}
