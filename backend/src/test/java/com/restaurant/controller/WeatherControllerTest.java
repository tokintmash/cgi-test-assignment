package com.restaurant.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getCurrentWeather_returnsWeatherDataOrNoContent() throws Exception {
        var result = mockMvc.perform(get("/api/weather"))
                .andReturn();

        int status = result.getResponse().getStatus();
        // Weather API may be unavailable in CI — accept both 200 and 204
        if (status == 200) {
            mockMvc.perform(get("/api/weather"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.temperatureC").isNumber())
                    .andExpect(jsonPath("$.windSpeedKmh").isNumber());
        } else {
            mockMvc.perform(get("/api/weather"))
                    .andExpect(status().isNoContent());
        }
    }
}
