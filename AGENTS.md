# AGENTS.md — Project Conventions for AI Agents

## Project Overview

Smart restaurant reservation system with an interactive SVG floor plan and recommendation engine.

- **Backend:** Spring Boot 3.x, Java 21, Maven, H2 in-memory database
- **Frontend:** React 19, TypeScript, Vite, plain CSS
- **Floor plan:** Raw SVG (no external libraries)

## Repository Structure

```
backend/          Spring Boot application (Maven)
frontend/         React + TypeScript application (Vite)
docs/             Screenshots and supplementary documentation
```

## Code Conventions

### Java (Backend)

- Java 21 — use records for DTOs, use `var` where type is obvious
- Spring Boot 3.x conventions: constructor injection (no `@Autowired` on fields), `@RestController`, `@Service`, `@Repository`
- Entity class naming: `RestaurantTable` (not `Table`) to avoid SQL/Java conflicts
- Package structure: `com.restaurant.{config,model,dto,repository,service,controller}`
- Use `@Valid` for request validation
- Use `application.yml` not `application.properties`
- Tests: JUnit 5 + Mockito, focus on `RecommendationService`

### TypeScript (Frontend)

- React 19 with functional components and hooks only (no class components)
- TypeScript strict mode
- State management: `useState` / `useReducer` only (no external state libraries)
- API calls: `fetch` wrapper (no axios)
- No component library (no MUI, no Ant Design) — plain HTML + CSS
- SVG floor plan built with native SVG elements (`<rect>`, `<circle>`, `<text>`, `<g>`)
- File naming: PascalCase for components (`FloorPlan.tsx`), camelCase for utilities (`reservationApi.ts`)

### CSS

- Plain CSS files, one per component if needed
- No CSS-in-JS, no Tailwind
- Use CSS custom properties for the color palette:
  - Available: `#4CAF50`
  - Reserved: `#F44336`
  - Recommended: `#FFD700`
  - Selected: `#2196F3`

## API Contract

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/tables/search` | Search with recommendations |
| `POST` | `/api/reservations` | Create a booking |
| `POST` | `/api/reservations/reset` | Regenerate random reservations |
| `GET` | `/api/tables` | All tables with status |

## Domain Model Summary

- **RestaurantTable** — id, name, capacity, zone (string), posX, posY, width, height, shape, features (enum set)
- **Reservation** — id, tableId, date, startTime, endTime, partySize, guestName
- **TableFeature** — enum: `WINDOW`, `PRIVATE`, `ACCESSIBLE`, `NEAR_PLAY_AREA`

## Key Design Decisions

1. Zone is a string field on `RestaurantTable`, not a separate entity
2. H2 in-memory — data is ephemeral, random reservations generated on startup
3. Recommendation scoring: efficiency (40%) + preference match (35%) + zone match (15%) + base (10%)
4. No authentication in MVP; table combining supported for large parties

## What NOT to Do

- Do not add external UI component libraries
- Do not add Redux, Zustand, or other state management
- Do not add Lombok (use Java records for DTOs, write constructors/getters for entities)
- Do not create a separate Zone entity
- Do not implement authentication
- Do not add Docker files unless explicitly asked or as part of the planned scope
- Do not over-engineer — this is a 1-week assignment, keep it simple

## Testing Expectations

- Backend: unit tests for `RecommendationService` (5+ scenarios covering perfect match, partial match, no preferences, oversized table, no results)
- Backend: integration test for search endpoint with MockMvc
- Frontend: manual E2E testing (no frontend test framework required)

## Git Conventions

- Commit style: conventional commits (`feat:`, `fix:`, `test:`, `docs:`, `chore:`)
- Use feature branches (`feat/`, `fix/`, `docs/`) merged into `main` via pull request or merge commit
- Each commit should be a logical, working increment

## Documentation Files

- `README.md` — setup, features, architecture overview
- `ARCHITECTURE.md` — domain model, API contract, design rationale
- `ASSUMPTIONS.md` — documented assumptions table
- `AI-USAGE.md` — AI-assisted development methodology
- `PROBLEMS.md` — issues encountered and how they were resolved

## Living Documentation Rules

**`AI-USAGE.md` must be updated whenever there is something meaningful to add.** After completing a phase, implementing a significant feature, or making an AI-assisted decision, append a row to the Change Log table in `AI-USAGE.md` with what AI did and what the human did. Do not batch documentation to the end — log as you go.

**`PROBLEMS.md` must be updated whenever an issue is encountered and resolved.** When debugging a bug, fixing a build failure, resolving a dependency conflict, or working around a limitation, append a row to the table in `PROBLEMS.md` with the problem, its cause, the solution, and today's date.

## UI Review Baseline

- The current accepted recommendation card layout may omit score breakdown even when score data is returned by the API.
- The current accepted floor plan may render tables with a uniform visual shape instead of matching the backend `shape` field.
- Hover tooltips and stronger visual differentiation for recommended vs selected tables are deferred polish items unless the user explicitly requests them.
- The current feature assignments per table are intentional: `NEAR_PLAY_AREA` is on M6, M7, P3, T3, T4 (not on T1/T2); `ACCESSIBLE` is on W4, M3, M6, M7, T2, T3 (not on W3). Do not flag these as inconsistencies with zone names.
- The Main Hall zone rect is intentionally fully transparent. The "PLAY AREA" label inside it is a visual annotation, not a separate zone entity.

## Code Attribution

This project is built almost entirely with AI. Human-written code is marked with `// HUMAN` comments. All other code is AI-generated.
