package com.restaurant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.dto.SearchRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TableControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllTables_returnsTables() throws Exception {
        mockMvc.perform(get("/api/tables"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)))
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].capacity").exists())
                .andExpect(jsonPath("$[0].zone").exists());
    }

    @Test
    void search_withValidRequest_returnsRecommendationsAndAllTables() throws Exception {
        SearchRequest request = new SearchRequest(
                LocalDate.now().plusDays(1),
                LocalTime.of(19, 0),
                4,
                120,
                null,
                Set.of()
        );

        mockMvc.perform(post("/api/tables/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendations").isArray())
                .andExpect(jsonPath("$.allTables").isArray())
                .andExpect(jsonPath("$.allTables.length()").value(greaterThan(0)));
    }

    @Test
    void search_withMissingRequiredFields_returns400() throws Exception {
        mockMvc.perform(post("/api/tables/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void search_withPartySizeZero_returns400() throws Exception {
        SearchRequest request = new SearchRequest(
                LocalDate.now().plusDays(1),
                LocalTime.of(19, 0),
                0,
                120,
                null,
                Set.of()
        );

        mockMvc.perform(post("/api/tables/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
