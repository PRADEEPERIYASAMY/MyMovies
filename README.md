# MyMovies

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)
![Language](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white)
![Min%20SDK](https://img.shields.io/badge/minSdk-23-blue)
![Target%20SDK](https://img.shields.io/badge/targetSdk-30-blue)
![DI](https://img.shields.io/badge/DI-Hilt-3DDC84)
![Local%20Store](https://img.shields.io/badge/Local%20Store-Room-02569B)
![Playback](https://img.shields.io/badge/Playback-ExoPlayer%20%2B%20TorrentStream-000000)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

**MyMovies** is a Kotlin Android client for discovering, streaming, and downloading movies from a YTS-style torrent catalog API, with local subtitle search (OpenSubtitles), an offline favorites/browse cache backed by Room, and BitTorrent-based playback via ExoPlayer + TorrentStream — all wired through a single-Activity, Hilt-driven MVVM pipeline.

Where a naive implementation would bolt a `RecyclerView` directly onto a Retrofit callback, this app treats the network response as *disposable* and the local Room cache as the *source of truth* for what the UI renders — network calls exist to refill the cache, not to feed the screen directly. That single decision is the spine the rest of this document explains.

---

## Table of Contents

1. [Problem Space](#1-problem-space)
2. [System Architecture](#2-system-architecture)
3. [Design Decisions & Rationale](#3-design-decisions--rationale)
   - [3.1 Cache-as-Source-of-Truth, Not Cache-as-Optimization](#31-cache-as-source-of-truth-not-cache-as-optimization)
   - [3.2 The `Result<T>` Sealed Wrapper](#32-the-resultt-sealed-wrapper)
   - [3.3 Torrent Playback — Magnet URI to ExoPlayer Frame](#33-torrent-playback--magnet-uri-to-exoplayer-frame)
   - [3.4 Subtitle Retrieval as a Second, Independent Client](#34-subtitle-retrieval-as-a-second-independent-client)
   - [3.5 Single-Activity Navigation](#35-single-activity-navigation)
4. [Module Reference](#4-module-reference)
5. [Data Flow — A Concrete Trace](#5-data-flow--a-concrete-trace)
6. [Tech Stack](#6-tech-stack)
7. [Known Rough Edges & Roadmap](#7-known-rough-edges--roadmap)
8. [Getting Started](#8-getting-started)
9. [License & Contributing](#9-license--contributing)

---

## 1. Problem Space

A movie catalog/torrent client has three properties that make "just call the API and bind a RecyclerView" the wrong architecture from the outset:

1. **The upstream API is a public, rate-limited, occasionally-flaky third-party service** (a YTS-compatible JSON API — see [`Routes.kt`](app/src/main/java/com/example/mymovies/api/Routes.kt)). Every screen that hits it directly inherits its latency and its outages.
2. **Playback is not "download then play."** A magnet link has no bytes at request time; ExoPlayer needs a readable file/stream, and TorrentStream needs to sequentially fetch and expose pieces as a `File` while the swarm is still filling in. The app has to bridge a torrent session and a media player without blocking the UI thread on either.
3. **Subtitles are sourced from a *different* provider than the video** (OpenSubtitles, via a bespoke `OpenSubtitlesService`, not the movie API), so the subtitle fetch is architecturally a second, parallel repository dependency, not a field on the movie response.

Every top-level decision in this codebase — the Room cache boundary, the `Result<T>` wrapper, the `DataSource`/`CacheSource` split, the `MovieRepository` fan-out — exists to answer one of those three problems without leaking their complexity into the Fragment layer.

## 2. System Architecture

```
┌────────────────────────────────────────────────────────────────────────┐
│                              MainActivity                              │
│               (single host Activity, Navigation Component)             │
│  SplashFragment → MovieFragment ⇄ TopRatedFragment ⇄ NewMoviesFragment  │
│                    ⇄ SavedFragment → InfoFragment → WatchFragment       │
└───────────────────────────────┬──────────────────────────────────────--┘
                                 │  observes StateFlow / LiveData
                                 ▼
                    ┌─────────────────────────┐
                    │   CommonViewModel          │
                    │   (@HiltViewModel,          │
                    │    extends BaseViewModel)    │
                    └────────────┬────────────-┘
                                 │  suspend calls, wrapped in Result<T>
                                 ▼
                    ┌─────────────────────────┐
                    │     MovieRepository        │
                    │  fetchTypeMovies·           │
                    │  fetchMovieDetails·          │
                    │  fetchTopRatedMovies·         │
                    │  save/delete/checkSavedMovie·  │
                    │  fetchMovieSubTitle·            │
                    │  downloadMovieSubTitle           │
                    └──────┬──────────┬───────────┬──┘
                           │          │           │
                           ▼          ▼           ▼
              ┌─────────────────┐ ┌───────────────┐ ┌────────────────────┐
              │   DataSource      │ │  CacheSource    │ │ OpenSubtitlesService │
              │  (Retrofit/        │ │  (Room DAO       │ │  (bespoke HTTP        │
              │   ApiInterface)    │ │   facade)        │ │   client, .srt fetch) │
              │  → list/details/    │ │  → MoviesDao,     │ │  → search by hash/     │
              │    rank/new movies   │ │    FavoriteDao     │ │    filename, download   │
              └─────────────────┘ └───────────────┘ └────────────────────┘
                           │          │
                           ▼          ▼
              ┌─────────────────────────────────┐
              │        MoviesDatabase (Room)       │
              │   movies(cache, TTL-less, type-      │
              │   tagged) · favorites (persistent)    │
              └─────────────────────────────────┘

                          WatchFragment (playback path)
              ┌─────────────────────────────────┐
              │  TorrentStream session               │
              │   magnet URI → sequential piece        │
              │   download → local file handle           │
              └────────────────────┬────────────────--┘
                                    ▼
              ┌─────────────────────────────────┐
              │            ExoPlayer                 │
              │   reads the growing local file as        │
              │   pieces complete; renders video frame    │
              └─────────────────────────────────┘
```

**DI backbone:** [`AppModule.kt`](app/src/main/java/com/example/mymovies/di/AppModule.kt) (app-level singletons: OkHttp/Retrofit client, Room database, `OpenSubtitlesService`), [`ApiModule.kt`](app/src/main/java/com/example/mymovies/api/ApiModule.kt) (network layer), and [`CacheModule.kt`](app/src/main/java/com/example/mymovies/cache/CacheModule.kt) (DAO providers) hand fully-constructed dependencies to `MovieRepository` via constructor injection — the repository never knows how a `Retrofit` instance or a `RoomDatabase` gets built, only that it receives a `DataSource` and a `CacheSource`.

## 3. Design Decisions & Rationale

### 3.1 Cache-as-Source-of-Truth, Not Cache-as-Optimization

The naive pattern — network call fills a list, list renders — is absent here on purpose. Look at [`fetchTypeMovies`](app/src/main/java/com/example/mymovies/repository/MovieRepository.kt):

```kotlin
suspend fun fetchTypeMovies(type: String, page: Int): Result<List<MoviesItem>> = try {
    getNetworkCategory(type, page)                                  // 1. refill cache from network
    val result = cacheSource.getCacheMoviesList(type, 20, page.times(10)) // 2. read from Room
    if (result.isEmpty()) throw IllegalArgumentException()
    Result.build { result }
} catch (e: Exception) {
    throw e
}
```

The Fragment never sees the network response shape directly for the browse lists — it sees whatever Room hands back, paginated with `limit`/`offset` semantics (`20, page.times(10)`) that are *independent* of whatever page size the upstream API happens to return. This buys three things simultaneously:

- **Decoupling of UI pagination from API pagination.** If the API changes its page size tomorrow, `MoviesDao` pagination is untouched.
- **A natural stale-cache invalidation point.** `deleteLastCache()` clears the type-tagged cache exactly once per cold app session (`firstLoad` companion flag) before the first category fetch, so subsequent category switches accumulate in the same table without wiping each other, while still guaranteeing a clean slate per session.
- **Graceful degradation.** `fetchTopRatedMovies` explicitly falls back to `getCacheRanking(page)` when the live network result is empty — the UI keeps rendering the last known-good ranking instead of an empty state, because the network being unreachable and the network returning zero results are treated as the same recoverable condition.

`checkFavMovieExist` after every `saveMovie` is the same instinct applied to writes: don't trust that a write succeeded — re-read it from the same source the UI will next query, so the "saved" state the UI reflects is always what Room actually has, not what the app *hopes* it wrote.

### 3.2 The `Result<T>` Sealed Wrapper

Every repository method returns `Result<T>`, not `T` and not a raw exception. This is the standard "don't let `try/catch` sprawl into the ViewModel" pattern, but it's worth being explicit about *why* it's non-negotiable here specifically: `MovieRepository` fans out to three independently-failing systems (YTS-style HTTP API, Room, OpenSubtitles HTTP API). Without a uniform result type, `CommonViewModel` would need three different failure-handling code paths instead of one. `Result.build { }` centralizes the success path; the `catch` blocks re-throw rather than swallow, which pushes the *decision* of what a failure means (empty state vs. retry vs. cached fallback) up to the one place — the repository method itself — that has enough context (which source failed, whether a fallback exists) to make that decision correctly.

### 3.3 Torrent Playback — Magnet URI to ExoPlayer Frame

`WatchFragment` is the most operationally interesting piece of this codebase, because it bridges two systems that were never designed to talk to each other:

- **TorrentStream** operates on *pieces*: it downloads a torrent's data sequentially (prioritizing the pieces needed for playback first) and exposes progress/file-ready callbacks.
- **ExoPlayer** operates on *files/streams*: it expects something it can seek and read, not a piece-completion callback.

The bridge is: TorrentStream is pointed at a magnet URI extracted from the movie's torrent metadata, told to stream sequentially, and once enough of the file is materialized on disk, ExoPlayer is handed a `Uri` pointing at that same growing local file. The player effectively starts consuming a file that TorrentStream is still writing to — which is *why* sequential (rather than rarest-first) piece prioritization matters: without it, the front of the file the player needs to read next might be the last piece to arrive.

This is also why permissions matter here in a way they wouldn't for a pure-streaming app — `READ_EXTERNAL_STORAGE`/`WRITE_EXTERNAL_STORAGE` in the manifest exist because the "stream" is, underneath, a real file being written to local storage in real time, not an in-memory buffer.

### 3.4 Subtitle Retrieval as a Second, Independent Client

`fetchMovieSubTitle` and `downloadMovieSubTitle` sit on `MovieRepository` next to the movie-catalog methods, but they talk to a completely different backend (`OpenSubtitlesService`, from `open-subtitles-android`) with a completely different contract — `search(userAgent, url)` returning `Array<OpenSubtitleItem>`, and a direct file download rather than a JSON model. Keeping this on the *same* repository (rather than spinning up a `SubtitleRepository`) was a deliberate simplification: from the ViewModel's perspective, "get me artifacts related to this movie" is one concern, even though underneath it fans out to two unrelated network clients. The seam is drawn at the DI boundary (`OpenSubtitlesService` is its own Hilt-provided singleton in `AppModule.kt`), so the two providers can be swapped independently without touching each other's code, even though they're consumed from the same class.

### 3.5 Single-Activity Navigation

`MainActivity` is the only entry point declared with an intent-filter in [`AndroidManifest.xml`](app/src/main/AndroidManifest.xml); everything else — `SplashFragment` → `MovieFragment`/`TopRatedFragment`/`NewMoviesFragment`/`SavedFragment` → `InfoFragment` → `WatchFragment` — is a Fragment transaction managed by the Navigation Component. `BaseActivity` and `BaseViewModel`/`CommonViewModel` exist specifically to hoist the things every screen needs (loading/error state plumbing, shared ViewModel scope) out of each Fragment, so `MovieFragment`, `TopRatedFragment`, and `NewMoviesFragment` — which are structurally near-identical (list, adapter, empty/error state) — differ only in *which* repository method they call, not in how they render the result.

## 4. Module Reference

```
app/src/main/java/com/example/mymovies/
├── api/                ApiInterface (Retrofit contract), ApiModule (Hilt network DI),
│                        DataSource (thin suspend wrapper over ApiInterface), Routes (endpoint constants) — §2, §3.1
├── cache/               CacheModule (Hilt DAO DI), CacheSource (Room facade used by the repository),
│                        MoviesDao, FavoriteDao, MoviesDatabase — §2, §3.1
├── di/                  AppModule — app-scoped singletons: HTTP client, Room database instance,
│                        OpenSubtitlesService — §3.4
├── models/              Movie, MovieDetails, FavoriteMovie, Result (§3.2), TypeConverters (Room <-> complex types),
│   └── response/         MoviesResponse, MoviesItem — raw API response shapes, kept separate from the
│                          Room-persisted Movie/FavoriteMovie models (§3.1)
├── repository/          MovieRepository — the single fan-out point over DataSource, CacheSource,
│                        and OpenSubtitlesService (§2, §3.1–§3.4)
├── utils/               UtilTypeConverter, ViewUtil — shared extension helpers (e.g. changeCategory,
│                        convertToFavorite used by the repository to tag/reshape data between layers)
│   └── constants/        shared constant definitions
├── viewmodels/           BaseViewModel, CommonViewModel (@HiltViewModel, the one ViewModel every
│   │                      screen shares), TemplateViewModel (scaffold for new screens)
│   └── actions/           sealed action/state contracts
├── views/
│   ├── activities/       MainActivity, BaseActivity (§3.5)
│   ├── fragments/        SplashFragment, MovieFragment, TopRatedFragment, NewMoviesFragment,
│   │                      SavedFragment, InfoFragment, WatchFragment (§3.3), TemplateFragment (scaffold)
│   └── adapters/         ItemMovieAdapter, ItemTopRatedAdapter, ItemSavedAdapter, ItemCastAdapter,
│                          ItemScreenShotAdapter, ItemSubtitleAdapter, ItemQualityAdapter,
│                          ItemMovieTypeAdapter, TemplateAdapter — one per list-backed screen
│       └── listener/       shared click/callback interfaces used across adapters
└── MyMoviesApplication.kt  @HiltAndroidApp entry point
```

## 5. Data Flow — A Concrete Trace

To make §3.1 concrete, here's what happens, end to end, when a user opens the "Action" category on `MovieFragment`:

1. `MovieFragment` asks `CommonViewModel` for movies of type `"Action"`, page `0`.
2. `CommonViewModel` calls `MovieRepository.fetchTypeMovies("Action", 0)`.
3. The repository calls `getNetworkCategory("Action", 0)`, which hits `DataSource.getMoviesCategory(...)` → `ApiInterface.getMoviesByCategory(...)` → the live YTS-style endpoint (`LIST_MOVIES` with `sort_by=download_count&order_by=desc&limit=50`, from [`Routes.kt`](app/src/main/java/com/example/mymovies/api/Routes.kt)).
4. On a non-empty network result: `deleteLastCache()` clears the session's stale rows exactly once, `result.changeCategory("Action")` tags every item with its category (since the upstream response doesn't carry that tag back), and `cacheSource.saveCacheMoviesList(result)` persists it via `MoviesDao`.
5. The repository then reads back through `cacheSource.getCacheMoviesList("Action", 20, 0)` — 20 rows, offset 0 — and returns *that*, wrapped in `Result.build { }`.
6. `CommonViewModel` exposes the `Result` to `MovieFragment`, which renders it through `ItemMovieAdapter` — completely unaware that a network call happened at all three steps upstream.
7. If step 3 throws (network down) or returns nothing, step 4 is skipped, and step 5 still runs — the user sees whatever was cached from the *previous* successful load of "Action", not a blank screen.

This is the pattern every other list-fetching path in `MovieRepository` follows with minor variations (`fetchTopRatedMovies` falls back to `getCacheRanking`; `fetchNewMovies` currently bypasses the cache read entirely — see [Roadmap](#7-known-rough-edges--roadmap)).

## 6. Tech Stack

| Concern | Choice | Notes |
|---|---|---|
| Language | Kotlin | |
| DI | Hilt (`hilt-android`, `hilt-android-compiler`) | App/network/cache singletons in `AppModule`, `ApiModule`, `CacheModule` — §2 |
| Async | Kotlin Coroutines + `kotlinx-coroutines-play-services` | `suspend` repository methods throughout |
| Networking | Retrofit2 + Gson converter + OkHttp logging interceptor | `ApiInterface`, `Routes` — movie catalog API |
| Local persistence | Room (`room-runtime`, `room-ktx`, `room-compiler`) | `MoviesDatabase`, `MoviesDao`, `FavoriteDao` — cache-as-source-of-truth, §3.1 |
| Navigation | Android Navigation Component (`navigation-fragment-ktx`, `navigation-ui-ktx`) | Single-Activity Fragment graph — §3.5 |
| Video playback | ExoPlayer | Reads the locally-materializing torrent file — §3.3 |
| Torrent engine | TorrentStream-Android | Sequential piece download from magnet URI — §3.3 |
| Subtitles | `open-subtitles-android` (`OpenSubtitlesService`) | Independent HTTP client for `.srt` search/download — §3.4 |
| Image loading | Glide (+ Picasso present for legacy call sites) | Poster/backdrop/cast images |
| Local auth | `androidx.biometric` | Optional biometric gate |
| Permission handling | Dexter | Runtime storage permission requests for torrent downloads |
| Crash/Analytics | Firebase Crashlytics, Analytics | |
| UI toolkit | Material Components, ConstraintLayout, ViewBinding, CardView, `transition-ktx`, CircleImageView, `android-gif-drawable`, `discrete-scrollview` | `viewBinding = true` at module level |

Full dependency list: [`app/build.gradle`](app/build.gradle).

## 7. Known Rough Edges & Roadmap

- **`fetchNewMovies` skips the Room round-trip** that every other list method uses — it returns the raw `MoviesResponse` from `DataSource` directly. Bringing it in line with `fetchTypeMovies`/`fetchTopRatedMovies` would give the "New" tab the same offline-fallback guarantee the rest of the app has.
- **Picasso and Glide are both still present.** V1-style leftover; consolidate on Glide and drop the Picasso dependency.
- **No automated tests yet.** `MovieRepository`'s fallback branches (§3.1) and `Result.build` (§3.2) are the highest-value, most side-effect-isolated starting points.
- **Torrent piece-priority tuning:** sequential download is functionally correct but not yet adaptive to playback position (e.g. re-prioritizing pieces on seek) — worth revisiting for scrubbing UX.
- **Extract subtitle logic into its own repository** once it grows past "search + download" (e.g. auto-sync/offset correction), to keep `MovieRepository` focused on the movie-catalog concern described in §3.4.
- **Formalize cache invalidation beyond the `firstLoad` flag** — a TTL or explicit pull-to-refresh signal would remove the implicit "once per cold start" assumption baked into `deleteLastCache()`.

## 8. Getting Started

```bash
git clone https://github.com/PRADEEPERIYASAMY/MyMovies.git
cd MyMovies
```

1. This project targets a YTS-compatible movie listing API (`Routes.kt`) — point `ApiModule`'s base URL at a reachable instance if the default is unavailable.
2. Open the repository root in Android Studio (Gradle + Hilt + Navigation Safe Args plugins) and let it sync.
3. Grant storage permissions on first run — required for torrent-backed playback and subtitle downloads (§3.3).
4. Build:

```bash
./gradlew build
```

## 9. License & Contributing

This project is built and maintained solo. If you'd like to contribute, open an issue first to discuss scope.

Licensed under [MIT](LICENSE).

---

**Author:** Pradeep Periyasamy
