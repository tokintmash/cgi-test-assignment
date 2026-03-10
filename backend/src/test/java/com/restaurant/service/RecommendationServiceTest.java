package com.restaurant.service;

import com.restaurant.dto.SearchRequest;
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

    private RecommendationService service;

    private static final LocalDate DATE = LocalDate.of(2026, 3, 10);
    private static final LocalTime TIME = LocalTime.of(19, 0);

    @BeforeEach
    void setUp() {
        service = new RecommendationService(tableRepository, reservationRepository);
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

        var response = service.search(request);

        assertEquals(1, response.recommendations().size());
        var rec = response.recommendations().get(0);
        assertEquals(1L, rec.tableId());
        assertEquals(1.0, rec.scoreBreakdown().efficiency());
        assertEquals(1.0, rec.scoreBreakdown().preferenceMatch());
        assertEquals(1.0, rec.scoreBreakdown().zoneMatch());
        // total = (1.0*0.40) + (1.0*0.35) + (1.0*0.15) + (0.1*0.10) = 0.91
        assertEquals(0.91, rec.score());
    }

    @Test
    void partialMatch_isExcluded() {
        var table = createTable(1L, "M1", 6, "Main Hall", Set.of(TableFeature.ACCESSIBLE));
        var request = new SearchRequest(DATE, TIME, 4, 120, "Window", Set.of(TableFeature.WINDOW, TableFeature.ACCESSIBLE));

        when(tableRepository.findAll()).thenReturn(List.of(table));
        when(reservationRepository.findByDate(DATE)).thenReturn(List.of());

        var response = service.search(request);

        assertTrue(response.recommendations().isEmpty());
    }

    @Test
    void noPreferences_defaultsToFullPreferenceScore() {
        var table = createTable(1L, "W1", 4, "Window", Set.of(TableFeature.WINDOW));
        var request = new SearchRequest(DATE, TIME, 4, 120, null, null);

        when(tableRepository.findAll()).thenReturn(List.of(table));
        when(reservationRepository.findByDate(DATE)).thenReturn(List.of());

        var response = service.search(request);

        assertEquals(1, response.recommendations().size());
        var rec = response.recommendations().get(0);
        assertEquals(1.0, rec.scoreBreakdown().preferenceMatch());
        assertEquals(1.0, rec.scoreBreakdown().zoneMatch());
    }

    @Test
    void oversizedTable_getsLowerEfficiency() {
        var small = createTable(1L, "W1", 2, "Window", Set.of());
        var large = createTable(2L, "M1", 8, "Window", Set.of());
        var request = new SearchRequest(DATE, TIME, 2, 120, null, null);

        when(tableRepository.findAll()).thenReturn(List.of(small, large));
        when(reservationRepository.findByDate(DATE)).thenReturn(List.of());

        var response = service.search(request);

        assertEquals(2, response.recommendations().size());
        var first = response.recommendations().get(0);
        var second = response.recommendations().get(1);
        // small table (capacity=2 for party=2) should rank higher
        assertEquals(1L, first.tableId());
        assertEquals(1.0, first.scoreBreakdown().efficiency());
        // large table (capacity=8 for party=2): efficiency = 1.0 - 6/8 = 0.25, floored to 0.3
        assertEquals(0.3, second.scoreBreakdown().efficiency(), 0.001);
        assertTrue(first.score() > second.score());
    }

    @Test
    void noResults_whenAllTablesTooSmall() {
        var table = createTable(1L, "W1", 2, "Window", Set.of());
        var request = new SearchRequest(DATE, TIME, 6, 120, null, null);

        when(tableRepository.findAll()).thenReturn(List.of(table));
        when(reservationRepository.findByDate(DATE)).thenReturn(List.of());

        var response = service.search(request);

        assertTrue(response.recommendations().isEmpty());
        assertEquals(1, response.allTables().size());
    }

    @Test
    void reservedTables_areExcludedFromRecommendations() {
        var table = createTable(1L, "W1", 4, "Window", Set.of());
        var request = new SearchRequest(DATE, TIME, 2, 120, null, null);

        var overlappingReservation = new Reservation(1L, DATE, LocalTime.of(18, 30), LocalTime.of(20, 30), 2, "Guest");

        when(tableRepository.findAll()).thenReturn(List.of(table));
        when(reservationRepository.findByDate(DATE)).thenReturn(List.of(overlappingReservation));

        var response = service.search(request);

        assertTrue(response.recommendations().isEmpty());
        assertEquals(1, response.allTables().size());
        assertEquals("reserved", response.allTables().get(0).status());
    }

    @Test
    void multipleTablesRankedByScore() {
        var perfectTable = createTable(1L, "W1", 4, "Window", Set.of(TableFeature.WINDOW));
        var okTable = createTable(2L, "M1", 6, "Main Hall", Set.of(TableFeature.WINDOW));
        var request = new SearchRequest(DATE, TIME, 4, 120, "Window", Set.of(TableFeature.WINDOW));

        when(tableRepository.findAll()).thenReturn(List.of(okTable, perfectTable));
        when(reservationRepository.findByDate(DATE)).thenReturn(List.of());

        var response = service.search(request);

        assertEquals(2, response.recommendations().size());
        assertEquals(1L, response.recommendations().get(0).tableId());
        assertEquals(2L, response.recommendations().get(1).tableId());
        assertTrue(response.recommendations().get(0).score() > response.recommendations().get(1).score());
    }
}
