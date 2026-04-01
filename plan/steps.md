# IMPLEMENTATION PROGRESS

✅ Step 1: Design Tokens (Values & Drawables) - COMPLETED
✅ Step 2: Data Layer (DTOs, API, Models, Mappers, Repository) - COMPLETED
✅ Step 3: Hilt DI Modules - COMPLETED
🔲 Step 4: Auth Screens (Splash, SignIn, SignUp, ForgotPassword) - COMPLETED
🔲 Step 5: MainActivity + Navigation Setup - PENDING
🔲 Step 6: Home Fragment - PENDING
🔲 Step 7: Movie Detail Fragment - PENDING
🔲 Step 8: Search Fragment - PENDING
🔲 Step 9: Library Fragment - PENDING
🔲 Step 10: Profile Fragment - PENDING
🔲 Step 11: Search Fragment + ViewModel + Paging 3 - PENDING
🔲 Step 12: Library Fragment + ViewModel - PENDING
🔲 Step 13: Profile Fragment + ViewModel - PENDING
🔲 Step 14: Polish — MotionLayout transitions, Lottie, ripple states - PENDING

---

# Steps for AI
## API.md
# API.md — OMDB Integration (Retrofit + Kotlin Coroutines)

## Setup

1. Get a free key at https://www.omdbapi.com/apikey.aspx
2. Add to `local.properties` (never commit this file):
   ```
   OMDB_API_KEY=your_key_here
   ```
3. Expose in `build.gradle.kts`:
   ```kotlin
   android {
       buildFeatures { buildConfig = true }
       defaultConfig {
           buildConfigField(
               "String", "OMDB_API_KEY",
               "\"${project.properties["OMDB_API_KEY"]}\""
           )
       }
   }
   ```
4. Access in code: `BuildConfig.OMDB_API_KEY`

---

## Data Layer

### data/remote/dto/OmdbSearchResponseDto.kt
```kotlin
data class OmdbSearchResponseDto(
    @SerializedName("Search")       val search: List<OmdbSearchItemDto>?,
    @SerializedName("totalResults") val totalResults: String?,
    @SerializedName("Response")     val response: String,
    @SerializedName("Error")        val error: String?
)

data class OmdbSearchItemDto(
    @SerializedName("Title")  val title: String,
    @SerializedName("Year")   val year: String,
    @SerializedName("imdbID") val imdbId: String,
    @SerializedName("Type")   val type: String,
    @SerializedName("Poster") val poster: String
)
```

### data/remote/dto/OmdbMovieDto.kt
```kotlin
data class OmdbMovieDto(
    @SerializedName("Title")        val title: String,
    @SerializedName("Year")         val year: String,
    @SerializedName("Rated")        val rated: String,
    @SerializedName("Released")     val released: String,
    @SerializedName("Runtime")      val runtime: String,
    @SerializedName("Genre")        val genre: String,
    @SerializedName("Director")     val director: String,
    @SerializedName("Writer")       val writer: String,
    @SerializedName("Actors")       val actors: String,
    @SerializedName("Plot")         val plot: String,
    @SerializedName("Language")     val language: String,
    @SerializedName("Country")      val country: String,
    @SerializedName("Awards")       val awards: String,
    @SerializedName("Poster")       val poster: String,
    @SerializedName("imdbRating")   val imdbRating: String,
    @SerializedName("imdbVotes")    val imdbVotes: String,
    @SerializedName("imdbID")       val imdbId: String,
    @SerializedName("Type")         val type: String,
    @SerializedName("Response")     val response: String,
    @SerializedName("Error")        val error: String?
)
```

---

### data/remote/api/OmdbApiService.kt
```kotlin
interface OmdbApiService {

    // Search by title query (paginated)
    @GET(".")
    suspend fun searchMovies(
        @Query("apikey") apiKey: String = BuildConfig.OMDB_API_KEY,
        @Query("s")      query:  String,
        @Query("page")   page:   Int = 1,
        @Query("type")   type:   String? = null   // "movie", "series", or null
    ): OmdbSearchResponseDto

    // Full movie details by IMDB ID
    @GET(".")
    suspend fun getMovieById(
        @Query("apikey") apiKey:  String = BuildConfig.OMDB_API_KEY,
        @Query("i")      imdbId:  String,
        @Query("plot")   plot:    String = "full"
    ): OmdbMovieDto

    // Full movie details by title (exact match)
    @GET(".")
    suspend fun getMovieByTitle(
        @Query("apikey") apiKey: String = BuildConfig.OMDB_API_KEY,
        @Query("t")      title:  String,
        @Query("plot")   plot:   String = "short"
    ): OmdbMovieDto
}
```

---

## Domain Layer

### domain/model/Movie.kt
```kotlin
data class Movie(
    val imdbId:     String,
    val title:      String,
    val year:       String,
    val rated:      String,
    val runtime:    String,         // Already formatted: "2h 22m"
    val genres:     List<String>,   // Split from "Sci-Fi, Action"
    val director:   String,
    val actors:     List<String>,   // Split from "Keanu Reeves, ..."
    val plot:       String,
    val poster:     String,         // High-res URL
    val imdbRating: Float,          // Parsed from "8.7"
    val type:       String
)
```

### domain/model/SearchResult.kt
```kotlin
data class SearchResult(
    val imdbId: String,
    val title:  String,
    val year:   String,
    val type:   String,
    val poster: String
)
```

---

### data/mapper/MovieMapper.kt
```kotlin
object MovieMapper {

    fun OmdbMovieDto.toDomain(): Movie = Movie(
        imdbId     = imdbId,
        title      = title,
        year       = year,
        rated      = rated,
        runtime    = formatRuntime(runtime),
        genres     = genre.split(", ").filter { it.isNotBlank() },
        director   = director,
        actors     = actors.split(", ").filter { it.isNotBlank() },
        plot       = plot,
        poster     = upgradePosterResolution(poster),
        imdbRating = imdbRating.toFloatOrNull() ?: 0f,
        type       = type
    )

    fun OmdbSearchItemDto.toDomain(): SearchResult = SearchResult(
        imdbId = imdbId,
        title  = title,
        year   = year,
        type   = type,
        poster = upgradePosterResolution(poster)
    )

    private fun formatRuntime(raw: String): String {
        val mins = raw.replace(" min", "").trim().toIntOrNull() ?: return raw
        val h = mins / 60
        val m = mins % 60
        return if (h > 0) "${h}h ${m}m" else "${m}m"
    }

    private fun upgradePosterResolution(url: String): String {
        if (url == "N/A") return ""
        return url.replace("SX300", "SX600")
    }
}
```

---

### data/repository/MovieRepository.kt (interface)
```kotlin
interface MovieRepository {
    suspend fun searchMovies(query: String, page: Int = 1): Resource<List<SearchResult>>
    suspend fun getMovieById(imdbId: String): Resource<Movie>
    suspend fun getMoviesByIds(ids: List<String>): List<Movie>
    fun getWatchlist(): Flow<List<WatchlistEntity>>
    suspend fun addToWatchlist(movie: Movie)
    suspend fun removeFromWatchlist(imdbId: String)
    suspend fun isInWatchlist(imdbId: String): Boolean
}
```

### data/repository/MovieRepositoryImpl.kt
```kotlin
@Singleton
class MovieRepositoryImpl @Inject constructor(
    private val api: OmdbApiService,
    private val watchlistDao: WatchlistDao,
    private val historyDao: HistoryDao
) : MovieRepository {

    // In-memory cache to stay within OMDB free tier (1000 req/day)
    private val cache = mutableMapOf<String, Movie>()

    override suspend fun getMovieById(imdbId: String): Resource<Movie> {
        cache[imdbId]?.let { return Resource.Success(it) }
        return try {
            val dto = api.getMovieById(imdbId = imdbId)
            if (dto.response == "False") {
                Resource.Error(dto.error ?: "Unknown error")
            } else {
                val movie = with(MovieMapper) { dto.toDomain() }
                cache[imdbId] = movie
                Resource.Success(movie)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun searchMovies(query: String, page: Int): Resource<List<SearchResult>> {
        return try {
            val response = api.searchMovies(query = query, page = page)
            if (response.response == "False") {
                Resource.Error(response.error ?: "No results")
            } else {
                val results = response.search
                    ?.map { with(MovieMapper) { it.toDomain() } }
                    ?: emptyList()
                Resource.Success(results)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun getMoviesByIds(ids: List<String>): List<Movie> {
        return ids.mapNotNull { id ->
            (getMovieById(id) as? Resource.Success)?.data
        }
    }

    override fun getWatchlist(): Flow<List<WatchlistEntity>> =
        watchlistDao.getAll()

    override suspend fun addToWatchlist(movie: Movie) =
        watchlistDao.insert(movie.toEntity())

    override suspend fun removeFromWatchlist(imdbId: String) =
        watchlistDao.deleteById(imdbId)

    override suspend fun isInWatchlist(imdbId: String): Boolean =
        watchlistDao.exists(imdbId)
}
```

---

## util/Resource.kt
```kotlin
sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}
```

---

## di/NetworkModule.kt
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG)
                    HttpLoggingInterceptor.Level.BODY
                else
                    HttpLoggingInterceptor.Level.NONE
            })
            .build()

    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://www.omdbapi.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides @Singleton
    fun provideOmdbApiService(retrofit: Retrofit): OmdbApiService =
        retrofit.create(OmdbApiService::class.java)
}
```

---

## constants/Editorial.kt — Curated Home Screen IDs

```kotlin
object Editorial {

    // Hero feature
    const val FEATURED_HERO_ID = "tt1856101"  // Blade Runner 2049

    // Trending row
    val TRENDING_IDS = listOf(
        "tt1856101",  // Blade Runner 2049
        "tt0816692",  // Interstellar
        "tt4154796",  // Avengers: Endgame
        "tt0133093",  // The Matrix
        "tt0468569",  // The Dark Knight
        "tt2015381"   // Guardians of the Galaxy
    )

    // Recently Added row
    val RECENTLY_ADDED_IDS = listOf(
        "tt1375666",  // Inception
        "tt0110912",  // Pulp Fiction
        "tt0482571",  // The Prestige
        "tt0120737",  // The Fellowship of the Ring
        "tt0167260"   // Return of the King
    )

    // Genre search keywords
    val GENRE_SEARCHES = mapOf(
        "SCI-FI"   to "sci-fi",
        "HORROR"   to "horror",
        "DRAMA"    to "drama",
        "ACTION"   to "action",
        "THRILLER" to "thriller",
        "NEO-NOIR" to "noir"
    )
}
```

---

## Rate Limit Strategy

- Free OMDB tier: 1,000 requests/day
- In-memory `Map<String, Movie>` cache — skip API if already fetched this session
- Room DB caches watchlist items locally — detail screen loads from DB first
- Search: debounce EditText input by **400ms** using `Flow.debounce(400)`
- Paging 3 for search results — load next page only on scroll

## COMPONENTS.md
# COMPONENTS.md — RecyclerView Adapters & Reusable XML Components

---

## 1. MovieCardAdapter

**File:** `presentation/common/adapters/MovieCardAdapter.kt`  
**Layout:** `item_movie_portrait.xml` / `item_movie_landscape.xml` / `item_movie_featured.xml`

### item_movie_portrait.xml
```xml
<!-- Root: MaterialCardView
     cardBackgroundColor: @color/surface_container
     cardCornerRadius: @dimen/card_corner_radius (12dp)
     cardElevation: 0dp
     Width: match_parent in GridLayoutManager(2) or fixed 140dp in horizontal list -->

<androidx.cardview.widget.CardView
    android:layout_width="140dp"
    android:layout_height="210dp"
    app:cardBackgroundColor="@color/surface_container"
    app:cardCornerRadius="@dimen/card_corner_radius"
    app:cardElevation="0dp">

    <!-- Movie poster image -->
    <ImageView
        android:id="@+id/iv_poster"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scaleType="centerCrop" />

    <!-- Rating badge overlay (top right) -->
    <TextView
        android:id="@+id/tv_rating"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="top|end"
        android:layout_margin="6dp"
        android:background="@drawable/bg_badge_dark"
        android:padding="4dp"
        android:drawableStart="@drawable/ic_star_small"
        android:drawableTint="@color/primary"
        style="@style/TextAppearance.NeonNoir.LabelSmall"
        android:textColor="@color/on_surface" />

</androidx.cardview.widget.CardView>

<!-- Title below card (NOT inside card, in parent LinearLayout) -->
<TextView
    android:id="@+id/tv_title"
    style="@style/TextAppearance.NeonNoir.BodyMedium"
    android:textColor="@color/on_surface"
    android:maxLines="1"
    android:ellipsize="end" />

<TextView
    android:id="@+id/tv_meta"
    style="@style/TextAppearance.NeonNoir.LabelSmall"
    android:textColor="@color/on_surface_variant" />
<!-- meta = "Genre • Year" -->
```

### item_movie_landscape.xml
```xml
<!-- Width: match_parent or 280dp, Height: 160dp -->
<FrameLayout>
    <ImageView android:id="@+id/iv_poster"
        android:scaleType="centerCrop"
        android:layout_width="match_parent"
        android:layout_height="160dp" />

    <!-- Progress bar at bottom (optional, shown when progress > 0) -->
    <ProgressBar
        android:id="@+id/pb_progress"
        style="@android:style/Widget.ProgressBar.Horizontal"
        android:layout_gravity="bottom"
        android:layout_height="3dp"
        android:progressTint="@color/primary"
        android:max="100"
        android:visibility="gone" />

    <!-- Gradient overlay -->
    <View android:background="@drawable/bg_card_overlay" />

    <!-- Title bottom-left overlay -->
    <TextView android:id="@+id/tv_title"
        android:layout_gravity="bottom|start"
        android:padding="12dp"
        style="@style/TextAppearance.NeonNoir.HeadlineSmall"
        android:textColor="@color/on_surface" />
</FrameLayout>
```

### MovieCardAdapter.kt
```kotlin
class MovieCardAdapter(
    private val onMovieClick: (String) -> Unit,
    private val onMovieLongClick: ((Movie) -> Unit)? = null
) : ListAdapter<SearchResult, MovieCardAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemMoviePortraitBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SearchResult) {
            binding.tvTitle.text = item.title
            binding.tvMeta.text = item.year
            Glide.with(binding.root)
                .load(item.poster)
                .placeholder(R.drawable.placeholder_poster)
                .centerCrop()
                .into(binding.ivPoster)

            binding.root.setOnClickListener { onMovieClick(item.imdbId) }
            onMovieLongClick?.let { cb ->
                binding.root.setOnLongClickListener {
                    // cb(item) — cast to Movie if needed
                    true
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMoviePortraitBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<SearchResult>() {
        override fun areItemsTheSame(o: SearchResult, n: SearchResult) = o.imdbId == n.imdbId
        override fun areContentsTheSame(o: SearchResult, n: SearchResult) = o == n
    }
}
```

---

## 2. GenreTileAdapter

**Layout:** `item_genre_tile.xml`

### item_genre_tile.xml
```xml
<!-- Root: FrameLayout or MaterialCardView, 1:1 or 1:0.75 aspect ratio -->
<FrameLayout
    android:layout_width="match_parent"
    android:layout_height="120dp">

    <!-- Background image -->
    <ImageView
        android:id="@+id/iv_bg"
        android:scaleType="centerCrop"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

    <!-- Gradient overlay -->
    <View
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="@drawable/bg_card_overlay" />

    <!-- Genre label (bottom-left) -->
    <TextView
        android:id="@+id/tv_label"
        android:layout_gravity="bottom|start"
        android:padding="12dp"
        style="@style/TextAppearance.NeonNoir.HeadlineMedium"
        android:textColor="@color/on_surface" />

</FrameLayout>
```

---

## 3. CastAdapter

**Layout:** `item_cast_member.xml`

### item_cast_member.xml
```xml
<LinearLayout android:orientation="horizontal" android:gravity="center_vertical">

    <!-- Initials avatar (no real headshots from OMDB) -->
    <TextView
        android:id="@+id/tv_initials"
        android:layout_width="40dp"
        android:layout_height="40dp"
        android:background="@drawable/shape_circle_secondary"
        android:gravity="center"
        style="@style/TextAppearance.NeonNoir.LabelMedium"
        android:textColor="@color/on_surface" />

    <LinearLayout android:orientation="vertical"
        android:layout_marginStart="12dp">
        <TextView android:id="@+id/tv_actor_name"
            style="@style/TextAppearance.NeonNoir.BodyMedium"
            android:textColor="@color/on_surface" />
        <TextView android:id="@+id/tv_character"
            style="@style/TextAppearance.NeonNoir.LabelSmall" />
    </LinearLayout>

</LinearLayout>
```

### CastAdapter.kt
```kotlin
class CastAdapter : RecyclerView.Adapter<CastAdapter.ViewHolder>() {

    private var actors: List<String> = emptyList()

    fun submitActors(raw: String) {
        actors = raw.split(", ").filter { it.isNotBlank() }
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemCastMemberBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemCastMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val name = actors[position]
        holder.binding.tvActorName.text = name
        holder.binding.tvInitials.text = name
            .split(" ")
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")
    }

    override fun getItemCount() = actors.size
}
```

---

## 4. SettingsRowAdapter

**Layout:** `item_settings_row.xml`

### item_settings_row.xml
```xml
<ConstraintLayout android:layout_height="64dp"
    android:background="?attr/selectableItemBackground">

    <!-- Icon -->
    <ImageView android:id="@+id/iv_icon"
        android:layout_width="24dp"
        android:layout_height="24dp"
        android:tint="@color/secondary" />

    <!-- Texts -->
    <LinearLayout android:orientation="vertical">
        <TextView android:id="@+id/tv_title"
            style="@style/TextAppearance.NeonNoir.BodyMedium"
            android:textColor="@color/on_surface" />
        <TextView android:id="@+id/tv_subtitle"
            style="@style/TextAppearance.NeonNoir.LabelSmall" />
    </LinearLayout>

    <!-- Chevron (hidden for Log Out) -->
    <ImageView android:id="@+id/iv_chevron"
        android:src="@drawable/ic_chevron_right"
        android:tint="@color/on_surface_variant" />

</ConstraintLayout>
```

---

## 5. Section Header Layout

**File:** `view_section_header.xml` (used with `<include>`)

```xml
<LinearLayout android:orientation="vertical"
    android:paddingStart="@dimen/screen_padding_horizontal"
    android:paddingEnd="@dimen/screen_padding_horizontal"
    android:paddingTop="@dimen/section_gap">

    <LinearLayout android:orientation="horizontal"
        android:gravity="center_vertical">

        <TextView android:id="@+id/tv_section_title"
            style="@style/TextAppearance.NeonNoir.HeadlineSmall"
            android:layout_weight="1" />

        <!-- Optional VIEW ALL link -->
        <TextView android:id="@+id/tv_view_all"
            android:text="VIEW ALL →"
            style="@style/TextAppearance.NeonNoir.LabelCaps"
            android:textColor="@color/primary"
            android:visibility="gone" />
    </LinearLayout>

    <TextView android:id="@+id/tv_section_subtitle"
        style="@style/TextAppearance.NeonNoir.BodyMedium"
        android:visibility="gone" />

</LinearLayout>
```

---

## 6. Hero Section Layout

**File:** `view_hero_section.xml`

```xml
<FrameLayout android:layout_height="360dp">

    <!-- Backdrop image -->
    <ImageView android:id="@+id/iv_hero_backdrop"
        android:scaleType="centerCrop"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

    <!-- Gradient overlay (transparent → surface) -->
    <View android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="@drawable/bg_hero_overlay" />

    <!-- Content overlay (bottom-aligned) -->
    <LinearLayout android:orientation="vertical"
        android:layout_gravity="bottom"
        android:padding="@dimen/screen_padding_horizontal">

        <!-- Tags row: PREMIERE pill + Sci-Fi • 2h 45m • 2024 -->
        <LinearLayout android:orientation="horizontal">
            <TextView android:id="@+id/tv_premiere_badge"
                android:background="@drawable/bg_badge_premiere"
                android:text="PREMIERE"
                style="@style/TextAppearance.NeonNoir.LabelCaps"
                android:textColor="@color/primary"
                android:padding="4dp" />
            <TextView android:id="@+id/tv_meta"
                style="@style/TextAppearance.NeonNoir.LabelMedium"
                android:layout_marginStart="8dp" />
        </LinearLayout>

        <!-- Title (displayLg) -->
        <TextView android:id="@+id/tv_hero_title"
            style="@style/TextAppearance.NeonNoir.DisplayLarge"
            android:textColor="@color/on_surface" />

        <!-- Plot excerpt -->
        <TextView android:id="@+id/tv_hero_plot"
            style="@style/TextAppearance.NeonNoir.BodyMedium"
            android:maxLines="2"
            android:ellipsize="end" />

        <!-- Action buttons row -->
        <LinearLayout android:orientation="horizontal"
            android:layout_marginTop="12dp">
            <Button android:id="@+id/btn_watch_now"
                android:background="@drawable/bg_button_primary"
                android:drawableStart="@drawable/ic_play"
                android:text="Watch Now" />
            <Button android:id="@+id/btn_watchlist"
                android:background="@drawable/bg_button_ghost"
                android:drawableStart="@drawable/ic_add"
                android:text="Watchlist"
                android:layout_marginStart="8dp" />
        </LinearLayout>

    </LinearLayout>
</FrameLayout>
```

---

## 7. Download Item Layout

**File:** `item_download.xml`

```xml
<ConstraintLayout android:layout_height="72dp">

    <ImageView android:id="@+id/iv_thumbnail"
        android:layout_width="96dp"
        android:layout_height="match_parent"
        android:scaleType="centerCrop" />

    <LinearLayout android:orientation="vertical">
        <LinearLayout android:orientation="horizontal">
            <TextView android:id="@+id/tv_title"
                style="@style/TextAppearance.NeonNoir.BodyMedium"
                android:textColor="@color/on_surface" />
            <TextView android:id="@+id/tv_paused_badge"
                android:text="PAUSED"
                style="@style/TextAppearance.NeonNoir.LabelCaps"
                android:textColor="@color/warning"
                android:visibility="gone" />
            <TextView android:id="@+id/tv_percent"
                android:textColor="@color/primary"
                style="@style/TextAppearance.NeonNoir.LabelMedium" />
        </LinearLayout>
        <ProgressBar android:id="@+id/pb_download"
            style="@android:style/Widget.ProgressBar.Horizontal"
            android:layout_height="3dp"
            android:progressTint="@color/primary"
            android:max="100" />
    </LinearLayout>

    <!-- Play/Pause control -->
    <ImageButton android:id="@+id/btn_control"
        android:src="@drawable/ic_pause"
        android:tint="@color/on_surface_variant"
        android:background="@android:color/transparent" />

</ConstraintLayout>
```

---

## 8. Bottom Navigation (activity_main.xml)

```xml
<androidx.coordinatorlayout.widget.CoordinatorLayout
    xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Navigation host -->
    <androidx.fragment.app.FragmentContainerView
        android:id="@+id/nav_host_fragment"
        android:name="androidx.navigation.fragment.NavHostFragment"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:defaultNavHost="true"
        app:navGraph="@navigation/nav_main" />

    <!-- Bottom navigation bar -->
    <com.google.android.material.bottomnavigation.BottomNavigationView
        android:id="@+id/bottom_nav"
        android:layout_gravity="bottom"
        android:layout_width="match_parent"
        android:layout_height="@dimen/bottom_nav_height"
        android:background="@color/glass_nav_bg"
        app:itemIconTint="@color/nav_item_color"   <!-- selector -->
        app:itemTextColor="@color/nav_item_color"
        app:menu="@menu/menu_bottom_nav"
        app:labelVisibilityMode="labeled" />

</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

### res/menu/menu_bottom_nav.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:id="@+id/nav_home"
        android:icon="@drawable/ic_home"
        android:title="HOME" />
    <item android:id="@+id/nav_search"
        android:icon="@drawable/ic_search"
        android:title="SEARCH" />
    <item android:id="@+id/nav_library"
        android:icon="@drawable/ic_film"
        android:title="MY MOVIES" />
    <item android:id="@+id/nav_profile"
        android:icon="@drawable/ic_person"
        android:title="PROFILE" />
</menu>
```

### res/color/nav_item_color.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:color="@color/primary" android:state_checked="true" />
    <item android:color="@color/secondary_fixed_dim" />
</selector>
```

---

## 9. Room Database

### local/entity/WatchlistEntity.kt
```kotlin
@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey val imdbId:    String,
    val title:     String,
    val year:      String,
    val poster:    String,
    val genre:     String,
    val rating:    Float,
    val addedAt:   Long = System.currentTimeMillis()
)
```

### local/dao/WatchlistDao.kt
```kotlin
@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist ORDER BY addedAt DESC")
    fun getAll(): Flow<List<WatchlistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE imdbId = :imdbId")
    suspend fun deleteById(imdbId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE imdbId = :imdbId)")
    suspend fun exists(imdbId: String): Boolean
}
```

### local/db/AppDatabase.kt
```kotlin
@Database(
    entities = [WatchlistEntity::class, HistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun watchlistDao(): WatchlistDao
    abstract fun historyDao(): HistoryDao
}
```

## PLAN.md
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

## PROMPTS.md
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

## SCREENS.md
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

## SKILL.md
---
name: neon-noir-android
description: Use this skill whenever building the Neon Noir Android movie app or any screen, component, ViewModel, adapter, or data layer file within it. Triggers include any mention of a fragment, layout XML, ViewModel, repository, adapter, Room entity, Retrofit service, Hilt module, or navigation graph that belongs to this project. Also use when the user asks to add a new feature, fix a bug, or refactor any Kotlin or XML file in the app. Do NOT use for React Native, Flutter, or web versions of this app.
---

# Neon Noir Android — Development Skill

## Project Identity

**App name:** Neon Noir  
**Stack:** Kotlin · XML Layouts · ViewBinding · MVVM · Hilt · Retrofit · Room · Coroutines + Flow · Navigation Component  
**API:** OMDB (`https://www.omdbapi.com/`) — key from `BuildConfig.OMDB_API_KEY`  
**Architecture:** Single Activity · Clean MVVM · Repository Pattern · Use Cases  
**Design system:** "The Neon Noir Curator" — deep dark surfaces, neon pink/purple/cyan accents, Epilogue + Manrope fonts

---

## Code Style — The One Non-Negotiable Rule

### Simple comments above functions only

Every function, method, and override must have **one short plain-English comment on the line directly above it**. No other inline comments. No block comments inside function bodies. No KDoc `/** */` blocks unless generating a library or public API.

**Correct:**
```kotlin
// Fetches full movie details and caches result in memory
override suspend fun getMovieById(imdbId: String): Resource<Movie> {
    cache[imdbId]?.let { return Resource.Success(it) }
    return try {
        val dto = api.getMovieById(imdbId = imdbId)
        val movie = with(MovieMapper) { dto.toDomain() }
        cache[imdbId] = movie
        Resource.Success(movie)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Network error")
    }
}

// Converts raw "142 min" string to "2h 22m" format
private fun formatRuntime(raw: String): String {
    val mins = raw.replace(" min", "").trim().toIntOrNull() ?: return raw
    val h = mins / 60
    val m = mins % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}
```

**Wrong — do not do this:**
```kotlin
/**
 * Fetches the movie by its IMDB identifier.
 * @param imdbId The IMDB ID string
 * @return Resource wrapping Movie domain model
 */
override suspend fun getMovieById(imdbId: String): Resource<Movie> {
    // Check cache first
    cache[imdbId]?.let {
        // Return early if cached
        return Resource.Success(it)
    }
    // Make network request
    val dto = api.getMovieById(imdbId = imdbId) // call API
    ...
}
```

**Rule summary:**
- ✅ One `//` comment directly above each function
- ✅ Comment is a plain verb phrase ("Loads…", "Returns…", "Submits…", "Toggles…")
- ❌ No `/** KDoc */` blocks
- ❌ No inline comments inside the function body
- ❌ No commented-out code
- ❌ No comments above properties or class declarations (keep those self-documenting via naming)

---

## Architecture Layers

### Layer Rules
Each layer only knows about the layer below it. Never import a ViewModel into a Repository. Never import Room entities into the presentation layer.

```
Presentation (Fragment + ViewModel)
    ↓ calls
Domain (UseCase)
    ↓ calls
Data (Repository → Remote API + Local DB)
```

### Presentation Layer
- **Fragment:** observes LiveData / StateFlow from ViewModel, updates Views, handles navigation
- **ViewModel:** holds UI state, calls UseCases, never imports Android Views or Context (except Application via `@HiltViewModel`)
- Use `viewModelScope.launch` for coroutines inside ViewModels
- Use `LiveData` for one-shot state updates, `StateFlow` for continuous streams (e.g. search query)

### Domain Layer
- **UseCase:** single `operator fun invoke()` that delegates to the Repository
- **Model:** plain Kotlin data class, no Android imports, no Room annotations

### Data Layer
- **Repository:** interface in domain, implementation in data
- **DTO:** data class with `@SerializedName` Gson annotations, nothing else
- **Mapper:** `object MovieMapper` with extension functions — never put mapping logic in DTOs or entities
- **Entity:** Room `@Entity` data class — never used in presentation layer directly

---

## ViewModel Pattern

Every ViewModel in this project follows this exact structure:

```kotlin
@HiltViewModel
class ExampleViewModel @Inject constructor(
    private val someUseCase: SomeUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData<ExampleUiState>(ExampleUiState.Idle)
    val uiState: LiveData<ExampleUiState> = _uiState

    // Triggers the main data load on first creation
    init { load() }

    // Loads data and posts result to uiState
    private fun load() {
        viewModelScope.launch {
            _uiState.value = ExampleUiState.Loading
            when (val result = someUseCase()) {
                is Resource.Success -> _uiState.value = ExampleUiState.Success(result.data)
                is Resource.Error   -> _uiState.value = ExampleUiState.Error(result.message)
                is Resource.Loading -> Unit
            }
        }
    }
}

sealed class ExampleUiState {
    object Idle : ExampleUiState()
    object Loading : ExampleUiState()
    data class Success<T>(val data: T) : ExampleUiState()
    data class Error(val message: String) : ExampleUiState()
}
```

---

## Fragment Pattern

Every Fragment in this project follows this exact structure:

```kotlin
@AndroidEntryPoint
class ExampleFragment : Fragment(R.layout.fragment_example) {

    private var _binding: FragmentExampleBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ExampleViewModel by viewModels()

    // Inflates ViewBinding and returns the root view
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExampleBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Sets up UI, adapters, observers, and click listeners
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
        setupClickListeners()
    }

    // Configures the RecyclerView with its adapter and layout manager
    private fun setupRecyclerView() { ... }

    // Observes LiveData from ViewModel and updates UI accordingly
    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ExampleUiState.Loading -> showLoading()
                is ExampleUiState.Success -> showContent(state.data)
                is ExampleUiState.Error   -> showError(state.message)
                else -> Unit
            }
        }
    }

    // Attaches click listeners to all interactive views
    private fun setupClickListeners() { ... }

    // Shows loading skeleton while data is being fetched
    private fun showLoading() { ... }

    // Populates the UI with successfully loaded content
    private fun showContent(data: Any) { ... }

    // Displays an error Snackbar with the given message
    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    // Cleans up ViewBinding reference to prevent memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

---

## RecyclerView Adapter Pattern

```kotlin
class ExampleAdapter(
    private val onClick: (String) -> Unit
) : ListAdapter<ExampleModel, ExampleAdapter.ViewHolder>(DiffCallback()) {

    // Creates and inflates a new ViewHolder from the item layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemExampleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    // Binds data from the item at the given position to the ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemExampleBinding)
        : RecyclerView.ViewHolder(binding.root) {

        // Populates all views in this row and attaches the click listener
        fun bind(item: ExampleModel) {
            binding.tvTitle.text = item.title
            binding.root.setOnClickListener { onClick(item.id) }
        }
    }

    // Calculates item differences efficiently for ListAdapter animations
    class DiffCallback : DiffUtil.ItemCallback<ExampleModel>() {
        override fun areItemsTheSame(o: ExampleModel, n: ExampleModel) = o.id == n.id
        override fun areContentsTheSame(o: ExampleModel, n: ExampleModel) = o == n
    }
}
```

---

## XML Layout Rules

Rules derived from `DESIGN.md` — enforce these in every layout file.

### ✅ Do
- Use `@color/surface`, `@color/surface_container_low`, `@color/surface_container` for backgrounds
- Use `@style/TextAppearance.NeonNoir.*` for all TextViews — never set `android:textSize` or `android:fontFamily` inline
- Use `@dimen/section_gap` (32dp) between major sections instead of divider `View`s
- Use `@drawable/bg_button_primary` (gradient pill) for all primary CTAs
- Use `@drawable/bg_input_default` / `@drawable/bg_input_focused` for all EditTexts
- Use `app:cardElevation="0dp"` on all `CardView`s — tonal layering only
- Use `android:textColor="@color/on_surface"` or `@color/on_surface_variant"` — never hardcode colors

### ❌ Never
- Never add a `<View android:layout_height="1dp">` divider — use spacing instead
- Never use `android:textColor="#FFFFFF"` — always use `@color/on_surface`
- Never set `app:cardElevation` above 0dp on content cards (only on floating modals)
- Never use `android:background` with a solid 1px opaque stroke — use ghost borders at 15% opacity
- Never hardcode `android:textSize`, `android:fontFamily`, `android:letterSpacing` inline

### ConstraintLayout guidelines
- Use `0dp` (match constraint) for width/height when stretching to constraints
- Chain horizontal buttons with `app:layout_constraintHorizontal_chainStyle="spread_inside"`
- Use `app:layout_constraintGuide_percent` for percentage-based positioning in hero layouts

---

## Design Token Quick Reference

| Token | XML name | Value |
|---|---|---|
| Base background | `@color/surface` | `#0E0E11` |
| Section background | `@color/surface_container_low` | `#131316` |
| Card background | `@color/surface_container` | `#19191D` |
| Input background | `@color/surface_container_highest` | `#252528` |
| Primary (pink) | `@color/primary` | `#FF8B9B` |
| Secondary (purple) | `@color/secondary` | `#A98BFF` |
| Tertiary (cyan) | `@color/tertiary` | `#81ECFF` |
| Primary text | `@color/on_surface` | `#F0EDF1` |
| Secondary text | `@color/on_surface_variant` | `#ACAAAE` |
| Inactive icons | `@color/secondary_fixed_dim` | `#6B5FA8` |
| CTA border radius | `@dimen/radius_full` | `999dp` |
| Card corner radius | `@dimen/card_corner_radius` | `12dp` |
| Section spacing | `@dimen/section_gap` | `32dp` |
| Screen padding | `@dimen/screen_padding_horizontal` | `16dp` |
| Button height | `@dimen/button_height` | `52dp` |

---

## OMDB API Patterns

### Making a call in a Repository
```kotlin
// Searches OMDB by title query with optional pagination
override suspend fun searchMovies(query: String, page: Int): Resource<List<SearchResult>> {
    return try {
        val response = api.searchMovies(query = query, page = page)
        if (response.response == "False") Resource.Error(response.error ?: "No results")
        else Resource.Success(response.search?.map { with(MovieMapper) { it.toDomain() } } ?: emptyList())
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Network error")
    }
}
```

### Debounced search in a ViewModel
```kotlin
private val _query = MutableStateFlow("")

// Collects the search query with 400ms debounce and triggers API call
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

// Updates the search query StateFlow
fun setQuery(q: String) { _query.value = q }
```

### Loading a poster with Glide
```kotlin
// Loads a poster image into the given ImageView with a dark placeholder
fun ImageView.loadPoster(url: String) {
    Glide.with(this)
        .load(url.ifBlank { null })
        .placeholder(R.drawable.placeholder_poster)
        .error(R.drawable.placeholder_poster)
        .centerCrop()
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}
```

---

## Navigation Pattern

### Navigating with Safe Args
```kotlin
// Navigates to detail screen passing the selected movie's IMDB ID
private fun openDetail(imdbId: String) {
    val action = HomeFragmentDirections.actionHomeToDetail(imdbId)
    findNavController().navigate(action)
}
```

### Handling back navigation
```kotlin
// Pops the back stack when the user presses the back button
binding.btnBack.setOnClickListener {
    findNavController().popBackStack()
}
```

---

## Glide Extensions

Put these in `util/Extensions.kt` and use them across all fragments and adapters.

```kotlin
// Loads a movie poster with crossfade and dark placeholder
fun ImageView.loadPoster(url: String) {
    Glide.with(this)
        .load(url.ifBlank { null })
        .placeholder(R.drawable.placeholder_poster)
        .centerCrop()
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}

// Loads a full-width backdrop with a center-crop and fade-in
fun ImageView.loadBackdrop(url: String) {
    Glide.with(this)
        .load(url.ifBlank { null })
        .placeholder(R.drawable.placeholder_backdrop)
        .centerCrop()
        .transition(DrawableTransitionOptions.withCrossFade(300))
        .into(this)
}

// Shows this view and hides the other
fun View.showWhileHiding(other: View) {
    visibility = View.VISIBLE
    other.visibility = View.GONE
}

// Converts dp value to pixels using the view's display metrics
fun View.dpToPx(dp: Int): Int =
    (dp * resources.displayMetrics.density).toInt()
```

---

## Hilt Module Pattern

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object ExampleModule {

    // Provides the singleton Retrofit instance configured for OMDB
    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://www.omdbapi.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    // Provides the OMDB API service created from the Retrofit instance
    @Provides @Singleton
    fun provideOmdbApiService(retrofit: Retrofit): OmdbApiService =
        retrofit.create(OmdbApiService::class.java)
}
```

For binding interfaces to implementations use `@Binds` in an `abstract class`:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    // Binds MovieRepositoryImpl as the concrete implementation of MovieRepository
    @Binds @Singleton
    abstract fun bindMovieRepository(impl: MovieRepositoryImpl): MovieRepository
}
```

---

## Room DAO Pattern

```kotlin
@Dao
interface WatchlistDao {

    // Returns a live stream of all watchlist items ordered by most recently added
    @Query("SELECT * FROM watchlist ORDER BY addedAt DESC")
    fun getAll(): Flow<List<WatchlistEntity>>

    // Inserts or replaces a watchlist entry
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WatchlistEntity)

    // Removes the watchlist entry with the given IMDB ID
    @Query("DELETE FROM watchlist WHERE imdbId = :imdbId")
    suspend fun deleteById(imdbId: String)

    // Returns true if an entry with the given IMDB ID already exists
    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE imdbId = :imdbId)")
    suspend fun exists(imdbId: String): Boolean

    // Returns the total count of saved watchlist items
    @Query("SELECT COUNT(*) FROM watchlist")
    fun getCount(): Flow<Int>
}
```

---

## Common Pitfalls to Avoid

| Mistake | Fix |
|---|---|
| Calling `binding.*` after `onDestroyView` | Always null `_binding` in `onDestroyView` |
| `viewModelScope` missing after process death | Use `SavedStateHandle` for critical state |
| Room query on main thread | All DAO functions must be `suspend` or return `Flow` |
| Retrofit call on main thread | All API functions must be `suspend` |
| Memory leak from anonymous listener in Fragment | Remove listeners in `onDestroyView` |
| `notifyDataSetChanged()` on ListAdapter | Use `submitList()` — `DiffUtil` handles diffs |
| Hardcoded OMDB key in source | Key lives in `local.properties`, exposed via `BuildConfig` |
| Showing raw DTO in UI | Always map DTO → domain model via `MovieMapper` before the presentation layer sees it |
| Inline `textSize` / `fontFamily` in XML | Always use `style="@style/TextAppearance.NeonNoir.*"` |
| 1dp divider `View` between sections | Use `android:layout_marginTop="@dimen/section_gap"` instead |

## TOKENS.md
# TOKENS.md — Design Tokens for Android XML

All tokens derived from `DESIGN.md`. Copy each block into the corresponding `res/values/` file.

---

## res/values/colors.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>

    <!-- ═══════════════════════════════════════ -->
    <!-- SURFACES                                -->
    <!-- ═══════════════════════════════════════ -->
    <color name="surface">#0E0E11</color>
    <color name="surface_container_lowest">#101013</color>
    <color name="surface_container_low">#131316</color>
    <color name="surface_container">#19191D</color>
    <color name="surface_container_high">#1F1F23</color>
    <color name="surface_container_highest">#252528</color>
    <color name="surface_bright">#2C2C30</color>

    <!-- ═══════════════════════════════════════ -->
    <!-- BRAND / ACCENT                          -->
    <!-- ═══════════════════════════════════════ -->
    <color name="primary">#FF8B9B</color>
    <color name="primary_container">#FF7389</color>
    <color name="secondary">#A98BFF</color>
    <color name="secondary_fixed">#C4B0FF</color>
    <color name="secondary_fixed_dim">#6B5FA8</color>
    <color name="tertiary">#81ECFF</color>

    <!-- ═══════════════════════════════════════ -->
    <!-- TEXT                                    -->
    <!-- ═══════════════════════════════════════ -->
    <!-- NEVER use pure white #FFFFFF for body text -->
    <color name="on_surface">#F0EDF1</color>
    <color name="on_surface_variant">#ACAAAE</color>
    <color name="on_primary">#0E0E11</color>

    <!-- ═══════════════════════════════════════ -->
    <!-- BORDERS (sparingly — NO 1px dividers)   -->
    <!-- ═══════════════════════════════════════ -->
    <color name="outline">#938F94</color>
    <color name="outline_variant">#48474B</color>
    <!-- Ghost border: outline_variant at 15% opacity = #26484748 -->
    <color name="ghost_border">#26484748</color>
    <!-- Tertiary ghost (input focus) at 20% = #3381ECFF -->
    <color name="ghost_border_focus">#3381ECFF</color>

    <!-- ═══════════════════════════════════════ -->
    <!-- SEMANTIC                                -->
    <!-- ═══════════════════════════════════════ -->
    <color name="error">#FFB3B3</color>
    <color name="warning">#FFD580</color>
    <color name="success">#80FFB3</color>

    <!-- ═══════════════════════════════════════ -->
    <!-- BADGE BACKGROUNDS (tinted)              -->
    <!-- ═══════════════════════════════════════ -->
    <!-- primary at 20% opacity -->
    <color name="badge_premiere_bg">#33FF8B9B</color>
    <!-- tertiary at 20% opacity -->
    <color name="badge_new_bg">#3381ECFF</color>
    <!-- secondary at 20% opacity -->
    <color name="badge_accent_bg">#33A98BFF</color>

    <!-- ═══════════════════════════════════════ -->
    <!-- GLASS / OVERLAY                         -->
    <!-- ═══════════════════════════════════════ -->
    <!-- Bottom nav glass background -->
    <color name="glass_nav_bg">#B3131316</color>
    <!-- Card overlay start (transparent) -->
    <color name="overlay_start">#00000000</color>
    <!-- Card overlay end -->
    <color name="overlay_end">#EB0E0E11</color>

</resources>
```

---

## res/values/dimens.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>

    <!-- ═══════════════════════════ -->
    <!-- SPACING (4pt base grid)     -->
    <!-- ═══════════════════════════ -->
    <dimen name="spacing_1">4dp</dimen>
    <dimen name="spacing_2">8dp</dimen>
    <dimen name="spacing_3">12dp</dimen>
    <dimen name="spacing_4">16dp</dimen>
    <dimen name="spacing_5">20dp</dimen>
    <dimen name="spacing_6">24dp</dimen>
    <dimen name="spacing_7">28dp</dimen>
    <dimen name="spacing_8">32dp</dimen>
    <dimen name="spacing_10">40dp</dimen>
    <dimen name="spacing_12">48dp</dimen>
    <dimen name="spacing_14">56dp</dimen>
    <dimen name="spacing_16">64dp</dimen>

    <!-- Section gaps (replaces divider lines per DESIGN.md) -->
    <dimen name="section_gap">32dp</dimen>
    <dimen name="row_gap">20dp</dimen>
    <dimen name="card_gap">8dp</dimen>
    <dimen name="card_gap_grid">12dp</dimen>

    <!-- Screen horizontal padding -->
    <dimen name="screen_padding_horizontal">16dp</dimen>

    <!-- ═══════════════════════════ -->
    <!-- BORDER RADIUS               -->
    <!-- ═══════════════════════════ -->
    <dimen name="radius_sm">6dp</dimen>
    <dimen name="radius_md">12dp</dimen>
    <dimen name="radius_lg">16dp</dimen>
    <dimen name="radius_xl">24dp</dimen>
    <dimen name="radius_full">999dp</dimen>   <!-- Pills, CTA buttons -->

    <!-- ═══════════════════════════ -->
    <!-- COMPONENT SIZES             -->
    <!-- ═══════════════════════════ -->
    <dimen name="button_height">52dp</dimen>
    <dimen name="button_height_compact">44dp</dimen>
    <dimen name="input_height">56dp</dimen>
    <dimen name="bottom_nav_height">64dp</dimen>
    <dimen name="avatar_sm">32dp</dimen>
    <dimen name="avatar_md">40dp</dimen>
    <dimen name="avatar_lg">80dp</dimen>
    <dimen name="progress_bar_height">3dp</dimen>
    <dimen name="card_corner_radius">12dp</dimen>
    <dimen name="genre_tile_corner_radius">12dp</dimen>

    <!-- ═══════════════════════════ -->
    <!-- ELEVATION (tonal layering)  -->
    <!-- ═══════════════════════════ -->
    <!-- We use tonal layering, not drop shadows.  -->
    <!-- Only floating modals get elevation.       -->
    <dimen name="elevation_card">0dp</dimen>
    <dimen name="elevation_modal">4dp</dimen>
    <dimen name="elevation_bottom_nav">8dp</dimen>

</resources>
```

---

## res/values/styles.xml — TextAppearances

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>

    <!-- ══════════════════════════════════════ -->
    <!-- EPILOGUE — Display / Headlines         -->
    <!-- ══════════════════════════════════════ -->

    <style name="TextAppearance.NeonNoir.DisplayLarge">
        <item name="fontFamily">@font/epilogue_extrabold</item>
        <item name="android:textSize">48sp</item>
        <item name="android:lineSpacingExtra">4dp</item>
        <item name="android:letterSpacing">-0.03</item>
        <item name="android:textColor">@color/on_surface</item>
    </style>

    <style name="TextAppearance.NeonNoir.DisplayMedium">
        <item name="fontFamily">@font/epilogue_bold</item>
        <item name="android:textSize">36sp</item>
        <item name="android:letterSpacing">-0.02</item>
        <item name="android:textColor">@color/on_surface</item>
    </style>

    <style name="TextAppearance.NeonNoir.HeadlineLarge">
        <item name="fontFamily">@font/epilogue_bold</item>
        <item name="android:textSize">28sp</item>
        <item name="android:letterSpacing">-0.01</item>
        <item name="android:textColor">@color/on_surface</item>
    </style>

    <style name="TextAppearance.NeonNoir.HeadlineMedium">
        <item name="fontFamily">@font/epilogue_semibold</item>
        <item name="android:textSize">22sp</item>
        <item name="android:letterSpacing">-0.01</item>
        <item name="android:textColor">@color/on_surface</item>
    </style>

    <style name="TextAppearance.NeonNoir.HeadlineSmall">
        <item name="fontFamily">@font/epilogue_semibold</item>
        <item name="android:textSize">18sp</item>
        <item name="android:textColor">@color/on_surface</item>
    </style>

    <!-- ══════════════════════════════════════ -->
    <!-- MANROPE — Body / Labels                -->
    <!-- ══════════════════════════════════════ -->

    <style name="TextAppearance.NeonNoir.BodyLarge">
        <item name="fontFamily">@font/manrope_regular</item>
        <item name="android:textSize">16sp</item>
        <item name="android:lineSpacingExtra">8dp</item>
        <item name="android:textColor">@color/on_surface_variant</item>
    </style>

    <style name="TextAppearance.NeonNoir.BodyMedium">
        <item name="fontFamily">@font/manrope_regular</item>
        <item name="android:textSize">14sp</item>
        <item name="android:lineSpacingExtra">6dp</item>
        <item name="android:textColor">@color/on_surface_variant</item>
    </style>

    <style name="TextAppearance.NeonNoir.BodySmall">
        <item name="fontFamily">@font/manrope_regular</item>
        <item name="android:textSize">12sp</item>
        <item name="android:textColor">@color/on_surface_variant</item>
    </style>

    <style name="TextAppearance.NeonNoir.LabelCaps">
        <item name="fontFamily">@font/manrope_semibold</item>
        <item name="android:textSize">11sp</item>
        <item name="android:letterSpacing">0.12</item>
        <item name="android:textAllCaps">true</item>
        <item name="android:textColor">@color/on_surface_variant</item>
    </style>

    <style name="TextAppearance.NeonNoir.LabelMedium">
        <item name="fontFamily">@font/manrope_medium</item>
        <item name="android:textSize">13sp</item>
        <item name="android:textColor">@color/on_surface_variant</item>
    </style>

    <style name="TextAppearance.NeonNoir.LabelSmall">
        <item name="fontFamily">@font/manrope_medium</item>
        <item name="android:textSize">11sp</item>
        <item name="android:textColor">@color/on_surface_variant</item>
    </style>

    <!-- Button label style -->
    <style name="TextAppearance.NeonNoir.Button">
        <item name="fontFamily">@font/manrope_semibold</item>
        <item name="android:textSize">14sp</item>
        <item name="android:letterSpacing">0.06</item>
        <item name="android:textAllCaps">true</item>
        <item name="android:textColor">@color/on_primary</item>
    </style>

</resources>
```

---

## res/values/themes.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>

    <style name="Theme.NeonNoir" parent="Theme.MaterialComponents.DayNight.NoActionBar">
        <!-- Background -->
        <item name="android:windowBackground">@color/surface</item>
        <item name="colorSurface">@color/surface</item>
        <item name="colorBackground">@color/surface</item>

        <!-- Brand colors -->
        <item name="colorPrimary">@color/primary</item>
        <item name="colorPrimaryVariant">@color/primary_container</item>
        <item name="colorSecondary">@color/secondary</item>

        <!-- Text -->
        <item name="android:textColorPrimary">@color/on_surface</item>
        <item name="android:textColorSecondary">@color/on_surface_variant</item>

        <!-- Status bar -->
        <item name="android:statusBarColor">@android:color/transparent</item>
        <item name="android:windowLightStatusBar">false</item>

        <!-- Navigation bar -->
        <item name="android:navigationBarColor">@color/surface_container_low</item>

        <!-- No window title -->
        <item name="windowNoTitle">true</item>

        <!-- Edge to edge -->
        <item name="android:windowDrawsSystemBarBackgrounds">true</item>
    </style>

    <!-- Splash screen theme -->
    <style name="Theme.NeonNoir.Splash" parent="Theme.SplashScreen">
        <item name="windowSplashScreenBackground">@color/surface</item>
        <item name="windowSplashScreenAnimatedIcon">@drawable/ic_app_logo</item>
        <item name="postSplashScreenTheme">@style/Theme.NeonNoir</item>
    </style>

</resources>
```

---

## Key Drawable Files

### res/drawable/bg_button_primary.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <gradient
        android:angle="0"
        android:startColor="@color/primary"
        android:endColor="@color/primary_container"
        android:type="linear" />
    <corners android:radius="@dimen/radius_full" />
</shape>
```

### res/drawable/bg_button_ghost.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="@android:color/transparent" />
    <stroke
        android:width="1dp"
        android:color="@color/ghost_border" />
    <corners android:radius="@dimen/radius_full" />
</shape>
```

### res/drawable/bg_input_default.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="@color/surface_container_highest" />
    <corners android:radius="@dimen/radius_lg" />
</shape>
```

### res/drawable/bg_input_focused.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="@color/surface_bright" />
    <stroke
        android:width="1dp"
        android:color="@color/ghost_border_focus" />
    <corners android:radius="@dimen/radius_lg" />
</shape>
```

### res/drawable/bg_hero_overlay.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <gradient
        android:angle="270"
        android:startColor="@color/overlay_start"
        android:centerColor="#800E0E11"
        android:endColor="@color/surface"
        android:type="linear" />
</shape>
```

### res/drawable/bg_card.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="@color/surface_container" />
    <corners android:radius="@dimen/radius_md" />
</shape>
```

