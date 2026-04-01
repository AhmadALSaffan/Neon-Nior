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
