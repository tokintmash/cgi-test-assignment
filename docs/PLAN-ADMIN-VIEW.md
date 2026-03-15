# Plan: Admin View

## Overview

Add an admin view with three panels:
1. **Table Layout Editor** — drag tables on the floor plan, assign zone/features via dropdowns
2. **Opening Hours** — configure per-day opening/closing times
3. **Weather Settings** — set location coordinates and penalty thresholds

All settings persist via backend API and are consumed by existing services.

---

## Prerequisites

- No routing library exists — install `react-router-dom`
- Weather location/thresholds are hardcoded — extract to a settings model
- Opening hours don't exist at all — new entity + validation
- No table update endpoint — add one

---

## Step 1: Backend — Settings Entity & API

**Goal:** Create a key-value settings store so admin config survives restarts (within session — H2 reseeds, but settings can be initialized with defaults).

### Files to create/modify:

1. **Create `RestaurantSettings.java`** entity — single-row config table:
   - `id` (Long, always 1)
   - `latitude` (double, default 59.44)
   - `longitude` (double, default 24.75)
   - `tempColdThreshold` (double, default 5.0) — below this, terrace closed
   - `tempCoolThreshold` (double, default 15.0) — below this, linear penalty
   - `windHighThreshold` (double, default 40.0) — above this, terrace closed
   - `windModerateThreshold` (double, default 20.0) — above this, linear penalty

2. **Create `OpeningHours.java`** entity:
   - `id` (Long)
   - `dayOfWeek` (DayOfWeek enum, unique)
   - `openTime` (LocalTime)
   - `closeTime` (LocalTime)
   - `closed` (boolean)

3. **Create `SettingsRepository.java`** and **`OpeningHoursRepository.java`**

4. **Create `SettingsService.java`**:
   - `getSettings()` → returns current settings (or defaults)
   - `updateSettings(RestaurantSettings)` → saves
   - `getOpeningHours()` → returns all 7 days
   - `updateOpeningHours(List<OpeningHours>)` → saves all 7

5. **Create `AdminController.java`**:
   - `GET /api/admin/settings` → current settings
   - `PUT /api/admin/settings` → update settings
   - `GET /api/admin/opening-hours` → all 7 days
   - `PUT /api/admin/opening-hours` → update all 7 days

6. **Modify `DataInitializer.java`**:
   - Seed default settings row if none exists
   - Seed default opening hours (Mon–Fri 10:00–22:00, Sat–Sun 11:00–23:00) if none exist

### Testing:
- Unit test for `SettingsService` defaults
- MockMvc test for admin endpoints

---

## Step 2: Backend — Table Update Endpoint & Zone/Feature Logic

**Goal:** Allow updating a table's position, zone, and features.

### Files to create/modify:

1. **Create `UpdateTableRequest.java`** DTO:
   - `posX` (Double, optional)
   - `posY` (Double, optional)
   - `zone` (String, optional)
   - `features` (Set<TableFeature>, optional)
   - `capacity` (Integer, optional)

2. **Add to `TableService.java`**:
   - `updateTable(Long id, UpdateTableRequest request)` — partial update, only non-null fields

3. **Add to `TableController.java`**:
   - `PUT /api/tables/{id}` → update table, return updated entity

4. **Modify `WeatherService.java`**:
   - Inject `SettingsRepository`
   - Build API URL from `settings.latitude`/`settings.longitude` instead of hardcoded values
   - Invalidate cache when settings change

5. **Modify `RecommendationService.java`**:
   - Inject `SettingsRepository`
   - Read penalty thresholds from settings instead of hardcoded constants
   - Read opening hours and validate requested time slot falls within them

6. **Modify `ReservationService.java`**:
   - Inject `OpeningHoursRepository`
   - In `createReservation()`, validate that the reservation time falls within opening hours for that day
   - Return clear error message if outside hours

### Testing:
- Unit test for `updateTable` partial updates
- Unit test for weather penalty with custom thresholds
- Unit test for reservation rejection outside opening hours
- MockMvc test for `PUT /api/tables/{id}`

---

## Step 3: Frontend — Routing & Admin Shell

**Goal:** Add React Router, create admin page skeleton with tab navigation.

### Instructions:
- Use the **ui-ux-pro-max** skill for designing the admin layout
- The admin view should feel professional but consistent with the existing app's color palette

### Files to create/modify:

1. **Install dependency**: `npm install react-router-dom`

2. **Modify `main.tsx`**: Wrap `<App />` in `<BrowserRouter>`

3. **Modify `App.tsx`**:
   - Add `<Routes>` with:
     - `/` → existing customer view (extract current content to `CustomerView.tsx`)
     - `/admin` → new `AdminView.tsx`
   - Add a small nav link in the header to toggle between Customer/Admin views

4. **Create `CustomerView.tsx`**:
   - Move all current App.tsx state and JSX here (pure extraction, no behavior changes)

5. **Create `AdminView.tsx`**:
   - Tab navigation: "Table Layout" | "Opening Hours" | "Weather Settings"
   - Each tab renders its corresponding panel component
   - Use existing app styling conventions (CSS modules or plain CSS matching `App.css`)

6. **Create `admin/` directory** under `src/components/`

7. **Update `nginx.conf`**: Ensure `/admin` path returns `index.html` for SPA routing (should already work with `try_files $uri /index.html` but verify)

---

## Step 4: Frontend — Table Layout Editor

**Goal:** Reuse FloorPlan SVG with drag support and zone/feature dropdowns.

### Instructions:
- Use the **ui-ux-pro-max** skill for the editor panel design
- The floor plan should be reusable — pass a `mode` prop ("view" | "admin") to `FloorPlan.tsx`

### Files to create/modify:

1. **Create `components/admin/TableEditor.tsx`**:
   - Renders `FloorPlan` in admin mode (full width, no recommendation panel)
   - Side panel or bottom panel shows selected table's properties:
     - Name (read-only)
     - Position X, Y (number inputs, updated on drag)
     - Zone (dropdown: Window, Main Hall, Private, Terrace)
     - Features (multi-select checkboxes: WINDOW, PRIVATE, ACCESSIBLE, NEAR_PLAY_AREA)
     - Capacity (number input)
     - Shape (dropdown: round, rectangle)
   - "Save" button per table → calls `PUT /api/tables/{id}`
   - Visual indicator for unsaved changes

2. **Modify `FloorPlan.tsx`**:
   - Accept `mode` prop ("view" | "admin")
   - In admin mode:
     - All tables are interactive (no search-dependent selectability)
     - Add `onMouseDown`/`onMouseMove`/`onMouseUp` handlers for SVG drag
     - Convert screen coordinates to SVG coordinates using `SVGSVGElement.getScreenCTM()`
     - Highlight the selected table
     - No recommendation overlays
   - In view mode: existing behavior unchanged

3. **Modify `TableShape.tsx`**:
   - In admin mode: show drag cursor, no status-based coloring (use neutral color + zone-based tint)

4. **Add to `api/reservationApi.ts`** (or create `api/adminApi.ts`):
   - `updateTable(id, data)` → `PUT /api/tables/${id}`
   - `getSettings()` → `GET /api/admin/settings`
   - `updateSettings(data)` → `PUT /api/admin/settings`
   - `getOpeningHours()` → `GET /api/admin/opening-hours`
   - `updateOpeningHours(data)` → `PUT /api/admin/opening-hours`

5. **Add TypeScript types** in `types/index.ts`:
   - `RestaurantSettings`
   - `OpeningHours`
   - `UpdateTableRequest`

---

## Step 5: Frontend — Opening Hours Panel

**Goal:** Grid editor for 7-day opening hours.

### Instructions:
- Use the **ui-ux-pro-max** skill for the form layout

### Files to create/modify:

1. **Create `components/admin/OpeningHoursEditor.tsx`**:
   - Fetches `GET /api/admin/opening-hours` on mount
   - Renders 7 rows (Monday–Sunday), each with:
     - Day name (label)
     - "Closed" toggle (checkbox)
     - Open time (time input or dropdown, disabled if closed)
     - Close time (time input or dropdown, disabled if closed)
   - "Save All" button → `PUT /api/admin/opening-hours`
   - Success/error toast

2. **Modify `SearchForm.tsx`**:
   - Fetch opening hours from API on mount
   - Use actual opening hours for time slot generation instead of hardcoded 10:00–22:00
   - Show "Closed" message if selected date's day is marked closed

---

## Step 6: Frontend — Weather Settings Panel

**Goal:** Configure location and penalty thresholds.

### Instructions:
- Use the **ui-ux-pro-max** skill for the form layout

### Files to create/modify:

1. **Create `components/admin/WeatherSettingsEditor.tsx`**:
   - Fetches `GET /api/admin/settings` on mount
   - Form fields:
     - **Location section:**
       - Latitude (number input, step 0.01)
       - Longitude (number input, step 0.01)
       - "Current weather preview" — calls `GET /api/weather` and shows temp/wind
     - **Temperature thresholds section:**
       - Cold threshold (°C) — below this, terrace unavailable
       - Cool threshold (°C) — below this, linear penalty applies
     - **Wind thresholds section:**
       - High wind threshold (km/h) — above this, terrace unavailable
       - Moderate wind threshold (km/h) — above this, linear penalty applies
   - "Save" button → `PUT /api/admin/settings`
   - Validation: cold < cool, moderate < high
   - Success/error toast

---

## Step 7: Testing & Polish

### Backend tests:
- `AdminControllerTest` — MockMvc tests for all admin endpoints
- `SettingsServiceTest` — defaults, updates
- `RecommendationServiceTest` — add test with custom thresholds from settings
- `ReservationServiceTest` — add test for opening hours enforcement

### Frontend verification:
- Navigate between customer and admin views
- Drag a table, change zone/features via dropdowns, save, verify in customer view
- Change opening hours, verify SearchForm reflects new hours
- Change weather location, verify weather widget updates

### Documentation:
- Update `AI-USAGE.md` with changelog entries
- Update `TESTS.md` with new test inventory
- Update `ARCHITECTURE.md` with admin view section
- Update `README.md` with admin view instructions

---

## Parallelization Strategy

Steps can be divided between sub-agents as follows:

| Agent | Steps | Dependencies |
|-------|-------|-------------|
| **Backend Agent** | Step 1 + Step 2 | None — can start immediately |
| **Frontend Agent 1** | Step 3 (routing shell) | Needs Step 1–2 API contract (types only, not implementation) |
| **Frontend Agent 2** | Step 4 (table editor) | Needs Step 3 done |
| **Frontend Agent 3** | Step 5 + Step 6 (hours + weather panels) | Needs Step 3 done |
| **Test Agent** | Step 7 | Needs all prior steps |

**Minimum serial path:** Steps 1–2 → Step 3 → Steps 4+5+6 (parallel) → Step 7

---

## Files Changed Summary

### New files:
- `backend/.../model/RestaurantSettings.java`
- `backend/.../model/OpeningHours.java`
- `backend/.../repository/SettingsRepository.java`
- `backend/.../repository/OpeningHoursRepository.java`
- `backend/.../service/SettingsService.java`
- `backend/.../controller/AdminController.java`
- `backend/.../dto/UpdateTableRequest.java`
- `frontend/src/components/CustomerView.tsx`
- `frontend/src/components/AdminView.tsx`
- `frontend/src/components/admin/TableEditor.tsx`
- `frontend/src/components/admin/OpeningHoursEditor.tsx`
- `frontend/src/components/admin/WeatherSettingsEditor.tsx`
- `frontend/src/api/adminApi.ts`

### Modified files:
- `backend/.../config/DataInitializer.java` (seed defaults)
- `backend/.../service/WeatherService.java` (read location from settings)
- `backend/.../service/RecommendationService.java` (read thresholds from settings)
- `backend/.../service/ReservationService.java` (opening hours validation)
- `backend/.../controller/TableController.java` (PUT endpoint)
- `backend/.../service/TableService.java` (update method)
- `frontend/package.json` (add react-router-dom)
- `frontend/src/main.tsx` (BrowserRouter)
- `frontend/src/App.tsx` (routing, extract to CustomerView)
- `frontend/src/components/FloorPlan.tsx` (admin mode + drag)
- `frontend/src/components/TableShape.tsx` (admin mode styling)
- `frontend/src/components/SearchForm.tsx` (dynamic opening hours)
- `frontend/src/types/index.ts` (new types)
- `frontend/src/api/reservationApi.ts` (or new adminApi.ts)
