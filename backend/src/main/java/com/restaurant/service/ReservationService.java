package com.restaurant.service;

import com.restaurant.config.DataInitializer;
import com.restaurant.dto.ReservationRequest;
import com.restaurant.model.Reservation;
import com.restaurant.repository.ReservationRepository;
import com.restaurant.repository.TableRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    public Reservation createReservation(ReservationRequest request) {
        var table = tableRepository.findById(request.tableId())
                .orElseThrow(() -> new IllegalArgumentException("Table not found: " + request.tableId()));

        var endTime = request.startTime().plusMinutes(request.duration());

        var overlaps = reservationRepository.findOverlapping(
                request.tableId(), request.date(), request.startTime(), endTime);

        if (!overlaps.isEmpty()) {
            throw new IllegalStateException("Table is already reserved for this time slot");
        }

        var reservation = new Reservation(
                request.tableId(),
                request.date(),
                request.startTime(),
                endTime,
                request.partySize(),
                request.guestName()
        );

        return reservationRepository.save(reservation);
    }

    public List<Reservation> getReservationsByDate(LocalDate date) {
        return reservationRepository.findByDate(date);
    }

    public void resetReservations() {
        dataInitializer.resetReservations();
    }
}
