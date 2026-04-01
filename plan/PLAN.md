# PLAN.md — Neon Noir Movie App (Android · Kotlin · XML · MVVM)

## Project Overview
A cinematic, dark-themed Android movie app ("Neon Noir") built natively with Kotlin, XML layouts, and clean MVVM architecture. Users browse movies via the OMDB API, manage a personal watchlist, track viewing history, and authenticate with email/Google.

---

## Tech Stack

| Layer | Library | Version |
|---|---|---|
| Language | Kotlin | 1.9+ |
| UI | XML Layouts + ViewBinding | — |
| Architecture | MVVM + Repository Pattern | — |
| Navigation | Navigation Component (Single Activity) | 2.7+ |
| DI | Hilt | 2.51+ |
| Networking | Retrofit 2 + OkHttp 3 | 2.9 / 4.12 |
| JSON | Gson (Retrofit converter) | 2.10 |
| Image Loading | Glide | 4.16 |
| Async | Kotlin Coroutines + Flow | 1.7+ |
| LiveData / StateFlow | AndroidX Lifecycle | 2.7+ |
| Local Storage | Room Database | 2.6+ |
| Preferences | DataStore (Preferences) | 1.1+ |
| Auth | Firebase Auth (Email + Google) | 22+ |
| Animations | MotionLayout + Lottie | — |
| Blur | BlurView (Dimezis) | 2.0.3 |
| Fonts | Custom XML fonts (Epilogue, Manrope) | — |
| Gradient | GradientDrawable in XML | — |
| Paging | Paging 3 | 3.2+ |

---

## Architecture: Clean MVVM

```
app/
├── data/
│   ├── remote/
│   │   ├── api/          OmdbApiService.kt (Retrofit interface)
│   │   └── dto/          OmdbMovieDto.kt, OmdbSearchDto.kt
│   ├── local/
│   │   ├── db/           AppDatabase.kt (Room)
│   │   ├── dao/          WatchlistDao.kt, HistoryDao.kt
│   │   └── entity/       WatchlistEntity.kt, HistoryEntity.kt
│   ├── repository/
│   │   ├── MovieRepository.kt        (interface)
│   │   └── MovieRepositoryImpl.kt    (implementation)
│   └── mapper/
│       └── MovieMapper.kt            (DTO → Domain model)
│
├── domain/
│   ├── model/
│   │   ├── Movie.kt
│   │   └── SearchResult.kt
│   └── usecase/
│       ├── GetMovieByIdUseCase.kt
│       ├── SearchMoviesUseCase.kt
│       ├── GetTrendingUseCase.kt
│       ├── AddToWatchlistUseCase.kt
│       ├── RemoveFromWatchlistUseCase.kt
│       ├── GetWatchlistUseCase.kt
│       └── GetHistoryUseCase.kt
│
├── presentation/
│   ├── auth/
│   │   ├── splash/       SplashFragment + SplashViewModel
│   │   ├── signin/       SignInFragment + SignInViewModel
│   │   ├── signup/       SignUpFragment + SignUpViewModel
│   │   └── forgot/       ForgotPasswordFragment + ForgotPasswordViewModel
│   ├── home/             HomeFragment + HomeViewModel
│   ├── search/           SearchFragment + SearchViewModel
│   ├── detail/           DetailFragment + DetailViewModel
│   ├── library/          LibraryFragment + LibraryViewModel
│   ├── profile/          ProfileFragment + ProfileViewModel
│   └── common/
│       ├── adapters/     MovieCardAdapter.kt, GenreTileAdapter.kt, CastAdapter.kt
│       └── views/        (custom views if needed)
│
├── di/
│   ├── NetworkModule.kt
│   ├── DatabaseModule.kt
│   ├── RepositoryModule.kt
│   └── UseCaseModule.kt
│
├── util/
│   ├── Extensions.kt
│   ├── Resource.kt       (sealed class: Loading / Success / Error)
│   └── Constants.kt
│
└── MainActivity.kt       (Single Activity host)
```

---

## Navigation Graph — Single Activity

`MainActivity` hosts a `NavHostFragment`. On launch it checks Firebase auth state and navigates to the appropriate graph.

### `nav_auth.xml`
```
splash_fragment
    ├──► sign_in_fragment
    │       ├──► sign_up_fragment
    │       └──► forgot_password_fragment
    └──► sign_up_fragment
```

### `nav_main.xml` (BottomNavigationView)
```
home_fragment ──► detail_fragment
search_fragment ──► detail_fragment
library_fragment ──► detail_fragment
profile_fragment ──► settings_fragment
```

---

## Res Folder Structure

```
res/
├── layout/
│   ├── activity_main.xml
│   ├── fragment_splash.xml
│   ├── fragment_sign_in.xml
│   ├── fragment_sign_up.xml
│   ├── fragment_forgot_password.xml
│   ├── fragment_home.xml
│   ├── fragment_search.xml
│   ├── fragment_detail.xml
│   ├── fragment_library.xml
│   ├── fragment_profile.xml
│   ├── item_movie_portrait.xml
│   ├── item_movie_landscape.xml
│   ├── item_movie_featured.xml
│   ├── item_genre_tile.xml
│   ├── item_cast_member.xml
│   ├── item_download.xml
│   └── item_settings_row.xml
│
├── drawable/
│   ├── bg_button_primary.xml        gradient pill CTA
│   ├── bg_button_glass.xml          semi-transparent surface
│   ├── bg_button_ghost.xml          transparent + outline variant
│   ├── bg_card.xml                  surfaceContainer rounded rect
│   ├── bg_input_default.xml         surfaceContainerHighest, no border
│   ├── bg_input_focused.xml         surfaceBright + tertiary ghost border
│   ├── bg_badge_premiere.xml
│   ├── bg_badge_new.xml
│   ├── bg_hero_overlay.xml          vertical gradient for hero
│   ├── bg_card_overlay.xml          gradient for genre tiles
│   ├── shape_avatar_ring.xml        gradient ring for profile avatar
│   ├── bg_bottom_nav.xml            glass bottom bar
│   └── ic_*.xml                     all vector icons
│
├── values/
│   ├── colors.xml
│   ├── dimens.xml
│   ├── strings.xml
│   ├── styles.xml                   TextAppearances + Widget styles
│   └── themes.xml                   App theme (dark, no action bar)
│
├── font/
│   ├── epilogue_bold.ttf
│   ├── epilogue_extrabold.ttf
│   ├── epilogue_semibold.ttf
│   ├── manrope_regular.ttf
│   ├── manrope_medium.ttf
│   └── manrope_semibold.ttf
│
└── navigation/
    ├── nav_auth.xml
    └── nav_main.xml
```

---

## Gradle Dependencies (app/build.gradle.kts)

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.gms.google-services")
    kotlin("kapt")
}

dependencies {
    // Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Navigation Component
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // ViewModel + LiveData + Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // Hilt DI
    implementation("com.google.dagger:hilt-android:2.51")
    kapt("com.google.dagger:hilt-android-compiler:2.51")

    // Retrofit + OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")

    // Firebase Auth + Google Sign-In
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    // Paging 3
    implementation("androidx.paging:paging-runtime-ktx:3.2.1")

    // Lottie
    implementation("com.airbnb.android:lottie:6.3.0")

    // BlurView
    implementation("com.github.Dimezis:BlurView:version-2.0.3")

    // Splash Screen API
    implementation("androidx.core:core-splashscreen:1.0.1")
}
```

---

## Implementation Order

1. `values/` — colors, dimens, styles, themes, fonts
2. `drawable/` — all shape/gradient drawables
3. Data layer — Retrofit service, DTOs, Room DB, DAOs, Entities
4. Domain layer — Models, UseCases, Repository interface
5. DI — all Hilt modules
6. `util/` — Resource sealed class, Extensions
7. Auth screens (Splash → SignIn → SignUp → ForgotPassword)
8. `MainActivity` + nav graphs + BottomNavigationView
9. Home Fragment + ViewModel + Adapters
10. Movie Detail Fragment + ViewModel
11. Search Fragment + ViewModel + Paging 3
12. Library Fragment + ViewModel
13. Profile Fragment + ViewModel
14. Polish — MotionLayout transitions, Lottie, ripple states
