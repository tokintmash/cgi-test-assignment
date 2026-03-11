# AI-Assisted Development

## Approach

This project was developed using AI tools as part of a deliberate engineering workflow. AI was used to accelerate development while all architectural decisions, code quality, and user experience remained under human control.

The goal was not to minimize effort, but to maximize quality within a constrained timeline by focusing human attention on judgment, design, and integration while leveraging AI for code generation and pattern implementation.

## Tools Used

- **Amp** — AI coding agent for code generation, architecture planning, and implementation
- **Claude Code** — UI/UX review and frontend refinement (with ui-ux-pro-max skill for design intelligence)
- **ChatGPT** — design discussions, algorithm reasoning, documentation drafting

## How AI Was Used

### Architecture & Planning (Human-Led, AI-Assisted)

- The implementation plan was created collaboratively: I defined project constraints, scope priorities, and quality goals. AI proposed a phased plan which I reviewed, compressed, and adapted.
- Domain model structure was discussed with AI, then reviewed for naming conventions, JPA relationship correctness, and real-world sensibility.
- Scoring algorithm weights were proposed by AI, then tested against realistic scenarios and adjusted based on results.

### Code Generation (AI-Generated, Human-Reviewed)

- Entity classes, repository interfaces, and controller boilerplate were AI-generated from specifications I wrote, then reviewed for correctness.
- The recommendation scoring service was AI-generated from a detailed algorithm description, then verified through unit tests I designed.
- React components including the SVG floor plan were AI-generated from layout specifications, then manually adjusted for visual quality and interaction behavior.
- Unit tests were co-created: I defined the scenarios, AI generated the test code, I verified the assertions were meaningful.

### What Was Purely Human

- Floor plan layout design — zone arrangement, table count, positioning
- UX flow decisions — what the user sees, in what order, with what feedback
- Scoring weight calibration — tested with sample queries until results felt right
- All Git commits and commit messages
- Documentation tone, content accuracy, and final review
- Integration testing — full user flow walkthrough in the browser
- Scope decisions — what to build, what to cut, what to document as future work

## Process

1. **Plan first** — architecture and scope decisions documented before any code was written
2. **Generate with specifications** — AI received detailed descriptions of what to produce, not open-ended requests
3. **Review everything** — every AI-generated file was read, understood, and tested before committing
4. **Test the critical path** — recommendation engine has unit test coverage because it is the core logic
5. **Document as I go** — assumptions and AI usage logged during development, not retrofitted

## Transparency

This project was built almost entirely with AI tools. My role was directing the AI, making architectural and UX decisions, and verifying that the result works correctly. Code written by me personally is marked with `// HUMAN` comments.

## Change Log

| Date | Area | What AI Did | What I Did |
|---|---|---|---|
| 2026-03-07 | Planning | Generated phased implementation plan | Defined constraints, compressed scope, made final decisions |
| 2026-03-07 | Architecture | Proposed domain model, API contract, scoring algorithm | Reviewed naming, relationship design, scoring weights |
| 2026-03-07 | Documentation | Drafted README, ARCHITECTURE, ASSUMPTIONS, AI-USAGE | Reviewed tone, accuracy, completeness |
| 2026-03-07 | Scaffolding | Generated Spring Boot + React project setup | Verified both projects start cleanly |
| 2026-03-08 | Backend Core | Generated entities (RestaurantTable, Reservation, TableFeature), repositories, services, controllers, DataInitializer, CorsConfig, ReservationRequest DTO via parallel sub-agents | Instructed AI to split work between sub-agents, created branch, reviewed and approved commands, tested all endpoints with curl and H2 console |
| 2026-03-08 | Recommendation Engine | Generated RecommendationService with scoring algorithm, DTOs (SearchRequest, SearchResponse, TableRecommendation, ScoreBreakdown, TableStatus), search endpoint on TableController, and 7 unit tests covering perfect match, partial match, no preferences, oversized table, no results, reserved tables, and ranking | Reviewed scoring formula against ARCHITECTURE.md spec, verified all 8 tests pass |
| 2026-03-08 | Code Review Fixes | Fixed N+1 query (batch-fetch reservations by date, filter in-memory), added `@FutureOrPresent` validation on ReservationRequest.date, added `@Transactional(isolation = SERIALIZABLE)` to prevent double-booking race condition, updated test mocks to match new `findByDate` approach | Ran code review, identified 3 issues, directed AI to fix all and update tests |
| 2026-03-09 | Frontend Phase 3 | Pulled Canva design guidelines, implemented full React frontend (typed API client, search form, SVG floor plan/table states, recommendation panel with score breakdown, booking dialog, end-to-end flow), and created responsive CSS theme with loading/error/empty states | Chose UI direction based on Canva guidance, reviewed interactions/state flow, validated with `npm run lint` and `npm run build`, and approved phase completion |
| 2026-03-09 | Frontend UI refinement | Applied dark theme across global/layout/component styles and simplified recommendation cards to show only table name, zone, and capacity | Requested targeted UI changes, reviewed updated visual hierarchy/content scope, and revalidated with `npm run lint` and `npm run build` |
| 2026-03-09 | Frontend layout and ranking tweak | Limited frontend recommendation set to top 5 results to simplify decision flow and keep floor-plan highlighting focused | Requested keeping Search panel in the left pane top area and reducing suggested table count, then verified output via lint/build |
| 2026-03-09 | Frontend layout update | Moved the Search panel from the left pane to the top of the right pane above recommendations to match requested panel hierarchy | Requested right-pane placement for Search, reviewed final pane ordering, and verified with `npm run lint` and `npm run build` |
| 2026-03-09 | Frontend layout proportions | Adjusted desktop pane ratio so the left pane is 20% wider than before (`2fr` to `2.4fr`) and the right pane proportionally narrower | Requested pane-width rebalance, reviewed responsive behavior, and revalidated with `npm run lint` and `npm run build` |
| 2026-03-09 | Floor plan shape update | Updated SVG table rendering to draw all tables as rectangles regardless of backend shape field | Requested consistent rectangular table visuals and verified with `npm run lint` and `npm run build` |
| 2026-03-09 | Search panel placement and orientation | Moved Search panel directly below header and converted form layout to a horizontal full-width block with responsive wrapping | Requested header-adjacent horizontal search bar matching header width, reviewed interaction flow, and revalidated via lint/build |
| 2026-03-09 | Search form row structure update | Refactored search inputs into a two-line layout with primary inputs on the first row, preferences on the second row, right-aligned submit button, and button label changed to `Search` | Requested 2-line options layout plus far-right search action and confirmed behavior/build correctness with lint/build |
| 2026-03-09 | Floor plan zone spacing alignment | Repositioned top zone rectangles (`Window`, `Main Hall`, `Private`) so horizontal gaps are equal between zones and container sides | Requested equal horizontal spacing for the top zones and validated the updated SVG layout with lint/build |
| 2026-03-09 | Floor plan margin tightening | Reduced equal horizontal top-zone margins by widening and repositioning `Window`, `Main Hall`, and `Private` zone bounds while preserving equal spacing | Requested smaller horizontal margins after equal-spacing alignment and validated with lint/build |
| 2026-03-09 | Floor plan vertical rebalance | Expanded upper zone heights, set terrace width to match combined `Main Hall + Private` span, and repositioned terrace lower with equal top/inter-zone/bottom vertical spacing | Requested terrace width/position and upper-zone height adjustments with equalized vertical spacing; validated with lint/build |
| 2026-03-10 | Floor plan baseline and terrace rebalance | Updated project guidance so future reviews treat compact recommendation cards, uniform table shapes, and deferred tooltip/state polish as intentional; shrank seeded table footprints, moved `W5` to terrace as `T4`, expanded terrace to full width, and tightened recommendation metadata to one line with `Seats` wording | Chose which review findings to ignore vs act on, approved the new zone/table distribution, and requested the recommendation-card content change |
| 2026-03-10 | Floor plan height parity | Adjusted the desktop grid and panel sizing so Floor plan and Recommendations stretch to the same height, and enlarged zone bounds while preserving the existing inter-zone gaps to create more internal breathing room | Requested equal panel height and more free space inside zones without changing the gaps between zones, then reviewed and approved the updated layout |
| 2026-03-10 | Table alignment follow-up | Repositioned seeded Window, Terrace, and Private tables to match the manually adjusted zone layout, moved terrace tables 20px right and bottom-aligned them, and reduced `P1`/`P2` capacity to 2 | Manually adjusted the zone gaps in the SVG, then specified the per-zone table alignment rules and private-table capacity changes |
| 2026-03-10 | Upper-zone vertical and private-table sizing tweak | Shifted all Window, Main Hall, and Private tables up by 22px in seed data and resized `P1`/`P2` to the same 48x48 footprint used by existing 2-seat tables while preserving their right alignment | Requested the upward shift and explicit size match to `M7` / `W1`, then reviewed the resulting layout constraints |
| 2026-03-10 | Floor plan architectural elements | Reduced Window and Private zone heights to 320, moved M1–M3 left edge to x=185, added white wall lines (vertical between Main Hall and Private, 3 horizontal separating Private tables), a light-blue window line along the left edge, and a terracotta entrance line in the gap between Window and Terrace zones | Specified zone heights, table repositioning, wall placement and colors, and chose where the window and entrance should go |
| 2026-03-10 | Floor plan layout and feature reassignment | Repositioned M4–M6 (right-aligned to M4's right edge), centered M7 between columns, made zone-main transparent, added "PLAY AREA" label, reassigned `NEAR_PLAY_AREA` to M6/M7/P3/T3/T4 and `ACCESSIBLE` to W4/M3/M6/M7/T2/T3 | Directed all table positions, zone visibility, label placement, and feature assignments per table |
| 2026-03-10 | Frontend code review & cleanup | Ran full code review of Phase 3, then fixed 6 issues: deleted Vite boilerplate `App.css`, set page title to "Restaurant Table Recommender", updated React 18 → 19 references in `AGENTS.md` (root + frontend) and `README.md`, wrapped `runSearch` in `useCallback` for stable reference, and removed unused `ScoreBreakdown` component + CSS | Requested the review, decided React 19 is correct (no React 18 requirement), and directed all fixes |
| 2026-03-10 | UI/UX review & search form refinement | Ran UI/UX review using ui-ux-pro-max design skill against screenshot, identifying accessibility (color-only info, focus rings, touch targets), typography (Trebuchet MS → Inter), and form UX issues. Implemented: switched global font to Inter via Google Fonts, replaced native date/time/number inputs with styled `<select>` dropdowns (time slots 10:00–22:00, party size 1–20, duration 30m–3h in 30min increments), added `color-scheme: dark` and custom SVG chevron for consistent dark-themed selects, made date field open picker on click, and replaced preference checkboxes with togglable pill-shaped tag buttons | Reviewed screenshot, directed which review findings to act on first, requested specific UX changes (select dropdowns over native inputs, 30min duration increments, tag-style preferences), tested each iteration and reported issues (date picker not opening), and approved final result |
| 2026-03-10 | Recommendation logic overhaul | Changed recommendation filtering from soft scoring to strict: tables must have **all** requested preferences and match the selected zone to appear. Added `WINDOW` feature to M1/M4, resized T3 to match M2/M5 dimensions, removed "Private area" from preference tags (covered by zone select), removed "Window" from zone dropdown (covered by preference tag), updated empty-state message to "No matches :(", and updated unit tests for strict filtering | Documented search issues in SEARCH.md, directed all logic and data changes, decided which concepts belong to zones vs preferences |
