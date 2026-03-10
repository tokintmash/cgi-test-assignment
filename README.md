# 🍽️ Smart Restaurant Reservation System

An intelligent table reservation system with an interactive SVG floor plan and transparent recommendation scoring.

![Demo Screenshot](docs/screenshots/main-view.png)

## ✨ Features

- **Interactive floor plan** — SVG-based top-down restaurant view with color-coded table status
- **Smart recommendations** — tables ranked by capacity efficiency, preference match, and zone fit
- **Transparent scoring** — see *why* each table is recommended with per-factor score breakdown
- **Preference support** — window seat, privacy, accessibility, children's play area proximity
- **Realistic demo data** — randomly generated reservations on each startup, with a reset button

## 🚀 Quick Start

### Prerequisites

- Java 21+
- Node.js 18+

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

The API starts at `http://localhost:8080`.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173` in your browser.

## 🏗️ Architecture

| Layer | Technology | Rationale |
|---|---|---|
| Backend | Spring Boot 3.x + Java 21 | Assignment requirement |
| Database | H2 (in-memory) | Zero configuration, ideal for demo |
| Frontend | React 19 + TypeScript | Strong ecosystem, type safety, fast dev with Vite |
| Floor plan | SVG (no libraries) | Simple, stylable, accessible, no external dependencies |
| Build | Maven | Convention in Spring Boot ecosystem |

### How the Recommendation Engine Works

When a user searches for a table, the system:

1. Filters out tables that are reserved during the requested time window
2. Filters by minimum capacity for the party size
3. Filters by zone if one is requested
4. Scores each remaining table on three factors:
   - **Capacity efficiency (40%)** — prefers tables whose size closely matches the party (avoids seating 2 people at an 8-top)
   - **Preference match (35%)** — fraction of requested preferences the table supports
   - **Zone match (15%)** — bonus if the table is in the preferred zone
   - **Base score (10%)** — ensures all valid tables get a minimum score
5. Returns the top-ranked tables with a per-factor score breakdown

See [ARCHITECTURE.md](ARCHITECTURE.md) for detailed design documentation.

## 📐 API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/tables/search` | Search available tables with recommendations |
| `POST` | `/api/reservations` | Book a table |
| `POST` | `/api/reservations/reset` | Regenerate random reservations |
| `GET` | `/api/tables` | Get all tables with current status |

## 🧪 Testing

```bash
cd backend
./mvnw test
```

Unit tests cover the recommendation scoring logic — the core "smart" component of the system.

## 📋 Documentation

- [ARCHITECTURE.md](ARCHITECTURE.md) — domain model, design decisions, API contract
- [ASSUMPTIONS.md](ASSUMPTIONS.md) — documented assumptions with reasoning
- [AI-USAGE.md](AI-USAGE.md) — AI-assisted development methodology and transparency
- [PROBLEMS.md](PROBLEMS.md) — issues encountered and how they were resolved

## 📝 License

Built as a technical assessment submission.
