# AI-Assisted Development

## Approach

This project was developed using AI tools as part of a deliberate engineering workflow. AI was used to accelerate development while all architectural decisions, code quality, and user experience remained under human control.

The goal was not to minimize effort, but to maximize quality within a constrained timeline by focusing human attention on judgment, design, and integration while leveraging AI for code generation and pattern implementation.

## Tools Used

- **Amp** — AI coding agent for code generation, architecture planning, and implementation
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
