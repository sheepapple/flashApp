# ⚡ Flash

The news, in a flash. A mobile news app where users scroll a feed of summary cards, tap one to expand it into an AI-generated summary with an image and a link to the original article, and react with likes, shares, and anonymous comments.

## Features

<table>
  <tr>
    <td width="50%" align="center">
      <img src="GithubFeedImg" width="100%" alt="Feed of news cards"/>
      <br/>
      <b>Feed of news cards</b>
      <br/>
      A scrollable, paged feed (loads more as you scroll) showing each story's title, image, section, and AI summary.
    </td>
    <td width="50%" align="center">
      <img src="GithubCardImg" width="100%" alt="Expandable story view"/>
      <br/>
      <b>Expandable story view</b>
      <br/>
      Tap a card to read the full AI-generated summary and open the original Guardian article.
    </td>
  </tr>
  <tr>
    <td width="50%" align="center">
      <img src="GithubCommentImg" width="100%" alt="Anonymous comments"/>
      <br/>
      <b>Anonymous comments</b>
      <br/>
      A public comment thread per article, plus a "my comments" view.
    </td>
    <td width="50%" align="center">
      <img src="GithubSocialImg" width="100%" alt="Likes, shares, and library"/>
      <br/>
      <b>Likes, shares & library</b>
      <br/>
      Like stories, share them out via the system share sheet, and revisit saved articles from your profile's library.
    </td>
  </tr>
  <tr>
    <td width="50%" align="center">
      <img src="GithubNotificationsImg" width="100%" alt="Notifications"/>
      <br/>
      <b>Notifications</b>
      <br/>
      In-app notifications that deep-link to the relevant article.
    </td>
    <td width="50%" align="center">
      <img src="GithubTopicImg" width="100%" alt="Accounts, profile, and topic preferences"/>
      <br/>
      <b>Accounts & topic preferences</b>
      <br/>
      Sign up, log in, manage a profile (username, photo, topics of interest), and filter topics you'd rather not see.
    </td>
  </tr>
</table>

## Tech stack

| Layer | Choice |
|---|---|
| Mobile client | Kotlin + [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3), Supabase Kotlin SDK, Coil for images |
| Ingestion worker | Node.js — fetches articles, summarizes, upserts to the database |
| Backend API | Node.js / Express (minimal scaffold — see note below) |
| Database & Auth | [Supabase](https://supabase.com) — hosted PostgreSQL + built-in authentication |
| News data | [Guardian API](https://open-platform.theguardian.com/), cached in Postgres |
| AI summaries | [Gemini API](https://ai.google.dev) (`gemini-3.1-flash-lite`) |

### Why Postgres / Supabase

We handle two kinds of data: **article data** (the raw story plus its summary) and **relational data** (users, comments tied to articles, likes linking users to articles, saved lists). SQL fits the relational side, and Postgres's `JSONB` column lets us store the raw Guardian response as-is in `raw_data` and pull out structured columns (`web_title`, `web_url`, `image_url`, `published_at`, `section`, `summary`) for fast reads. Supabase gives us that Postgres database plus authentication in one free, integrated service — so there's no second user system to keep in sync.

## Architecture

```
                 ┌──────────────────┐
  Guardian API ─►│ Ingestion worker │──┐
  (newest-first  │   (worker/)      │  │ summarize each new
   pagination)   └──────────────────┘  │ article, then upsert
                          │            ▼
                     Gemini API   ┌────────────┐
                     (summaries)  │  Supabase  │
                                  │ (Postgres  │
                                  │  + Auth)   │
                                  └────────────┘
                                        ▲
   Android app ─────(read feed,         │
   (Supabase Kotlin   auth, likes, ─────┘
    SDK + anon key)   saves, comments)
```

Key idea: the app **never** calls the Guardian API directly. The ingestion worker polls the Guardian API, generates a summary for each new story via Gemini, and upserts everything into Postgres. Articles already in the database are skipped so re-runs don't burn the daily Gemini/Guardian quotas. Users only ever read from our own database, so external API call volume is fixed by the ingestion schedule, not by user traffic.

For this build, the Android app reads Supabase **directly** through the Supabase Kotlin SDK (using the public anon key) rather than going through the Express backend. `backend/` is a minimal scaffold (a single `/feed` endpoint) that a real deployment can grow into — a server tier can slot in front of Supabase later for per-user routing without changing the UI.

## Security note

Supabase auth is used for accounts; the anon key shipped in the client is the public, row-level-security-gated key — not a secret. The **service-role key** is used only by the worker and backend (server-side) and is never shipped to the app. Comment threads are intentionally public; like *counts* are public while saves are per-user.

## Stability

The app needs no device sensors — just an internet connection. Because the data pipeline depends on external APIs, the worker is built defensively: it throttles to stay under the Guardian (1 call/sec, 500/day) and Gemini (15/min, 500/day) rate limits, dedupes against stories already stored, and treats a failed summary as `null` so one bad summary never breaks ingestion.

## Future goals

- **More free publishers** — expand ingestion beyond the Guardian to additional free news APIs, widening topic and regional coverage without adding user-facing cost.
- **Paid publisher integration** — let users link their own subscriptions (e.g. NYT, WSJ) so the app can pull and summarize articles from paywalled sources they already pay for, rather than being limited to freely available content.
- **Smarter recommendations** — move beyond simple topic filtering to a more complex recommendation algorithm that factors in reading history, likes/saves, and engagement patterns to personalize the feed.

## Getting started

> Prerequisites: a [Supabase](https://supabase.com) project, a [Guardian API](https://open-platform.theguardian.com/access/) key, a [Gemini API](https://ai.google.dev) key, [Android Studio](https://developer.android.com/studio), and Node.js.

### 1. Database

Create an `articles` table in Supabase with columns: `id`, `web_title`, `web_url` (unique), `published_at`, `image_url`, `section`, `summary`, `raw_data` (JSONB) — plus tables for users, comments, likes, and saves.

### 2. Worker (and backend) — Node

```bash
cp .env.example .env   # then fill in the values below

cd worker
npm install
node index.js          # fetch from Guardian, summarize via Gemini, upsert to Supabase

# optional minimal backend:
cd ../backend
npm install
node index.js          # serves GET /feed on localhost:3000
```

`.env` (used by the worker/backend — server-side only):

```
SUPABASE_URL=
SUPABASE_ANON_KEY=
SUPABASE_SERVICE_ROLE_KEY=   # server only — never ship to the client
NEWS_API_KEY=                # Guardian API key
GEMINI_API_KEY=
```

### 3. Android app

Open `app/` in Android Studio and run on an emulator or device. Supabase credentials are read from `app/local.properties` (gitignored) and exposed via `BuildConfig`:

```
SUPABASE_URL=
SUPABASE_ANON_KEY=
```

> Note: build/run the app from **Android Studio**, not the Gradle CLI.

## Project structure

```
flash/
├── app/        # Android client (Kotlin + Jetpack Compose) — reads Supabase directly
├── backend/    # Express API scaffold (single /feed endpoint)
├── worker/     # scheduled Guardian ingestion + Gemini summary generation
└── README.md
```

## Team

- [Alex Mizrahi](https://github.com/sheepapple) - Ui Development, Feed screen, Social features

- [Jiaen Ma](https://github.com/aXun-2) - Ui Backend and Database design, User authentication, Notification designer


---

_Coursework project — Northeastern University._
