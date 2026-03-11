package com.restaurant.service;

import com.restaurant.config.DataInitializer;
import com.restaurant.dto.ReservationRequest;
import com.restaurant.model.Reservation;
import com.restaurant.repository.ReservationRepository;
import com.restaurant.repository.TableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final TableRepository tableRepository;
    private final DataInitializer dataInitializer;

    public ReservationService(ReservationRepository reservationRepository,
                              TableRepository tableRepository,
                              DataInitializer dataInitializer) {
        this.reservationRepository = reservationRepository;
        this.tableRepository = tableRepository;
        this.dataInitializer = dataInitializer;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public List<Reservation> createReservation(ReservationRequest request) {
        var ids = request.resolvedTableIds();
        if (ids.isEmpty()) {
            throw new IllegalArgumentException("At least one table ID is required");
        }

        var endTime = request.startTime().plusMinutes(request.duration());
        var reservations = new ArrayList<Reservation>();

        for (Long id : ids) {
            tableRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Table not found: " + id));

            var overlaps = reservationRepository.findOverlapping(id, request.date(), request.startTime(), endTime);
            if (!overlaps.isEmpty()) {
                throw new IllegalStateException("Table is already reserved for this time slot");
            }

            reservations.add(new Reservation(
                    id,
                    request.date(),
                    request.startTime(),
                    endTime,
                    request.partySize(),
                    request.guestName()
            ));
        }

        return reservationRepository.saveAll(reservations);
    }

    public List<Reservation> getReservationsByDate(LocalDate date) {
        return reservationRepository.findByDate(date);
    }

    public void resetReservations() {
        dataInitializer.resetReservations();
    }
}
