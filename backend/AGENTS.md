# Backend Agent Instructions

## Stack

- Spring Boot 3.5.0, Java 21, Maven (use `./mvnw`), H2 in-memory
- Dependencies: spring-boot-starter-web, spring-boot-starter-data-jpa, h2, spring-boot-starter-validation, spring-boot-devtools

## Package Structure

```
com.restaurant/
├── config/          CorsConfig, DataInitializer
├── model/           RestaurantTable, Reservation, TableFeature (enum)
├── dto/             SearchRequest, SearchResponse, TableRecommendation, ReservationRequest
├── repository/      TableRepository, ReservationRepository
├── service/         TableService, ReservationService, RecommendationService
└── controller/      TableController, ReservationController
```

## Conventions

- Entity class: `RestaurantTable` (not `Table`)
- Use Java records for DTOs
- Constructor injection only (no `@Autowired` on fields)
- Use `@Valid` on request bodies
- Config in `application.yml` (not `.properties`)
- No Lombok — write constructors/getters for entities, use records for DTOs

## Key Rules

- Reservation overlap detection: two reservations overlap if they share any time on the same table and date
- Random reservations: generate on startup via `CommandLineRunner`, spread across lunch (11:00–14:00) and dinner (18:00–22:00)
- Scoring weights are constants in `RecommendationService`: efficiency=0.40, preferences=0.35, zone=0.15, base=0.10
- Tables with `capacity < partySize` are excluded, not scored low
- Return score breakdown per table in search response

## Testing

- Unit tests: `RecommendationServiceTest` — 7 scenarios (JUnit 5 + Mockito)
- Integration tests: `TableControllerTest` (4) + `ReservationControllerTest` (5) — MockMvc with `@SpringBootTest`
- Context load: `RestaurantReservationApplicationTests`
- Run: `./mvnw test`
- **When tests are added, removed, or renamed, update `/TESTS.md` at the project root.**
