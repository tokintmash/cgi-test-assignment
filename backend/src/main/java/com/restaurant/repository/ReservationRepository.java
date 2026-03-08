package com.restaurant.repository;

import com.restaurant.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByTableIdAndDate(Long tableId, LocalDate date);

    List<Reservation> findByDate(LocalDate date);

    @Query("SELECT r FROM Reservation r WHERE r.tableId = :tableId AND r.date = :date " +
           "AND r.startTime < :endTime AND r.endTime > :startTime")
    List<Reservation> findOverlapping(Long tableId, LocalDate date, LocalTime startTime, LocalTime endTime);
}
