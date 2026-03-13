# Progress Tracker

## Phases

| # | Phase | Branch | Status | Depends On |
|---|---|---|---|---|
| 0 | Project scaffolding & documentation | `main` | ✅ Done | — |
| 1 | Backend core (entities, repos, seed data, API) | `feat/backend-core` | ✅ Done | Phase 0 |
| 2 | Recommendation engine + unit tests | `feat/recommendation-engine` | ✅ Done | Phase 1 |
| 3 | Frontend (search, floor plan, booking) | `feat/frontend-ui` | ✅ Done | Phase 2 |
| 4 | Integration, polish & testing | `fix/integration-polish` | ⬜ Not started | Phase 3 |
| 4.5 | Weather-aware recommendations | `feat/weather-widget` | ✅ Done | Phase 4 |
| 5 | Docker | `feat/docker` | ✅ Done | Phase 4.5 |
| 6 | Documentation & submission | `docs/final` | ⬜ Not started | Phase 5 |

## Status Key

- ⬜ Not started
- 🔧 In progress
- ✅ Done
- ⏸️ Blocked

## Phase Details

### Phase 0: Scaffolding & Documentation ✅
- [x] Spring Boot project initialized (3.5.0 + Java 25)
- [x] React + TypeScript + Vite initialized
- [x] README, ARCHITECTURE, ASSUMPTIONS, AI-USAGE, AGENTS, PROBLEMS created
- [x] .gitignore configured for full stack
- [x] application.yml configured (H2, JPA, server port)

### Phase 1: Backend Core (`feat/backend-core`) ✅
- [x] `TableFeature` enum (WINDOW, PRIVATE, ACCESSIBLE, NEAR_PLAY_AREA)
- [x] `RestaurantTable` entity with position/dimension fields
- [x] `Reservation` entity
- [x] `TableRepository` and `ReservationRepository`
- [x] `DataInitializer` — seed 15-20 tables across 4 zones
- [x] `DataInitializer` — generate 10-15 random reservations on startup
- [x] `GET /api/tables` — all tables with current status
- [x] `POST /api/reservations` — create booking with overlap check
- [x] `POST /api/reservations/reset` — regenerate random reservations
- [x] `CorsConfig` for frontend dev
- [x] Manual test with curl

### Phase 2: Recommendation Engine (`feat/recommendation-engine`) ✅
- [x] `RecommendationService` with scoring algorithm
- [x] Score breakdown in response (efficiency, preference, zone, base)
- [x] `POST /api/tables/search` — search with ranked recommendations
- [x] Search request DTO with validation
- [x] Unit tests: perfect match
- [x] Unit tests: partial match
- [x] Unit tests: no preferences
- [x] Unit tests: oversized table penalty
- [x] Unit tests: no results

### Phase 3: Frontend UI (`feat/frontend-ui`)
- [x] TypeScript types/interfaces matching API contract
- [x] API client (`reservationApi.ts`)
- [x] `SearchForm` component (date, time, party size, zone, preferences)
- [x] `FloorPlan` SVG component with zones and table rendering
- [x] `TableShape` component with color states (available/reserved/recommended/selected)
- [x] `RecommendationPanel` with ranked results
- [x] `BookingDialog` confirmation
- [x] Wire full flow: search → recommendations → select → book → update
- [x] Loading, error, and empty states
- [x] CSS styling and layout

### Review Baseline

- Do not flag the current UI for omitting score breakdown inside recommendation cards; score data may exist in the API without being surfaced in the card layout.
- Do not flag the current floor plan for rendering a uniform table shape; preserving per-table visual shapes is optional unless explicitly requested.
- Treat hover tooltips and stronger recommended/selected state emphasis as deferred polish for a later phase, not current defects by default.

### Phase 4: Integration & Polish (`fix/integration-polish`)
- [ ] End-to-end flow testing
- [x] Bug fixes from integration
- [x] "Reset reservations" button in UI (done in Phase 3)
- [x] Table hover tooltips (capacity, features, status)
- [x] Visual polish (transitions, hover effects, color palette)
- [x] Edge cases (no results, past dates, large party)
- [x] Backend integration tests (MockMvc)
- [x] Fixed layout height (floor plan + recommendations locked at 740px, right pane scrolls)

### Phase 4.5: Weather-Aware Recommendations (`feat/weather-widget`)
- [x] `WeatherData` record DTO
- [x] `WeatherService` — Open-Meteo fetch, 10-min cache, 5s timeout
- [x] `WeatherController` — `GET /api/weather`
- [x] Add `weatherPenalty` to `ScoreBreakdown`
- [x] Add `weather` + `weatherWarning` to `SearchResponse`
- [x] Update `RecommendationService` — new weights (Z=0.10, W=0.10, B=0.05), penalty calc
- [x] Frontend types + API client (`getWeather()`)
- [x] Weather badge in header + warning banner
- [x] Unit tests: terrace penalty in cold/warm/unavailable weather
- [x] `WeatherServiceTest` — JSON parsing, cache TTL, failure fallback
- [x] Update TESTS.md, AI-USAGE.md

### Phase 5: Docker (`feat/docker`)
- [x] Backend `Dockerfile` (multi-stage build: JDK 25 build → JRE 25 run)
- [x] Frontend `Dockerfile` (Node 22 build → nginx serve)
- [x] Frontend `nginx.conf` (SPA routing + `/api/` reverse proxy to backend)
- [x] `docker-compose.yml` (single command startup)
- [x] Verify full stack starts from `docker-compose up`

### Phase 6: Documentation & Submission (`docs/final`)
- [ ] README finalized with screenshots
- [ ] ARCHITECTURE.md verified against final implementation
- [ ] AI-USAGE.md change log complete
- [ ] ASSUMPTIONS.md reviewed
- [ ] PROBLEMS.md updated with all issues encountered
- [ ] Fresh-clone verification (git clone → run → works)
- [ ] Git history review
