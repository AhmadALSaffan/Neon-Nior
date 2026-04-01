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
