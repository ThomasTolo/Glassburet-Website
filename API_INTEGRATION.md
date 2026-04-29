# API Integration Guide

## Overview

The Glassburet frontend is now fully connected to the Spring Boot backend via RESTful APIs. All data is fetched dynamically from the backend and no longer hardcoded.

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
- `scoreApi.getLeaderboard(period)` - Get leaderboard (period: 'daily', 'weekly', 'monthly', 'alltime')
- `scoreApi.getScoresForDate(date)` - Get scores for specific date

### Gallery API
- `galleryApi.getAll(category)` - Get photos optionally filtered by category
- `galleryApi.upload(photoDto)` - Upload new photo
- `galleryApi.delete(id)` - Delete photo

### Stats API
- `statsApi.getStats()` - Get overall statistics (member count, quotes, events this week)

## Environment Configuration

### Development
Create `.env` in root with:
```
VITE_API_URL=
```

### Production
Update `.env` with your production API URL:
```
VITE_API_URL=https://api.glassburet.example.com/api
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

The backend uses H2 in-memory database by default (configured in `application.properties`). Data persists for the session but resets on server restart.

To use PostgreSQL in production, update `application.properties` and provide connection environment variables.
