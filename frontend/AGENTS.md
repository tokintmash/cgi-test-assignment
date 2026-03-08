# Frontend Agent Instructions

## Stack

- React 18, TypeScript (strict mode), Vite
- No component libraries (no MUI, no Ant Design)
- No state libraries (no Redux, no Zustand) — use `useState` / `useReducer`
- No axios — use `fetch` wrapper
- Plain CSS (no Tailwind, no CSS-in-JS)

## File Structure

```
src/
├── types/           index.ts (shared interfaces)
├── api/             reservationApi.ts (fetch wrapper)
├── components/      PascalCase.tsx files
│   ├── SearchForm.tsx
│   ├── FloorPlan.tsx
│   ├── TableShape.tsx
│   ├── RecommendationPanel.tsx
│   ├── BookingDialog.tsx
│   └── ScoreBreakdown.tsx
└── styles/          app.css (or per-component CSS files)
```

## Conventions

- Functional components + hooks only
- PascalCase for component files, camelCase for utilities
- SVG floor plan: native SVG elements (`<rect>`, `<circle>`, `<text>`, `<g>`) — no SVG libraries

## Color Palette (CSS custom properties)

```css
--color-available: #4CAF50;
--color-reserved: #F44336;
--color-recommended: #FFD700;
--color-selected: #2196F3;
```

## API Base URL

Development: `http://localhost:8080`

## Key Rules

- Floor plan tables are positioned using `posX`, `posY`, `width`, `height` from the API
- Tables are colored by status: available (green), reserved (red), recommended (gold), selected (blue)
- Recommended tables show score breakdown (efficiency, preference match, zone match)
- Clicking a recommended/available table opens a booking confirmation
- Reserved tables are visually distinct and not clickable
- Show loading, error, and empty states for all API calls
