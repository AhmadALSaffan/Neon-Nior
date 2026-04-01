# Design System Document: The Cinematic Editorial Experience

## 1. Overview & Creative North Star

**Creative North Star: "The Neon Noir Curator"**

This design system is built to transform a standard streaming interface into a high-end, immersive editorial experience. We are moving away from the "grid of posters" template and toward a "Digital Premiere" feel. The system utilizes deep, ink-like backgrounds contrasted with vibrant neon accents derived from the brand palette (pinks, purples, and blues). 

The goal is **Immersive Depth**. By using intentional asymmetry, overlapping typography, and cinematic scale, we create a sense of theater. We treat the screen not as a flat surface, but as a multi-layered stage where content is the star and the UI is the sophisticated lighting.

---

## 2. Colors & Surface Philosophy

Our palette is grounded in the deep `#0e0e11` background, allowing the vibrant primary (`#ff8b9b`), secondary (`#a98bff`), and tertiary (`#81ecff`) colors to glow like neon lights in a dark theater.

### The "No-Line" Rule
**Explicit Instruction:** Prohibition of 1px solid borders for sectioning. Boundaries must be defined solely through background color shifts. To separate a navigation rail from the main feed, use `surface-container-low` against `surface`. To separate content categories, use vertical breathing room (Spacing 16 or 20) rather than horizontal rules.

### Surface Hierarchy & Nesting
Treat the UI as a series of physical layers—like stacked sheets of frosted glass.
*   **Base:** `surface` (#0e0e11) – The foundation.
*   **Sections:** `surface-container-low` (#131316) – Large structural areas.
*   **Interactive Cards:** `surface-container` (#19191d) or `surface-container-high` (#1f1f23) – Content that sits "above" the base.
*   **Floating Elements:** Use `surface-bright` (#2c2c30) for small pop-overs to ensure maximum contrast.

### The "Glass & Gradient" Rule
To capture the vibrant spirit of the logo, main CTAs and hero highlights must use gradients. Transitioning from `primary` (#ff8b9b) to `primary-container` (#ff7389) adds a "liquid" feel that flat color cannot replicate. For floating player controls or navigation bars, use semi-transparent surface colors with a **32px backdrop-blur** (Glassmorphism) to allow movie key art to bleed through the UI.

---

## 3. Typography: The Editorial Voice

We use a high-contrast pairing to balance cinematic drama with functional legibility.

*   **Display & Headlines (Epilogue):** This is our "Grand Opening" font. Use `display-lg` and `headline-lg` with tight letter spacing for movie titles and featured headers. Its bold, geometric nature demands attention and feels custom-tailored.
*   **Body & Labels (Manrope):** A modern, highly legible sans-serif for metadata (duration, rating, cast). Use `body-md` for descriptions to ensure readability against dark backgrounds.
*   **Visual Hierarchy:** Titles should be significantly larger than metadata—an intentional 3:1 scale ratio—to create an editorial, magazine-style layout.

---

## 4. Elevation & Depth

We eschew traditional "Material" shadows in favor of **Tonal Layering**.

*   **The Layering Principle:** Place a `surface-container-lowest` card on a `surface-container-low` section. The subtle shift in hex value creates a soft, natural lift.
*   **Ambient Shadows:** For "floating" modals, use an extra-diffused shadow:
    *   *Blur:* 40px - 60px.
    *   *Opacity:* 6%.
    *   *Color:* Tint the shadow with `secondary` (#a98bff) rather than black to mimic the color bleed of a neon screen.
*   **The "Ghost Border" Fallback:** If accessibility requires a container edge, use `outline-variant` (#48474b) at **15% opacity**. Never use 100% opaque borders.

---

## 5. Components

### Buttons
*   **Primary:** Gradient fill (`primary` to `primary-container`). Roundedness: `full`. No border.
*   **Secondary:** Glassmorphism style. Semi-transparent `surface-variant` with a backdrop blur.
*   **States:** On hover, increase the `surface-tint` overlay by 10% to "light up" the button.

### Cards & Movie Tiles
*   **Style:** No borders. Use `md` (0.75rem) or `lg` (1rem) rounded corners.
*   **Hover:** Scale the card by 1.05x and apply a subtle `primary` glow shadow.
*   **Spacing:** Use vertical white space (Spacing 8 or 10) to separate "Continue Watching" from "Trending" rather than divider lines.

### Inputs & Search
*   **Field:** Use `surface-container-highest` with a `none` border. 
*   **Focus:** Transition the background to `surface-bright` and add a "Ghost Border" using `tertiary` (#81ecff) at 20% opacity.

### Featured Hero (Custom Component)
A large, asymmetrical layout where the movie character (masked PNG) overlaps the `display-lg` title. This "breaking of the box" is essential for the high-end editorial feel.

---

## 6. Do’s and Don’ts

### Do:
*   **Do** use `secondary-fixed-dim` for inactive icons to maintain a sophisticated, low-contrast look that doesn't distract from the video content.
*   **Do** embrace negative space. Let the `surface` background breathe between rows of content.
*   **Do** use the `full` roundedness scale for "Play" buttons to mimic the iconic look of the brand logo.

### Don't:
*   **Don't** use pure white (#FFFFFF) for text. Always use `on-surface` (#f0edf1) or `on-surface-variant` (#acaaae) to reduce eye strain in dark mode.
*   **Don't** use standard 1px dividers. If you feel the need for a line, increase your spacing scale instead.
*   **Don't** use drop shadows on text. Let the high-contrast typography scale and background-to-text ratio handle the legibility.