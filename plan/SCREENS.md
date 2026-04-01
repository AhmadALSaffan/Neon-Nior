# SCREENS.md — Fragment + XML Layout Specifications

Each section covers: XML layout structure, ViewModel state, key bindings, and navigation actions.

---

## Fragment 1 — Splash / Landing
**File:** `fragment_splash.xml` + `SplashFragment.kt`

### XML Layout Structure
```xml
<!-- Root: ConstraintLayout, fitsSystemWindows=true, bg=@color/surface -->

<!-- Blurred cinematic background image -->
<ImageView android:id="@+id/iv_bg" />   <!-- scaleType: centerCrop, full screen -->
<View android:id="@+id/bg_dim" />        <!-- semi-transparent surface overlay -->

<!-- App Icon card (surfaceContainerLow, rounded 24dp) -->
<androidx.cardview.widget.CardView
    android:id="@+id/card_icon"
    app:cardCornerRadius="24dp"
    app:cardBackgroundColor="@color/surface_container_low" />
    <!-- Inside: ImageView with app logo -->

<!-- Brand name -->
<TextView
    android:id="@+id/tv_brand"
    style="@style/TextAppearance.NeonNoir.LabelCaps"
    android:text="N E O N   N O I R"
    android:letterSpacing="0.3" />

<!-- Hero headline -->
<TextView
    android:id="@+id/tv_headline"
    style="@style/TextAppearance.NeonNoir.DisplayLarge"
    android:text="Cinematic stories,\nredefined." />

<!-- Subtitle -->
<TextView
    android:id="@+id/tv_subtitle"
    style="@style/TextAppearance.NeonNoir.BodyLarge"
    android:gravity="center"
    android:text="Experience world-class storytelling..." />

<!-- GET STARTED button (white fill on splash only) -->
<Button
    android:id="@+id/btn_get_started"
    android:background="@drawable/bg_button_white"
    android:textColor="@color/surface"
    android:text="GET STARTED →"
    android:height="@dimen/button_height" />

<!-- SIGN IN button (glass style) -->
<Button
    android:id="@+id/btn_sign_in"
    android:background="@drawable/bg_button_glass"
    android:text="SIGN IN" />

<!-- Footer row -->
<TextView android:id="@+id/tv_edition"
    style="@style/TextAppearance.NeonNoir.LabelCaps"
    android:text="EDITION 24.1" />
<View android:id="@+id/dot_status"
    android:background="@color/success" />  <!-- 6dp circle -->
<TextView android:id="@+id/tv_status"
    style="@style/TextAppearance.NeonNoir.LabelCaps"
    android:text="CORE SYSTEMS ACTIVE" />
```

### SplashViewModel
```kotlin
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    val isLoggedIn: LiveData<Boolean> = liveData {
        emit(authRepository.isLoggedIn())
    }
}
```

### Navigation
- GET STARTED → `sign_up_fragment`
- SIGN IN → `sign_in_fragment`
- If `isLoggedIn == true` → directly to `nav_main`

---

## Fragment 2 — Sign In
**File:** `fragment_sign_in.xml` + `SignInViewModel.kt`

### XML Layout Structure
```xml
<!-- Root: ScrollView > ConstraintLayout, bg=@color/surface -->

<!-- Header bar -->
<ImageView android:id="@+id/iv_logo" />  <!-- Neon Noir gradient logo -->
<TextView android:id="@+id/tv_new_here"
    android:text="new here?" />
<TextView android:id="@+id/tv_sign_up_link"
    android:textColor="@color/primary"
    android:text="SIGN UP" />

<!-- Card (surfaceContainerLow, radius_xl) -->
<androidx.cardview.widget.CardView
    app:cardBackgroundColor="@color/surface_container_low"
    app:cardCornerRadius="@dimen/radius_xl"
    app:cardElevation="0dp">

    <!-- Gradient accent top bar (3dp height) -->
    <View android:id="@+id/v_gradient_bar"
        android:layout_height="3dp"
        android:background="@drawable/bg_gradient_brand" />
    <!-- bg_gradient_brand: horizontal primary → secondary -->

    <TextView android:text="Welcome Back"
        style="@style/TextAppearance.NeonNoir.HeadlineLarge" />

    <TextView android:text="Sign in to access your curated library."
        style="@style/TextAppearance.NeonNoir.BodyMedium" />

    <!-- Email input -->
    <TextView android:text="EMAIL OR USERNAME"
        style="@style/TextAppearance.NeonNoir.LabelCaps" />
    <EditText android:id="@+id/et_email"
        android:background="@drawable/bg_input_default"
        android:hint="name@example.com"
        android:inputType="textEmailAddress"
        android:drawableStart="@drawable/ic_email" />

    <!-- Password input -->
    <LinearLayout android:orientation="horizontal">
        <TextView android:text="PASSWORD"
            style="@style/TextAppearance.NeonNoir.LabelCaps" />
        <TextView android:id="@+id/tv_forgot"
            android:text="Forgot Password?"
            android:textColor="@color/secondary" />
    </LinearLayout>
    <EditText android:id="@+id/et_password"
        android:background="@drawable/bg_input_default"
        android:inputType="textPassword"
        android:drawableStart="@drawable/ic_lock"
        android:drawableEnd="@drawable/ic_eye" />

    <!-- Sign In button -->
    <Button android:id="@+id/btn_sign_in"
        android:background="@drawable/bg_button_primary"
        android:text="Sign In →"
        style="@style/TextAppearance.NeonNoir.Button" />

    <!-- Divider with label -->
    <LinearLayout android:orientation="horizontal">
        <View android:layout_weight="1" android:background="@color/surface_container_high" />
        <TextView android:text="OR CONTINUE WITH"
            style="@style/TextAppearance.NeonNoir.LabelCaps" />
        <View android:layout_weight="1" android:background="@color/surface_container_high" />
    </LinearLayout>

    <!-- Social buttons -->
    <LinearLayout android:orientation="horizontal">
        <Button android:id="@+id/btn_google"
            android:background="@drawable/bg_card"
            android:text="Google"
            android:drawableStart="@drawable/ic_google" />
        <Button android:id="@+id/btn_apple"
            android:background="@drawable/bg_card"
            android:text="Apple"
            android:drawableStart="@drawable/ic_apple" />
    </LinearLayout>
</androidx.cardview.widget.CardView>

<!-- Footer -->
<TextView android:text="By continuing, you agree to our Terms of Service and Privacy Policy."
    style="@style/TextAppearance.NeonNoir.BodySmall"
    android:gravity="center" />
```

### SignInViewModel
```kotlin
class SignInViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SignInUiState>(SignInUiState.Idle)
    val uiState: StateFlow<SignInUiState> = _uiState

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = SignInUiState.Loading
            authRepository.signInWithEmail(email, password)
                .onSuccess { _uiState.value = SignInUiState.Success }
                .onFailure { _uiState.value = SignInUiState.Error(it.message ?: "Error") }
        }
    }
}

sealed class SignInUiState {
    object Idle : SignInUiState()
    object Loading : SignInUiState()
    object Success : SignInUiState()
    data class Error(val message: String) : SignInUiState()
}
```

---

## Fragment 3 — Sign Up
**File:** `fragment_sign_up.xml` + `SignUpViewModel.kt`

### XML Layout — Key Elements
```xml
<!-- Logo + italic tagline at top -->
<TextView android:text="NEON NOIR"
    android:textStyle="italic"
    android:textColor="@color/primary" />
<TextView android:text="THE DIGITAL PREMIERE AWAITS"
    style="@style/TextAppearance.NeonNoir.LabelCaps" />

<!-- Form card (surfaceContainerLow) -->
<TextView android:text="Create Account"
    style="@style/TextAppearance.NeonNoir.HeadlineLarge" />
<TextView android:text="Join the elite circle of cinematic curators."
    style="@style/TextAppearance.NeonNoir.BodyMedium" />

<!-- FULL NAME -->
<EditText android:id="@+id/et_full_name"
    android:drawableStart="@drawable/ic_person"
    android:hint="Enter your full name" />

<!-- EMAIL ADDRESS -->
<EditText android:id="@+id/et_email"
    android:drawableStart="@drawable/ic_email"
    android:inputType="textEmailAddress" />

<!-- PASSWORD -->
<EditText android:id="@+id/et_password"
    android:drawableStart="@drawable/ic_lock"
    android:inputType="textPassword" />

<!-- CONFIRM PASSWORD (shield icon) -->
<EditText android:id="@+id/et_confirm"
    android:drawableStart="@drawable/ic_shield"
    android:inputType="textPassword" />

<!-- Terms checkbox -->
<CheckBox android:id="@+id/cb_terms" />
<TextView>
    I agree to the
    <TextView android:textColor="@color/secondary" android:text="Terms of Service" />
    and
    <TextView android:textColor="@color/secondary" android:text="Privacy Policy" />
</TextView>

<!-- Sign Up button (primary gradient) -->
<Button android:id="@+id/btn_sign_up"
    android:background="@drawable/bg_button_primary"
    android:text="SIGN UP →" />

<!-- Already have account? -->
<Button android:id="@+id/btn_sign_in"
    android:background="@drawable/bg_button_ghost"
    android:text="→ Sign In" />

<!-- Decorative footer icons (all in secondary_fixed_dim) -->
<LinearLayout android:orientation="horizontal">
    <ImageView src="@drawable/ic_clapperboard" />
    <ImageView src="@drawable/ic_screen" />
    <ImageView src="@drawable/ic_filmstrip" />
    <ImageView src="@drawable/ic_hq" />
</LinearLayout>
```

---

## Fragment 4 — Forgot Password
**File:** `fragment_forgot_password.xml`

### Key Elements
```xml
<!-- Transparent header -->
<TextView android:text="NEON NOIR" android:textColor="@color/primary" />
<Button android:id="@+id/btn_back" android:text="← BACK TO SITE" />

<!-- Blurred background image -->
<ImageView android:id="@+id/iv_bg" android:alpha="0.4" />

<!-- Centered card (surfaceContainer) -->
<ImageView android:id="@+id/iv_icon"
    android:src="@drawable/ic_lock_refresh"
    android:tint="@color/primary" />

<TextView android:text="LOST ACCESS?"
    style="@style/TextAppearance.NeonNoir.DisplayMedium"
    android:textStyle="bold" />

<TextView android:text="Enter your cinematic credentials below..."
    style="@style/TextAppearance.NeonNoir.BodyMedium"
    android:gravity="center" />

<EditText android:id="@+id/et_email"
    android:drawableStart="@drawable/ic_email"
    android:hint="name@neonnoir.com" />

<Button android:id="@+id/btn_reset"
    android:background="@drawable/bg_button_primary"
    android:text="RESET PASSWORD →" />

<!-- Sign in link -->
<TextView android:text="Suddenly remembered?" />
<TextView android:id="@+id/tv_sign_in_link"
    android:text="Sign In →"
    android:textColor="@color/secondary" />

<!-- Rotating movie quote -->
<TextView android:id="@+id/tv_quote"
    style="@style/TextAppearance.NeonNoir.LabelCaps"
    android:alpha="0.4"
    android:gravity="center" />
```

---

## Fragment 5 — Home
**File:** `fragment_home.xml` + `HomeViewModel.kt`

### XML Layout — NestedScrollView
```xml
<!-- Root: CoordinatorLayout -->

<!-- AppBarLayout (transparent, scrollable) -->
<AppBarLayout android:background="@android:color/transparent">
    <MaterialToolbar android:id="@+id/toolbar">
        <ImageButton android:id="@+id/btn_menu" src="@drawable/ic_menu" />
        <TextView android:text="Lumina Noir" android:textColor="@color/primary" />
        <ImageButton android:id="@+id/btn_search" src="@drawable/ic_search" />
        <ImageView android:id="@+id/iv_avatar"
            android:background="@drawable/shape_circle" />
    </MaterialToolbar>
</AppBarLayout>

<!-- NestedScrollView -->
<NestedScrollView>
    <LinearLayout android:orientation="vertical">

        <!-- Hero Section (custom view or included layout) -->
        <include layout="@layout/view_hero_section"
            android:id="@+id/hero_section" />

        <!-- Trending Now row -->
        <include layout="@layout/view_section_header"
            android:id="@+id/header_trending" />
        <RecyclerView android:id="@+id/rv_trending"
            android:orientation="horizontal"
            app:layoutManager="LinearLayoutManager" />

        <!-- Top Genres grid -->
        <include layout="@layout/view_section_header"
            android:id="@+id/header_genres" />
        <GridLayout android:id="@+id/grid_genres"
            android:columnCount="2"
            android:rowCount="2" />
        <!-- Or: RecyclerView with GridLayoutManager(2) -->

        <!-- Recently Added row -->
        <include layout="@layout/view_section_header"
            android:id="@+id/header_recent" />
        <RecyclerView android:id="@+id/rv_recent"
            android:orientation="horizontal" />

    </LinearLayout>
</NestedScrollView>
```

### HomeViewModel
```kotlin
class HomeViewModel @Inject constructor(
    private val getTrendingUseCase: GetTrendingUseCase,
    private val getMovieByIdUseCase: GetMovieByIdUseCase
) : ViewModel() {

    private val _heroMovie = MutableLiveData<Resource<Movie>>()
    val heroMovie: LiveData<Resource<Movie>> = _heroMovie

    private val _trendingMovies = MutableLiveData<Resource<List<Movie>>>()
    val trendingMovies: LiveData<Resource<List<Movie>>> = _trendingMovies

    private val _recentMovies = MutableLiveData<Resource<List<Movie>>>()
    val recentMovies: LiveData<Resource<List<Movie>>> = _recentMovies

    init { loadAll() }

    private fun loadAll() {
        viewModelScope.launch {
            _heroMovie.value = Resource.Loading
            _heroMovie.value = getMovieByIdUseCase(Editorial.FEATURED_HERO_ID)

            _trendingMovies.value = Resource.Loading
            val trending = getTrendingUseCase(Editorial.TRENDING_IDS)
            _trendingMovies.value = Resource.Success(trending)

            val recent = getTrendingUseCase(Editorial.RECENTLY_ADDED_IDS)
            _recentMovies.value = Resource.Success(recent)
        }
    }
}
```

---

## Fragment 6 — Movie Detail
**File:** `fragment_detail.xml` + `DetailViewModel.kt`

### XML Layout (CollapsingToolbarLayout)
```xml
<CoordinatorLayout>

    <AppBarLayout android:layout_height="300dp">
        <CollapsingToolbarLayout
            app:contentScrim="@color/surface"
            app:layout_scrollFlags="scroll|exitUntilCollapsed">

            <!-- Backdrop image -->
            <ImageView android:id="@+id/iv_backdrop"
                android:scaleType="centerCrop" />

            <!-- Gradient overlay on backdrop -->
            <View android:background="@drawable/bg_hero_overlay" />

            <!-- Tags + Title overlaid on image -->
            <LinearLayout android:id="@+id/ll_tags">
                <!-- genre badge, runtime, year, rated -->
            </LinearLayout>
            <TextView android:id="@+id/tv_title"
                style="@style/TextAppearance.NeonNoir.DisplayLarge" />
            <TextView android:id="@+id/tv_rating"
                android:drawableStart="@drawable/ic_star"
                android:drawableTint="@color/primary" />

            <MaterialToolbar app:layout_collapseMode="pin">
                <ImageButton android:id="@+id/btn_back" />
                <ImageButton android:id="@+id/btn_share" />
            </MaterialToolbar>
        </CollapsingToolbarLayout>
    </AppBarLayout>

    <NestedScrollView>
        <LinearLayout android:orientation="vertical"
            android:padding="@dimen/screen_padding_horizontal">

            <!-- Action buttons -->
            <Button android:id="@+id/btn_play"
                android:background="@drawable/bg_button_primary"
                android:text="▶ PLAY NOW" />
            <Button android:id="@+id/btn_watchlist"
                android:background="@drawable/bg_button_ghost"
                android:text="+ WATCHLIST" />

            <!-- Plot section -->
            <TextView android:text="THE GLITCH"
                style="@style/TextAppearance.NeonNoir.LabelCaps"
                android:textColor="@color/secondary" />
            <TextView android:id="@+id/tv_plot"
                style="@style/TextAppearance.NeonNoir.BodyMedium" />

            <!-- Cast section -->
            <TextView android:text="MAIN CAST"
                style="@style/TextAppearance.NeonNoir.LabelCaps"
                android:textColor="@color/secondary" />
            <RecyclerView android:id="@+id/rv_cast" />

            <!-- Metadata -->
            <TextView android:id="@+id/tv_director_label"
                style="@style/TextAppearance.NeonNoir.LabelCaps" android:text="DIRECTOR" />
            <TextView android:id="@+id/tv_director"
                style="@style/TextAppearance.NeonNoir.HeadlineSmall" />
            <TextView android:id="@+id/tv_studio_label"
                style="@style/TextAppearance.NeonNoir.LabelCaps" android:text="STUDIO" />
            <TextView android:id="@+id/tv_studio"
                style="@style/TextAppearance.NeonNoir.HeadlineSmall" />

            <!-- Genre keyword tags -->
            <com.google.android.material.chip.ChipGroup android:id="@+id/chip_group_genres" />

            <!-- Related movies -->
            <TextView android:text="RELATED MOVIES"
                style="@style/TextAppearance.NeonNoir.HeadlineSmall" />
            <RecyclerView android:id="@+id/rv_related"
                app:layoutManager="GridLayoutManager"
                app:spanCount="2" />

        </LinearLayout>
    </NestedScrollView>
</CoordinatorLayout>
```

### DetailViewModel
```kotlin
class DetailViewModel @Inject constructor(
    private val getMovieByIdUseCase: GetMovieByIdUseCase,
    private val addToWatchlistUseCase: AddToWatchlistUseCase,
    private val removeFromWatchlistUseCase: RemoveFromWatchlistUseCase,
    private val repository: MovieRepository
) : ViewModel() {

    private val _movie = MutableLiveData<Resource<Movie>>()
    val movie: LiveData<Resource<Movie>> = _movie

    private val _isInWatchlist = MutableLiveData<Boolean>()
    val isInWatchlist: LiveData<Boolean> = _isInWatchlist

    private val _relatedMovies = MutableLiveData<List<SearchResult>>()
    val relatedMovies: LiveData<List<SearchResult>> = _relatedMovies

    fun load(imdbId: String) {
        viewModelScope.launch {
            _movie.value = Resource.Loading
            val result = getMovieByIdUseCase(imdbId)
            _movie.value = result
            _isInWatchlist.value = repository.isInWatchlist(imdbId)

            if (result is Resource.Success) {
                val genre = result.data.genres.firstOrNull() ?: ""
                val related = repository.searchMovies(genre)
                if (related is Resource.Success) {
                    _relatedMovies.value = related.data.filter { it.imdbId != imdbId }
                }
            }
        }
    }

    fun toggleWatchlist(movie: Movie) {
        viewModelScope.launch {
            val inList = _isInWatchlist.value ?: false
            if (inList) removeFromWatchlistUseCase(movie.imdbId)
            else addToWatchlistUseCase(movie)
            _isInWatchlist.value = !inList
        }
    }
}
```

---

## Fragment 7 — Search
**File:** `fragment_search.xml` + `SearchViewModel.kt`

### XML Layout
```xml
<LinearLayout android:orientation="vertical">

    <!-- Header -->
    <MaterialToolbar>
        <TextView android:text="Lumina Noir" android:textColor="@color/primary" />
        <ImageButton android:id="@+id/btn_avatar" />
    </MaterialToolbar>

    <!-- Search bar row -->
    <LinearLayout android:orientation="horizontal">
        <EditText android:id="@+id/et_search"
            android:background="@drawable/bg_input_default"
            android:hint="Search for movies, actors, or genres..."
            android:drawableStart="@drawable/ic_search" />
        <ImageButton android:id="@+id/btn_filter"
            android:src="@drawable/ic_filter"
            android:tint="@color/on_surface_variant" />
    </LinearLayout>

    <!-- Default state content -->
    <LinearLayout android:id="@+id/ll_default_state">
        <!-- Recent searches -->
        <LinearLayout android:orientation="horizontal">
            <TextView android:text="Recent Searches"
                style="@style/TextAppearance.NeonNoir.HeadlineSmall" />
            <TextView android:id="@+id/tv_clear_all"
                android:text="Clear All" android:textColor="@color/primary" />
        </LinearLayout>
        <!-- Horizontal chip scroll for recent search items -->
        <HorizontalScrollView>
            <LinearLayout android:id="@+id/ll_recent_chips" android:orientation="horizontal" />
        </HorizontalScrollView>

        <!-- Explore genres grid -->
        <TextView android:text="Explore Genres"
            style="@style/TextAppearance.NeonNoir.HeadlineSmall" />
        <RecyclerView android:id="@+id/rv_genres"
            app:layoutManager="GridLayoutManager"
            app:spanCount="2" />
    </LinearLayout>

    <!-- Search results (shown while query active) -->
    <RecyclerView android:id="@+id/rv_results"
        android:visibility="gone" />

    <!-- Popular for You section (shown in default state) -->
    <TextView android:text="Popular for You"
        style="@style/TextAppearance.NeonNoir.HeadlineSmall" />
    <RecyclerView android:id="@+id/rv_popular" />

</LinearLayout>
```

### SearchViewModel — Debounced Search
```kotlin
class SearchViewModel @Inject constructor(
    private val searchMoviesUseCase: SearchMoviesUseCase
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _results = MutableLiveData<Resource<List<SearchResult>>>()
    val results: LiveData<Resource<List<SearchResult>>> = _results

    init {
        viewModelScope.launch {
            _query
                .debounce(400)
                .filter { it.isNotBlank() }
                .distinctUntilChanged()
                .collectLatest { query ->
                    _results.postValue(Resource.Loading)
                    _results.postValue(searchMoviesUseCase(query))
                }
        }
    }

    fun setQuery(q: String) { _query.value = q }
}
```

---

## Fragment 8 — Library
**File:** `fragment_library.xml` + `LibraryViewModel.kt`

### XML Layout (key sections)
```xml
<LinearLayout android:orientation="vertical">

    <!-- Tab filter pills -->
    <HorizontalScrollView>
        <RadioGroup android:orientation="horizontal">
            <RadioButton android:id="@+id/tab_all" android:text="All" />
            <RadioButton android:id="@+id/tab_watchlist" android:text="Watchlist" />
            <RadioButton android:id="@+id/tab_downloads" android:text="Downloads" />
        </RadioGroup>
    </HorizontalScrollView>
    <!-- Style active tab: bg_button_primary, inactive: bg_button_ghost -->

    <!-- Continue Watching -->
    <TextView android:text="Continue Watching"
        style="@style/TextAppearance.NeonNoir.HeadlineSmall" />
    <RecyclerView android:id="@+id/rv_continue"
        android:orientation="horizontal" />

    <!-- Active Downloads -->
    <TextView android:text="Active Downloads"
        style="@style/TextAppearance.NeonNoir.HeadlineSmall" />
    <RecyclerView android:id="@+id/rv_downloads" />

    <!-- Watchlist grid -->
    <LinearLayout android:orientation="horizontal">
        <TextView android:text="Your Watchlist"
            style="@style/TextAppearance.NeonNoir.HeadlineSmall" />
        <ImageButton android:id="@+id/btn_grid_view" />
        <ImageButton android:id="@+id/btn_list_view" />
    </LinearLayout>
    <RecyclerView android:id="@+id/rv_watchlist"
        app:layoutManager="GridLayoutManager"
        app:spanCount="2" />

    <!-- Empty state -->
    <LinearLayout android:id="@+id/ll_empty_state" android:visibility="gone">
        <ImageView src="@drawable/ic_heart_outline"
            android:tint="@color/secondary_fixed_dim" />
        <TextView android:text="No favorites yet"
            style="@style/TextAppearance.NeonNoir.HeadlineSmall" />
        <TextView android:text="Tap the heart icon on any movie..."
            style="@style/TextAppearance.NeonNoir.BodyMedium" />
    </LinearLayout>

    <!-- Discover More CTA -->
    <Button android:id="@+id/btn_discover"
        android:background="@drawable/bg_button_ghost"
        android:text="⊕ DISCOVER MORE" />

</LinearLayout>
```

---

## Fragment 9 — Profile
**File:** `fragment_profile.xml` + `ProfileViewModel.kt`

### XML Layout (key sections)
```xml
<LinearLayout android:orientation="vertical">

    <!-- Header -->
    <MaterialToolbar>
        <ImageButton android:id="@+id/btn_back" src="@drawable/ic_arrow_back" />
        <TextView android:text="Profile" />
        <ImageButton android:id="@+id/btn_settings" src="@drawable/ic_settings"
            android:tint="@color/primary" />
    </MaterialToolbar>

    <!-- Profile card -->
    <androidx.cardview.widget.CardView
        app:cardBackgroundColor="@color/surface_container_low"
        app:cardCornerRadius="@dimen/radius_xl">

        <!-- Avatar with gradient ring -->
        <FrameLayout>
            <ImageView android:id="@+id/iv_ring"
                android:background="@drawable/shape_avatar_ring" />
            <ImageView android:id="@+id/iv_avatar"
                android:background="@drawable/shape_circle_clip" />
            <ImageView android:id="@+id/iv_verified"
                android:src="@drawable/ic_verified"
                android:tint="@color/primary" />
        </FrameLayout>

        <TextView android:id="@+id/tv_name"
            style="@style/TextAppearance.NeonNoir.DisplayMedium" />

        <!-- Premium badge pill -->
        <TextView android:id="@+id/tv_badge"
            android:background="@drawable/bg_badge_premiere"
            android:text="PREMIUM MEMBER"
            style="@style/TextAppearance.NeonNoir.LabelCaps"
            android:textColor="@color/primary" />

        <TextView android:id="@+id/tv_bio"
            style="@style/TextAppearance.NeonNoir.BodyMedium"
            android:gravity="center" />
    </androidx.cardview.widget.CardView>

    <!-- Stats grid (2 cards) -->
    <LinearLayout android:orientation="horizontal">
        <androidx.cardview.widget.CardView app:cardBackgroundColor="@color/surface_container">
            <TextView android:id="@+id/tv_watched_count"
                android:textColor="@color/primary"
                style="@style/TextAppearance.NeonNoir.DisplayMedium" />
            <TextView android:text="MOVIES WATCHED"
                style="@style/TextAppearance.NeonNoir.LabelCaps" />
        </androidx.cardview.widget.CardView>
        <androidx.cardview.widget.CardView app:cardBackgroundColor="@color/surface_container">
            <TextView android:id="@+id/tv_watchlist_count"
                android:textColor="@color/secondary"
                style="@style/TextAppearance.NeonNoir.DisplayMedium" />
            <TextView android:text="IN WATCHLIST"
                style="@style/TextAppearance.NeonNoir.LabelCaps" />
        </androidx.cardview.widget.CardView>
    </LinearLayout>

    <!-- Genre bubbles -->
    <com.google.android.material.chip.ChipGroup android:id="@+id/chip_genres" />

    <!-- Continue Watching -->
    <RecyclerView android:id="@+id/rv_continue"
        android:orientation="horizontal" />

    <!-- Settings list -->
    <RecyclerView android:id="@+id/rv_settings" />
    <!-- Uses item_settings_row.xml, see COMPONENTS.md -->

</LinearLayout>
```
