# Glassburet — Website

A full-stack application for the Glassburet community at UiB. Built with Vue 3 + Vite frontend and Spring Boot 3.3 backend.

## Tech Stack
- **Frontend**: Vue 3, Vite, vue-router, WebSocket support
- **Backend**: Spring Boot 3.3, Spring Security (JWT), Spring Data JPA, PostgreSQL, H2
- **CI/CD**: GitHub Actions with JaCoCo test coverage reporting
- **Deployment**: Docker & Docker Compose

## Prerequisites
- **Frontend**: Node.js 18+ (or a recent LTS) and npm
- **Backend**: JDK 21, Gradle 8.10+

## Quick start
Install dependencies:

```bash
npm install
```

Run the development server (hot-reload):

```bash
npm run dev
```

Build for production:

```bash
npm run build
```

Preview the production build locally:

```bash
npm run preview
```

## Project Structure

```
.
├── frontend/          # Vue 3 + Vite application
│   └── src/
│       ├── components/       # Reusable Vue components
│       ├── pages/            # Page components
│       ├── services/         # API client, auth, WebSocket
│       ├── router/           # Route definitions
│       └── main.js           # App bootstrap
├── backend/           # Spring Boot application
│   ├── src/
│   │   ├── main/java/com/glassburet/
│   │   │   ├── controller/   # REST endpoints
│   │   │   ├── service/      # Business logic
│   │   │   ├── model/        # JPA entities
│   │   │   ├── dto/          # Data transfer objects
│   │   │   ├── config/       # Configuration beans
│   │   │   ├── security/     # JWT & auth
│   │   │   └── realtime/     # WebSocket handlers
│   │   └── test/             # Unit & integration tests
│   └── build.gradle          # Gradle with JaCoCo
└── docker-compose.yml        # Development environment

## Files of Interest
- `frontend/src/services/api.js` — Centralized API client
- `frontend/src/services/realtime.js` — WebSocket subscriptions
- `backend/build.gradle` — Build configuration with JaCoCo
- `.github/workflows/backend-ci-cd.yml` — CI/CD pipeline

