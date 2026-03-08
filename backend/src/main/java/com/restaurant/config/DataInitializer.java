package com.restaurant.config;

import com.restaurant.model.Reservation;
import com.restaurant.model.RestaurantTable;
import com.restaurant.model.TableFeature;
import com.restaurant.repository.ReservationRepository;
import com.restaurant.repository.TableRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final TableRepository tableRepository;
    private final ReservationRepository reservationRepository;

    public DataInitializer(TableRepository tableRepository, ReservationRepository reservationRepository) {
        this.tableRepository = tableRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public void run(String... args) {
        seedTables();
        generateRandomReservations();
    }

    private void seedTables() {
        // Window zone (left wall)
        tableRepository.save(new RestaurantTable(null, "W1", 2, "Window", 50, 50, 60, 60, "round", Set.of(TableFeature.WINDOW)));
        tableRepository.save(new RestaurantTable(null, "W2", 2, "Window", 50, 140, 60, 60, "round", Set.of(TableFeature.WINDOW)));
        tableRepository.save(new RestaurantTable(null, "W3", 4, "Window", 50, 230, 80, 60, "rectangle", Set.of(TableFeature.WINDOW, TableFeature.ACCESSIBLE)));
        tableRepository.save(new RestaurantTable(null, "W4", 4, "Window", 50, 320, 80, 60, "rectangle", Set.of(TableFeature.WINDOW)));
        tableRepository.save(new RestaurantTable(null, "W5", 2, "Window", 50, 410, 60, 60, "round", Set.of(TableFeature.WINDOW)));

        // Main Hall zone (center)
        tableRepository.save(new RestaurantTable(null, "M1", 4, "Main Hall", 220, 50, 80, 80, "rectangle", Set.of()));
        tableRepository.save(new RestaurantTable(null, "M2", 6, "Main Hall", 220, 160, 100, 80, "rectangle", Set.of()));
        tableRepository.save(new RestaurantTable(null, "M3", 4, "Main Hall", 220, 270, 80, 80, "rectangle", Set.of()));
        tableRepository.save(new RestaurantTable(null, "M4", 8, "Main Hall", 350, 50, 120, 80, "rectangle", Set.of()));
        tableRepository.save(new RestaurantTable(null, "M5", 6, "Main Hall", 350, 160, 100, 80, "rectangle", Set.of()));
        tableRepository.save(new RestaurantTable(null, "M6", 4, "Main Hall", 350, 270, 80, 80, "rectangle", Set.of()));
        tableRepository.save(new RestaurantTable(null, "M7", 2, "Main Hall", 350, 380, 60, 60, "round", Set.of()));

        // Private zone (top-right)
        tableRepository.save(new RestaurantTable(null, "P1", 6, "Private", 520, 50, 100, 80, "rectangle", Set.of(TableFeature.PRIVATE)));
        tableRepository.save(new RestaurantTable(null, "P2", 4, "Private", 520, 160, 80, 80, "rectangle", Set.of(TableFeature.PRIVATE)));
        tableRepository.save(new RestaurantTable(null, "P3", 8, "Private", 520, 270, 120, 80, "rectangle", Set.of(TableFeature.PRIVATE)));

        // Terrace zone (bottom)
        tableRepository.save(new RestaurantTable(null, "T1", 4, "Terrace", 220, 420, 80, 60, "rectangle", Set.of(TableFeature.NEAR_PLAY_AREA)));
        tableRepository.save(new RestaurantTable(null, "T2", 4, "Terrace", 330, 420, 80, 60, "rectangle", Set.of(TableFeature.NEAR_PLAY_AREA)));
        tableRepository.save(new RestaurantTable(null, "T3", 6, "Terrace", 440, 420, 100, 60, "rectangle", Set.of(TableFeature.NEAR_PLAY_AREA)));
    }

    public void resetReservations() {
        reservationRepository.deleteAll();
        generateRandomReservations();
    }

    private void generateRandomReservations() {
        var random = new Random(42);
        var tables = tableRepository.findAll();
        var today = LocalDate.now();

        var lunchSlots = List.of(
                LocalTime.of(11, 0), LocalTime.of(11, 30), LocalTime.of(12, 0),
                LocalTime.of(12, 30), LocalTime.of(13, 0)
        );
        var dinnerSlots = List.of(
                LocalTime.of(18, 0), LocalTime.of(18, 30), LocalTime.of(19, 0),
                LocalTime.of(19, 30), LocalTime.of(20, 0), LocalTime.of(20, 30),
                LocalTime.of(21, 0)
        );
        var guestNames = List.of("Alice", "Bob", "Charlie", "Diana", "Eve",
                "Frank", "Grace", "Hank", "Ivy", "Jack");

        int targetCount = 10 + random.nextInt(6); // 10-15
        int created = 0;
        int attempts = 0;

        while (created < targetCount && attempts < 100) {
            attempts++;

            var table = tables.get(random.nextInt(tables.size()));
            var date = today.plusDays(random.nextInt(4)); // today + next 3 days

            List<LocalTime> slots = random.nextBoolean() ? lunchSlots : dinnerSlots;
            var startTime = slots.get(random.nextInt(slots.size()));
            var endTime = startTime.plusMinutes(120);

            int partySize = 1 + random.nextInt(table.getCapacity());
            var guestName = guestNames.get(random.nextInt(guestNames.size()));

            var overlaps = reservationRepository.findOverlapping(table.getId(), date, startTime, endTime);
            if (!overlaps.isEmpty()) {
                continue;
            }

            reservationRepository.save(new Reservation(table.getId(), date, startTime, endTime, partySize, guestName));
            created++;
        }
    }
}
