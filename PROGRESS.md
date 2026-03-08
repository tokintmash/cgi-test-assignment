# Progress Tracker

## Phases

| # | Phase | Branch | Status | Depends On |
|---|---|---|---|---|
| 0 | Project scaffolding & documentation | `main` | ✅ Done | — |
| 1 | Backend core (entities, repos, seed data, API) | `feat/backend-core` | ⬜ Not started | Phase 0 |
| 2 | Recommendation engine + unit tests | `feat/recommendation-engine` | ⬜ Not started | Phase 1 |
| 3 | Frontend (search, floor plan, booking) | `feat/frontend-ui` | ⬜ Not started | Phase 2 |
| 4 | Integration, polish & testing | `fix/integration-polish` | ⬜ Not started | Phase 3 |
| 5 | Docker | `feat/docker` | ⬜ Not started | Phase 4 |
| 6 | Documentation & submission | `docs/final` | ⬜ Not started | Phase 5 |

## Status Key

- ⬜ Not started
- 🔧 In progress
- ✅ Done
- ⏸️ Blocked

## Phase Details

### Phase 0: Scaffolding & Documentation ✅
- [x] Spring Boot project initialized (3.5.0 + Java 21)
- [x] React + TypeScript + Vite initialized
- [x] README, ARCHITECTURE, ASSUMPTIONS, AI-USAGE, AGENTS, PROBLEMS created
- [x] .gitignore configured for full stack
- [x] application.yml configured (H2, JPA, server port)

### Phase 1: Backend Core (`feat/backend-core`)
- [ ] `TableFeature` enum (WINDOW, PRIVATE, ACCESSIBLE, NEAR_PLAY_AREA)
- [ ] `RestaurantTable` entity with position/dimension fields
- [ ] `Reservation` entity
- [ ] `TableRepository` and `ReservationRepository`
- [ ] `DataInitializer` — seed 15-20 tables across 4 zones
- [ ] `DataInitializer` — generate 10-15 random reservations on startup
- [ ] `GET /api/tables` — all tables with current status
- [ ] `POST /api/reservations` — create booking with overlap check
- [ ] `POST /api/reservations/reset` — regenerate random reservations
- [ ] `CorsConfig` for frontend dev
- [ ] Manual test with curl

### Phase 2: Recommendation Engine (`feat/recommendation-engine`)
- [ ] `RecommendationService` with scoring algorithm
- [ ] Score breakdown in response (efficiency, preference, zone, base)
- [ ] `POST /api/tables/search` — search with ranked recommendations
- [ ] Search request DTO with validation
- [ ] Unit tests: perfect match
- [ ] Unit tests: partial match
- [ ] Unit tests: no preferences
- [ ] Unit tests: oversized table penalty
- [ ] Unit tests: no results

### Phase 3: Frontend UI (`feat/frontend-ui`)
- [ ] TypeScript types/interfaces matching API contract
- [ ] API client (`reservationApi.ts`)
- [ ] `SearchForm` component (date, time, party size, zone, preferences)
- [ ] `FloorPlan` SVG component with zones and table rendering
- [ ] `TableShape` component with color states (available/reserved/recommended/selected)
- [ ] `RecommendationPanel` with ranked results and score breakdown
- [ ] `BookingDialog` confirmation
- [ ] Wire full flow: search → recommendations → select → book → update
- [ ] Loading, error, and empty states
- [ ] CSS styling and layout

### Phase 4: Integration & Polish (`fix/integration-polish`)
- [ ] End-to-end flow testing
- [ ] Bug fixes from integration
- [ ] "Reset reservations" button in UI
- [ ] Table hover tooltips (capacity, features, status)
- [ ] Visual polish (transitions, hover effects, color palette)
- [ ] Edge cases (no results, past dates, large party)
- [ ] Backend integration tests (MockMvc)

### Phase 5: Docker (`feat/docker`)
- [ ] Backend `Dockerfile` (multi-stage build)
- [ ] Frontend `Dockerfile` (build + nginx)
- [ ] `docker-compose.yml` (single command startup)
- [ ] Verify full stack starts from `docker-compose up`

### Phase 6: Documentation & Submission (`docs/final`)
- [ ] README finalized with screenshots
- [ ] ARCHITECTURE.md verified against final implementation
- [ ] AI-USAGE.md change log complete
- [ ] ASSUMPTIONS.md reviewed
- [ ] PROBLEMS.md updated with all issues encountered
- [ ] Fresh-clone verification (git clone → run → works)
- [ ] Git history review
