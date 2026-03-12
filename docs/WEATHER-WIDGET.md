# Phase 4.5: Weather-Aware Recommendations

## Context

The restaurant has a Terrace zone with outdoor tables. In cold or windy weather, recommending these tables is a poor experience. We'll fetch real-time weather from Open-Meteo (free, no API key) and apply a scoring penalty to Terrace tables in bad conditions. The frontend header will also display current weather.

## Design Decisions

- **Backend fetches weather** — scoring logic owns its inputs; frontend gets weather data from the search response + a dedicated endpoint for initial load
- **New scoring component** (not a filter) — Terrace tables still appear but rank lower; users who explicitly want Terrace still see results
- **Open-Meteo API** — no key, no secrets, `java.net.http.HttpClient` (built-in since Java 11)
- **Tallinn coordinates** (59.44, 24.75) — hardcoded, matching CGI Estonia context

## API Details

**URL:** `https://api.open-meteo.com/v1/forecast?latitude=59.44&longitude=24.75&current=temperature_2m,wind_speed_10m`

**Sample response** (2026-03-12):
```json
{
  "current_units": { "temperature_2m": "°C", "wind_speed_10m": "km/h" },
  "current": {
    "time": "2026-03-12T12:45",
    "temperature_2m": 7.5,
    "wind_speed_10m": 20.5
  }
}
```

**Parse:** `root.get("current").get("temperature_2m")` and `root.get("current").get("wind_speed_10m")`

## Clarifications (from plan review)

1. **HttpClient timeout** — 5-second connect + read timeout to prevent blocking the search endpoint
2. **Cache concurrency** — volatile reference to an immutable `WeatherData` record; no synchronization needed
3. **Frontend fallback** — weather badge hidden when weather is `null` (no "N/A" or spinner)
4. **Combination scoring** — combinations inherit the worst (most negative) weather penalty of the two tables in the pair
5. **`weatherWarning` text** — `"Outdoor seating may be uncomfortable — temperature {X}°C, wind {Y} km/h"` (shown when Terrace is in the request zone and penalty is active)
6. **`SearchResponse` constructor** — all call sites (service + tests) must be updated for the two new fields

## Scoring Changes

Current weights:
```
E×0.40 + P×0.35 + Z×0.15 + B×0.10 = 1.0
```

New weights (redistribute from zone and base):
```
E×0.40 + P×0.35 + Z×0.10 + W×0.10 + B×0.05 = 1.0
```

Weather component `W`:
- Non-Terrace tables: `W = 0.0` (neutral — no bonus, no penalty)
- Terrace tables: `W = penalty` (0.0 in good weather, down to -1.0 in bad)

Penalty calculation:
- `tempPenalty`: 0.0 at >=15C, linearly to -1.0 at <=5C
- `windPenalty`: 0.0 at <=20 km/h, linearly to -1.0 at >=40 km/h
- `penalty = min(tempPenalty, windPenalty)` (most negative wins)

Impact: perfect-score indoor table goes from 0.91 to 0.855. Terrace table in 3C weather: weather component contributes -0.10 to total score, noticeably pushing it down the list.

## Implementation Steps

### 1. `WeatherData` DTO (new)
**File:** `backend/src/main/java/com/restaurant/dto/WeatherData.java`
```java
public record WeatherData(double temperatureC, double windSpeedKmh) {}
```

### 2. `WeatherService` (new)
**File:** `backend/src/main/java/com/restaurant/service/WeatherService.java`
- `@Service`, uses `java.net.http.HttpClient` + Jackson `ObjectMapper`
- `getCurrentWeather()` returns `WeatherData` (or `null` on failure)
- In-memory cache with 10-min TTL (volatile field + `Instant` comparison)
- Parse `current.temperature_2m` and `current.wind_speed_10m` from JSON

### 3. Modify `ScoreBreakdown`
**File:** `backend/src/main/java/com/restaurant/dto/ScoreBreakdown.java`
- Add `double weatherPenalty` field (between zoneMatch and base)

### 4. Modify `SearchResponse`
**File:** `backend/src/main/java/com/restaurant/dto/SearchResponse.java`
- Add `WeatherData weather` and `String weatherWarning` fields

### 5. Modify `RecommendationService`
**File:** `backend/src/main/java/com/restaurant/service/RecommendationService.java`
- Inject `WeatherService`
- Update weight constants: `WEIGHT_ZONE=0.10`, `WEIGHT_WEATHER=0.10`, `WEIGHT_BASE=0.05`
- New method: `calculateWeatherPenalty(String zone, WeatherData weather)` — returns 0.0 for non-Terrace or null weather
- Update `calculateScore()` to accept `WeatherData` and include penalty
- Update `toRecommendation()` to include weather weight in total
- Update `findCombinations()` / `calculateCombinationScore()` similarly
- In `search()`: fetch weather, pass through, add `weatherWarning` if Terrace is requested and penalty active
- Return new `SearchResponse(recommendations, combinations, allTableStatuses, weather, warning)`

### 6. `WeatherController` (new)
**File:** `backend/src/main/java/com/restaurant/controller/WeatherController.java`
- `GET /api/weather` — returns `WeatherData` (for frontend header on initial load)

### 7. Update frontend types
**File:** `frontend/src/types/index.ts`
- Add `WeatherData` interface
- Add `weatherPenalty` to `ScoreBreakdown`
- Add `weather` and `weatherWarning` to `SearchResponse`

### 8. Update frontend API client
**File:** `frontend/src/api/reservationApi.ts`
- Add `getWeather()` method calling `GET /api/weather`

### 9. Weather display in header + warning
**File:** `frontend/src/App.tsx`
- State: `weather: WeatherData | null`
- Fetch on mount via `reservationApi.getWeather()`
- Update from search response too: `setWeather(response.weather)`
- Render weather badge in `.header-top-row`
- Show `weatherWarning` as info banner when present

### 10. Weather badge CSS
**File:** `frontend/src/styles/app.css`
- `.weather-badge` — pill style matching dark header theme

### 11. Update tests
**File:** `backend/src/test/java/com/restaurant/service/RecommendationServiceTest.java`
- Mock `WeatherService` in setUp (returns null by default — no penalty)
- Fix `perfectMatch_scoresHighest`: expected score changes from 0.91 to 0.855
- New tests:
  - `terraceTable_penalizedInColdWeather` — mock 3C, verify negative weatherPenalty
  - `terraceTable_noPenaltyInWarmWeather` — mock 22C, verify weatherPenalty = 0.0
  - `weatherUnavailable_noPenaltyApplied` — mock null, verify no penalty

**File:** `backend/src/test/java/com/restaurant/service/WeatherServiceTest.java` (new)
- Test JSON parsing
- Test cache TTL behavior
- Test failure fallback (returns null)

### 12. Update PROGRESS.md, AI-USAGE.md, TESTS.md

## Files Modified

| File | Action |
|------|--------|
| `backend/.../dto/WeatherData.java` | New |
| `backend/.../service/WeatherService.java` | New |
| `backend/.../controller/WeatherController.java` | New |
| `backend/.../dto/ScoreBreakdown.java` | Add weatherPenalty field |
| `backend/.../dto/SearchResponse.java` | Add weather + weatherWarning |
| `backend/.../service/RecommendationService.java` | Inject weather, new penalty, updated weights |
| `backend/.../service/RecommendationServiceTest.java` | Fix scores, add weather tests |
| `backend/.../service/WeatherServiceTest.java` | New |
| `frontend/src/types/index.ts` | Add WeatherData, update interfaces |
| `frontend/src/api/reservationApi.ts` | Add getWeather() |
| `frontend/src/App.tsx` | Weather state, header badge, warning |
| `frontend/src/styles/app.css` | Weather badge styles |
| `PROGRESS.md` | Insert Phase 4.5 |
| `AI-USAGE.md` | Changelog entry |
| `TESTS.md` | New test entries |

## Verification

1. `cd backend && mvn test` — all tests pass (including updated scores and new weather tests)
2. Start backend + frontend, observe weather badge in header
3. Search with no zone filter — Terrace tables should rank lower in cold weather
4. Search with Terrace zone selected — tables appear but warning banner shown
5. Disconnect internet / mock failure — no penalty applied, weather badge shows fallback state
