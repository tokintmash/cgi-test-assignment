# Assumptions & Design Decisions

Documented assumptions made during development, with reasoning for each.

| # | Assumption | Reasoning |
|---|---|---|
| 1 | **Single restaurant, single floor** | The assignment describes one restaurant with zones. Multi-restaurant support would multiply scope without demonstrating additional technical skill. Documented as a future extensibility point. |
| 2 | **No user authentication** | The assignment focuses on reservation search and recommendation logic. Authentication is orthogonal and would consume time better spent on the core feature. A real system would require it. |
| 3 | **2-hour default reservation duration** | Standard restaurant reservation length. Users can optionally select 1h, 1.5h, 2h, 2.5h, or 3h. Simpler than free-form time entry and easier to validate overlap checks. |
| 4 | **H2 in-memory database, no persistence across restarts** | Enables a zero-configuration demo experience. Random reservations regenerate on each startup, so the app is always in a demoable state. In production, this would be PostgreSQL or similar. |
| 5 | **10-15 random reservations generated on startup** | Creates a realistic-looking floor plan for demo purposes. Spread across lunch (11:00–14:00) and dinner (18:00–22:00) windows. A "Reset" button allows regeneration without restart. |
| 6 | **Table combining for large parties** | If a party exceeds any single table's capacity, the system recommends pushing adjacent tables together. Adjacency is determined by table positions in the floor plan. |
| 7 | **Top 5 recommendations returned** | Balances guidance (highlighting the best option) with user choice (offering alternatives). The #1 result is visually emphasized on the floor plan. |
| 8 | **Scoring weights: efficiency 40%, preferences 35%, zone 15%, base 10%** | Efficiency is weighted highest because seating 2 people at an 8-top is a real business cost. Preference match is second because it drives customer satisfaction. Weights are defined as constants and easily adjustable. |
| 9 | **Floor plan is a stylized SVG, not a photorealistic rendering** | A top-down schematic view with labeled zones and positioned tables is sufficient to demonstrate the concept. Photorealism would require design assets and add no technical value. |
| 10 | **Docker included for easy setup** | A `docker-compose.yml` is provided so reviewers can start the full stack with a single command. Both backend and frontend are containerized. |
| 11 | **Party size can exceed a single table's capacity** | The system supports table combining (see #6), so party size is not capped at the largest single table. |
| 12 | **All times are server-local, no timezone handling** | For a single-restaurant demo, timezone conversion adds complexity without user-facing benefit. A production system would use UTC storage with timezone-aware display. |
