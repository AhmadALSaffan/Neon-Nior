<div align="center">

<img src="https://ik.imagekit.io/qeitebnxx/icon(1).png" alt="Neon Noir Banner" width="10%" />

# 🎬 Neon Noir
### A cinematic, dark-themed Android movie app

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-API%2026+-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com/)
[![MVVM](https://img.shields.io/badge/Architecture-MVVM-FF8B9B?style=flat-square)](#architecture)
[![OMDB API](https://img.shields.io/badge/API-OMDB-81ECFF?style=flat-square)](https://www.omdbapi.com/)
[![Firebase](https://img.shields.io/badge/Auth-Firebase-FFCA28?style=flat-square&logo=firebase&logoColor=black)](https://firebase.google.com/)
[![Status](https://img.shields.io/badge/Status-In%20Development-orange?style=flat-square)](#)

</div>

---

## Overview

**Neon Noir** is a native Android movie discovery app built with Kotlin and a clean MVVM architecture. It lets users search millions of movies and series via the OMDB API, manage a personal watchlist, track viewing history, and sign in with email or Google — all wrapped in a deep dark aesthetic with neon pink, purple, and cyan accents.

> Fonts: **Epilogue** (display) · **Manrope** (body)  
> Color palette: `#0E0E11` base · `#FF8B9B` pink · `#A98BFF` purple · `#81ECFF` cyan

---

## Screenshots

| Home | Detail | Search | Library |
|---|---|---|---|
| <img src="https://ik.imagekit.io/qeitebnxx/Screenshot_20260401_135121_Neon%20Noir.jpg" width="110%" style="vertical-align: top"> | <img src="https://ik.imagekit.io/qeitebnxx/Screenshot-20260401-141353-Neon.jpg" width="100%" style="vertical-align: top"> | <img src="https://ik.imagekit.io/qeitebnxx/Screenshot_20260404_171715_Neon%20Noir.jpg" width="100%" alt="Search placeholder"> | <img src="https://ik.imagekit.io/qeitebnxx/Screenshot_20260406_231035_Neon%20Noir.jpg" width="100%" alt="Library placeholder"> |
---

## Features

- 🔐 **Authentication** — Email/password sign-up and Google Sign-In via Firebase Auth
- 🎬 **Curated Home Screen** — Hero feature, Trending, and Recently Added editorial rows
- 🔍 **Search** — Real-time movie/series search with 400ms debounce and Paging 3
- 🎞️ **Detail Screen** — Full plot, cast, runtime, IMDb rating, genre chips, and poster
- 📚 **Watchlist** — Add/remove movies, persisted locally via Room Database
- 🕒 **History** — View and manage your recently browsed titles
- 👤 **Profile** — User info, settings, and sign-out
- 📴 **Offline-first** — In-memory cache + Room DB minimise repeated API calls

---

## Tech Stack

| Layer | Technology | Version |
|---|---|---|
| Language | Kotlin | 1.9+ |
| UI | XML Layouts + ViewBinding | — |
| Architecture | MVVM + Clean Architecture | — |
| Navigation | Navigation Component (Single Activity) | 2.7+ |
| DI | Hilt | 2.51+ |
| Networking | Retrofit 2 + OkHttp 3 | 2.9 / 4.12 |
| JSON | Gson | 2.10 |
| Image Loading | Glide | 4.16 |
| Async | Kotlin Coroutines + Flow | 1.7+ |
| Local Storage | Room Database | 2.6+ |
| Preferences | DataStore (Preferences) | 1.1+ |
| Auth | Firebase Auth (Email + Google) | 22+ |
| Animations | MotionLayout + Lottie | — |
| Blur | BlurView (Dimezis) | 2.0.3 |
| Paging | Paging 3 | 3.2+ |
| Fonts | Epilogue · Manrope (bundled TTF) | — |

---

## Architecture

Neon Noir follows **Clean MVVM** with a strict one-way dependency rule — each layer only knows about the layer directly below it.

```text
Presentation  (Fragment + ViewModel)
      ↓
Domain       (UseCase + Model)
      ↓
Data         (Repository → Remote API + Local Room DB)
```

### Project Structure

```text
app/
├── data/
│   ├── remote/
│   │   ├── api/          OmdbApiService.kt
│   │   └── dto/          OmdbMovieDto.kt, OmdbSearchResponseDto.kt
│   ├── local/
│   │   ├── db/           AppDatabase.kt
│   │   ├── dao/          WatchlistDao.kt, HistoryDao.kt
│   │   └── entity/       WatchlistEntity.kt, HistoryEntity.kt
│   ├── repository/
│   │   ├── MovieRepository.kt         (interface)
│   │   └── MovieRepositoryImpl.kt     (implementation + in-memory cache)
│   └── mapper/
│       └── MovieMapper.kt             (DTO → Domain)
│
├── domain/
│   ├── model/            Movie.kt, SearchResult.kt
│   └── usecase/          GetMovieByIdUseCase, SearchMoviesUseCase,
│                         GetTrendingUseCase, AddToWatchlistUseCase,
│                         RemoveFromWatchlistUseCase, GetWatchlistUseCase,
│                         GetHistoryUseCase
│
├── presentation/
│   ├── auth/             splash · signin · signup · forgot_password
│   ├── home/             HomeFragment + HomeViewModel
│   ├── search/           SearchFragment + SearchViewModel
│   ├── detail/           DetailFragment + DetailViewModel
│   ├── library/          LibraryFragment + LibraryViewModel
│   ├── profile/          ProfileFragment + ProfileViewModel
│   └── common/
│       └── adapters/     MovieCardAdapter, GenreTileAdapter, CastAdapter
│
├── di/                   NetworkModule, DatabaseModule,
│                         RepositoryModule, UseCaseModule
│ 
├── constants/            Editorial.kt 
├── util/                 Resource.kt, Extensions.kt, Constants.kt
└── MainActivity.kt       (Single Activity host)
```

---

## Navigation

`MainActivity` hosts a single `NavHostFragment`. Firebase auth state is checked at launch and routes to either the auth graph or the main graph.

```text
nav_auth.xml
  splash ──► sign_in ──► sign_up
                    └──► forgot_password

nav_main.xml  (BottomNavigationView — 4 tabs)
  home    ──► detail
  search  ──► detail
  library ──► detail
  profile ──► settings
```

---

## Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17+
- A free [OMDB API key](https://www.omdbapi.com/apikey.aspx)
- A Firebase project with **Email/Password** and **Google Sign-In** enabled

### 1 — Clone the repository

```bash
git clone https://github.com/AhmadALSaffan/Neon-Nior.git
cd Neon-Nior
```

### 2 — Add secrets to `local.properties`

> ⚠️ **Never commit `local.properties`.** It is already listed in `.gitignore`.

```properties
# local.properties
OMDB_API_KEY=your_omdb_key_here
```

### 3 — Add Firebase config

1. Go to your [Firebase Console](https://console.firebase.google.com/) → Project settings → Download **`google-services.json`**
2. Place it in `app/google-services.json`
3. Enable **Email/Password** and **Google** sign-in methods under Authentication → Sign-in method

### 4 — Build & Run

```bash
./gradlew assembleDebug
```

Or press **▶ Run** in Android Studio.

---

## API — OMDB Integration

The app uses the [OMDB API](https://www.omdbapi.com/) for all movie data.

| Endpoint | Purpose |
|---|---|
| `?s={query}&page={n}` | Paginated title search |
| `?i={imdbId}&plot=full` | Full movie details by IMDb ID |
| `?t={title}&plot=short` | Movie details by exact title |

### Rate Limit Strategy (Free tier: 1,000 req/day)

- **In-memory cache** — `Map<String, Movie>` skips the network if a movie was already fetched this session
- **Room DB** — Watchlist items load from local DB first; no extra API call on revisit
- **Search debounce** — `Flow.debounce(400ms)` prevents firing on every keystroke
- **Paging 3** — Next page only fetched on scroll, not eagerly

---

## Curated Content (`Editorial.kt`)

The Home screen is powered by a hardcoded editorial list of IMDb IDs — no backend required.

| Row | Titles |
|---|---|
| Hero | Blade Runner 2049 |
| Trending | Blade Runner 2049, Interstellar, Avengers: Endgame, The Matrix, The Dark Knight, Guardians of the Galaxy |
| Recently Added | Inception, Pulp Fiction, The Prestige, Fellowship of the Ring, Return of the King |
| Genre Tiles | SCI-FI · HORROR · DRAMA · ACTION · THRILLER · NEO-NOIR |

---

## Design System

| Token | Value |
|---|---|
| Base background `surface` | `#0E0E11` |
| Card background `surface_container` | `#19191D` |
| Primary accent (pink) | `#FF8B9B` |
| Secondary accent (purple) | `#A98BFF` |
| Tertiary accent (cyan) | `#81ECFF` |
| Primary text | `#F0EDF1` |
| Secondary text | `#ACAAAE` |
| Card corner radius | `12dp` |
| Screen horizontal padding | `16dp` |
| Section gap | `32dp` |
| Button height | `52dp` |

All TextViews use `@style/TextAppearance.NeonNoir.*` — never inline `textSize` or `fontFamily`.

---

## Contributing

Pull requests are welcome! For major changes, please open an issue first to discuss what you'd like to change.

1. Fork the repo
2. Create your feature branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m "feat: add your feature"`
4. Push to the branch: `git push origin feature/your-feature`
5. Open a Pull Request

---

## License

```text
This is my project — any help, feedback, or contributions are more than welcome!
```

---

<div align="center">
  Built with ❤️ by <a href="https://github.com/AhmadALSaffan">Ahmad AlSaffan</a>
</div>
