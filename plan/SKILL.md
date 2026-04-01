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
