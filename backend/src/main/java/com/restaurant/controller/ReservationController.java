package com.restaurant.controller;

import com.restaurant.dto.ReservationRequest;
import com.restaurant.model.Reservation;
import com.restaurant.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<?> createReservation(@Valid @RequestBody ReservationRequest request) {
        try {
            List<Reservation> reservations = reservationService.createReservation(request);
            if (reservations.size() == 1) {
                return ResponseEntity.status(HttpStatus.CREATED).body(reservations.get(0));
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(reservations);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelReservation(@PathVariable Long id) {
        try {
            reservationService.cancelReservation(id);
            return ResponseEntity.ok(Map.of("message", "Reservation cancelled successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> resetReservations() {
        reservationService.resetReservations();
        return ResponseEntity.ok(Map.of("message", "Reservations reset successfully"));
    }
}
