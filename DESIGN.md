---
name: Finger Seal
description: Desktop database management tool — SQL editor, table explorer, connection manager, dashboard
colors:
  primary: "#0d7377"
  on-primary: "#ffffff"
  primary-container: "#b2dfdb"
  on-primary-container: "#002b29"
  secondary: "#5a7d7a"
  on-secondary: "#ffffff"
  secondary-container: "#d6e8e5"
  on-secondary-container: "#172624"
  tertiary: "#7a6b4a"
  surface: "#f5f5f0"
  surface-container-low: "#efefe9"
  surface-container: "#e8e8e2"
  surface-container-high: "#e0e0d8"
  surface-container-highest: "#d6d6ce"
  on-surface: "#1c1c1a"
  on-surface-variant: "#5a5a52"
  outline: "#7a7a72"
  outline-variant: "#c8c8be"
  error: "#ba1a1a"
  background: "#f5f5f0"
typography:
  display:
    fontFamily: "Plus Jakarta Sans, sans-serif"
    fontSize: "clamp(1.5rem, 3vw, 2rem)"
    fontWeight: 700
    lineHeight: 1.2
  headline:
    fontFamily: "Plus Jakarta Sans, sans-serif"
    fontSize: "clamp(1.125rem, 2vw, 1.5rem)"
    fontWeight: 600
    lineHeight: 1.3
  title:
    fontFamily: "Plus Jakarta Sans, sans-serif"
    fontSize: "1rem"
    fontWeight: 600
    lineHeight: 1.4
  body:
    fontFamily: "Plus Jakarta Sans, sans-serif"
    fontSize: "0.875rem"
    fontWeight: 400
    lineHeight: 1.5
  label:
    fontFamily: "Plus Jakarta Sans, sans-serif"
    fontSize: "0.75rem"
    fontWeight: 500
    lineHeight: 1.4
    letterSpacing: "0.05em"
    textTransform: "uppercase"
  code:
    fontFamily: "JetBrains Mono, monospace"
    fontSize: "0.8125rem"
    fontWeight: 400
    lineHeight: 1.5
rounded:
  sm: "4px"
  md: "8px"
  lg: "12px"
  xl: "16px"
spacing:
  xs: "4px"
  sm: "8px"
  md: "16px"
  lg: "24px"
  xl: "32px"
components:
  button-primary:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    rounded: "{rounded.sm}"
    padding: "8px 16px"
  button-primary-hover:
    backgroundColor: "#0b6367"
    textColor: "{colors.on-primary}"
    rounded: "{rounded.sm}"
    padding: "8px 16px"
  button-secondary:
    backgroundColor: "{colors.secondary-container}"
    textColor: "{colors.on-secondary-container}"
    rounded: "{rounded.sm}"
    padding: "8px 16px"
  button-secondary-hover:
    backgroundColor: "#c5d8d5"
    textColor: "{colors.on-secondary-container}"
    rounded: "{rounded.sm}"
    padding: "8px 16px"
  card:
    backgroundColor: "{colors.surface-container-low}"
    rounded: "{rounded.lg}"
    padding: "{spacing.md}"
  input:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.sm}"
  nav-item-active:
    backgroundColor: "{colors.secondary-container}"
    textColor: "{colors.on-secondary-container}"
    rounded: "{rounded.sm}"
    padding: "4px 8px"
  nav-item-default:
    backgroundColor: "transparent"
    textColor: "{colors.on-surface-variant}"
    rounded: "{rounded.sm}"
    padding: "4px 8px"
---

# Design System: Finger Seal

## 1. Overview

**Creative North Star: "La Consola del Constructor"**

Finger Seal is a developer console for a weekend builder. It feels like a well-organized workbench — tools within reach, surfaces clear, a place where focus lives. The interface doesn't perform; it enables. Every pixel serves the developer's query, their schema map, their connection health.

The system uses **restrained confidence**: teal as a sharp, purposeful accent applied sparingly (buttons, active states, key highlights) against a warm neutral ground with generous whitespace. Containers stack with subtle tonal layering and soft elevation shadows — enough depth to feel crafted, not enough to distract. The typography is quiet and capable: Plus Jakarta Sans for clean UI, JetBrains Mono for precise code.

This system explicitly rejects: legacy database tool clutter (too many toolbars, tiny fonts, 90s icons), dense observability dashboards (everything screaming for attention at once), and forced dark mode. The interface adapts to the OS; the user chooses the override.

**Key Characteristics:**
- Warm neutral ground with teal precision — the teal is rare enough to matter
- Tonal layering with soft elevation, not just borders
- Generous whitespace as an active design tool, not empty space
- Typography-first hierarchy: scale and weight do the work, not color
- Flat surfaces at rest, lifted on hover — micro-elevation as interaction feedback
- System-adaptive light/dark with manual toggle; no default allegiance

## 2. Colors: The Workshop Palette

The palette is warm-neutral with a teal spine. Saturation is concentrated in the primary; neutrals lean slightly warm (cream-ash undertone). In dark mode, the teal becomes luminous (`#72cfc9`) against dark warm grays — glow without neon.

### Primary
- **Teal Técnico** (`#0d7377` / oklch(0.52 0.09 195)): Buttons, active navigation items, selected state highlights, link text, toggles. Used on ≤10% of any screen. Its rarity signals interactivity.
- **On Primary** (`#ffffff`): Text and icons on primary backgrounds.
- **Primary Container** (`#b2dfdb`): Lighter variant for hover states, badges, container backgrounds in active sections.
- **On Primary Container** (`#002b29`): Text on primary container backgrounds.

### Secondary
- **Verde Industrial** (`#5a7d7a` / oklch(0.56 0.04 185)): Secondary actions, less prominent interactive elements, metadata accents.
- **Secondary Container** (`#d6e8e5`): Active nav items, selected rows, section headers. Covers larger areas than primary.
- **On Secondary Container** (`#172624`): Text on secondary container backgrounds.

### Tertiary
- **Tierra Oliva** (`#7a6b4a`): Warm earthy accent for tags, filetype badges, non-critical structured data highlights. Appears very rarely.
- **Tertiary Container** (`#f0e0c0`): Background for tertiary-accented sections.
- **On Tertiary Container** (`#282010`): Text on tertiary container backgrounds.

### Neutral
- **Ceniza Cálida** (`#f5f5f0`): Surface (main content background). Warm cream-ash.
- **Ceniza Contenedor Baja** (`#efefe9`): container-low — sidebar, card backgrounds.
- **Ceniza Contenedor** (`#e8e8e2`): container — neutral section dividers.
- **Ceniza Contenedor Alta** (`#e0e0d8`): container-high — table headers, search bars.
- **Ceniza Contenedor Altísima** (`#d6d6ce`): container-highest — pressed states, elevated surfaces.
- **Ceniza Brillante** (`#fafaf7`): surface-bright — modal surfaces, drawers.
- **Ceniza Oscura** (`#1c1c1a`): on-surface — primary text.
- **Ceniza Variante** (`#5a5a52`): on-surface-variant — secondary text, disabled states.
- **Línea** (`#7a7a72`): outline — active borders, dividers.
- **Línea Variante** (`#c8c8be`): outline-variant — muted borders, subtle dividers.
- **Base** (`#f5f5f0`): background — app background.
- **Superficie Inversa** (`#31312e`): inverse-surface — tooltips, popover backgrounds.

### Dark Mode Adaptations

In dark mode, the entire warm-neutral family shifts to deep warm grays (`#1c1c1a` surface, `#0e0e0c` container-lowest, `#32322e` container-highest). The primary becomes luminous (`#72cfc9`), secondary becomes lighter (`#b2c9c5`). The outlines invert: lines are darker than surfaces (#32322e for variant, #6a6a62 for active). Error goes soft pink (`#ffb4ab`) against dark red containers.

### Named Rules
**The Teal Threshold Rule.** Teal appears on ≤10% of any screen. If a design passes 10%, re-home elements to secondary or container colors. Teal's rarity is its power.

## 3. Typography

**Display / UI Font:** Plus Jakarta Sans (sans-serif, warm humanist, with `font-variation-settings: 'wght' 300..800`)
**Code Font:** JetBrains Mono (monospace, ligature-free, clear distinction between `1`, `l`, `I`, `O`, `0`)

**Character:** The pairing is editorial-quiet. Plus Jakarta Sans gives approachable warmth without sliding into "friendly SaaS" — it's the Sans Serif of a craftsperson's notebook. JetBrains Mono is precise and calm, not technical-cold; it's there to clarify, not to intimidate.

### Hierarchy
- **Display** (700, `clamp(1.5rem, 3vw, 2rem)`, 1.2): Dashboard section titles, welcome screens. Rare — one per view at most.
- **Headline** (600, `clamp(1.125rem, 2vw, 1.5rem)`, 1.3): Card titles, panel headers, dialog headings.
- **Title** (600, `1rem`, 1.4): Component titles, nav headers, sidebar item labels.
- **Body** (400, `0.875rem`, 1.5): All running text, table cells, descriptions, copy. Maximum line length 75ch.
- **Label / Caps** (500, `0.75rem`, 1.4, 0.05em letter-spacing, uppercase): Badges, chip labels, metadata headers, section tags.
- **Code** (400, `0.8125rem`, 1.5, JetBrains Mono): SQL textareas, query results, table cell code values, monospaced column names. Unambiguous character shapes.

### Named Rules
**The Mono Precision Rule.** Code is always in JetBrains Mono, never in the UI font. SQL keywords, identifiers, function names, and data values all use the code face when they represent data, not interface labels.

## 4. Elevation

The system is **predominantly flat with soft elevation** as a responsive layer. At rest, containers distinguish themselves through tonal layering (surface-container-low vs surface-container vs surface-container-high) rather than shadows. Elevation appears only as a response to interaction or to signal hierarchy — hover states lift by 1px with a soft shadow, modals and popovers sit above a dimmed backdrop.

The default elevation vocabulary is zero. Shadows are earned, not given.

### Shadow Vocabulary
- **Hover Lift** (`0 4px 12px rgba(0,0,0,0.06)`): Applied on card hover via the `.hover-lift` utility. Translates the card up by 1px.
- **Modal / Popover** (`0 8px 30px rgba(0,0,0,0.12)`): Active modal backdrops, dropdown menus, popover panels.
- **Sticky Header** (`0 2px 8px rgba(0,0,0,0.04)`): Table headers and other sticky elements that need subtle separation from scrolling content below.

### Named Rules
**The Flat-Before-Elevation Rule.** All surfaces are flat at rest. Shadows are interaction responses, not default styling. A card without hover shadow is correct; a card with a resting shadow is wrong unless it's a modal.

## 5. Components

### Buttons
- **Shape:** Gently squared corners (4px / `rounded-sm`)
- **Primary (Teal Técnico):** Solid teal (`#0d7377`), white text (`#ffffff`), 8px horizontal / 4px vertical padding. Hover: darker teal (`#0b6367`), `opacity: 0.9` fallback. Active: `opacity: 0.8`. Transitions on background-color and opacity over 150ms ease. The most important action per panel.
- **Secondary (Container):** Background from secondary-container (`#d6e8e5`), text from on-secondary-container (`#172624`). Hover: lighter container variant. Secondary actions, Cancel buttons, "View all" links. Uses the same 4px radius and padding as primary.
- **Ghost / Text (minimal):** No background at rest, on-surface-variant text, on-surface hover. For low-urgency inline actions — "Clear", "Edit", "Remove". Transitions text color over 150ms.
- **Icon buttons:** 32px × 32px, transparent, on-surface-variant icon, on-surface hover. For toolbar controls, toggle icons.

### Cards / Containers
- **Corner Style:** Rounded corners (12px / `rounded-lg` or 16px / `rounded-xl` for cards; 8px / `rounded-md` for internal groups)
- **Background:** `surface-container-low` (`#efefe9`) for main cards; `surface-container` (`#e8e8e2`) for nested groups; `surface` (`#f5f5f0`) for inline sections
- **Border:** 1px solid `outline-variant` (`#c8c8be`), 60% opacity variant for subtle edges
- **Shadow Strategy:** None at rest. `.hover-lift` utility adds `0 4px 12px rgba(0,0,0,0.06)` + `translateY(-1px)` on hover
- **Internal Padding:** 16px (`md`) as default; 12px on compact cards; 24px on spacious panels

### Inputs / Textareas
- **Style:** No border at rest (background contrast does the separation). Border on focus (1px `outline-variant` or `primary`).
- **Background:** `surface` or `surface-container` for textareas.
- **Radius:** 4px (`rounded-sm`)
- **Focus:** Border shifts from `outline-variant` to `primary`, no glow. The color change is signal enough.
- **Code Input:** The SQL textarea uses JetBrains Mono at `0.8125rem`, no border, full-width, with `.custom-scrollbar` (4px thin scrollbar).

### Navigation (Sidebar)
- **Style:** Vertical, full-height, `surface-container-low` background with `outline-variant` right border.
- **Items:** 32px tall, 8px horizontal padding, `rounded-sm` (4px). Default: transparent background, on-surface-variant text. Hover: `surface-variant` background. Active: `secondary-container` background (`#d6e8e5`) with `on-secondary-container` text (`#172624`) and `font-semibold`.
- **Icons:** 16px Material Symbols Outlined, `FILL 0` at rest, `FILL 1` on active states for subtle weight shift.
- **Section headers:** 10px uppercase label-caps, `outline` color, 0.05em tracking. Not clickable.

### Chips / Tags
- **Shape:** 4px radius, 8px horizontal / 2px vertical padding
- **Style:** `surface-variant` background, `on-surface-variant` text, `outline-variant` border
- **State change:** Active chip uses `primary-container` background, `on-primary-container` text

### Tables (Data Grids)
- **Rows:** Alternating backgrounds via `bg-surface` / `bg-surface-container-lowest` for visual rhythm.
- **Headers:** `surface-container-high` background, `on-surface-variant` label-caps text, sticky with subtle shadow.
- **Cells:** `0.8125rem` JetBrains Mono code font for data values, body font for labels.
- **Scrollbar:** 4px thin custom scrollbar in `outline-variant` / `outline`.

## 6. Do's and Don'ts

### Do:
- **Do** use the teal accent sparingly (≤10% of screen area). Its rarity is its signal.
- **Do** prefer surface-container-low cards with hover-lift over flat lists when grouping related information.
- **Do** let whitespace breathe. 16px padding inside cards, 24px between sections, generous margins.
- **Do** use secondary-container for active nav items and selected states — it's calm enough to fill broader areas than primary.
- **Do** format SQL keywords with the `.sql-keyword` class (primary color, 700 weight) for legibility.
- **Do** respect the system theme. The manual toggle exists for the user, not for the designer.

### Don't:
- **Don't** create a legacy database tool look: cluttered toolbars, 90s icons, cramped spacing, small fonts — anything that signals pgAdmin or DBeaver.
- **Don't** build dense, dark observability dashboards like Grafana or Datadog — too much information competing for attention at once.
- **Don't** force dark mode. Dark surfaces are the user's choice, not the default.
- **Don't** use side-stripe borders (`border-left: 3px solid primary` on cards or list items). Use full borders, background tints, or nothing.
- **Don't** use gradient text (`background-clip: text`). Emphasis comes from weight and size, not decoration.
- **Don't** use glassmorphism (frosted glass with backdrop blur) as a decorative default.
- **Don't** use the hero-metric template (big number, small label, supporting stats, gradient accent) — a SaaS cliché.
- **Don't** use identical card grids (same-sized cards with icon + heading + text repeated endlessly).
- **Don't** use modals as the first interaction thought. Inline and progressive alternatives come first.
- **Don't** use em dashes (—) in copy. Use commas, colons, semicolons, or parentheses.
