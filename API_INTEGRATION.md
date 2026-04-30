# API Integration Guide

## Overview

The Glassburet frontend is fully connected to the Spring Boot 3.3 backend via RESTful HTTP APIs and WebSocket for real-time updates. All data is fetched dynamically from the backend and no longer hardcoded. Authentication uses JWT tokens issued by the backend.

## Frontend API Client

Located in `src/services/api.js`, the API client provides centralized methods for all endpoints:

### Quotes API
- `quoteApi.getAll()` - Get all quotes
- `quoteApi.getFeatured()` - Get featured quote of the day
- `quoteApi.create(quoteDto)` - Create new quote
- `quoteApi.delete(id)` - Delete quote

### Liners API
- `linerApi.getAll()` - Get all liners
- `linerApi.incrementSaidCount(id)` - Increment times a liner was said

### Events API
- `eventApi.getUpcoming()` - Get upcoming events
- `eventApi.getByCategory(category)` - Filter by category
- `eventApi.create(eventDto)` - Create new event
- `eventApi.toggleAttendance(eventId, memberName)` - Toggle attendance
- `eventApi.delete(id)` - Delete event

### Scores API
- `scoreApi.submit(scoreSubmitDto)` - Submit score
- `scoreApi.getLeaderboard(period, game)` - Get leaderboard (period: 'daily', 'weekly', 'monthly', 'alltime'; game: 'WORDLE', 'CONNECTIONS', etc.)
- `scoreApi.getScoresForDate(date)` - Get scores for specific date

### Puzzle APIs

#### Wordle Puzzles
- `GET /api/puzzles/wordle` - Get all Wordle puzzles
- `GET /api/puzzles/wordle/daily` - Get daily Wordle puzzle
- `POST /api/puzzles/wordle` - Create new Wordle puzzle

#### Connections Puzzles
- `GET /api/puzzles/connections` - Get all Connections puzzles
- `GET /api/puzzles/connections/daily` - Get daily Connections puzzle
- `POST /api/puzzles/connections` - Create new Connections puzzle

#### Native Puzzles
- `GET /api/puzzles/native` - Get all custom puzzles
- `POST /api/puzzles/native` - Create new custom puzzle

### Gallery API
- `galleryApi.getAll(category)` - Get photos optionally filtered by category
- `galleryApi.upload(photoDto)` - Upload new photo
- `galleryApi.delete(id)` - Delete photo

### Stats API
- `statsApi.getStats()` - Get overall statistics (member count, quotes, events this week)

## Authentication

The backend uses JWT (JSON Web Tokens) via Spring Security:
- Login endpoint: `POST /auth/login` with credentials
- JWT token returned in response
- Token included in `Authorization: Bearer <token>` header for authenticated endpoints
- Token stored in browser session/localStorage on client
- Automatic token refresh on token expiration

**No secrets are exposed in the frontend** — sensitive configuration is handled server-side.

## Real-Time Updates (WebSocket)

Connected via `src/services/realtime.js`:
- `subscribeToUpdates(channel, callback)` - Subscribe to real-time events
- Supported channels: `scores`, `puzzles`, `events`, `liners`
- Updates broadcast to all connected clients when data changes
- Re-establishes connection on disconnect

## Environment Configuration

### Development
Create `.env` in root with:
```
VITE_API_URL=
```

### Production
Update `.env` with your production API URL:
```
VITE_API_URL=https://api.example.com/
```

The API URL is automatically picked up by Vite and injected at build time via `import.meta.env.VITE_API_URL`.

## Running Locally

### Option 1: Docker Compose (Recommended)
```bash
docker-compose up --build
```
Starts both frontend dev server and backend in Docker.

### Option 2: Separate Terminals

**Terminal 1 - Backend:**
```bash
cd backend
./gradlew bootRun
```
Backend runs on `http://localhost:8080`

**Terminal 2 - Frontend:**
```bash
npm install
npm run dev
```
Frontend runs on `http://localhost:5173`

## Pages Connected to API

| Page | Data Source | Endpoints Used |
|------|---|---|
| Hjem | Stats + Featured Quote | `/api/stats`, `/api/quotes/featured` |
| Quotes | All Quotes | `/api/quotes` |
| Liners | All Liners with Counts | `/api/liners`, `/api/liners/{id}/said` |
| Poeng | Leaderboard | `/api/scores/leaderboard` |
| Aktiviteter | Upcoming Events | `/api/events`, `/api/events/{id}/attendance` |
| Galleri | Photos | `/api/gallery` |

## Error Handling

All API calls include try-catch blocks. Failed requests log to console and gracefully degrade:
- Loading states shown while fetching
- Empty states displayed if no data
- Error messages logged for debugging

## Database

The backend uses H2 in-memory database by default (configured in `src/main/resources/application.properties`). Data persists for the session but resets on server restart.

For production, PostgreSQL is configured via environment variables (no connection strings in code).

## Testing & Coverage

### Running Tests
```bash
cd backend
./gradlew test
```

### JaCoCo Test Coverage
- **Automatic**: Coverage reports generated after test run
- **Location**: `backend/build/reports/jacoco/test/`
- **Formats**: 
  - HTML report: `index.html` (interactive dashboard)
  - XML report: `jacocoTestReport.xml` (CI/CD integration)
- **CI/CD**: Coverage reports uploaded as artifacts on every test run
- **Access**: GitHub Actions > Artifacts tab

## Contact

For issues, suggestions, or contributions:
- **Email**: thomastj278@gmail.com
- **Footer**: Added contact link in the app footer
