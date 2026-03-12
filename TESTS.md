# Test Inventory

> **Keep this file in sync.** When tests are added, removed, or renamed, update the relevant section below.

Run all backend tests: `cd backend && ./mvnw test`

## Backend — Unit Tests

### RecommendationServiceTest (8 tests)

| # | Test | Verifies |
|---|---|---|
| 1 | `perfectMatch_scoresHighest` | Exact capacity + all preferences + correct zone scores ~0.91 |
| 2 | `partialMatch_isExcluded` | Tables missing a requested preference are excluded entirely |
| 3 | `noPreferences_defaultsToFullPreferenceScore` | Empty preference set gives full preference score (1.0) |
| 4 | `oversizedTable_getsLowerEfficiency` | Table within cap but larger than party gets lower efficiency |
| 5 | `smallParty_excludesOversizedTables` | Party of 1-2 capped at 4 seats, party of 3-4 capped at 6, 5+ uncapped |
| 6 | `noResults_whenAllTablesTooSmall` | Returns empty list when no table fits the party |
| 7 | `reservedTables_areExcludedFromRecommendations` | Reserved tables do not appear in recommendations |
| 8 | `multipleTablesRankedByScore` | Results are sorted by descending score |

### RestaurantReservationApplicationTests (1 test)

| # | Test | Verifies |
|---|---|---|
| 1 | `contextLoads` | Spring context starts without errors |

## Backend — Integration Tests (MockMvc)

### TableControllerTest (4 tests)

| # | Test | Verifies |
|---|---|---|
| 1 | `getAllTables_returnsTables` | `GET /api/tables` returns non-empty array with expected fields |
| 2 | `search_withValidRequest_returnsRecommendationsAndAllTables` | `POST /api/tables/search` returns recommendations + allTables |
| 3 | `search_withMissingRequiredFields_returns400` | Empty JSON body returns 400 |
| 4 | `search_withPartySizeZero_returns400` | partySize=0 fails `@Min(1)` validation |

### ReservationControllerTest (5 tests)

| # | Test | Verifies |
|---|---|---|
| 1 | `createReservation_withValidRequest_returns201` | Valid booking returns 201 with correct fields |
| 2 | `createReservation_withMissingGuestName_returns400` | Blank guestName fails `@NotBlank` validation |
| 3 | `createReservation_withNonExistentTable_returns400` | Non-existent table ID returns 400 with error |
| 4 | `createReservation_withOverlappingTime_returns409` | Double-booking same table/time returns 409 |
| 5 | `resetReservations_returns200WithMessage` | `POST /api/reservations/reset` returns success message |

## Frontend

No automated tests yet. Frontend is verified via `npx tsc --noEmit` (type check) and `npx vite build` (production build).

## Summary

| Layer | Type | Count |
|---|---|---|
| Backend | Unit (Mockito) | 8 |
| Backend | Context load | 1 |
| Backend | Integration (MockMvc) | 9 |
| **Total** | | **18** |
