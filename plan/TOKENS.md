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
