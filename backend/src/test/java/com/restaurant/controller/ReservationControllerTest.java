package com.restaurant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.dto.ReservationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final LocalDate FUTURE_DATE = LocalDate.now().plusDays(10);

    @Test
    void createReservation_withValidRequest_returns201() throws Exception {
        ReservationRequest request = new ReservationRequest(
                1L, null, FUTURE_DATE, LocalTime.of(10, 0), 120, 2, "John Doe"
        );

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.guestName").value("John Doe"))
                .andExpect(jsonPath("$.tableId").value(1));
    }

    @Test
    void createReservation_withMissingGuestName_returns400() throws Exception {
        ReservationRequest request = new ReservationRequest(
                1L, null, FUTURE_DATE, LocalTime.of(10, 0), 120, 2, ""
        );

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createReservation_withNonExistentTable_returns400() throws Exception {
        ReservationRequest request = new ReservationRequest(
                99999L, null, FUTURE_DATE, LocalTime.of(10, 0), 120, 2, "Jane Doe"
        );

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void createReservation_withOverlappingTime_returns409() throws Exception {
        ReservationRequest first = new ReservationRequest(
                1L, null, FUTURE_DATE, LocalTime.of(15, 0), 120, 2, "First Guest"
        );

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isCreated());

        ReservationRequest overlapping = new ReservationRequest(
                1L, null, FUTURE_DATE, LocalTime.of(16, 0), 120, 2, "Second Guest"
        );

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(overlapping)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void resetReservations_returns200WithMessage() throws Exception {
        mockMvc.perform(post("/api/reservations/reset"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Reservations reset successfully"));
    }
}
