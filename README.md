# XenoPilot

Production-grade AI-native CRM foundation. This repository intentionally includes only the platform shell, authentication, API architecture, PostgreSQL integration, Docker support, and theme-ready SaaS dashboard. CRM features are not implemented yet.

## Structure

```text
frontend/   Next.js 15, TypeScript, TailwindCSS, shadcn-style UI, React Query
backend/    Spring Boot 3, Java 21, Security, JWT, JPA/Hibernate, PostgreSQL
```

## Quick Start

1. Copy environment files:

```bash
cp frontend/.env.example frontend/.env.local
cp backend/.env.example backend/.env
```

2. Start PostgreSQL and the apps with Docker:

```bash
docker compose up --build
```

3. Open:

- Frontend: http://localhost:3000
- Backend health: http://localhost:8080/api/health

## Local Development

Backend:

```bash
cd backend
mvn spring-boot:run
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

## Auth API

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`

JWTs are returned to the frontend and stored client-side for this scaffold. For hardened deployments, prefer an HttpOnly cookie boundary or a backend-for-frontend pattern.
