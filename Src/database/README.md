# Database (ITR2)

This project uses **Room** (SQLite) as the persistent storage engine.

## Location

- **Schema & DAO**: `Src/app/src/main/java/com/example/motivationalmornings/Persistence/DailyContentRepository.kt`
  - Entities: `Intention`, `QuoteOfTheDay`, `RssFeedUrl`
  - DAO: `DailyContentDao`
  - Database: `AppDatabase`
- **Repository implementation**: `RoomContentRepository` in `Persistence/ContentRepository.kt`

## Setup

No separate DB server or installation is required. Room uses an embedded SQLite database stored in the app's data directory (`motivational_mornings_db`).

Default content (five quotes) is inserted when the database is first created (see `AppDatabase` callback).

## Switching to stub (for testing)

Set `DatabaseConfig.USE_REAL_DATABASE = false` in `DatabaseConfig.kt` to use the in-memory stub (`HardcodedContentRepository`) instead of Room. Same content is available in both.
