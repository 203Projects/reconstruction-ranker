package com.jaejal.reconstruction.design

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 재잘이 (Jaejal-i) — the guide mascot. The gold ㅈ-roof logo come alive: a friendly little
 * building with a gold hard-hat ridge, drawn on a 16×16 pixel grid.
 *
 * Four moods share one base body; each mood overrides only the ~6 face cells, so they read as
 * one character. Rendered as a static [PixelSprite]; [JaejalMascot] adds an idle bob/sway.
 */

enum class MascotMood { Happy, Thinking, Celebrating, Worried }

object Mascot {
    private val G = ConstructionColors.Gold
    private val Cu = ConstructionColors.Copper
    private val Sky = ConstructionColors.Sky
    private val Mist = ConstructionColors.MapMist
    private val Cloud = ConstructionColors.Cloud
    private val Paper = ConstructionColors.Paper
    private val Dark = ConstructionColors.PaperAlt
    private val Blush = ConstructionColors.Loss.copy(alpha = 0.4f)
    private val Sweat = ConstructionColors.SkySoft

    /** The shared body: ㅈ-roof head, building torso, feet. Face cells are added per-mood. */
    private fun bodyCells(): MutableList<Cell> = mutableListOf(
        // ROW 0–3 — head = ㅈ roof
        Cell(3, 0, G, w = 10),                         // ridge bar
        Cell(7, 1, G, w = 2),                          // apex
        Cell(2, 1, Cu), Cell(13, 1, Cu),               // hat brim
        Cell(5, 1, G, w = 6), Cell(5, 2, G, w = 6),    // gold dome
        Cell(6, 1, Cloud),                             // hat shine
        // ROW 6–12 — body = little building
        Cell(4, 6, Sky, w = 8, h = 7),                 // facade block
        Cell(11, 6, Mist, h = 7),                      // right shadow column
        Cell(5, 7, G, w = 2, h = 2), Cell(8, 7, G, w = 2, h = 2),   // upper windows
        Cell(5, 10, G, w = 2, h = 2), Cell(8, 10, G, w = 2, h = 2), // lower windows
        Cell(9, 9, G),                                 // gold trowel
        // ROW 13–15 — base
        Cell(4, 13, Dark, w = 8),                      // foundation
        Cell(4, 14, Mist, h = 2), Cell(11, 14, Mist, h = 2)        // feet
    )

    /** Face cells for a mood (eyes/mouth/extras at rows 4–5). */
    private fun faceCells(mood: MascotMood): List<Cell> = when (mood) {
        MascotMood.Happy -> listOf(
            Cell(5, 4, Paper), Cell(10, 4, Paper),     // round eyes
            Cell(7, 5, Dark), Cell(8, 5, Dark),        // smile
            Cell(12, 4, Blush)                         // blush
        )
        MascotMood.Thinking -> listOf(
            Cell(5, 4, Paper), Cell(10, 4, Paper),     // eyes
            Cell(7, 5, Dark, w = 2),                   // flat mouth
            Cell(13, 2, G)                             // bobbing "?" prop
        )
        MascotMood.Celebrating -> listOf(
            Cell(5, 4, Dark), Cell(10, 4, Dark),       // ^ ^ happy-closed eyes
            Cell(6, 5, G, w = 4),                      // wide gold open mouth
            Cell(2, 5, G), Cell(13, 5, G)              // arms up
        )
        MascotMood.Worried -> listOf(
            Cell(5, 4, Paper), Cell(10, 4, Paper),     // eyes
            Cell(7, 5, ConstructionColors.Loss),       // small "o" mouth
            Cell(3, 3, Sweat)                          // sweat bead
        )
    }

    fun sprite(mood: MascotMood): Sprite {
        val cells = bodyCells().apply { addAll(faceCells(mood)) }
        return Sprite(16, 16, cells)
    }
}

/**
 * 재잘이 with an idle animation. [mood] swaps the face; the whole sprite gently bobs (±4dp
 * vertical, ~1.8s) and sways (±1.5°, ~2.6s) on desynced loops so it feels alive.
 *
 * When [animate] is false (reduce-motion), it renders still.
 */
@Composable
fun JaejalMascot(
    mood: MascotMood = MascotMood.Happy,
    size: Dp = 64.dp,
    animate: Boolean = isAnimationEnabled,
    modifier: Modifier = Modifier
) {
    val sprite = Mascot.sprite(mood)
    if (!animate) {
        PixelSprite(sprite, modifier = modifier.size(size), name = "Jaejal", contentDescription = Brand.MASCOT_NAME)
        return
    }
    val transition = rememberInfiniteTransition(label = "mascotIdle")
    val bob by transition.animateFloat(
        initialValue = -4f, targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bob"
    )
    val sway by transition.animateFloat(
        initialValue = -1.5f, targetValue = 1.5f,
        animationSpec = infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "sway"
    )
    PixelSprite(
        sprite,
        modifier = modifier
            .size(size)
            .graphicsLayer { translationY = bob }
            .rotate(sway),
        name = "Jaejal",
        contentDescription = Brand.MASCOT_NAME
    )
}
