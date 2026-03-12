# Architecture

## Domain Model

```mermaid
erDiagram
    RestaurantTable {
        Long id
        String name
        int capacity
        String zone
        double posX
        double posY
        double width
        double height
        String shape
    }
    RestaurantTable ||--o{ TableFeature : has
    RestaurantTable ||--o{ Reservation : "reserved by"
    
    TableFeature {
        enum WINDOW
        enum PRIVATE
        enum ACCESSIBLE
        enum NEAR_PLAY_AREA
    }
    
    Reservation {
        Long id
        Long tableId
        LocalDate date
        LocalTime startTime
        LocalTime endTime
        int partySize
        String guestName
    }
```

### Entity Design Decisions

- **`RestaurantTable`** instead of `Table` to avoid collision with `java.util.Table` and SQL reserved word ambiguity.
- **Zone is a string field**, not a separate entity. The restaurant has a fixed set of zones (Window, Main Hall, Private, Terrace) defined in seed data. A full `Zone` entity adds JPA complexity without benefit at this scale.
- **Table position and dimensions** (`posX`, `posY`, `width`, `height`, `shape`) are stored on the entity to drive the SVG floor plan rendering. The frontend reads these to position tables.
- **`TableFeature` is an enum** stored as an element collection. Each table can have multiple features (e.g., a table can be both `WINDOW` and `ACCESSIBLE`).

## Floor Plan Layout

The restaurant has 4 zones with approximately 15-20 tables:

| Zone | Description | Typical features |
|---|---|---|
| Window | Tables along the window wall | `WINDOW`, sometimes `ACCESSIBLE` |
| Main Hall | Central dining area, adjacent to play area | Largest tables; some have `ACCESSIBLE`, `NEAR_PLAY_AREA` |
| Private | Enclosed/semi-enclosed area | `PRIVATE`; lowest table also `NEAR_PLAY_AREA` |
| Terrace | Outdoor/near-outdoor seating | Some tables `NEAR_PLAY_AREA`, some `ACCESSIBLE`, some with no features |

Table capacities range from 2 to 8. Shapes are rectangular or round.

### Architectural Elements

The SVG floor plan includes static architectural features rendered as lines:

- **Walls** (white, width 2) — a vertical wall separates Main Hall from the Private zone; three horizontal walls subdivide the Private area into individual rooms.
- **Window** (light blue `#7EC8E3`, width 3) — runs along the left edge beside the Window zone, representing a glass wall.
- **Entrance** (terracotta `#D4915E`, width 3) — fills the gap between the Window and Terrace zones on the left edge.

## API Contract

### POST `/api/tables/search`

**Request:**
```json
{
  "date": "2026-03-10",
  "startTime": "19:00",
  "partySize": 4,
  "duration": 120,
  "zone": "Window",
  "preferences": ["WINDOW", "ACCESSIBLE"]
}
```

- `zone` is optional. If omitted, all zones are searched.
- `preferences` is optional. If omitted, all tables pass the preference filter.
- `duration` is in minutes, defaults to 120 if omitted.

**Response:**
```json
{
  "recommendations": [
    {
      "tableId": 5,
      "tableName": "W3",
      "zone": "Window",
      "capacity": 4,
      "features": ["WINDOW", "ACCESSIBLE"],
      "score": 0.75,
      "scoreBreakdown": {
        "efficiency": 1.0,
        "preferenceMatch": 1.0,
        "zoneMatch": 1.0,
        "weatherPenalty": 0.0,
        "base": 0.1
      },
      "posX": 50,
      "posY": 120,
      "width": 60,
      "height": 60,
      "shape": "rectangle"
    }
  ],
  "allTables": [
    {
      "tableId": 1,
      "tableName": "W1",
      "zone": "Window",
      "capacity": 2,
      "status": "available",
      "features": ["WINDOW"]
    }
  ],
  "weather": { "temperatureC": 7.5, "windSpeedKmh": 20.5 },
  "weatherWarning": "Outdoor seating may be uncomfortable — temperature 8°C, wind 6 m/s"
}
```

### POST `/api/reservations`

**Request:**
```json
{
  "tableId": 5,
  "date": "2026-03-10",
  "startTime": "19:00",
  "duration": 120,
  "partySize": 4,
  "guestName": "Jane Doe"
}
```

**Response:** `201 Created` with reservation details, or `409 Conflict` if table is already booked for that time.

### POST `/api/reservations/reset`

Clears all reservations and regenerates random ones. No request body. Returns `200 OK`.

### GET `/api/tables`

Returns all tables with their current reservation status for today. Used for initial floor plan rendering.

## Recommendation Scoring Algorithm

```
totalScore = (efficiency × 0.35) + (preferenceMatch × 0.30) + (zoneMatch × 0.10) + (weatherPenalty × 0.20) + (base × 0.05)
```

| Factor | Calculation | Weight |
|---|---|---|
| Efficiency | `1.0 - ((capacity - partySize) / capacity)`, floor at 0.3 | 35% |
| Preference match | `matchedPreferences / requestedPreferences`, or 1.0 if none requested | 30% |
| Zone match | 1.0 if zone matches or no zone requested, 0.5 otherwise | 10% |
| Weather penalty | 0.0 for non-Terrace or warm weather; linear from 0.0 (≥15°C) to -1.0 (≤5°C); also penalizes wind >20 km/h | 20% |
| Base | Always 0.1 — ensures every valid table gets a nonzero score | 5% |

Tables are excluded from results if:
- `capacity < partySize`
- The table does not have **all** requested preferences
- A zone is selected and the table is not in that zone
- Weather penalty reaches -1.0 (Terrace tables at ≤5°C or wind ≥40 km/h)

### Weather Integration

Real-time weather is fetched from the Open-Meteo API (Tallinn coordinates, no API key required). The `WeatherService` caches data for 10 minutes with a 5-second HTTP timeout. On failure, stale cached data is returned; if no cached data exists, weather is treated as unavailable and no penalty is applied.

The frontend displays weather in the header as a pill badge and shows an amber warning banner inside the header when terrace conditions are poor.

## Technology Choice Rationale

| Decision | Chosen | Alternatives considered | Why |
|---|---|---|---|
| Database | H2 in-memory | PostgreSQL, MySQL | Zero setup, sufficient for demo, avoids Docker dependency |
| Frontend | React + TypeScript + Vite | Vue, Angular, plain HTML | Largest ecosystem, strong AI code generation support, fast dev server |
| Floor plan | Raw SVG | Canvas, D3.js, Konva | Simplest, no dependencies, easy to style with CSS, accessible |
| API style | REST | GraphQL | Simpler for 4 endpoints, easier to test, no schema overhead |
| State management | React useState | Redux, Zustand | App has one page and minimal state — a library adds complexity for no gain |
| CSS approach | Plain CSS | Tailwind, styled-components | Fewer build dependencies, sufficient for this scope |
