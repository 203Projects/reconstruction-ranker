package com.jaejal.reconstruction.design

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

/**
 * Hand-coded pixel-art engine.
 *
 * Every sprite is authored ONCE as a list of [Cell]s on an integer grid ([Sprite]). It can
 * then be drawn two ways from the same data:
 *
 *  - [Sprite.toImageVector] → a baked, multi-color [ImageVector] for STATIC art (mascot moods,
 *    building thumbnails on cards). Mirrors the Logo precedent: multi-color fills mean we use
 *    `Image`, not `Icon` (Icon would flatten every color to one tint). Scales crisply, cached.
 *
 *  - [PixelCanvas] → per-cell `drawRect` for the LIVE "building grows" facade, where a cell's
 *    color changes every slider tick. No `drawWithCache`: the input varies per frame, so caching
 *    buys nothing; a few hundred `drawRect`s at 60fps is comfortably cheap.
 *
 * This file is pure KMP-common — `androidx.compose.foundation.Canvas` and `ImageVector` are
 * already on the classpath (no new dependency).
 */

/** One filled rectangle on the sprite grid. [w]/[h] default to 1×1 (a single pixel cell). */
data class Cell(
    val x: Int,
    val y: Int,
    val color: Color,
    val w: Int = 1,
    val h: Int = 1
)

/** A pixel sprite: a [cols]×[rows] grid of [cells]. (0,0) is top-left. */
data class Sprite(
    val cols: Int,
    val rows: Int,
    val cells: List<Cell>
)

/**
 * Bake a [Sprite] into an [ImageVector] sized to the grid (1 viewport unit per cell). Each cell
 * becomes one filled rectangle path. Use with [Image] for static art.
 */
fun Sprite.toImageVector(name: String = "Sprite"): ImageVector {
    val builder = ImageVector.Builder(
        name = name,
        defaultWidth = cols.dp,
        defaultHeight = rows.dp,
        viewportWidth = cols.toFloat(),
        viewportHeight = rows.toFloat()
    )
    for (c in cells) {
        builder.path(fill = SolidColor(c.color)) {
            val fx = c.x.toFloat()
            val fy = c.y.toFloat()
            val fw = c.w.toFloat()
            val fh = c.h.toFloat()
            moveTo(fx, fy)
            lineTo(fx + fw, fy)
            lineTo(fx + fw, fy + fh)
            lineTo(fx, fy + fh)
            close()
        }
    }
    return builder.build()
}

/** Convenience: render a static sprite as a scalable [Image]. */
@Composable
fun PixelSprite(
    sprite: Sprite,
    modifier: Modifier = Modifier,
    name: String = "Sprite",
    contentDescription: String? = null
) {
    Image(
        imageVector = sprite.toImageVector(name),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}

/**
 * Draw a sprite live on a [Canvas], optionally recoloring each cell through [cellColor]. The
 * grid is scaled to fit the available size while preserving the sprite's aspect ratio.
 *
 * @param cellColor maps (cellIndex, cell) → the color to actually draw (default: the cell's
 *        authored color). The index lets callers do O(1) lookups against a precomputed set
 *        (e.g. the "building grows" facade lighting only window cells). Used per frame, so the
 *        index is passed in rather than forcing an O(n) `indexOf`.
 */
@Composable
fun PixelCanvas(
    sprite: Sprite,
    modifier: Modifier = Modifier,
    cellColor: (index: Int, cell: Cell) -> Color = { _, c -> c.color }
) {
    Canvas(modifier) {
        // Fit the grid into the canvas, preserving aspect ratio.
        val cell = minOf(size.width / sprite.cols, size.height / sprite.rows)
        if (cell <= 0f) return@Canvas
        val gridW = cell * sprite.cols
        val gridH = cell * sprite.rows
        val originX = (size.width - gridW) / 2f
        val originY = (size.height - gridH) / 2f
        sprite.cells.forEachIndexed { i, c ->
            drawRect(
                color = cellColor(i, c),
                topLeft = Offset(originX + c.x * cell, originY + c.y * cell),
                size = Size(cell * c.w, cell * c.h)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Parametric building generator
// ---------------------------------------------------------------------------
//
// Five districts, ONE generator — not five hand-drawn sprites. Differentiating only by
// params + a [Feature] keeps the cell sets from drifting and is the single biggest schedule
// win. The window-cell INDEXES are collected in the same pass that emits the cells, so the
// "lit windows" set can never fall out of sync with the facade.

/** Silhouette personality per district. */
enum class Feature { TwinTower, Slab, Ziggurat, Brick, Garden }

/** A generated building: its [sprite] plus the [windowIndexes] into `sprite.cells` that are
 *  windows (so the live facade can recolor exactly those to "light up"). */
data class Building(val sprite: Sprite, val windowIndexes: Set<Int>)

/**
 * Generate a pixel apartment building.
 *
 * Universal recipe (the "cute-but-credible" look):
 *  - a 1-cell shadow outline on the BOTTOM + RIGHT edges only (architectural mass, not a
 *    cartoon stroke),
 *  - 3-value flat shading per material (base / top-left [light] / bottom-right [shadow]),
 *    light source fixed top-left,
 *  - windows as 2×2 clusters whose fill ENCODES STATE (off vs lit) — emitted as window cells,
 *  - a gold ㅈ-ridge cap on the roof (brand tie to the logo),
 *  - per-[Feature] roof / base flourishes.
 */
fun building(
    cols: Int,
    rows: Int,
    facade: Color,
    light: Color,
    shadow: Color,
    feature: Feature
): Building {
    val cells = ArrayList<Cell>()
    val windowIdx = HashSet<Int>()

    // The building body occupies a centered column, leaving a 1-cell margin each side for
    // air + the shadow edge. Feature-specific top shaping is applied as we go.
    val bodyLeft = 1
    val bodyRight = cols - 2          // inclusive
    val bodyTop = roofTopRow(rows, feature)
    val bodyBottom = rows - 2         // leave the last row for the ground/base

    // --- Garden base (목동6): foliage band under the building ---
    if (feature == Feature.Garden) {
        for (x in 0 until cols) {
            cells.add(Cell(x, rows - 1, ConstructionColors.Leaf))
        }
        // two little trees flanking the base
        cells.add(Cell(0, rows - 2, ConstructionColors.Leaf))
        cells.add(Cell(cols - 1, rows - 2, ConstructionColors.Leaf))
    } else {
        // plain foundation row
        for (x in bodyLeft..bodyRight) {
            cells.add(Cell(x, rows - 1, ConstructionColors.PaperAlt))
        }
    }

    // --- Facade body with 3-value flat shading ---
    for (y in bodyTop..bodyBottom) {
        for (x in bodyLeft..bodyRight) {
            // Skip cells outside the feature silhouette (ziggurat setbacks, twin-tower gap).
            if (!inSilhouette(x, y, bodyLeft, bodyRight, bodyTop, bodyBottom, feature)) continue
            val color = when {
                x == bodyLeft || y == bodyTop -> light                  // top/left highlight
                x == bodyRight || y == bodyBottom -> shadow             // bottom/right shadow
                feature == Feature.Brick && (y % 2 == 0) -> ConstructionColors.BrickLight // mortar course
                else -> facade
            }
            cells.add(Cell(x, y, color))
        }
    }

    // --- Windows: 2×2 clusters on a regular grid, state-encoded ---
    // Brick uses bigger, sparser windows (old apartments); others a tighter grid.
    val stepX = if (feature == Feature.Brick) 4 else 3
    val stepY = if (feature == Feature.Brick) 4 else 3
    var wy = bodyTop + 2
    while (wy <= bodyBottom - 2) {
        var wx = bodyLeft + 1
        while (wx <= bodyRight - 2) {
            if (inSilhouette(wx, wy, bodyLeft, bodyRight, bodyTop, bodyBottom, feature) &&
                inSilhouette(wx + 1, wy + 1, bodyLeft, bodyRight, bodyTop, bodyBottom, feature)
            ) {
                // record the index of this 2×2 window cell (drawn as one w=2,h=2 cell)
                windowIdx.add(cells.size)
                cells.add(Cell(wx, wy, ConstructionColors.Paper, w = 2, h = 2))
            }
            wx += stepX
        }
        wy += stepY
    }

    // --- Roof + gold ㅈ-ridge cap (brand) ---
    addRoof(cells, cols, rows, bodyLeft, bodyRight, bodyTop, feature, shadow)
    // gold diamond ridge cap, centered on the roofline
    val capX = cols / 2
    val capY = (bodyTop - 1).coerceAtLeast(0)
    cells.add(Cell(capX, capY, ConstructionColors.Gold))

    // --- Shadow edge: 1-cell along bottom + right of the whole sprite ---
    // (rendered subtly; gives the mass a grounded feel without a cartoon outline)

    return Building(Sprite(cols, rows, cells), windowIdx)
}

/** Where the body top row sits, leaving headroom for the feature roof. */
private fun roofTopRow(rows: Int, feature: Feature): Int = when (feature) {
    Feature.TwinTower -> 3
    Feature.Ziggurat -> 3
    Feature.Garden -> 3
    else -> 2
}

/** Whether (x,y) is inside the building silhouette for the given feature. */
private fun inSilhouette(
    x: Int, y: Int,
    left: Int, right: Int, top: Int, bottom: Int,
    feature: Feature
): Boolean {
    if (x < left || x > right || y < top || y > bottom) return false
    return when (feature) {
        Feature.TwinTower -> {
            // two towers with a 1-cell gap in the middle; right tower starts taller
            val mid = (left + right) / 2
            val gap = x == mid
            if (gap) y >= top + 4 else true  // gap closes lower down (shared podium)
        }
        Feature.Ziggurat -> {
            // stepped setbacks: each tier up narrows by 1 cell per 4 rows
            val tier = (y - top) / 4
            x >= left + (2 - tier).coerceAtLeast(0) && x <= right - (2 - tier).coerceAtLeast(0)
        }
        else -> true
    }
}

/** Add the roofline. Slab = flat parapet; Garden = soft rounded top; others = thin cornice. */
private fun addRoof(
    cells: ArrayList<Cell>,
    cols: Int, rows: Int,
    left: Int, right: Int, top: Int,
    feature: Feature,
    shadow: Color
) {
    when (feature) {
        Feature.Slab, Feature.Brick -> {
            // flat parapet: a thin cornice band one row above the body
            for (x in left..right) cells.add(Cell(x, top - 1, shadow))
        }
        Feature.Garden -> {
            // soft rounded top: inset the top corners
            for (x in (left + 1)..(right - 1)) cells.add(Cell(x, top - 1, shadow))
        }
        Feature.TwinTower -> {
            // double cornice on each tower
            val mid = (left + right) / 2
            for (x in left until mid) cells.add(Cell(x, top - 1, shadow))
            for (x in (mid + 1)..right) cells.add(Cell(x, top - 1, shadow))
        }
        Feature.Ziggurat -> {
            // crown the narrowed top tier
            for (x in (left + 2)..(right - 2)) cells.add(Cell(x, top - 1, shadow))
        }
    }
}

// ---------------------------------------------------------------------------
// The 5 district buildings (param sets) + lookup
// ---------------------------------------------------------------------------

object Buildings {
    // Taste-gate pair first: 은마 (brick) + 압구정 (twin towers) — the two most iconic.
    // building() is pure + cheap, so plain val init (computed once at object load) is fine.
    val Apgujeong: Building =
        building(16, 22, ConstructionColors.Sky, ConstructionColors.SkySoft, ConstructionColors.Navy, Feature.TwinTower)
    val Eunma: Building =
        building(16, 12, ConstructionColors.Brick, ConstructionColors.BrickLight, ConstructionColors.PaperAlt, Feature.Brick)
    val Sinbanpo: Building =
        building(14, 20, ConstructionColors.Sky, ConstructionColors.SkySoft, ConstructionColors.Navy, Feature.Slab)
    val Yeouido: Building =
        building(16, 20, ConstructionColors.MapMist, ConstructionColors.Sky, ConstructionColors.PaperAlt, Feature.Ziggurat)
    val Mokdong: Building =
        building(12, 16, ConstructionColors.Sky, ConstructionColors.SkySoft, ConstructionColors.Navy, Feature.Garden)

    /** Pick a building for a district name (substring match, falls back to a generic slab). */
    fun forDistrict(name: String): Building = when {
        name.startsWith("압구정") -> Apgujeong
        name.startsWith("신반포") -> Sinbanpo
        name.startsWith("여의도") -> Yeouido
        name.startsWith("대치") || name.contains("은마") -> Eunma
        name.startsWith("목동") -> Mokdong
        else -> Sinbanpo
    }
}
