# ⚡ Flash

The news, in a flash. A mobile news app where users scroll a feed of summary cards ("nodes"), tap one to expand it into an AI-generated summary with images and a link to the original article, and react with likes, shares, and anonymous comments.

## Features

- **Feed of news nodes** — scrollable cards showing title, image, and a short truncated summary
- **Expandable story popup** — tap a node to open an AI-generated summary, images, and a link out to the original publisher
- **Anonymous comments** — public comment thread per article
- **Likes & shares** — react to and share stories
- **Library** — liked and saved articles
- **Notifications** — breaking stories, comment replies, and digests
- **Accounts** — sign up, log in, and a personal profile (topics of interest, profile photo)

## Tech stack

| Layer | Choice |
|---|---|
| Mobile client | _(your app framework — e.g. Kotlin / Jetpack Compose)_ |
| Backend API | _(your server — e.g. Node.js / Express)_ |
| Database & Auth | [Supabase](https://supabase.com) — hosted PostgreSQL + built-in authentication + file storage |
| News data | [Guardian API](https://open-platform.theguardian.com/), cached in Postgres |
| AI summaries | [Gemini API](https://ai.google.dev.)
| Hosting (dev) | Render / Railway / Fly (backend + worker) |

### Why Postgres / Supabase

We handle two kinds of data: **article data** (the raw article info) and **relational data** (users, comments tied to articles, likes linking users to articles, saved lists). SQL fits the relational side, and Postgres's `JSONB` column lets us dump the raw response from the undecided news API as-is and structure it later. Supabase gives us that Postgres database plus authentication in one free, integrated service — so there's no second user system to keep in sync.

## Architecture

```
                 ┌──────────────────┐
   News API ───► │  Ingestion worker │ ──(upsert)──► ┌────────────┐
  (scheduled)    │  (cron / poller)  │               │  Supabase  │
                 └──────────────────┘   ┌──────────► │ (Postgres  │
                          │             │            │  + Auth)   │
                   Gemini API           │            └────────────┘
                  (summaries)           │                  ▲
                                        │                  │
   Mobile app ──(request + token)──► Backend API ──────────┘
                                  (reads/filters by
                                   authenticated user)
```

Key idea: the app **never** calls the news API directly. A background worker polls the news API on a schedule, generates summaries via the Gemini API, and caches everything in Postgres. Users only ever read from our own database — so API call volume is fixed by the polling schedule, not by user traffic, which keeps us under rate limits.

## Security note

The backend always derives the current user from their **Supabase auth token**, never from an ID sent by the client. Personal queries (saves, likes) are filtered by that authenticated user ID, so one user can never read another's data. Comment threads are intentionally public; like *counts* are public while *who* liked something is per-user.

## Stability

The app needs no device sensors — just an internet connection. Because it depends on external API calls, a **health / pulse check** monitors the API gateway and ingestion worker. The most important signal is *time since last successful fetch*; it also watches for rate-limit (`429`) and error (`5xx`) responses.

## Getting started

> Prerequisites: a [Supabase](https://supabase.com) project and a Gemini API key.

```bash
# 1. Clone
git clone <repo-url>
cd flash

# 2. Configure environment
cp .env.example .env
# then fill in the values below

# 3. Install & run (adjust to your stack)
# backend:
npm install
npm run dev
```

### Environment variables

```
SUPABASE_URL=
SUPABASE_ANON_KEY=
SUPABASE_SERVICE_ROLE_KEY=   # backend only — never ship to the client
NEWS_API_KEY=
GEMINI_API_KEY=
```

## Project structure

```
flash/
├── app/        # mobile client
├── backend/    # API server (serves the app, talks to Supabase)
├── worker/     # scheduled news ingestion + summary generation
└── README.md
```

## Team

_(team members)_

---

_Coursework project — Northeastern University._
