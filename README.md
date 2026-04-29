# Glassburet — Website

Small Vue 3 + Vite site used as the Glassburet frontend.

## Prerequisites
- Node.js 18+ (or a recent LTS) and npm

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

Notes
- The Vite entry is `index.html` at the project root.
- The app uses Vue 3 and `vue-router` (hash history) so it can be served as static files.
- For a simple static preview of `dist/` after `npm run build` you can also use `npx serve dist` or any static file server.

Files of interest
- `src/` — application source
- `src/main.js` — app bootstrap
- `src/router/index.js` — routes and `createWebHashHistory()`
- `index.html` — Vite entry

