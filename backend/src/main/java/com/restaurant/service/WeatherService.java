package com.restaurant.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.dto.WeatherData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;

@Service
public class WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);

    private static final String WEATHER_URL =
            "https://api.open-meteo.com/v1/forecast?latitude=59.44&longitude=24.75&current=temperature_2m,wind_speed_10m";

    private static final Duration CACHE_TTL = Duration.ofMinutes(10);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private volatile WeatherData cachedWeather;
    private volatile Instant cacheExpiry = Instant.EPOCH;

    public WeatherService() {
        this(HttpClient.newBuilder()
                .connectTimeout(REQUEST_TIMEOUT)
                .build(), new ObjectMapper());
    }

    WeatherService(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    public WeatherData getCurrentWeather() {
        if (Instant.now().isBefore(cacheExpiry) && cachedWeather != null) {
            return cachedWeather;
        }

        try {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(WEATHER_URL))
                    .timeout(REQUEST_TIMEOUT)
                    .GET()
                    .build();

            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("Weather API returned status {}", response.statusCode());
                return cachedWeather;
            }

            var weather = parseWeatherJson(response.body());
            cachedWeather = weather;
            cacheExpiry = Instant.now().plus(CACHE_TTL);
            return weather;
        } catch (Exception e) {
            log.warn("Failed to fetch weather data: {}", e.getMessage());
            return cachedWeather;
        }
    }

    WeatherData parseWeatherJson(String json) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        JsonNode current = root.get("current");
        double temperature = current.get("temperature_2m").asDouble();
        double windSpeed = current.get("wind_speed_10m").asDouble();
        return new WeatherData(temperature, windSpeed);
    }

    // Visible for testing
    void clearCache() {
        cachedWeather = null;
        cacheExpiry = Instant.EPOCH;
    }
}
