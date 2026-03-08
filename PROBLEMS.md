# Problems & Solutions

Issues encountered during development and how they were resolved.

| # | Problem | Cause | Solution | Date |
|---|---|---|---|---|
| 1 | N+1 query problem in RecommendationService — `findOverlapping` called twice per table (once for status, once for availability), causing ~41 queries per search | Each table triggered its own DB query inside a loop | Fetch all reservations for the date once with `findByDate`, then filter in-memory using Java Streams for overlap checks | 2026-03-08 |
| 2 | Users could book reservations in the past — no date validation on ReservationRequest | `date` field had `@NotNull` but no temporal constraint | Added `@FutureOrPresent` annotation to the `date` field | 2026-03-08 |
| 3 | Race condition in ReservationService — concurrent requests could both pass the overlap check and create double bookings | No transaction isolation between the overlap check and the save | Added `@Transactional(isolation = Isolation.SERIALIZABLE)` to `createReservation` | 2026-03-08 |
