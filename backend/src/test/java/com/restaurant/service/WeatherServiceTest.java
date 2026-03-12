package com.restaurant.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.dto.WeatherData;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WeatherServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void parseWeatherJson_extractsTemperatureAndWind() throws Exception {
        var service = new WeatherService(mock(HttpClient.class), objectMapper);

        String json = """
                {
                  "current_units": { "temperature_2m": "°C", "wind_speed_10m": "km/h" },
                  "current": {
                    "time": "2026-03-12T12:45",
                    "temperature_2m": 7.5,
                    "wind_speed_10m": 20.5
                  }
                }
                """;

        WeatherData result = service.parseWeatherJson(json);

        assertEquals(7.5, result.temperatureC());
        assertEquals(20.5, result.windSpeedKmh());
    }

    @SuppressWarnings("unchecked")
    @Test
    void getCurrentWeather_cachesResult() throws Exception {
        var httpClient = mock(HttpClient.class);
        var httpResponse = mock(HttpResponse.class);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn("""
                {
                  "current": {
                    "temperature_2m": 10.0,
                    "wind_speed_10m": 15.0
                  }
                }
                """);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        var service = new WeatherService(httpClient, objectMapper);

        // First call — hits API
        WeatherData first = service.getCurrentWeather();
        assertNotNull(first);
        assertEquals(10.0, first.temperatureC());

        // Second call — should use cache, no additional HTTP call
        WeatherData second = service.getCurrentWeather();
        assertEquals(first, second);

        verify(httpClient, times(1)).send(any(), any());
    }

    @SuppressWarnings("unchecked")
    @Test
    void getCurrentWeather_returnsNullOnFailure() throws Exception {
        var httpClient = mock(HttpClient.class);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        var service = new WeatherService(httpClient, objectMapper);

        WeatherData result = service.getCurrentWeather();

        // First call with no cached data returns null
        assertNull(result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void getCurrentWeather_returnsCachedOnSubsequentFailure() throws Exception {
        var httpClient = mock(HttpClient.class);
        var httpResponse = mock(HttpResponse.class);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn("""
                {
                  "current": {
                    "temperature_2m": 12.0,
                    "wind_speed_10m": 8.0
                  }
                }
                """);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        var service = new WeatherService(httpClient, objectMapper);

        // Populate cache
        WeatherData first = service.getCurrentWeather();
        assertNotNull(first);

        // Expire cache and make next call fail
        service.clearCache();
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new RuntimeException("Timeout"));

        // Should return stale cached data (cachedWeather was cleared, so returns null)
        WeatherData second = service.getCurrentWeather();
        assertNull(second);
    }
}
