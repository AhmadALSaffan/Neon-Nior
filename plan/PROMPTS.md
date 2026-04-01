# PROMPTS.md — AI Coding Prompts (Kotlin · XML · MVVM)

Use these prompts in order with an AI coding assistant (Cursor, Copilot, Claude Code, etc). Feed the referenced spec files as context before each prompt.

---

## Step 0 — Project Bootstrap

**Context files:** `PLAN.md`

```
Create a new Android project named "NeonNoir" with package name "com.neonnoir.app" 
using Kotlin and the following setup:

1. minSdk 26, targetSdk 34, compileSdk 34
2. Enable ViewBinding in build.gradle.kts: buildFeatures { viewBinding = true }
3. Enable BuildConfig: buildFeatures { buildConfig = true }
4. Add all dependencies from the Gradle Dependencies section in PLAN.md
5. Apply Hilt plugin and Navigation SafeArgs plugin in both root and app build.gradle.kts
6. Add google-services.json placeholder note (Firebase setup)
7. Create the full package folder structure from PLAN.md's Architecture section
8. Create the res/ folder structure from PLAN.md's Res Folder section (empty files are fine)
```

---

## Step 1 — Design Tokens (Values & Drawables)

**Context files:** `TOKENS.md`

```
Create all Android resource files exactly as specified in TOKENS.md:

1. res/values/colors.xml — full color palette with all surface, brand, text, badge, and overlay colors
2. res/values/dimens.xml — full spacing scale, border radii, component sizes
3. res/values/styles.xml — all TextAppearance styles (DisplayLarge → LabelSmall) using Epilogue and Manrope fonts
4. res/values/themes.xml — dark NoActionBar theme, splash theme

Then create these drawable XML files (all specified in TOKENS.md):
- bg_button_primary.xml (gradient pill)
- bg_button_ghost.xml (transparent + ghost border)
- bg_button_glass.xml (surfaceContainer semi-transparent)
- bg_card.xml (surfaceContainer rounded rect)
- bg_input_default.xml (surfaceContainerHighest, no border)
- bg_input_focused.xml (surfaceBright + tertiary ghost border)
- bg_hero_overlay.xml (transparent → surface gradient, angle 270)
- bg_card_overlay.xml (for genre tiles)
- bg_badge_premiere.xml (primary 20% bg + radius_full)
- bg_badge_new.xml (tertiary 20% bg)
- bg_gradient_brand.xml (horizontal primary → secondary, for Sign In card top bar)
- shape_circle.xml (for avatar backgrounds)
- shape_avatar_ring.xml (gradient ring for Profile avatar)

Place Epilogue and Manrope font files in res/font/
```

---

## Step 2 — Data Layer

**Context files:** `API.md`, `PLAN.md`

```
Build the complete data layer as specified in API.md:

1. data/remote/dto/OmdbSearchResponseDto.kt — with OmdbSearchItemDto inner class
2. data/remote/dto/OmdbMovieDto.kt — all fields with @SerializedName annotations
3. data/remote/api/OmdbApiService.kt — Retrofit interface with searchMovies, getMovieById, getMovieByTitle
4. domain/model/Movie.kt — clean domain model
5. domain/model/SearchResult.kt
6. data/mapper/MovieMapper.kt — extension functions toDomain() on both DTOs, with formatRuntime() and upgradePosterResolution() helpers
7. data/local/entity/WatchlistEntity.kt + HistoryEntity.kt — Room entities as in COMPONENTS.md
8. data/local/dao/WatchlistDao.kt + HistoryDao.kt — Room DAOs with Flow return types
9. data/local/db/AppDatabase.kt — Room database class
10. data/repository/MovieRepository.kt — interface
11. data/repository/MovieRepositoryImpl.kt — implementation with in-memory cache Map
12. util/Resource.kt — sealed class with Loading, Success<T>, Error

Read the OMDB API key from BuildConfig.OMDB_API_KEY (set in build.gradle.kts from local.properties).
```

---

## Step 3 — Hilt DI Modules

**Context files:** `API.md`, `PLAN.md`

```
Create all Hilt dependency injection modules in the di/ package:

1. di/NetworkModule.kt
   - @Singleton OkHttpClient with HttpLoggingInterceptor (BODY in debug, NONE in release)
   - @Singleton Retrofit with baseUrl "https://www.omdbapi.com/"
   - @Singleton OmdbApiService

2. di/DatabaseModule.kt
   - @Singleton AppDatabase using Room.databaseBuilder, name "neon_noir.db"
   - @Singleton WatchlistDao (from AppDatabase)
   - @Singleton HistoryDao (from AppDatabase)

3. di/RepositoryModule.kt
   - @Binds MovieRepository → MovieRepositoryImpl
   - @Binds AuthRepository → AuthRepositoryImpl (Firebase)

4. di/UseCaseModule.kt
   - @Provides for each UseCase in domain/usecase/ (or use @Inject constructors)

5. Annotate Application class with @HiltAndroidApp
6. Annotate MainActivity with @AndroidEntryPoint
7. Annotate all Fragments with @AndroidEntryPoint
```

---

## Step 4 — Auth Screens

**Context files:** `SCREENS.md` (Fragments 1–4), `TOKENS.md`

```
Build all 4 authentication fragments and their XML layouts as specified in SCREENS.md.

For each fragment, create:
- The XML layout file in res/layout/
- The Fragment class using ViewBinding
- The ViewModel with LiveData/StateFlow for UI state

Fragment 1: SplashFragment / fragment_splash.xml
- Blurred ImageView background
- App icon CardView with gradient logo
- "N E O N  N O I R" LabelCaps tracking text
- DisplayLarge headline + BodyLarge subtitle
- GET STARTED button (white fill, routes to SignUp)
- SIGN IN glass button (routes to SignIn)
- Footer row: edition label + success-color dot + status text
- SplashViewModel checks Firebase auth → navigates to nav_main if logged in

Fragment 2: SignInFragment / fragment_sign_in.xml
- Header with logo + "new here? SIGN UP" link
- Card (surfaceContainerLow) with 3dp gradient top bar (primary → secondary)
- Email EditText + Password EditText with eye toggle
- Forgot Password link
- Sign In primary button
- OR CONTINUE WITH divider + Google + Apple social buttons
- SignInViewModel with SignInUiState (Idle/Loading/Success/Error)
- Show ProgressBar on loading, snackbar on error

Fragment 3: SignUpFragment / fragment_sign_up.xml
- Italic pink NEON NOIR logo + tagline
- Full Name, Email, Password, Confirm Password fields
- Terms checkbox with secondary-colored links
- SIGN UP primary button + Sign In ghost button
- Decorative icon row at bottom (secondaryFixedDim tint)

Fragment 4: ForgotPasswordFragment / fragment_forgot_password.xml
- Transparent header with BACK TO SITE
- Blurred background image (alpha 0.4)
- Refresh-lock icon in primary color
- LOST ACCESS? in DisplayMedium bold
- Email input + RESET PASSWORD primary button
- Sign In link in secondary color
- Rotating movie quote at bottom (labelCaps, alpha 0.4)
  - Rotate from a list of 5 quotes, pick random on fragment start

Wire navigation in nav_auth.xml.
```

---

## Step 5 — MainActivity + Navigation Setup

**Context files:** `PLAN.md`, `COMPONENTS.md` (Bottom Navigation)

```
Set up MainActivity and both navigation graphs:

1. activity_main.xml:
   - CoordinatorLayout root
   - FragmentContainerView for NavHostFragment (nav_main)
   - BottomNavigationView at bottom with @color/glass_nav_bg background
   - itemIconTint and itemTextColor using nav_item_color selector (primary when checked, secondaryFixedDim otherwise)
   - NO top border on BottomNavigationView — use background color contrast only

2. res/menu/menu_bottom_nav.xml — 4 items: HOME, SEARCH, MY MOVIES, PROFILE

3. res/color/nav_item_color.xml — state selector

4. nav_auth.xml — splash → sign_in → sign_up / forgot_password
   Use Safe Args for passing any needed arguments.

5. nav_main.xml — bottom nav destinations + detail_fragment as a non-tab destination
   home → detail, search → detail, library → detail

6. MainActivity.kt:
   - @AndroidEntryPoint, check Firebase auth in onCreate
   - If not logged in: setContentView auth layout (no bottom nav)
   - If logged in: setContentView main layout, set up BottomNavigationView with NavController
   - Hide BottomNavigationView when on detail_fragment (addOnDestinationChangedListener)
   - Handle edge-to-edge insets
```

---

## Step 6 — Home Fragment

**Context files:** `SCREENS.md` (Fragment 5), `COMPONENTS.md`

```
Build HomeFragment, HomeViewModel, and fragment_home.xml.

Layout (fragment_home.xml):
- CoordinatorLayout root
- AppBarLayout with transparent MaterialToolbar (hamburger, "Lumina Noir" in primary, search icon, avatar)
- NestedScrollView with vertical LinearLayout inside
- Include view_hero_section.xml for the hero
- Include view_section_header.xml (id: header_trending) + horizontal RecyclerView (id: rv_trending)
- Include view_section_header.xml (id: header_genres) + RecyclerView with GridLayoutManager(2) (id: rv_genres)
- Include view_section_header.xml (id: header_recent) + horizontal RecyclerView (id: rv_recent)
- Use @dimen/section_gap top padding between each section — NO divider lines

HomeViewModel:
- heroMovie: LiveData<Resource<Movie>> — fetch Editorial.FEATURED_HERO_ID
- trendingMovies: LiveData<Resource<List<Movie>>> — fetch Editorial.TRENDING_IDS
- recentMovies: LiveData<Resource<List<Movie>>> — fetch Editorial.RECENTLY_ADDED_IDS

HomeFragment:
- Observe heroMovie → populate view_hero_section with Glide for backdrop + title + plot + tags
- Observe trendingMovies → set MovieCardAdapter on rv_trending
- Set GenreTileAdapter on rv_genres from Editorial.GENRE_SEARCHES
- Observe recentMovies → set MovieCardAdapter on rv_recent
- Show SkeletonLoader (or shimmer) while Loading state
- Card click → navigate to detail_fragment with imdbId argument via Safe Args
```

---

## Step 7 — Movie Detail Fragment

**Context files:** `SCREENS.md` (Fragment 6), `COMPONENTS.md`

```
Build DetailFragment, DetailViewModel, and fragment_detail.xml.

Layout uses CollapsingToolbarLayout:
- CoordinatorLayout > AppBarLayout (height 300dp) > CollapsingToolbarLayout
- Backdrop ImageView + bg_hero_overlay gradient View inside collapsing toolbar
- Tags LinearLayout + title TextView (DisplayLarge) + rating row at bottom of backdrop
- Transparent MaterialToolbar pinned (back button + share icon)
- NestedScrollView body with padding @dimen/screen_padding_horizontal:
  - PLAY NOW primary button (full width)
  - + WATCHLIST ghost button (full width, toggles state)
  - Section label "THE GLITCH" (LabelCaps in secondary)
  - Plot TextView
  - Section label "MAIN CAST" (LabelCaps in secondary)
  - RecyclerView rv_cast (using CastAdapter)
  - DIRECTOR / STUDIO label+value pairs
  - ChipGroup for genre tags (Material Chip, surface background, onSurfaceVariant text)
  - RELATED MOVIES header + RecyclerView rv_related (GridLayoutManager 2)

DetailViewModel:
- load(imdbId) → fetches movie, checks watchlist status, fetches related
- toggleWatchlist() → adds/removes, updates isInWatchlist LiveData
- Observe isInWatchlist → change watchlist button icon/text (+ WATCHLIST / ✓ IN WATCHLIST)

Load backdrop and poster using Glide with a crossFade transition.
```

---

## Step 8 — Search Fragment

**Context files:** `SCREENS.md` (Fragment 7), `API.md`

```
Build SearchFragment, SearchViewModel, and fragment_search.xml.

Layout:
- LinearLayout root with toolbar, search bar row (EditText + filter ImageButton)
- Default state LinearLayout (ll_default_state): recent chips + genre grid + popular list
- Results RecyclerView (rv_results, initially GONE)

SearchViewModel:
- Uses StateFlow + debounce(400ms) + distinctUntilChanged on query input
- setQuery(q) updates the flow
- results: LiveData<Resource<List<SearchResult>>>
- popularMovies: LiveData<Resource<List<SearchResult>>> (fetches "2024" on init)

SearchFragment:
- EditText.addTextChangedListener → viewModel.setQuery(text)
- If query is blank → show ll_default_state, hide rv_results
- If query is not blank → hide ll_default_state, show rv_results
- Observe results → update MovieCardAdapter on rv_results
- Recent searches: persist to DataStore, show as chip TextViews in ll_recent_chips
  - Each chip has "⟳" prefix and click → fills search bar with that term
  - Clear All → clears DataStore list + removes all chips
- Genre tiles RecyclerView with GenreTileAdapter from Editorial.GENRE_SEARCHES
  - Genre tile click → sets query to that genre's search term
- Filter bottom sheet: BottomSheetDialogFragment with type/year/genre filters
```

---

## Step 9 — Library Fragment

**Context files:** `SCREENS.md` (Fragment 8), `COMPONENTS.md`

```
Build LibraryFragment, LibraryViewModel, and fragment_library.xml.

Layout:
- Tab filter pills (All / Watchlist / Downloads) using RadioGroup styled as pills
  - Active: bg_button_primary, Inactive: bg_button_ghost
- Continue Watching: horizontal RecyclerView using item_movie_landscape.xml with ProgressBar
- Active Downloads: RecyclerView using item_download.xml (DownloadAdapter)
- Your Watchlist: RecyclerView with GridLayoutManager(2) using item_movie_portrait.xml
  - Grid/List toggle button changes spanCount between 2 and 1
- Empty states per section: shown when list is empty
- DISCOVER MORE ghost button at bottom → navigates to search

LibraryViewModel:
- watchlist: LiveData → watchlistDao.getAll() as Flow, converted to LiveData
- history: LiveData → historyDao.getRecentlyWatched() — for Continue Watching
- Filter tabs show/hide sections:
  - ALL: all three sections visible
  - WATCHLIST: only watchlist section visible  
  - DOWNLOADS: only downloads section visible

Tab filter: use RadioGroup.setOnCheckedChangeListener → toggle section visibility
```

---

## Step 10 — Profile Fragment

**Context files:** `SCREENS.md` (Fragment 9), `COMPONENTS.md`

```
Build ProfileFragment, ProfileViewModel, and fragment_profile.xml.

Layout:
- Toolbar with back button + "Profile" title + gear icon (→ placeholder settings)
- Profile card (surfaceContainerLow, radius_xl):
  - Avatar FrameLayout: gradient ring ImageView + clipped circular avatar ImageView + verified badge
  - Display name (DisplayMedium), Premium badge pill, bio quote (BodyMedium italic)
- Stats: 2-column LinearLayout with CardViews
  - Movies Watched (primary color number), In Watchlist (secondary color number)
  - Reviews card (tertiary), Favorite Genres ChipGroup
- Continue Watching horizontal RecyclerView (reuse from Library)
- Settings RecyclerView using SettingsRowAdapter:
  - Account Settings, Preferences, Help & Support
  - Log Out row: icon + text in @color/primary, no chevron

ProfileViewModel:
- User display name + avatar from Firebase Auth currentUser
- watchedCount: historyDao.getCount()
- watchlistCount: watchlistDao.getCount()
- favoriteGenres: derive from top 3 genres in history items
- Log out: FirebaseAuth.signOut() → navigate to nav_auth

Favorite genre bubbles: create Material Chips with distinct colors:
  - Action: primary, Noir: secondary, Thriller: tertiary
```

---

## Step 11 — Polish Pass

**Context files:** `DESIGN.md`

```
Final polish pass across all screens:

1. Edge-to-edge: apply WindowInsetsCompat to all fragments so content draws behind status bar and nav bar

2. Shared element transitions between MovieCard and DetailFragment:
   - Set transitionName on iv_poster in adapter
   - Use FragmentNavigator.Extras to pass shared element
   - Apply MaterialContainerTransform in DetailFragment

3. Ripple effects: add ?attr/selectableItemBackground or custom ripple drawables to all clickable cards

4. Loading shimmer: implement ShimmerLayout or Lottie skeleton for all async-loaded lists

5. Empty states: every RecyclerView section in Library and Search has a styled empty state view

6. Status bar: force light icons off (dark content) — already handled in theme windowLightStatusBar=false

7. Verify DESIGN.md compliance across all layouts:
   - No View with android:background using a 1px solid border (only ghost borders via shapes)
   - No android:textColor="#FFFFFF" — always @color/on_surface or @color/on_surface_variant
   - All primary CTA buttons use @drawable/bg_button_primary (gradient pill)
   - Section spacing via android:layout_marginTop="@dimen/section_gap", never a <View height="1dp">
   - No android:elevation or cardElevation on regular cards — only floating modals use elevation

8. Lottie animations:
   - Add a subtle play-button Lottie on the Hero section Watch Now button
   - Add a checkmark Lottie when user adds to watchlist (brief 0.5s animation)
```
