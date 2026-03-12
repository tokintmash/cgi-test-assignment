package com.restaurant.controller;

import com.restaurant.dto.WeatherData;
import com.restaurant.service.WeatherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping
    public ResponseEntity<WeatherData> getCurrentWeather() {
        var weather = weatherService.getCurrentWeather();
        if (weather == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(weather);
    }
}
